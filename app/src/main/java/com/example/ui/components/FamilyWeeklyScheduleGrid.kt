package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyEvent
import com.example.data.entities.FamilyMember
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FamilyWeeklyScheduleGrid(
    events: List<FamilyEvent>,
    members: List<FamilyMember>,
    onDeleteEvent: (FamilyEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableIntStateOf(0) } // 0 = All/Next 24h, 1..7 = Mon..Sun
    var viewMode by remember { mutableStateOf("24H") } // "24H", "GRID", "LIST"

    val calendar = Calendar.getInstance()
    val nowMillis = System.currentTimeMillis()
    val twentyFourHoursMillis = nowMillis + (24 * 60 * 60 * 1000L)

    // Identify events in the next 24 hours
    val upcoming24HEvents = remember(events) {
        events.filter { event ->
            // If date is within next 24h or scheduled for today
            event.dateEpochMillis in (nowMillis - 3600*1000L)..twentyFourHoursMillis ||
            event.timeString.contains("Today", ignoreCase = true)
        }.sortedBy { it.dateEpochMillis }
    }

    // Days of the week generator starting from current week
    val dayNames = listOf("🔥 Next 24H", "Mon 📅", "Tue 📅", "Wed 📅", "Thu 📅", "Fri 📅", "Sat 🎈", "Sun ☀️")

    // Helper to get member color hex
    fun getMemberColor(attendeeNames: String): Color {
        val member = members.find { m -> attendeeNames.contains(m.name, ignoreCase = true) }
        return if (member != null) {
            try {
                Color(android.graphics.Color.parseColor(member.colorHex))
            } catch (e: Exception) {
                Color(0xFFE05A47)
            }
        } else {
            when {
                attendeeNames.contains("Kids", ignoreCase = true) -> Color(0xFF4A90E2)
                attendeeNames.contains("Mom", ignoreCase = true) -> Color(0xFF9B51E0)
                attendeeNames.contains("Dad", ignoreCase = true) -> Color(0xFF27AE60)
                else -> Color(0xFFF2994A)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Spotlight Banner for Next 24 Hours
        if (upcoming24HEvents.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next 24 Hours Highlights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${upcoming24HEvents.size} Upcoming",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(upcoming24HEvents) { ev ->
                            val cardColor = getMemberColor(ev.attendeeNames)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.width(220.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(cardColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = ev.timeString,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ev.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )

                                    if (ev.location.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = ev.location,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    if (ev.attendeeNames.isNotBlank()) {
                                        Text(
                                            text = "👤 ${ev.attendeeNames}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cardColor,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(top = 4.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // View Mode Selector & Day Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Schedule Grid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = viewMode == "24H",
                    onClick = { viewMode = "24H"; selectedDayIndex = 0 },
                    label = { Text("Next 24H", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = viewMode == "GRID",
                    onClick = { viewMode = "GRID" },
                    label = { Text("Grid", fontSize = 11.sp) }
                )
            }
        }

        // Days of week selector chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(dayNames.indices.toList()) { index ->
                FilterChip(
                    selected = selectedDayIndex == index,
                    onClick = { selectedDayIndex = index },
                    label = { Text(dayNames[index]) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Color Legend by Family Member
        if (members.isNotEmpty()) {
            Text(
                text = "Color-Coded Family Members:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(members) { m ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(m.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, color)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            FamilyAvatar(avatarEmoji = m.avatarEmoji, photoUri = m.photoUri, size = 20.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = m.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}
