package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyEvent
import com.example.data.entities.FamilyMember
import com.example.data.entities.HouseholdProfile
import com.example.data.repository.WeatherInfo

@Composable
fun ClockHeader(
    profile: HouseholdProfile?,
    activeMember: FamilyMember?,
    timeString: String,
    dateString: String,
    greeting: String,
    weather: WeatherInfo,
    nextEvent: FamilyEvent?,
    syncStatus: String = "Synced",
    onParentModeClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Family Identity
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🏡", fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile?.name ?: "Family Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeMember != null) activeMember.name else "Shared Hub",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // CENTER: Clock & Date
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // RIGHT: Info & Actions
            Row(
                modifier = Modifier.weight(1.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Next Event Snippet
                if (nextEvent != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.padding(end = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Next: ${nextEvent.title}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(nextEvent.timeString, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                            }
                        }
                    }
                }

                // Weather
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(weather.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${weather.temperatureF}°C", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text(profile?.homeLocation ?: "Local", style = MaterialTheme.typography.labelSmall)
                }

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onParentModeClick) {
                        Icon(Icons.Default.AdminPanelSettings, "Parent", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { /* Sync status */ }) {
                        Icon(Icons.Default.CloudSync, "Sync", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = onEmergencyClick) {
                        Icon(Icons.Default.Warning, "SOS", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
