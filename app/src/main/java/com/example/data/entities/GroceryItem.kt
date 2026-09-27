package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Produce", // "Produce", "Dairy & Eggs", "Bakery", "Pantry", "Snacks", "Household", "Drinks"
    val quantity: String = "1",
    val isPurchased: Boolean = false,
    val addedByMemberName: String = "Family",
    val createdAtMillis: Long = System.currentTimeMillis()
)
