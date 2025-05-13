package com.m7md7sn.capstoneApp.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m7md7sn.capstoneApp.R
import com.m7md7sn.capstoneApp.data.model.SensorReading
import com.m7md7sn.capstoneApp.data.model.SystemPart
import com.m7md7sn.capstoneApp.ui.theme.TannitheaTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.m7md7sn.capstoneApp.ui.screen.monitoring.phColor
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onSensorCardClick: (Int) -> Unit = {},
    onSystemPartClick: (Int) -> Unit = {},
) {
    val sensorReadings by viewModel.sensorReadings.collectAsState()
    val systemParts by viewModel.systemParts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Determine the most critical sensor (for the big widget)
    val criticalSensor = sensorReadings.minByOrNull { reading ->
        // Example: pH farthest from 7 is most critical, others by value
        when (reading.label.lowercase()) {
            "ph" -> kotlin.math.abs((reading.value.toFloatOrNull() ?: 7f) - 7f)
            else -> -(reading.value.toFloatOrNull() ?: 0f)
        }
    }
    val criticalColor = when (criticalSensor?.label?.lowercase()) {
        "ph" -> if ((criticalSensor.value.toFloatOrNull() ?: 7f) in 6.5..8.5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val criticalLabel = criticalSensor?.label ?: "-"
    val criticalValue = criticalSensor?.value ?: "-"

    // Status banner logic
    val allNormal = sensorReadings.all { reading ->
        when (reading.label.lowercase()) {
            "ph" -> (reading.value.toFloatOrNull() ?: 7f) in 6.5..8.5
            "tds" -> (reading.value.toFloatOrNull() ?: 0f) < 500f
            "turbidity" -> (reading.value.toFloatOrNull() ?: 0f) < 50f
            "temperature" -> (reading.value.toFloatOrNull() ?: 25f) in 20f..35f
            else -> true
        }
    }
    val statusText = if (allNormal) "All Systems Normal" else "Check Sensor Values"
    val statusColor = if (allNormal) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && sensorReadings.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null && sensorReadings.isEmpty()) {
                ErrorView(
                    errorMessage = error ?: "Unknown error",
                    onRetry = { viewModel.refresh() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 0.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Large circular water quality widget
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .neumorphicShadow(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = criticalValue,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = criticalColor
                            )
                            Text(
                                text = criticalLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = criticalColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
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
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    // Quick actions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(icon = Icons.Filled.Refresh, label = "Refresh") { viewModel.refresh() }
                        QuickActionButton(icon = Icons.Filled.Settings, label = "Control") { onSystemPartClick(0) }
                        QuickActionButton(icon = Icons.Filled.History, label = "History") { onSystemPartClick(1) }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    // Mini sensor cards row
                    Text(
                        text = "Sensors",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sensorReadings) { reading ->
                            MiniSensorCard(reading = reading, onClick = { onSensorCardClick(sensorReadings.indexOf(reading)) })
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    // System parts status row
                    Text(
                        text = "System Parts",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(systemParts) { part ->
                            SystemPartStatusCard(part = part, onClick = { onSystemPartClick(systemParts.indexOf(part)) })
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Show refresh indicator if there's an ongoing refresh
                if (isLoading && sensorReadings.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
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

@Composable
fun SectionHeader(title: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            thickness = 3.dp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun SensorGridModern(
    readings: List<SensorReading>,
    onSensorCardClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        readings.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEachIndexed { colIndex, reading ->
                    SensorCardModern(
                        reading = reading,
                        modifier = Modifier.weight(1f),
                        onCardClick = { onSensorCardClick(rowIndex * 2 + colIndex) }
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SensorCardModern(
    reading: SensorReading,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Neumorphic shadow colors
    val lightShadow = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.8f)
    val darkShadow = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f)
    val cardBg = MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = modifier
            .height(130.dp)
            .padding(2.dp)
            .neumorphicShadow(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        onClick = onCardClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = reading.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (reading.unit != null) "${reading.value} ${reading.unit}" else reading.value,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Neumorphic shadow modifier
@Composable
fun Modifier.neumorphicShadow(cornerRadius: Dp): Modifier = this.then(
    Modifier.shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = MaterialTheme.colorScheme.surfaceBright,
        spotColor = MaterialTheme.colorScheme.surfaceDim
    )
)

@Composable
fun SystemPartsListVertical(parts: List<SystemPart>, onSystemPartClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        parts.forEachIndexed { idx, part ->
            SystemPartLargeCard(part = part, onClick = { onSystemPartClick(idx) })
        }
    }
}

@Composable
fun SystemPartLargeCard(part: SystemPart, onClick: () -> Unit) {
    val lightShadow = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.8f)
    val darkShadow = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f)
    val cardBg = MaterialTheme.colorScheme.secondaryContainer
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(2.dp)
            .neumorphicShadow(18.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Icon(
                imageVector = part.icon,
                contentDescription = part.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = part.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun MiniSensorCard(reading: SensorReading, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 110.dp, height = 80.dp)
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
                text = reading.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = reading.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SystemPartStatusCard(part: SystemPart, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = part.icon,
                contentDescription = part.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = part.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // For preview, use sample data instead of ViewModel
    TannitheaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(title = "Water Quality")
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sample readings for preview
                val previewReadings = listOf(
                    SensorReading("TDS", "12.0", "ppm", Icons.Outlined.Sensors),
                    SensorReading("pH", "7", null, Icons.Outlined.Sensors),
                    SensorReading("Turbidity", "3.5", "NTU", Icons.Outlined.Sensors),
                    SensorReading("Temperature", "18", "°C", Icons.Outlined.Bolt)
                )
                SensorGridModern(readings = previewReadings, onSensorCardClick = {})
                
                Spacer(modifier = Modifier.height(32.dp))
                SectionHeader(title = "System Parts")
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sample system parts for preview
                val previewParts = listOf(
                    SystemPart("Pumps", Icons.Outlined.Sync),
                    SystemPart("Sensors", Icons.Outlined.Sensors)
                )
                SystemPartsListVertical(parts = previewParts, onSystemPartClick = {})
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}