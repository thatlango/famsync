package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.FamilyCard
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit
) {
    var alwaysOn by remember { mutableStateOf(true) }
    var nightMode by remember { mutableStateOf(false) }
    var largeText by remember { mutableStateOf(false) }
    var autoRotate by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(title = "Home Hub Optimization", caption = "Configure for shared kitchen tablets or wall screens")

            FamilyCard {
                SettingsSwitchRow(
                    title = "Always-on Display",
                    subtitle = "Keep the hub visible at all times",
                    checked = alwaysOn,
                    onCheckedChange = { alwaysOn = it }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsSwitchRow(
                    title = "Auto-Dim at Night",
                    subtitle = "Reduce brightness after 9:00 PM",
                    checked = nightMode,
                    onCheckedChange = { nightMode = it }
                )
            }

            FamilyCard {
                SettingsSwitchRow(
                    title = "Large Text Mode",
                    subtitle = "Easier to read from across the room",
                    checked = largeText,
                    onCheckedChange = { largeText = it }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsSwitchRow(
                    title = "Auto-Rotate",
                    subtitle = "Supports landscape kitchen mounts",
                    checked = autoRotate,
                    onCheckedChange = { autoRotate = it }
                )
            }
            
            FamilyCard {
                Text("Screen Timeout", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                var sliderPosition by remember { mutableFloatStateOf(5f) }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 1f..60f,
                    steps = 10
                )
                Text("${sliderPosition.toInt()} minutes", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
