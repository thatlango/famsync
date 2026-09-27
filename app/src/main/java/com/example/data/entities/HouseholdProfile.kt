package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_profile")
data class HouseholdProfile(
    @PrimaryKey val id: Int = 1, // Singleton
    val name: String,
    val displayName: String = "",
    val nickname: String = "",
    val motto: String = "",
    val photoUri: String? = null,
    val coverImageUri: String? = null,
    val homeLocation: String = "",
    val address: String = "",
    val country: String = "Uganda",
    val language: String = "English",
    val timezone: String = "Africa/Kampala",
    val anniversary: String = "",
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val currentMode: String = "Home",
    val parentPin: String = "",
    val useBiometrics: Boolean = false,
    val ambientMode: String = "Clock", // "Clock", "Photo", "Dashboard"
    val nightModeEnabled: Boolean = true,
    val autoBrightness: Boolean = true,
    val burnInProtection: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)
