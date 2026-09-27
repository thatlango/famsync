package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FamilyDao
import com.example.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.familyDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: FamilyDao) {
                // Seed Household Profile
                dao.updateHouseholdProfile(
                    HouseholdProfile(
                        name = "Odur Family",
                        nickname = "Odur Hub",
                        motto = "Growing Together Every Day",
                        homeLocation = "Gulu, Uganda",
                        timezone = "Africa/Kampala"
                    )
                )

                // Seed Members with Birthdays
                val momId = dao.insertMember(FamilyMember(name = "Sarah (Mom)", role = "Parent", relationship = "Mother", avatarEmoji = "👩", colorHex = "#E05A47", points = 120, birthdayMonth = 9, birthdayDay = 5, birthdayYear = 1988))
                val dadId = dao.insertMember(FamilyMember(name = "David (Dad)", role = "Parent", relationship = "Father", avatarEmoji = "👨", colorHex = "#3B82F6", points = 95, birthdayMonth = 4, birthdayDay = 12, birthdayYear = 1985))
                val emmaId = dao.insertMember(FamilyMember(name = "Emma", role = "Child", relationship = "Daughter", avatarEmoji = "👧", colorHex = "#EC4899", points = 60, birthdayMonth = 8, birthdayDay = 18, birthdayYear = 2018))
                val noahId = dao.insertMember(FamilyMember(name = "Noah", role = "Child", relationship = "Son", avatarEmoji = "👦", colorHex = "#D97706", points = 45, birthdayMonth = 11, birthdayDay = 24, birthdayYear = 2021))

                // Seed Events including School Calendar & Kids Activities
                val now = System.currentTimeMillis()
                val dayMs = 86400000L

                dao.insertEvent(
                    FamilyEvent(
                        title = "School Term Begins 🏫",
                        description = "Back to school! Pack backpack & lunchbox",
                        dateEpochMillis = now + (3 * dayMs),
                        timeString = "08:00 AM",
                        category = "School Calendar",
                        attendeeNames = "Emma, Noah",
                        isKidsActivity = true,
                        location = "Oakridge Elementary"
                    )
                )
                dao.insertEvent(
                    FamilyEvent(
                        title = "Emma Soccer Practice ⚽",
                        description = "Bring water bottle & shin guards",
                        dateEpochMillis = now,
                        timeString = "16:30",
                        category = "Kids Activity",
                        attendeeNames = "Emma, Sarah (Mom)",
                        isKidsActivity = true,
                        location = "Westside Park Field 2"
                    )
                )
                dao.insertEvent(
                    FamilyEvent(
                        title = "School Science Fair 🔬",
                        description = "Volcano project presentation",
                        dateEpochMillis = now + (10 * dayMs),
                        timeString = "10:00 AM",
                        category = "School Calendar",
                        attendeeNames = "Emma, Everyone",
                        isKidsActivity = true,
                        location = "School Auditorium"
                    )
                )
                dao.insertEvent(
                    FamilyEvent(
                        title = "Family Pizza & Movie Night 🍕",
                        description = "Homemade pepperoni & veggie pizzas",
                        dateEpochMillis = now + dayMs,
                        timeString = "18:30",
                        category = "Family Event",
                        attendeeNames = "Everyone",
                        isKidsActivity = false,
                        location = "Living Room"
                    )
                )
                dao.insertEvent(
                    FamilyEvent(
                        title = "Noah Swim Lessons",
                        description = "Goggles and towel needed",
                        dateEpochMillis = now + (2 * dayMs),
                        timeString = "10:00",
                        category = "Kids Activity",
                        attendeeNames = "Noah, David (Dad)",
                        isKidsActivity = true,
                        location = "Community Pool"
                    )
                )

                // Seed Mood Check-ins
                val todayStr = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())
                dao.insertMood(
                    MoodCheckIn(
                        memberId = emmaId,
                        memberName = "Emma",
                        moodEmoji = "🤩",
                        moodLabel = "Excited",
                        note = "Looking forward to soccer practice!",
                        timestampMillis = now - 3600000,
                        dateString = todayStr
                    )
                )
                dao.insertMood(
                    MoodCheckIn(
                        memberId = momId,
                        memberName = "Sarah (Mom)",
                        moodEmoji = "😊",
                        moodLabel = "Happy",
                        note = "Productive morning!",
                        timestampMillis = now - 7200000,
                        dateString = todayStr
                    )
                )

                // Seed Groceries
                dao.insertGroceryItem(GroceryItem(name = "Organic Whole Milk", category = "Dairy & Eggs", quantity = "2 Gallons", addedByMemberName = "Sarah (Mom)"))
                dao.insertGroceryItem(GroceryItem(name = "Fresh Strawberries", category = "Produce", quantity = "1 Box", addedByMemberName = "Emma"))
                dao.insertGroceryItem(GroceryItem(name = "Whole Wheat Bread", category = "Bakery", quantity = "1 Loaf", addedByMemberName = "David (Dad)"))
                dao.insertGroceryItem(GroceryItem(name = "Cheddar Cheese Slice", category = "Dairy & Eggs", quantity = "1 Pack", isPurchased = true, addedByMemberName = "David (Dad)"))
                dao.insertGroceryItem(GroceryItem(name = "Honey Oat Cereal", category = "Pantry", quantity = "1 Box", addedByMemberName = "Noah"))

                // Seed Tasks & Chores
                dao.insertTask(
                    TaskItem(
                        title = "Clean Up Playroom Toys",
                        description = "Put building blocks back in storage bins",
                        assignedMemberId = noahId,
                        assignedMemberName = "Noah",
                        rewardPoints = 15,
                        category = "Chores",
                        isRotating = true
                    )
                )
                dao.insertTask(
                    TaskItem(
                        title = "Set & Wipe Dinner Table 🍽️",
                        description = "Place placemats, napkins, and cutlery for family dinner",
                        assignedMemberId = emmaId,
                        assignedMemberName = "Emma",
                        rewardPoints = 15,
                        category = "Chores",
                        isRotating = true
                    )
                )
                dao.insertTask(
                    TaskItem(
                        title = "Read 20 Minutes",
                        description = "Chapter 4 of Magic Treehouse",
                        assignedMemberId = emmaId,
                        assignedMemberName = "Emma",
                        rewardPoints = 20,
                        category = "Schoolwork",
                        isRotating = false
                    )
                )
                dao.insertTask(
                    TaskItem(
                        title = "Feed Buddy 🐶",
                        description = "Give fresh water & 1 cup kibble",
                        assignedMemberId = emmaId,
                        assignedMemberName = "Emma",
                        rewardPoints = 10,
                        isCompleted = true,
                        category = "Pet Care",
                        isRotating = true
                    )
                )
                dao.insertTask(
                    TaskItem(
                        title = "Take Out Recycling ♻️",
                        description = "Empty kitchen recycling bin into outdoor cart",
                        assignedMemberId = dadId,
                        assignedMemberName = "David (Dad)",
                        rewardPoints = 10,
                        category = "Chores",
                        isRotating = true
                    )
                )
            }
        }
    }
}
