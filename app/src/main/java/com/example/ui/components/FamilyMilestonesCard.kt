package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.FamilyMember
import com.example.util.BirthdayUtil

@Composable
fun FamilyMilestonesCard(
    members: List<FamilyMember>,
    onOpenMemberDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val membersWithBirthdays = remember(members) {
        members.filter { it.birthdayMonth in 1..12 && it.birthdayDay in 1..31 }
    }

    val milestonesList = remember(membersWithBirthdays) {
        membersWithBirthdays.mapNotNull { member ->
            val days = BirthdayUtil.getDaysUntilBirthday(member)
            if (days != null) Pair(member, days) else null
        }.sortedBy { it.second }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("family_milestones_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Family Birthday Milestones 🎂",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upcoming birthdays & celebration countdowns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onOpenMemberDialog,
                    modifier = Modifier.testTag("milestones_add_bday_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Manage Family Birthdays"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (milestonesList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenMemberDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎈", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No Birthdays Configured Yet",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Tap here to add birthdays for family members & kids!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(milestonesList, key = { it.first.id }) { (member, days) ->
                        val isToday = days == 0
                        val isTomorrow = days == 1

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = if (isToday) 6.dp else 2.dp,
                            border = if (isToday) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF4081)) else null,
                            modifier = Modifier.width(150.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar & Badge
                                Box(contentAlignment = Alignment.TopEnd) {
                                    FamilyAvatar(
                                        avatarEmoji = member.avatarEmoji,
                                        photoUri = member.photoUri,
                                        size = 52.dp,
                                        backgroundColor = if (isToday) Color(0xFFFFF0F5) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    if (isToday) {
                                        Text("👑", fontSize = 16.sp, modifier = Modifier.offset(x = 4.dp, y = (-4).dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )

                                Text(
                                    text = "${BirthdayUtil.getMonthName(member.birthdayMonth)} ${member.birthdayDay}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = when {
                                        isToday -> Color(0xFFFF4081)
                                        isTomorrow -> Color(0xFFF59E0B)
                                        days <= 30 -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isToday -> "🎉 TODAY! 🎈"
                                            isTomorrow -> "🎂 Tomorrow!"
                                            days <= 30 -> "⏳ in $days days"
                                            else -> "$days days away"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isToday -> Color.White
                                            isTomorrow -> Color.White
                                            days <= 30 -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
