package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val assignedMemberId: Long = 0,
    val assignedMemberName: String = "Anyone",
    val rewardPoints: Int = 10,
    val dueDateMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAtMillis: Long = 0,
    val category: String = "Chores", // "Chores", "Schoolwork", "Pet Care", "Fitness", "Other"
    val isRotating: Boolean = false,
    val priority: Int = 1 // 0 = Low, 1 = Medium, 2 = High
)
