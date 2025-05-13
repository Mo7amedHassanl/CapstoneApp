package com.m7md7sn.capstoneApp.ui.screen.sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7md7sn.capstoneApp.data.model.TimedSensorReading
import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _readings = MutableStateFlow<List<TimedSensorReading>>(emptyList())
    val readings: StateFlow<List<TimedSensorReading>> = _readings
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadReadings(sensorName: String) {
        _isLoading.value = true
        _error.value = null
        
        // First load with synchronous method for immediate data
        _readings.value = repository.getTimedSensorReadings(sensorName)
        
        // Then subscribe to real-time updates
        viewModelScope.launch {
            repository.getTimedSensorReadingsFlow(sensorName)
                .catch { e ->
                    _error.value = "Error loading sensor data: ${e.message}"
                    _isLoading.value = false
                }
                .collect { readings ->
                    _readings.value = readings
                    _isLoading.value = false
                }
        }
    }
    
    fun refresh(sensorName: String) {
        loadReadings(sensorName)
    }
} 