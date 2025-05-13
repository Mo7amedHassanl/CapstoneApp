package com.m7md7sn.capstoneApp.ui.screen.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7md7sn.capstoneApp.data.model.SensorStatus
import com.m7md7sn.capstoneApp.data.model.TimedSensorReading
import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _sensorStatuses = MutableStateFlow<List<SensorStatus>>(emptyList())
    val sensorStatuses: StateFlow<List<SensorStatus>> = _sensorStatuses
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sensorReadings = MutableStateFlow<Map<String, List<TimedSensorReading>>>(emptyMap())
    val sensorReadings: StateFlow<Map<String, List<TimedSensorReading>>> = _sensorReadings

    init {
        loadStatuses()
        loadAllSensorReadings()
    }

    private fun loadStatuses() {
        _isLoading.value = true
        _error.value = null
        
        // First load with synchronous method for immediate data
        _sensorStatuses.value = repository.getSensorStatuses()
        
        // Then subscribe to real-time updates
        viewModelScope.launch {
            repository.getSensorStatusesFlow()
                .catch { e ->
                    _error.value = "Error loading sensor data: ${e.message}"
                    _isLoading.value = false
                }
                .collect { statuses ->
                    _sensorStatuses.value = statuses
                    _isLoading.value = false
                }
        }
    }
    
    fun refresh() {
        loadStatuses()
    }

    private fun loadAllSensorReadings() {
        val sensorNames = listOf("pH", "Turbidity", "TDS", "Temperature")
        sensorNames.forEach { name ->
            viewModelScope.launch {
                repository.getTimedSensorReadingsFlow(name)
                    .catch { /* handle error if needed */ }
                    .collect { readings ->
                        _sensorReadings.update { it + (name to readings) }
                    }
            }
        }
    }

    fun getReadingsForSensor(sensorName: String): List<TimedSensorReading> {
        return sensorReadings.value[sensorName] ?: emptyList()
    }
} 