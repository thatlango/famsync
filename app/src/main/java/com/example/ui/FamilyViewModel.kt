package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FamilyDatabase
import com.example.data.entities.*
import com.example.data.repository.CityLocation
import com.example.data.repository.FamilyRepository
import com.example.data.repository.WeatherInfo
import com.example.data.repository.WeatherRepository
import com.example.util.ParentPin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class FamilyTab {
    HOME, TASKS, CALENDAR, MEALS, MORE
}

data class MealPlanItem(
    val id: Long = System.currentTimeMillis(),
    val dayOfWeek: String,
    val mealType: String = "Dinner",
    val recipeTitle: String,
    val emoji: String = "🍲",
    val ingredients: List<String> = emptyList(),
    val chefMemberName: String = "Family",
    val isCooked: Boolean = false
)

data class FamilyGoal(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val category: String,
    val emoji: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean = false
)

data class QuickNote(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val authorName: String,
    val dateString: String,
    val isUrgent: Boolean = false,
    val isPinned: Boolean = true
)

data class DraftEmergencyContact(
    val name: String,
    val relation: String,
    val phone: String,
    val notes: String = ""
)

data class RewardStoreItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val costStars: Int,
    val emoji: String,
    val description: String
)

data class RoutineItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val durationSeconds: Int,
    val category: String, // "Morning", "Evening"
    val emoji: String,
    val instructions: String
)

enum class ThemeMode {
    LIGHT, DARK, AUTO
}

enum class HouseMode {
    HOME, SCHOOL_MORNING, AFTER_SCHOOL, HOMEWORK, DINNER, QUIET_TIME, BEDTIME, WEEKEND, AWAY, VACATION
}

class FamilyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FamilyDatabase.getDatabase(application)
    private val familyRepo = FamilyRepository(db.familyDao())
    private val weatherRepo = WeatherRepository()

    // 1. GLOBAL HUB STATE
    private val _currentTab = MutableStateFlow(FamilyTab.HOME)
    val currentTab: StateFlow<FamilyTab> = _currentTab

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted

    val householdProfile: StateFlow<HouseholdProfile?> = familyRepo.householdProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // 2. SECURITY & MODES
    private val _isParentMode = MutableStateFlow(false)
    val isParentMode: StateFlow<Boolean> = _isParentMode

    private val _activeMember = MutableStateFlow<FamilyMember?>(null)
    val activeMember: StateFlow<FamilyMember?> = _activeMember

    private val _houseMode = MutableStateFlow(HouseMode.HOME)
    val houseMode: StateFlow<HouseMode> = _houseMode

    private val _isAmbientMode = MutableStateFlow(false)
    val isAmbientMode: StateFlow<Boolean> = _isAmbientMode

    private var inactivityJob: Job? = null
    private var parentAutoLockJob: Job? = null
    private var failedPinAttempts = 0
    private var pinLockedUntil = 0L

    fun selectActiveMember(member: FamilyMember?) {
        _activeMember.value = member
        _isParentMode.value = false
        resetInactivityTimer()
    }

    fun enterParentMode(pin: String): Boolean {
        val now = System.currentTimeMillis()
        if (now < pinLockedUntil) return false
        val stored = householdProfile.value?.parentPin.orEmpty()
        if (ParentPin.verify(pin, stored)) {
            failedPinAttempts = 0
            _isParentMode.value = true
            _activeMember.value = null
            resetParentAutoLockTimer()
            return true
        }
        failedPinAttempts++
        if (failedPinAttempts >= 5) {
            pinLockedUntil = now + 60_000L
            failedPinAttempts = 0
        }
        return false
    }

    fun exitParentMode() {
        _isParentMode.value = false
    }

    fun toggleAmbientMode() {
        _isAmbientMode.value = !_isAmbientMode.value
    }

    fun setHouseMode(mode: HouseMode) {
        _houseMode.value = mode
    }

    private fun resetInactivityTimer() {
        inactivityJob?.cancel()
        if (_activeMember.value != null) {
            inactivityJob = viewModelScope.launch {
                delay(60000) // 1 min auto-return to Shared
                _activeMember.value = null
            }
        }
    }

    private fun resetParentAutoLockTimer() {
        parentAutoLockJob?.cancel()
        if (_isParentMode.value) {
            parentAutoLockJob = viewModelScope.launch {
                delay(300000) // 5 mins auto-lock
                _isParentMode.value = false
            }
        }
    }

    // 3. GAMIFICATION & XP
    fun completeTaskWithRewards(task: TaskItem) {
        viewModelScope.launch {
            familyRepo.completeTask(task)
            val memberId = task.assignedMemberId
            if (memberId > 0L) {
                // Award XP and potentially level up
                val member = members.value.find { it.id == memberId }
                member?.let {
                    val newXp = it.xp + (task.rewardPoints * 2)
                    val newLevel = (newXp / 500) + 1
                    familyRepo.updateMember(it.copy(xp = newXp, level = newLevel))
                }
            }
        }
    }

    // 4. DATA ACTIONS
    fun updateHouseholdProfile(profile: HouseholdProfile) {
        if (_isParentMode.value && profile.id == 1 && profile.parentPin == householdProfile.value?.parentPin) {
            viewModelScope.launch { familyRepo.updateHouseholdProfile(profile) }
        }
    }

    fun completeOnboarding(
        city: CityLocation,
        familyName: String,
        members: List<com.example.ui.screens.DraftMember>,
        priorities: List<String>,
        parentPin: String,
        emergencyContacts: List<DraftEmergencyContact>
    ) {
        if (!ParentPin.isValid(parentPin) || familyName.isBlank() || householdProfile.value != null) return
        viewModelScope.launch {
            familyRepo.updateHouseholdProfile(
                HouseholdProfile(
                    name = familyName,
                    homeLocation = city.name,
                    parentPin = ParentPin.hash(parentPin)
                )
            )
            members.forEach { m ->
                familyRepo.insertMember(
                    FamilyMember(
                        name = m.name,
                        role = m.role,
                        avatarEmoji = m.avatarEmoji,
                        birthdayMonth = m.birthdayMonth,
                        birthdayDay = m.birthdayDay,
                        birthdayYear = m.birthdayYear
                    )
                )
            }
            _isOnboardingCompleted.value = true
        }
    }

    fun reopenOnboarding() { /* Existing households must be edited in Parent Management. */ }

    // 5. DATA FLOWS
    val members: StateFlow<List<FamilyMember>> = familyRepo.members.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val events: StateFlow<List<FamilyEvent>> = familyRepo.events.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val moods: StateFlow<List<MoodCheckIn>> = familyRepo.moods.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val groceries: StateFlow<List<GroceryItem>> = familyRepo.groceries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks: StateFlow<List<TaskItem>> = familyRepo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6. WEATHER & TIME
    val weatherState: StateFlow<WeatherInfo> = weatherRepo.weatherState
    val selectedCity: StateFlow<CityLocation> = weatherRepo.selectedCity
    val availableCities = weatherRepo.availableCities
    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults

    fun searchCities(query: String) { viewModelScope.launch { _searchResults.value = weatherRepo.searchCities(query) } }
    fun selectCity(city: CityLocation) { viewModelScope.launch { weatherRepo.selectCity(city) } }
    fun detectLocation(lat: Double, lon: Double) { viewModelScope.launch { weatherRepo.selectCity(weatherRepo.detectCityFromLocation(lat, lon)) } }
    fun refreshWeather() { viewModelScope.launch { weatherRepo.fetchWeather() } }

    private val _timeString = MutableStateFlow("")
    val timeString: StateFlow<String> = _timeString
    private val _dateString = MutableStateFlow("")
    val dateString: StateFlow<String> = _dateString

    init {
        viewModelScope.launch {
            _isOnboardingCompleted.value = familyRepo.householdProfile.first() != null
        }
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                _timeString.value = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
                _dateString.value = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(cal.time)
                delay(10000)
            }
        }
        viewModelScope.launch { weatherRepo.fetchWeather() }
    }

    // 7. MODULE ACTIONS
    fun selectTab(tab: FamilyTab) { _currentTab.value = tab }
    
    fun toggleTaskCompleted(task: TaskItem) { 
        if (task.rewardPoints > 20) {
            // Require parent approval or higher XP logic here
            completeTaskWithRewards(task)
        } else {
            completeTaskWithRewards(task)
        }
    }
    
    fun recordMood(member: FamilyMember, emoji: String, label: String, note: String) {
        viewModelScope.launch { 
            familyRepo.insertMood(MoodCheckIn(memberId = member.id, memberName = member.name, moodEmoji = emoji, moodLabel = label, note = note, dateString = "Today")) 
        }
    }

    fun addFamilyMember(name: String, role: String, emoji: String, color: String, bm: Int, bd: Int, by: Int, photo: String?) {
        viewModelScope.launch { familyRepo.insertMember(FamilyMember(name = name, role = role, avatarEmoji = emoji, colorHex = color, birthdayMonth = bm, birthdayDay = bd, birthdayYear = by, photoUri = photo)) }
    }
    fun updateFamilyMember(member: FamilyMember) { viewModelScope.launch { familyRepo.updateMember(member) } }
    fun deleteFamilyMember(member: FamilyMember) { if (!_isParentMode.value) return; viewModelScope.launch { familyRepo.deleteMember(member) } }

    fun addEvent(title: String, desc: String, time: String, cat: String, attendees: String, isKids: Boolean, loc: String) {
        viewModelScope.launch { familyRepo.insertEvent(FamilyEvent(title = title, description = desc, timeString = time, category = cat, attendeeNames = attendees, isKidsActivity = isKids, location = loc, dateEpochMillis = System.currentTimeMillis())) }
    }
    fun deleteEvent(event: FamilyEvent) { if (!_isParentMode.value) return; viewModelScope.launch { familyRepo.deleteEvent(event) } }

    fun addTask(title: String, desc: String, member: FamilyMember?, pts: Int, cat: String) {
        viewModelScope.launch { familyRepo.insertTask(TaskItem(title = title, description = desc, assignedMemberId = member?.id ?: 0, assignedMemberName = member?.name ?: "Anyone", rewardPoints = pts, category = cat)) }
    }
    fun deleteTask(task: TaskItem) { if (!_isParentMode.value) return; viewModelScope.launch { familyRepo.deleteTask(task) } }

    fun addGrocery(name: String, cat: String, qty: String, addedBy: String) {
        viewModelScope.launch { familyRepo.insertGrocery(GroceryItem(name = name, category = cat, quantity = qty, addedByMemberName = addedBy)) }
    }
    fun toggleGroceryPurchased(item: GroceryItem) { viewModelScope.launch { familyRepo.updateGrocery(item.copy(isPurchased = !item.isPurchased)) } }
    fun deleteGrocery(item: GroceryItem) { viewModelScope.launch { familyRepo.deleteGrocery(item) } }
    
    // Rewards
    private val _rewardStoreItems = MutableStateFlow<List<RewardStoreItem>>(listOf(
        RewardStoreItem(1, "Movie Night 🍿", 50, "🎬", "Family choice movie with popcorn"),
        RewardStoreItem(2, "Ice Cream Trip 🍦", 30, "🍦", "Trip to the local ice cream shop"),
        RewardStoreItem(3, "Extra TV Time 📺", 20, "🎮", "30 minutes of extra screen time"),
        RewardStoreItem(4, "Pocket Money 💵", 100, "💰", "$5 weekly allowance contribution")
    ))
    val rewardStoreItems: StateFlow<List<RewardStoreItem>> = _rewardStoreItems

    fun redeemRewardStoreItem(member: FamilyMember, reward: RewardStoreItem): Boolean {
        if (!_isParentMode.value) return false
        if (member.points >= reward.costStars) {
            viewModelScope.launch { familyRepo.deductMemberPoints(member.id, reward.costStars) }
            return true
        }
        return false
    }

    // Placeholders for remaining modules
    val familyGoals = MutableStateFlow<List<FamilyGoal>>(emptyList())
    val routineItems = MutableStateFlow<List<RoutineItem>>(emptyList())
    val quickNotes = MutableStateFlow<List<QuickNote>>(emptyList())
    
    fun incrementGoalProgress(id: Long) {}
    fun toggleGoalCompleted(id: Long) {}
    fun addFamilyGoal(t: String, c: String, e: String, tr: Int) {}
    fun deleteFamilyGoal(id: Long) {}
    fun addQuickNote(t: String, a: String, u: Boolean) {}
    fun deleteQuickNote(id: Long) {}
    fun toggleNotePinned(id: Long) {}

    // Theme Logic
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode
    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }
    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.AUTO
            ThemeMode.AUTO -> ThemeMode.LIGHT
        }
    }

    // Legacy / Shared Helpers for UI compatibility
    val customEmergencyContacts = MutableStateFlow<List<DraftEmergencyContact>>(emptyList())
    val mealPlans = MutableStateFlow<List<MealPlanItem>>(emptyList())
    val greetingString = MutableStateFlow("Hello Family")
    
    fun addMealPlanItem(d: String, ty: String, t: String, e: String, i: List<String>, c: String) {}
    fun toggleMealCooked(id: Long) {}
    fun addMealIngredientsToGroceryList(i: List<String>, a: String): Int { return 0 }
    fun deleteMood(m: MoodCheckIn) { if (!_isParentMode.value) return; viewModelScope.launch { familyRepo.deleteMood(m) } }
    fun clearCompletedGroceries() { viewModelScope.launch { familyRepo.clearCompletedGroceries() } }

    fun rotateWeeklyChores() {}
    fun toggleTaskRotation(task: TaskItem) {}
}
