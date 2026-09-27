package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entities.HouseholdProfile
import com.example.ui.components.FamilyCard
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentCenterScreen(
    profile: HouseholdProfile?,
    onUpdateProfile: (HouseholdProfile) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("EXIT", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Management Hub", caption = "Configure household security and tools")
            }

            item {
                FamilyCard {
                    Text("Security & Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParentActionRow(Icons.Default.VpnKey, "Change Parent PIN", "Current: ****") { /* PIN Change */ }
                    ParentActionRow(Icons.Default.Fingerprint, "Use Biometrics", "Enable for quick access") { /* Biometrics Toggle */ }
                    ParentActionRow(Icons.Default.NoEncryption, "Content Restrictions", "Set kid-safe boundaries") { /* Restrictions */ }
                }
            }

            item {
                FamilyCard {
                    Text("Module Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParentActionRow(Icons.Default.Star, "Reward Management", "Edit star values and store") { /* Rewards */ }
                    ParentActionRow(Icons.Default.Task, "Task Logic", "Set approval requirements") { /* Tasks */ }
                    ParentActionRow(Icons.Default.Timer, "Routine Builder", "Configure family sequences") { /* Routines */ }
                }
            }

            item {
                FamilyCard {
                    Text("System", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParentActionRow(Icons.Default.Backup, "Backup & Restore", "Cloud sync and local copies") { /* Backup */ }
                    ParentActionRow(Icons.Default.Download, "Data Export", "Download CSV/JSON history") { /* Export */ }
                    ParentActionRow(Icons.Default.Settings, "Family Settings", "Reset or migrate hub") { /* Settings */ }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Lock Management Mode", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ParentActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
