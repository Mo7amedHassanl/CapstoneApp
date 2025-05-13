package com.m7md7sn.capstoneApp.ui.screen.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m7md7sn.capstoneApp.data.model.SensorStatus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.m7md7sn.capstoneApp.data.model.TimedSensorReading
import com.m7md7sn.capstoneApp.ui.screen.sensor.VicoSensorLineChart

@Composable
fun MonitoringScreen(
    viewModel: MonitoringViewModel = hiltViewModel(),
    onSensorCardClick: (Int) -> Unit = {}
) {
    val sensors by viewModel.sensorStatuses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // For segmented chart
    val sensorTypes = sensors.map { it.name }
    var selectedTab by remember { mutableStateOf(0) }

    val readings = if (sensorTypes.isNotEmpty()) viewModel.getReadingsForSensor(sensorTypes[selectedTab]) else emptyList<TimedSensorReading>()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && sensors.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null && sensors.isEmpty()) {
            ErrorView(
                errorMessage = error ?: "Unknown error",
                onRetry = { viewModel.refresh() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (sensors.all { it.isWorking }) "All Sensors Working" else "Some Sensors Not Working",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                // Horizontal row of sensor status cards
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sensors) { sensor ->
                        MonitoringSensorStatusCard(sensor = sensor, onClick = { onSensorCardClick(sensors.indexOf(sensor)) })
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                // Segmented control/tab row for chart
                if (sensorTypes.isNotEmpty()) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        sensorTypes.forEachIndexed { index, type ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(type) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    // Chart for selected sensor
                    if (readings.isNotEmpty()) {
                        VicoSensorLineChart(readings)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No data for ${sensorTypes[selectedTab]}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            // Show refresh indicator if there's an ongoing refresh
            if (isLoading && sensors.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun MonitoringSensorStatusCard(sensor: SensorStatus, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 120.dp, height = 90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = sensor.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = sensor.value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sensor.state,
                style = MaterialTheme.typography.labelSmall,
                color = if (sensor.state == "Normal") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = if (sensor.isWorking) "Working" else "Not Working",
                style = MaterialTheme.typography.labelSmall,
                color = if (sensor.isWorking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun NoDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No sensor data available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

fun phColor(pH: Float): Color {
    return when {
        pH < 1f -> Color(0xFFFF0000) // Red
        pH < 2f -> Color(0xFFFF4500) // Orange-Red
        pH < 3f -> Color(0xFFFF9900) // Orange
        pH < 4f -> Color(0xFFFFC100) // Yellow-Orange
        pH < 5f -> Color(0xFFFFFF00) // Yellow
        pH < 6f -> Color(0xFFBFFF00) // Yellow-Green
        pH < 7f -> Color(0xFF00FF00) // Green
        pH < 8f -> Color(0xFF00FFB0) // Green-Cyan
        pH < 9f -> Color(0xFF00FFFF) // Cyan
        pH < 10f -> Color(0xFF00BFFF) // Blue-Green
        pH < 11f -> Color(0xFF007FFF) // Blue
        pH < 12f -> Color(0xFF4B0082) // Blue-Violet
        pH < 13f -> Color(0xFF8B00FF) // Violet
        else -> Color(0xFF800080) // Purple
    }
}

@Composable
fun MonitoringSensorCard(sensor: SensorStatus) {
    val overlayColor = when (sensor.name.lowercase()) {
        "ph" -> phColor(sensor.value)
        else -> {
            if (sensor.state == "Normal") {
                Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFF44336), Color(0xFFE57373)))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(2.dp)
            .neumorphicShadow(cornerRadius = 20.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
        ) {
            // Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .let {
                        when (overlayColor) {
                            is Color -> it.background(overlayColor.copy(alpha = 0.5f))
                            is Brush -> it.background(overlayColor, alpha = 0.5f)
                            else -> it
                        }
                    }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = sensor.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${sensor.value} ${sensor.unit}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sensor.state,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sensor.isWorking) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Working",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Working", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Not Working",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Not Working", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.neumorphicShadow(cornerRadius: Dp): Modifier = this.then(
    Modifier.shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = MaterialTheme.colorScheme.surfaceBright,
        spotColor = MaterialTheme.colorScheme.surfaceDim
    )
)

// Helper to determine state and create the list
fun getSensorStatuses(
    ph: Float, phWorking: Boolean,
    turbidity: Float, turbidityWorking: Boolean,
    tds: Float, tdsWorking: Boolean,
    temp: Float, tempWorking: Boolean
): List<SensorStatus> {
    val phState = when {
        ph < 6.5f -> "Low"
        ph > 8.5f -> "High"
        else -> "Normal"
    }
    val turbidityState = if (turbidity < 50f) "Normal" else "High"
    val tdsState = if (tds < 500f) "Normal" else "High"
    val tempState = when {
        temp < 20f -> "Low"
        temp > 35f -> "High"
        else -> "Normal"
    }
    return listOf(
        SensorStatus("pH", ph, "", phWorking, phState),
        SensorStatus("Turbidity", turbidity, "NTU", turbidityWorking, turbidityState),
        SensorStatus("TDS", tds, "ppm", tdsWorking, tdsState),
        SensorStatus("Temperature", temp, "°C", tempWorking, tempState)
    )
}

@Preview(showBackground = true)
@Composable
fun MonitoringScreenPreview() {
    // For preview, use sample data instead of ViewModel
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Water Quality Sensors",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Sample sensor statuses for preview
        val previewSensors = listOf(
            SensorStatus("pH", 7.2f, "", true, "Normal"),
            SensorStatus("Turbidity", 30f, "NTU", true, "Normal"),
            SensorStatus("TDS", 600f, "ppm", false, "High"),
            SensorStatus("Temperature", 19f, "°C", true, "Normal")
        )
        
        previewSensors.forEach { sensor ->
            MonitoringSensorCard(sensor)
        }
    }
} 