package com.m7md7sn.capstoneApp.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.m7md7sn.capstoneApp.data.model.FirebaseSensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSensorRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    private val sensorDataRef = database.getReference("sensor_data")
    
    /**
     * Get a flow of the latest sensor data from Firebase
     */
    fun getLatestSensorData(): Flow<Map<String, FirebaseSensorData>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataMap = mutableMapOf<String, FirebaseSensorData>()
                
                for (childSnapshot in snapshot.children) {
                    val key = childSnapshot.key ?: continue
                    val sensorData = childSnapshot.getValue(FirebaseSensorData::class.java) ?: continue
                    sensorDataMap[key] = sensorData
                }
                
                trySend(sensorDataMap).isSuccess
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        sensorDataRef.addValueEventListener(valueEventListener)
        
        // Remove listener when flow collection ends
        awaitClose {
            sensorDataRef.removeEventListener(valueEventListener)
        }
    }
    
    /**
     * Get a flow of sensor data history for a specific sensor
     */
    fun getSensorHistory(): Flow<List<FirebaseSensorData>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataList = mutableListOf<FirebaseSensorData>()
                
                for (childSnapshot in snapshot.children) {
                    val sensorData = childSnapshot.getValue(FirebaseSensorData::class.java) ?: continue
                    sensorDataList.add(sensorData)
                }
                
                trySend(sensorDataList.sortedBy { it.timestamp }).isSuccess
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        // Limit to last 50 readings
        sensorDataRef.limitToLast(50).addValueEventListener(valueEventListener)
        
        // Remove listener when flow collection ends
        awaitClose {
            sensorDataRef.removeEventListener(valueEventListener)
        }
    }
} 