package com.example.data.dao

import androidx.room.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    // Family Members
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("UPDATE family_members SET points = points + :addPoints WHERE id = :memberId")
    suspend fun addMemberPoints(memberId: Long, addPoints: Int)

    // Household Profile
    @Query("SELECT * FROM household_profile WHERE id = 1")
    fun getHouseholdProfile(): Flow<HouseholdProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHouseholdProfile(profile: HouseholdProfile)

    // Family Events
    @Query("SELECT * FROM family_events ORDER BY dateEpochMillis ASC")
    fun getAllEvents(): Flow<List<FamilyEvent>>

    @Query("SELECT * FROM family_events WHERE dateEpochMillis >= :startMillis AND dateEpochMillis <= :endMillis ORDER BY dateEpochMillis ASC")
    fun getEventsBetween(startMillis: Long, endMillis: Long): Flow<List<FamilyEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: FamilyEvent): Long

    @Update
    suspend fun updateEvent(event: FamilyEvent)

    @Delete
    suspend fun deleteEvent(event: FamilyEvent)

    // Mood Check-Ins
    @Query("SELECT * FROM mood_checkins ORDER BY timestampMillis DESC")
    fun getAllMoods(): Flow<List<MoodCheckIn>>

    @Query("SELECT * FROM mood_checkins WHERE dateString = :dateStr ORDER BY timestampMillis DESC")
    fun getMoodsForDate(dateStr: String): Flow<List<MoodCheckIn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodCheckIn): Long

    @Delete
    suspend fun deleteMood(mood: MoodCheckIn)

    // Grocery Items
    @Query("SELECT * FROM grocery_items ORDER BY isPurchased ASC, createdAtMillis DESC")
    fun getAllGroceryItems(): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItem): Long

    @Update
    suspend fun updateGroceryItem(item: GroceryItem)

    @Delete
    suspend fun deleteGroceryItem(item: GroceryItem)

    @Query("DELETE FROM grocery_items WHERE isPurchased = 1")
    suspend fun clearCompletedGroceries()

    // Tasks & Chores
    @Query("SELECT * FROM task_items ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)
}
