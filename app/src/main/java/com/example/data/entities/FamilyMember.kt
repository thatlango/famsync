package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nickname: String = "",
    val role: String, // "Parent", "Guardian", "Teen", "Child", "Relative", "Caregiver", "Guest", "Pet"
    val relationship: String = "",
    val avatarEmoji: String,
    val colorHex: String = "#E05A47",
    val points: Int = 0,
    val streak: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val choresRemainingToday: Int = 0,
    val birthdayMonth: Int = 0,
    val birthdayDay: Int = 0,
    val birthdayYear: Int = 0,
    val schoolOrWorkplace: String = "",
    val pin: String? = null,
    val photoUri: String? = null,
    val isArchived: Boolean = false,
    val notes: String = "",
    val allergies: String = "",
    val medicalNotes: String = "",
    val favoriteMeals: String = "",
    val permissionsJson: String = "" // Placeholder for future feature permissions
) {
    fun getAge(): Int? {
        if (birthdayYear <= 0 || birthdayMonth <= 0 || birthdayDay <= 0) return null
        val today = java.util.Calendar.getInstance()
        val birthDate = java.util.Calendar.getInstance().apply {
            set(birthdayYear, birthdayMonth - 1, birthdayDay)
        }
        var age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
}
