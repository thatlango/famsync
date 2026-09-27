package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyEvent
import com.example.ui.FamilyTab
import java.text.SimpleDateFormat
import java.util.*

private val ScheduleCardBg = Color(0xFFFEF3C7) // Soft Warm Amber
private val HeaderTextColor = Color(0xFF78350F)
private val AccentColor = Color(0xFFB45309)

data class CalendarDayInfo(
    val date: Date,
    val dayName: String, // "Mon", "Tue"
    val dayNumber: String, // "3", "4"
    val isToday: Boolean,
    val startOfDayMillis: Long,
    val endOfDayMillis: Long,
    val eventCount: Int
)

@Composable
fun WeeklyCalendarScheduleWidget(
    events: List<FamilyEvent>,
    onNavigateTab: (FamilyTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var weekOffset by remember { mutableIntStateOf(0) }

    // Reference calendar for calculations
    val calendar = remember(weekOffset) {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, weekOffset)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val todayCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // Build list of 7 days for current week view
    val daysOfWeek = remember(weekOffset, events) {
        val list = mutableListOf<CalendarDayInfo>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val numFormat = SimpleDateFormat("d", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        for (i in 0 until 7) {
            val date = tempCal.time
            val startMillis = tempCal.timeInMillis
            val endMillis = startMillis + (24 * 60 * 60 * 1000) - 1

            val isToday = (tempCal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    tempCal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR))

            // Count events on this day
            val count = events.count { ev ->
                ev.dateEpochMillis in startMillis..endMillis
            }

            list.add(
                CalendarDayInfo(
                    date = date,
                    dayName = dayFormat.format(date),
                    dayNumber = numFormat.format(date),
                    isToday = isToday,
                    startOfDayMillis = startMillis,
                    endOfDayMillis = endMillis,
                    eventCount = count
                )
            )

            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    // Default selected index: Today if in this week, otherwise Monday (0)
    var selectedDayIndex by remember(weekOffset) {
        val todayIdx = daysOfWeek.indexOfFirst { it.isToday }
        mutableIntStateOf(if (todayIdx != -1) todayIdx else 0)
    }

    val selectedDayInfo = daysOfWeek.getOrNull(selectedDayIndex) ?: daysOfWeek.first()

    // Filter events for selected day
    val selectedDayEvents = remember(selectedDayInfo, events) {
        events.filter { ev ->
            ev.dateEpochMillis in selectedDayInfo.startOfDayMillis..selectedDayInfo.endOfDayMillis
        }.sortedBy { it.timeString }
    }

    val weekRangeTitle = remember(daysOfWeek) {
        val monthFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        if (daysOfWeek.isNotEmpty()) {
            "${monthFormat.format(daysOfWeek.first().date)} - ${monthFormat.format(daysOfWeek.last().date)}"
        } else ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_calendar_schedule_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ScheduleCardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title + Week Selector + Navigate Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = HeaderTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Family Weekly Schedule",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = HeaderTextColor
                        )
                        Text(
                            text = weekRangeTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { weekOffset-- },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Week",
                            tint = AccentColor
                        )
                    }

                    if (weekOffset != 0) {
                        TextButton(
                            onClick = { weekOffset = 0 },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentColor
                            )
                        }
                    }

                    IconButton(
                        onClick = { weekOffset++ },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Week",
                            tint = AccentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { onNavigateTab(FamilyTab.CALENDAR) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Schedule Tab",
                            tint = AccentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7-Day Calendar Selector Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, dayInfo ->
                    val isSelected = index == selectedDayIndex
                    val isToday = dayInfo.isToday

                    val chipBg = when {
                        isSelected -> Color(0xFFD97706) // Bold Amber / Gold
                        isToday -> Color(0xFFFDE68A)    // Soft Yellow
                        else -> Color.White.copy(alpha = 0.85f)
                    }

                    val textColor = when {
                        isSelected -> Color.White
                        isToday -> Color(0xFF78350F)
                        else -> Color(0xFF451A03)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(chipBg)
                            .border(
                                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                color = if (isToday && !isSelected) Color(0xFFD97706) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedDayIndex = index }
                            .padding(vertical = 8.dp, horizontal = 2.dp)
                            .testTag("calendar_day_chip_$index")
                    ) {
                        Text(
                            text = dayInfo.dayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = if (isSelected) 0.95f else 0.7f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayInfo.dayNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Event Indicator Dots/Badge
                        if (dayInfo.eventCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else Color(0xFFD97706)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${dayInfo.eventCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFFD97706) else Color.White
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Day Header & Events List
            AnimatedContent(
                targetState = selectedDayInfo,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "day_events_transition"
            ) { targetDay ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fullDateFormat = remember(targetDay) {
                        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(targetDay.date)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fullDateFormat,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HeaderTextColor
                        )

                        Text(
                            text = "${selectedDayEvents.size} ${if (selectedDayEvents.size == 1) "Event" else "Events"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (selectedDayEvents.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateTab(FamilyTab.CALENDAR) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("✨", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "No appointments scheduled. Tap to add!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Event",
                                    tint = AccentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedDayEvents.forEach { ev ->
                                EventItemCard(event = ev, onNavigateTab = onNavigateTab)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItemCard(
    event: FamilyEvent,
    onNavigateTab: (FamilyTab) -> Unit
) {
    val categoryIcon = when (event.category.lowercase()) {
        "kids activity", "kids" -> "🎨"
        "school" -> "🏫"
        "medical", "doctor" -> "🏥"
        "sports" -> "⚽"
        "family event", "family" -> "🎉"
        "reminder" -> "🔔"
        else -> "📅"
    }

    val categoryColor = when (event.category.lowercase()) {
        "kids activity", "kids" -> Color(0xFFEC4899)
        "school" -> Color(0xFF3B82F6)
        "medical", "doctor" -> Color(0xFF10B981)
        "sports" -> Color(0xFFF59E0B)
        else -> Color(0xFF8B5CF6)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateTab(FamilyTab.CALENDAR) }
            .testTag("event_item_${event.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(categoryIcon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = event.timeString,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (event.description.isNotEmpty() || event.location.isNotEmpty() || event.attendeeNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = event.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (event.description.isNotEmpty()) {
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (event.attendeeNames.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = event.attendeeNames,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
