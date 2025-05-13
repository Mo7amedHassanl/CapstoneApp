package com.m7md7sn.capstoneApp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7md7sn.capstoneApp.data.model.SensorReading
import com.m7md7sn.capstoneApp.data.model.SystemPart
import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _sensorReadings = MutableStateFlow<List<SensorReading>>(emptyList())
    val sensorReadings: StateFlow<List<SensorReading>> = _sensorReadings

    private val _systemParts = MutableStateFlow<List<SystemPart>>(emptyList())
    val systemParts: StateFlow<List<SystemPart>> = _systemParts
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadData()
    }

    private fun loadData() {
        // Load system parts immediately
        _systemParts.value = repository.getSystemParts()
        
        // Start with the synchronous method to get initial data
        _sensorReadings.value = repository.getSensorReadings()
        
        // Then subscribe to real-time updates
        viewModelScope.launch {
            repository.getSensorReadingsFlow()
                .catch { e ->
                    _error.value = "Error loading sensor data: ${e.message}"
                    _isLoading.value = false
                }
                .collect { readings ->
                    _sensorReadings.value = readings
                    _isLoading.value = false
                }
        }
    }
    
    fun refresh() {
        _isLoading.value = true
        _error.value = null
        loadData()
    }
} 