package com.m7md7sn.capstoneApp.ui.screen.control

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ControlViewModel @Inject constructor() : ViewModel() {
    private val _pumpStates = MutableStateFlow(List(7) { false })
    val pumpStates: StateFlow<List<Boolean>> = _pumpStates

    fun togglePump(index: Int) {
        _pumpStates.value = _pumpStates.value.toMutableList().also { it[index] = !it[index] }
    }
} 