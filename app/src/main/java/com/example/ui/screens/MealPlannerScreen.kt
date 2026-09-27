package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MealPlanItem
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MealPlannerScreen(
    mealPlans: List<MealPlanItem>,
    onAddIngredientsToGrocery: (List<String>, String) -> Int,
    onToggleMealCooked: (Long) -> Unit,
    onAddOrUpdateMeal: (String, String, String, String, List<String>, String) -> Unit,
    onNavigateToGrocery: () -> Unit
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val today = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()) }
    var selectedDay by remember { mutableStateOf(today) }
    var expandedMealId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(
                    title = "Meal Planner",
                    caption = "What's cooking tonight?",
                    action = {
                        IconButton(onClick = onNavigateToGrocery) {
                            Icon(Icons.Default.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }

            // Compact Day Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(days) { day ->
                        val isSelected = day == selectedDay
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDay = day },
                            label = { Text(day.take(3), fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // AI Suggestion Banner
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡 AI Hint:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tuesdays are usually Taco nights!", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Day Meals
            val dayMeals = mealPlans.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
            if (dayMeals.isEmpty()) {
                item {
                    EmptyState(emoji = "🥘", title = "No meals planned", subtitle = "Tap + to plan for $selectedDay")
                }
            } else {
                items(dayMeals) { meal ->
                    ExpandableMealCard(
                        meal = meal,
                        isExpanded = expandedMealId == meal.id,
                        onExpand = { expandedMealId = if (expandedMealId == meal.id) null else meal.id },
                        onSync = { onAddIngredientsToGrocery(meal.ingredients, meal.chefMemberName) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ExpandableMealCard(
    meal: MealPlanItem,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onSync: () -> Unit
) {
    FamilyCard(onClick = onExpand) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(meal.emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.recipeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Chef: ${meal.chefMemberName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = MaterialTheme.colorScheme.primary)
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Ingredients", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                meal.ingredients.forEach { ing ->
                    Text("• $ing", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSync,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Sync, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add all to Grocery", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
