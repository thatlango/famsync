package com.example.data.repository

import com.example.data.dao.FamilyDao
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

class FamilyRepository(private val dao: FamilyDao) {
    val members: Flow<List<FamilyMember>> = dao.getAllMembers()
    val events: Flow<List<FamilyEvent>> = dao.getAllEvents()
    val moods: Flow<List<MoodCheckIn>> = dao.getAllMoods()
    val groceries: Flow<List<GroceryItem>> = dao.getAllGroceryItems()
    val tasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val householdProfile: Flow<HouseholdProfile?> = dao.getHouseholdProfile()

    fun getMoodsForDate(dateStr: String): Flow<List<MoodCheckIn>> = dao.getMoodsForDate(dateStr)

    // Household
    suspend fun updateHouseholdProfile(profile: HouseholdProfile) = dao.updateHouseholdProfile(profile)

    // Members
    suspend fun insertMember(member: FamilyMember) = dao.insertMember(member)
    suspend fun updateMember(member: FamilyMember) = dao.updateMember(member)
    suspend fun deleteMember(member: FamilyMember) = dao.deleteMember(member)
    suspend fun deductMemberPoints(memberId: Long, points: Int) = dao.addMemberPoints(memberId, -points)

    // Events
    suspend fun insertEvent(event: FamilyEvent) = dao.insertEvent(event)
    suspend fun updateEvent(event: FamilyEvent) = dao.updateEvent(event)
    suspend fun deleteEvent(event: FamilyEvent) = dao.deleteEvent(event)

    // Moods
    suspend fun insertMood(mood: MoodCheckIn) = dao.insertMood(mood)
    suspend fun deleteMood(mood: MoodCheckIn) = dao.deleteMood(mood)

    // Groceries
    suspend fun insertGrocery(item: GroceryItem) = dao.insertGroceryItem(item)
    suspend fun updateGrocery(item: GroceryItem) = dao.updateGroceryItem(item)
    suspend fun deleteGrocery(item: GroceryItem) = dao.deleteGroceryItem(item)
    suspend fun clearCompletedGroceries() = dao.clearCompletedGroceries()

    // Tasks
    suspend fun insertTask(task: TaskItem) = dao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = dao.deleteTask(task)

    suspend fun completeTask(task: TaskItem) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAtMillis = if (!task.isCompleted) System.currentTimeMillis() else 0
        )
        dao.updateTask(updated)
        // Award points if completing
        if (!task.isCompleted && task.assignedMemberId > 0) {
            dao.addMemberPoints(task.assignedMemberId, task.rewardPoints)
        }
    }
}
