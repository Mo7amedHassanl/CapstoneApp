package com.m7md7sn.capstoneApp.data.repository

import com.m7md7sn.capstoneApp.data.model.FirebaseSensorData
import com.m7md7sn.capstoneApp.data.model.SensorReading
import com.m7md7sn.capstoneApp.data.model.SystemPart
import com.m7md7sn.capstoneApp.data.model.TimedSensorReading
import com.m7md7sn.capstoneApp.data.model.SensorStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Sync
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class SensorRepository @Inject constructor(
    private val firebaseRepository: FirebaseSensorRepository
) {
    // Cache the latest sensor data
    private var latestData: Map<String, FirebaseSensorData>? = null
    
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSensorReadingsFlow(): Flow<List<SensorReading>> {
        return firebaseRepository.getLatestSensorData().map { sensorDataMap ->
            latestData = sensorDataMap
            
            // Find the latest entry
            val latestEntry = sensorDataMap.entries.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
                ?: return@map emptyList<SensorReading>()
            
            listOf(
                SensorReading("TDS", latestEntry.tds.toString(), "ppm", Icons.Outlined.Sensors),
                SensorReading("pH", String.format("%.2f", latestEntry.pH), null, Icons.Outlined.Sensors),
                SensorReading("Turbidity", String.format("%.2f", latestEntry.turbidity), "NTU", Icons.Outlined.Sensors),
                SensorReading("Temperature", 
                             if (latestEntry.temperature < -100) "N/A" else latestEntry.temperature.toString(), 
                             "°C", 
                             Icons.Outlined.Bolt)
            )
        }
    }

    // Fallback method for non-flow access
    fun getSensorReadings(): List<SensorReading> {
        val latestEntry = latestData?.entries?.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
        
        return if (latestEntry != null) {
            listOf(
                SensorReading("TDS", latestEntry.tds.toString(), "ppm", Icons.Outlined.Sensors),
                SensorReading("pH", String.format("%.2f", latestEntry.pH), null, Icons.Outlined.Sensors),
                SensorReading("Turbidity", String.format("%.2f", latestEntry.turbidity), "NTU", Icons.Outlined.Sensors),
                SensorReading("Temperature", 
                             if (latestEntry.temperature < -100) "N/A" else latestEntry.temperature.toString(), 
                             "°C", 
                             Icons.Outlined.Bolt)
            )
        } else {
            // Fallback to dummy data if no real data is available yet
            listOf(
                SensorReading("TDS", "0", "ppm", Icons.Outlined.Sensors),
                SensorReading("pH", "0", null, Icons.Outlined.Sensors),
                SensorReading("Turbidity", "0", "NTU", Icons.Outlined.Sensors),
                SensorReading("Temperature", "N/A", "°C", Icons.Outlined.Bolt)
            )
        }
    }

    fun getSystemParts(): List<SystemPart> = listOf(
        SystemPart("Pumps", Icons.Outlined.Sync),
        SystemPart("Sensors", Icons.Outlined.Sensors)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTimedSensorReadingsFlow(sensorName: String): Flow<List<TimedSensorReading>> {
        return firebaseRepository.getSensorHistory().map { sensorDataList ->
            when (sensorName.lowercase()) {
                "ph" -> sensorDataList.map { 
                    TimedSensorReading(it.pH, formatTimestamp(it.timestamp)) 
                }
                "tds" -> sensorDataList.map { 
                    TimedSensorReading(it.tds, formatTimestamp(it.timestamp)) 
                }
                "turbidity" -> sensorDataList.map { 
                    TimedSensorReading(it.turbidity, formatTimestamp(it.timestamp)) 
                }
                "temperature" -> sensorDataList.filter { it.temperature > -100 }.map { 
                    TimedSensorReading(it.temperature, formatTimestamp(it.timestamp)) 
                }
                else -> emptyList()
            }.takeLast(10)
        }
    }
    
    // Fallback for non-flow access
    fun getTimedSensorReadings(sensorName: String): List<TimedSensorReading> {
        // If we have cached data, use it
        val sensorHistory = latestData?.values?.toList() ?: return emptyList()
        
        return when (sensorName.lowercase()) {
            "ph" -> sensorHistory.map { 
                TimedSensorReading(it.pH, formatTimestamp(it.timestamp)) 
            }
            "tds" -> sensorHistory.map { 
                TimedSensorReading(it.tds, formatTimestamp(it.timestamp)) 
            }
            "turbidity" -> sensorHistory.map { 
                TimedSensorReading(it.turbidity, formatTimestamp(it.timestamp)) 
            }
            "temperature" -> sensorHistory.filter { it.temperature > -100 }.map { 
                TimedSensorReading(it.temperature, formatTimestamp(it.timestamp)) 
            }
            else -> emptyList()
        }.takeLast(5)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSensorStatusesFlow(): Flow<List<SensorStatus>> {
        return firebaseRepository.getLatestSensorData().map { sensorDataMap ->
            val latestEntry = sensorDataMap.entries.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
                ?: return@map emptyList<SensorStatus>()
            
            listOf(
                SensorStatus(
                    name = "pH",
                    value = latestEntry.pH,
                    unit = "",
                    isWorking = true,
                    state = determinePHState(latestEntry.pH)
                ),
                SensorStatus(
                    name = "Turbidity",
                    value = latestEntry.turbidity,
                    unit = "NTU",
                    isWorking = true,
                    state = determineTurbidityState(latestEntry.turbidity)
                ),
                SensorStatus(
                    name = "TDS",
                    value = latestEntry.tds,
                    unit = "ppm",
                    isWorking = true,
                    state = determineTDSState(latestEntry.tds)
                ),
                SensorStatus(
                    name = "Temperature",
                    value = if (latestEntry.temperature < -100) 0f else latestEntry.temperature,
                    unit = "°C",
                    isWorking = latestEntry.temperature > -100,
                    state = if (latestEntry.temperature < -100) "Offline" else "Normal"
                )
            )
        }
    }
    
    fun getSensorStatuses(): List<SensorStatus> {
        val latestEntry = latestData?.entries?.maxByOrNull { it.key.toDoubleOrNull() ?: 0.0 }?.value
        
        return if (latestEntry != null) {
            listOf(
                SensorStatus(
                    name = "pH",
                    value = latestEntry.pH,
                    unit = "",
                    isWorking = true,
                    state = determinePHState(latestEntry.pH)
                ),
                SensorStatus(
                    name = "Turbidity",
                    value = latestEntry.turbidity,
                    unit = "NTU",
                    isWorking = true,
                    state = determineTurbidityState(latestEntry.turbidity)
                ),
                SensorStatus(
                    name = "TDS",
                    value = latestEntry.tds,
                    unit = "ppm",
                    isWorking = true,
                    state = determineTDSState(latestEntry.tds)
                ),
                SensorStatus(
                    name = "Temperature",
                    value = if (latestEntry.temperature < -100) 0f else latestEntry.temperature,
                    unit = "°C",
                    isWorking = latestEntry.temperature > -100,
                    state = if (latestEntry.temperature < -100) "Offline" else "Normal"
                )
            )
        } else {
            // Fallback to dummy data
            listOf(
                SensorStatus("pH", 0f, "", false, "Offline"),
                SensorStatus("Turbidity", 0f, "NTU", false, "Offline"),
                SensorStatus("TDS", 0f, "ppm", false, "Offline"),
                SensorStatus("Temperature", 0f, "°C", false, "Offline")
            )
        }
    }
    
    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            timestamp
        }
    }
    
    private fun determinePHState(ph: Float): String = when {
        ph < 6.5f -> "Low"
        ph > 8.5f -> "High"
        else -> "Normal"
    }
    
    private fun determineTurbidityState(turbidity: Float): String = when {
        turbidity > 500f -> "High"
        else -> "Normal"
    }
    
    private fun determineTDSState(tds: Float): String = when {
        tds > 500f -> "High"
        tds < 50f -> "Low"
        else -> "Normal"
    }
} 