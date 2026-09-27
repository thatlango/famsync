package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_checkins")
data class MoodCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val memberName: String,
    val moodEmoji: String, // 😊, 🤩, 😌, 😴, 🥺, 😟, 🤪, 😡
    val moodLabel: String, // Happy, Excited, Calm, Tired, Sad, Anxious, Playful, Frustrated
    val note: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateString: String // YYYY-MM-DD
)
