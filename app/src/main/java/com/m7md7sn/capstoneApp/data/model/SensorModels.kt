package com.m7md7sn.capstoneApp.data.model

import androidx.compose.ui.graphics.vector.ImageVector

// Represents a sensor reading for the main screen
data class SensorReading(val label: String, val value: String, val unit: String?, val icon: ImageVector? = null)

// Represents a system part (e.g., Pumps, Sensors)
data class SystemPart(val name: String, val icon: ImageVector)

// Represents a sensor reading at a specific time (for trends)
data class TimedSensorReading(val value: Float, val time: String)

// Represents the status of a sensor (for monitoring)
data class SensorStatus(
    val name: String,
    val value: Float,
    val unit: String,
    val isWorking: Boolean,
    val state: String
) 