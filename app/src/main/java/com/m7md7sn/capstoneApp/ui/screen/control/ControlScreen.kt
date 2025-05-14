package com.m7md7sn.capstoneApp.ui.screen.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ControlScreen(
    modifier: Modifier = Modifier,
    viewModel: ControlViewModel = hiltViewModel()
) {
    val pumpNames = listOf(
        "Monitoring Pump",
        "Acid Injection to Adsorption",
        "Adsorption to Aloe Vera",
        "Aloe Vera to Container",
        "Aloe Vera Fan",
        "Aloe Vera to Monitoring",
        "Monitoring to Last Place"
    )
    val pumpStates by viewModel.pumpStates.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        // Grid of pump controls
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pumpNames.size) { index ->
                PumpControlCard(
                    name = pumpNames[index],
                    isOn = pumpStates[index],
                    onToggle = { viewModel.togglePump(index) }
                )
            }
        }
    }
}

@Composable
fun PumpControlCard(name: String, isOn: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Switch(
                    checked = isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOn) "ON" else "OFF",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
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
fun Modifier.neumorphicShadow(cornerRadius: Dp): Modifier = this.then(
    Modifier.shadow(
        elevation = 12.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = MaterialTheme.colorScheme.surfaceBright,
        spotColor = MaterialTheme.colorScheme.surfaceDim
    )
) 