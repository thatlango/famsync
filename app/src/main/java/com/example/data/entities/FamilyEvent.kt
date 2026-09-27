package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_events")
data class FamilyEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateEpochMillis: Long,
    val timeString: String = "All Day",
    val category: String = "Kids Activity", // "Kids Activity", "Family Event", "School", "Medical", "Sports", "Reminder"
    val attendeeNames: String = "", // Comma-separated member names
    val isKidsActivity: Boolean = false,
    val location: String = ""
)
