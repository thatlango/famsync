package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ExpressiveMoodItem(
    val emoji: String,
    val label: String,
    val containerColor: Color,
    val textColor: Color
)

val defaultMoodGridList = listOf(
    ExpressiveMoodItem("😊", "Happy", Color(0xFFFEF3C7), Color(0xFF92400E)),
    ExpressiveMoodItem("🤩", "Excited", Color(0xFFFCE7F3), Color(0xFF9D174D)),
    ExpressiveMoodItem("😌", "Calm", Color(0xFFE0F2FE), Color(0xFF075985)),
    ExpressiveMoodItem("🤪", "Playful", Color(0xFFF3E8FF), Color(0xFF6B21A8)),
    ExpressiveMoodItem("😴", "Tired", Color(0xFFEDE9FE), Color(0xFF5B21B6)),
    ExpressiveMoodItem("🎨", "Creative", Color(0xFFECFDF5), Color(0xFF065F46)),
    ExpressiveMoodItem("🥳", "Festive", Color(0xFFFFE4E6), Color(0xFF9F1239)),
    ExpressiveMoodItem("💪", "Strong", Color(0xFFFFF7ED), Color(0xFF9A3412)),
    ExpressiveMoodItem("🥺", "Sad", Color(0xFFE0F2FE), Color(0xFF1E40AF)),
    ExpressiveMoodItem("😟", "Anxious", Color(0xFFFEF9C3), Color(0xFF854D0E)),
    ExpressiveMoodItem("🧠", "Focused", Color(0xFFE0E7FF), Color(0xFF3730A3)),
    ExpressiveMoodItem("😡", "Upset", Color(0xFFFEE2E2), Color(0xFF991B1B))
)

@Composable
fun DailyMoodGrid(
    selectedMood: ExpressiveMoodItem,
    onMoodSelected: (ExpressiveMoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_mood_grid")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            items(defaultMoodGridList) { mood ->
                val isSelected = selectedMood.label == mood.label
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "scale"
                )

                Surface(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onMoodSelected(mood) }
                        .testTag("mood_grid_item_${mood.label.lowercase()}"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) mood.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, mood.textColor) else null,
                    shadowElevation = if (isSelected) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mood.emoji,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mood.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) mood.textColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
