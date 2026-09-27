package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.entities.FamilyMember
import com.example.data.entities.GroceryItem
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryScreen(
    groceries: List<GroceryItem>,
    members: List<FamilyMember>,
    onAddGrocery: (name: String, category: String, quantity: String, addedBy: String) -> Unit,
    onTogglePurchased: (GroceryItem) -> Unit,
    onDeleteGrocery: (GroceryItem) -> Unit,
    onClearCompleted: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Produce", "Meat", "Bakery", "Dairy", "Household", "Drinks", "Snacks")

    val pendingGroceries = groceries.filter { !it.isPurchased }
    val filteredGroceries = remember(pendingGroceries, selectedCategory, searchQuery) {
        pendingGroceries.filter { 
            (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_grocery_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with Search, Voice, Barcode
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search shared list...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { /* Voice */ }) { Icon(Icons.Default.Mic, null) }
                            IconButton(onClick = { /* Barcode */ }) { Icon(Icons.Default.QrCodeScanner, null) }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Summary
                item {
                    SummarySection(groceries)
                }

                // Section 2: AI Suggestions
                item {
                    SectionHeader(
                        title = "Smart Suggestions",
                        caption = "Based on your family habits",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SmartGrocerySuggestions(
                        members = members,
                        onAddGrocery = onAddGrocery
                    )
                }

                // Section 3: Categories
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Section 4: Shopping List
                if (filteredGroceries.isEmpty()) {
                    item {
                        EmptyState(
                            emoji = "🛒",
                            title = "List is empty",
                            subtitle = "Add items to your shared family list."
                        )
                    }
                } else {
                    val grouped = filteredGroceries.groupBy { it.category }
                    grouped.forEach { (category, items) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(items) { item ->
                            GroceryChecklistItem(
                                item = item,
                                onToggle = { onTogglePurchased(item) },
                                onDelete = { onDeleteGrocery(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false }
        ) {
            AddGrocerySheetContent(
                members = members,
                onAdd = { name, cat, qty, addedBy ->
                    onAddGrocery(name, cat, qty, addedBy)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun SummarySection(groceries: List<GroceryItem>) {
    val pending = groceries.filter { !it.isPurchased }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FamilyCard(modifier = Modifier.weight(1f)) {
            Text("${pending.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Items", style = MaterialTheme.typography.labelSmall)
        }
        FamilyCard(modifier = Modifier.weight(1f)) {
            Text("Est. Budget", style = MaterialTheme.typography.labelSmall)
            Text("UGX 45k", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GroceryChecklistItem(
    item: GroceryItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        ChecklistItem(
            title = item.name,
            isCompleted = item.isPurchased,
            onCheckedChange = { onToggle() },
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.quantity, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        )
    }
}

@Composable
private fun AddGrocerySheetContent(
    members: List<FamilyMember>,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Produce") }
    var quantity by remember { mutableStateOf("1") }
    var addedBy by remember { mutableStateOf(members.firstOrNull()?.name ?: "Family") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add to Grocery List", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Qty") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Button(
            onClick = { onAdd(name, category, quantity, addedBy) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Add Item", fontWeight = FontWeight.Bold)
        }
    }
}
