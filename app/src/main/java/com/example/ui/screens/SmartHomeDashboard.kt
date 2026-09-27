package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.FamilyCard
import com.example.ui.components.SectionHeader

data class SmartDevice(
    val id: String,
    val name: String,
    val room: String,
    val type: DeviceType,
    val status: String,
    val isOn: Boolean = false
)

enum class DeviceType { LIGHT, PLUG, THERMOSTAT, SPEAKER, CAMERA, TV }

@Composable
fun SmartHomeDashboard() {
    val devices = remember {
        mutableStateListOf(
            SmartDevice("1", "Living Room Light", "Living Room", DeviceType.LIGHT, "60%", true),
            SmartDevice("2", "Kitchen Plug", "Kitchen", DeviceType.PLUG, "Idle", false),
            SmartDevice("3", "Thermostat", "Hallway", DeviceType.THERMOSTAT, "72°F", true),
            SmartDevice("4", "Home Speaker", "Living Room", DeviceType.SPEAKER, "Playing", true),
            SmartDevice("5", "Front Door Cam", "Outside", DeviceType.CAMERA, "Online", true),
            SmartDevice("6", "Bedroom TV", "Bedroom", DeviceType.TV, "Off", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Smart Home",
            caption = "Matter & Google Home compatible devices",
            action = {
                IconButton(onClick = { /* Add Device */ }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(devices) { device ->
                DeviceCard(device) {
                    val index = devices.indexOf(device)
                    if (index != -1) {
                        devices[index] = device.copy(isOn = !device.isOn, status = if (!device.isOn) "On" else "Off")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(device: SmartDevice, onToggle: () -> Unit) {
    FamilyCard(
        containerColor = if (device.isOn) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        onClick = onToggle
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (device.isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (device.type) {
                                DeviceType.LIGHT -> Icons.Default.Lightbulb
                                DeviceType.PLUG -> Icons.Default.Power
                                DeviceType.THERMOSTAT -> Icons.Default.Thermostat
                                DeviceType.SPEAKER -> Icons.AutoMirrored.Filled.VolumeUp
                                DeviceType.CAMERA -> Icons.Default.Videocam
                                DeviceType.TV -> Icons.Default.Tv
                            },
                            contentDescription = null,
                            tint = if (device.isOn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Switch(checked = device.isOn, onCheckedChange = { onToggle() }, modifier = Modifier.scale(0.8f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(device.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${device.room} • ${device.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
