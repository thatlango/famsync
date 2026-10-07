package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FamilyDao
import com.example.data.entities.*

@Database(
    entities = [
        FamilyMember::class,
        FamilyEvent::class,
        MoodCheckIn::class,
        GroceryItem::class,
        TaskItem::class,
        HouseholdProfile::class
    ],
    version = 9,
    exportSchema = false
)
abstract class FamilyDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao

    companion object {
        @Volatile
        private var INSTANCE: FamilyDatabase? = null

        fun getDatabase(context: Context): FamilyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamilyDatabase::class.java,
                    "family_hub_database"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
