package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.components.ExpressiveMoodItem
import java.text.SimpleDateFormat
import java.util.*

enum class TimeOfDayPeriod(
    val title: String,
    val icon: String,
    val greeting: String,
    val accentColor: Color,
    val bgGradientColor: Color
) {
    MORNING("Morning", "🌅", "Good Morning", Color(0xFFD97706), Color(0xFFFEF3C7)),
    AFTERNOON("Afternoon", "☀️", "Good Afternoon", Color(0xFF0284C7), Color(0xFFE0F2FE)),
    EVENING("Evening", "🌆", "Good Evening", Color(0xFFC026D3), Color(0xFFFAE8FF)),
    NIGHT("Late Night", "🌙", "Good Night", Color(0xFF4338CA), Color(0xFFE0E7FF))
}

data class LocalHoliday(
    val id: String,
    val name: String,
    val emoji: String,
    val dateString: String,
    val month: Int, // Calendar.JANUARY etc (0-based)
    val day: Int,
    val country: String,
    val category: String,
    val description: String,
    val suggestedMoods: List<Triple<String, String, Color>> // Emoji, Label, ChipBg
)

data class MoodContextResult(
    val timeOfDayPeriod: TimeOfDayPeriod,
    val activeHolidayName: String?,
    val holidayEmoji: String?,
    val bannerTitle: String,
    val bannerSubtitle: String,
    val accentColor: Color,
    val containerBgColor: Color,
    val contextualMoods: List<ExpressiveMoodItem>
)

object HolidayAndSeasonalContext {

    // Calculate Easter Sunday using Anonymous Gregorian Algorithm (Meeus/Jones/Butcher)
    fun getEasterSundayDate(year: Int): Calendar {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31 // 3 = March, 4 = April
        val day = ((h + l - 7 * m + 114) % 31) + 1

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun getTimeOfDayPeriod(calendar: Calendar = Calendar.getInstance()): TimeOfDayPeriod {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> TimeOfDayPeriod.MORNING
            in 12..16 -> TimeOfDayPeriod.AFTERNOON
            in 17..21 -> TimeOfDayPeriod.EVENING
            else -> TimeOfDayPeriod.NIGHT
        }
    }

    // List of standard local/area holidays for US, UK, Canada, Australia, etc.
    fun getLocalHolidaysForYear(year: Int, country: String = "United States"): List<LocalHoliday> {
        val list = mutableListOf<LocalHoliday>()

        // Fixed Global Holidays
        list.add(
            LocalHoliday(
                id = "new_year_$year",
                name = "New Year's Day",
                emoji = "🎆",
                dateString = "Jan 1, $year",
                month = Calendar.JANUARY,
                day = 1,
                country = "Global",
                category = "New Year",
                description = "Fresh start of the year with resolutions and celebrations!",
                suggestedMoods = listOf(
                    Triple("🎆", "Hopeful", Color(0xFFE0E7FF)),
                    Triple("🥳", "Celebratory", Color(0xFFFCE7F3)),
                    Triple("✨", "Inspired", Color(0xFFFEF3C7)),
                    Triple("🍾", "Excited", Color(0xFFDCFCE7))
                )
            )
        )

        list.add(
            LocalHoliday(
                id = "valentines_$year",
                name = "Valentine's Day",
                emoji = "💖",
                dateString = "Feb 14, $year",
                month = Calendar.FEBRUARY,
                day = 14,
                country = "Global",
                category = "Love & Family",
                description = "Day of love, affection, and family appreciation.",
                suggestedMoods = listOf(
                    Triple("💖", "Loving", Color(0xFFFFE4E6)),
                    Triple("🥰", "Appreciative", Color(0xFFFCE7F3)),
                    Triple("🌹", "Romantic", Color(0xFFFEE2E2)),
                    Triple("💕", "Warm", Color(0xFFFFF1F2))
                )
            )
        )

        list.add(
            LocalHoliday(
                id = "st_patricks_$year",
                name = "St. Patrick's Day",
                emoji = "☘️",
                dateString = "Mar 17, $year",
                month = Calendar.MARCH,
                day = 17,
                country = "Global",
                category = "Cultural",
                description = "Feast of Saint Patrick with green celebrations and good fortune!",
                suggestedMoods = listOf(
                    Triple("☘️", "Lucky", Color(0xFFDCFCE7)),
                    Triple("🟢", "Festive", Color(0xFFD1FAE5)),
                    Triple("🎩", "Playful", Color(0xFFECFDF5)),
                    Triple("🍻", "Cheerful", Color(0xFFFEF3C7))
                )
            )
        )

        // Easter Sunday Calculation
        val easterCal = getEasterSundayDate(year)
        val easterMonth = easterCal.get(Calendar.MONTH)
        val easterDay = easterCal.get(Calendar.DAY_OF_MONTH)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        list.add(
            LocalHoliday(
                id = "easter_$year",
                name = "Easter Sunday",
                emoji = "🐣",
                dateString = dateFormat.format(easterCal.time),
                month = easterMonth,
                day = easterDay,
                country = "Global",
                category = "Spring Holiday",
                description = "Joyful spring celebration with family egg hunts and renewal.",
                suggestedMoods = listOf(
                    Triple("🐣", "Joyful", Color(0xFFFEF3C7)),
                    Triple("🐰", "Egg-cited", Color(0xFFFCE7F3)),
                    Triple("🌸", "Renewed", Color(0xFFE0F2FE)),
                    Triple("🥚", "Blessed", Color(0xFFDCFCE7))
                )
            )
        )

        // Mother's Day (2nd Sunday in May for US/CA/AU)
        val mothersDayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.MAY)
            set(Calendar.DAY_OF_MONTH, 1)
            var count = 0
            while (count < 2) {
                if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) count++
                if (count < 2) add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        list.add(
            LocalHoliday(
                id = "mothers_day_$year",
                name = "Mother's Day",
                emoji = "💐",
                dateString = dateFormat.format(mothersDayCal.time),
                month = Calendar.MAY,
                day = mothersDayCal.get(Calendar.DAY_OF_MONTH),
                country = "Global",
                category = "Family",
                description = "Honoring mothers, grandmothers, and family caregivers.",
                suggestedMoods = listOf(
                    Triple("💐", "Grateful", Color(0xFFFCE7F3)),
                    Triple("💖", "Loving", Color(0xFFFFE4E6)),
                    Triple("👑", "Cherished", Color(0xFFFEF3C7)),
                    Triple("🌹", "Warm", Color(0xFFFEE2E2))
                )
            )
        )

        // Father's Day (3rd Sunday in June)
        val fathersDayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 1)
            var count = 0
            while (count < 3) {
                if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) count++
                if (count < 3) add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        list.add(
            LocalHoliday(
                id = "fathers_day_$year",
                name = "Father's Day",
                emoji = "👔",
                dateString = dateFormat.format(fathersDayCal.time),
                month = Calendar.JUNE,
                day = fathersDayCal.get(Calendar.DAY_OF_MONTH),
                country = "Global",
                category = "Family",
                description = "Honoring fathers and paternal figures in the family.",
                suggestedMoods = listOf(
                    Triple("👔", "Proud", Color(0xFFE0F2FE)),
                    Triple("💙", "Grateful", Color(0xFFDBEAFE)),
                    Triple("💪", "Strong", Color(0xFFFEF3C7)),
                    Triple("🛠️", "Appreciated", Color(0xFFF1F5F9))
                )
            )
        )

        // Country Specific Holidays
        when (country) {
            "United States" -> {
                list.add(
                    LocalHoliday(
                        id = "us_independence_$year",
                        name = "Independence Day (4th of July)",
                        emoji = "🎆",
                        dateString = "Jul 4, $year",
                        month = Calendar.JULY,
                        day = 4,
                        country = "United States",
                        category = "National Holiday",
                        description = "American Independence Day with fireworks, BBQs, and family picnics.",
                        suggestedMoods = listOf(
                            Triple("🎆", "Patriotic", Color(0xFFDBEAFE)),
                            Triple("🍔", "Festive", Color(0xFFFEE2E2)),
                            Triple("🗽", "Proud", Color(0xFFE0E7FF)),
                            Triple("🍦", "Carefree", Color(0xFFFEF3C7))
                        )
                    )
                )
                list.add(
                    LocalHoliday(
                        id = "us_thanksgiving_$year",
                        name = "Thanksgiving Day",
                        emoji = "🦃",
                        dateString = "Nov 26, $year", // Approx 4th Thursday
                        month = Calendar.NOVEMBER,
                        day = 26,
                        country = "United States",
                        category = "National Holiday",
                        description = "A time to give thanks, gather with family, and enjoy a feast.",
                        suggestedMoods = listOf(
                            Triple("🦃", "Thankful", Color(0xFFFEF3C7)),
                            Triple("🍂", "Grateful", Color(0xFFFFEDD5)),
                            Triple("🥧", "Content", Color(0xFFFEE2E2)),
                            Triple("🛋️", "Cozy", Color(0xFFF1F5F9))
                        )
                    )
                )
            }
            "United Kingdom" -> {
                list.add(
                    LocalHoliday(
                        id = "uk_summer_bank_$year",
                        name = "Summer Bank Holiday",
                        emoji = "🇬🇧",
                        dateString = "Aug 31, $year",
                        month = Calendar.AUGUST,
                        day = 31,
                        country = "United Kingdom",
                        category = "Bank Holiday",
                        description = "UK national bank holiday to relax and enjoy the end of summer.",
                        suggestedMoods = listOf(
                            Triple("🏖️", "Relaxed", Color(0xFFE0F2FE)),
                            Triple("🍵", "Cozy", Color(0xFFFEF3C7)),
                            Triple("☀️", "Upbeat", Color(0xFFDCFCE7)),
                            Triple("🍦", "Joyful", Color(0xFFFCE7F3))
                        )
                    )
                )
            }
            "Canada" -> {
                list.add(
                    LocalHoliday(
                        id = "canada_day_$year",
                        name = "Canada Day",
                        emoji = "🍁",
                        dateString = "Jul 1, $year",
                        month = Calendar.JULY,
                        day = 1,
                        country = "Canada",
                        category = "National Holiday",
                        description = "Celebrating the anniversary of Confederation with parades and festivities.",
                        suggestedMoods = listOf(
                            Triple("🍁", "Proud", Color(0xFFFEE2E2)),
                            Triple("🥳", "Joyful", Color(0xFFFFE4E6)),
                            Triple("🏕️", "Adventurous", Color(0xFFDCFCE7)),
                            Triple("🎆", "Festive", Color(0xFFE0E7FF))
                        )
                    )
                )
            }
            "Australia" -> {
                list.add(
                    LocalHoliday(
                        id = "australia_day_$year",
                        name = "Australia Day",
                        emoji = "🦘",
                        dateString = "Jan 26, $year",
                        month = Calendar.JANUARY,
                        day = 26,
                        country = "Australia",
                        category = "National Holiday",
                        description = "National day celebrating Australian culture, outdoor BBQs, and beach fun.",
                        suggestedMoods = listOf(
                            Triple("🦘", "Sunny", Color(0xFFFEF3C7)),
                            Triple("🏖️", "Relaxed", Color(0xFFE0F2FE)),
                            Triple("🥩", "Festive", Color(0xFFFFEDD5)),
                            Triple("🌊", "Upbeat", Color(0xFFDBEAFE))
                        )
                    )
                )
            }
        }

        // Halloween
        list.add(
            LocalHoliday(
                id = "halloween_$year",
                name = "Halloween",
                emoji = "🎃",
                dateString = "Oct 31, $year",
                month = Calendar.OCTOBER,
                day = 31,
                country = "Global",
                category = "Seasonal",
                description = "Costumes, trick-or-treating, spooky fun, and sweet treats!",
                suggestedMoods = listOf(
                    Triple("🎃", "Spooky", Color(0xFFFFEDD5)),
                    Triple("👻", "Playful", Color(0xFFF3E8FF)),
                    Triple("🧙", "Thrilled", Color(0xFFE0E7FF)),
                    Triple("🍬", "Excited", Color(0xFFFCE7F3))
                )
            )
        )

        // Christmas Eve & Day
        list.add(
            LocalHoliday(
                id = "christmas_$year",
                name = "Christmas Day",
                emoji = "🎄",
                dateString = "Dec 25, $year",
                month = Calendar.DECEMBER,
                day = 25,
                country = "Global",
                category = "Winter Holiday",
                description = "Joyous holiday of giving, family feasts, decorations, and cheer.",
                suggestedMoods = listOf(
                    Triple("🎄", "Festive", Color(0xFFDCFCE7)),
                    Triple("🎅", "Merry", Color(0xFFFEE2E2)),
                    Triple("🎁", "Generous", Color(0xFFFEF3C7)),
                    Triple("❄️", "Cozy", Color(0xFFE0F2FE))
                )
            )
        )

        return list.sortedWith(compareBy({ it.month }, { it.day }))
    }

    fun getCurrentContext(
        calendar: Calendar = Calendar.getInstance(),
        selectedCountry: String = "United States"
    ): MoodContextResult {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val timePeriod = getTimeOfDayPeriod(calendar)
        val holidays = getLocalHolidaysForYear(year, selectedCountry)

        // Check if today matches a holiday or is in a holiday period window (e.g. Easter week, Dec 15-31 Christmas, Oct 20-31 Halloween)
        val easterCal = getEasterSundayDate(year)
        val easterStartMillis = easterCal.timeInMillis - (3 * 24 * 60 * 60 * 1000L) // Good Friday window
        val easterEndMillis = easterCal.timeInMillis + (2 * 24 * 60 * 60 * 1000L)
        val currentMillis = calendar.timeInMillis

        var activeHolidayName: String? = null
        var holidayEmoji: String? = null
        var accentColor = timePeriod.accentColor
        var containerBgColor = timePeriod.bgGradientColor
        val moodList = mutableListOf<ExpressiveMoodItem>()

        val todayHoliday = holidays.firstOrNull { it.month == month && it.day == day }

        if (todayHoliday != null) {
            activeHolidayName = todayHoliday.name
            holidayEmoji = todayHoliday.emoji
            todayHoliday.suggestedMoods.forEach { (emoji, label, color) ->
                moodList.add(ExpressiveMoodItem(emoji, label, color, Color(0xFF1E293B)))
            }
        } else if (month == Calendar.DECEMBER && day >= 15) {
            activeHolidayName = "Christmas Season"
            holidayEmoji = "🎄"
            accentColor = Color(0xFF15803D)
            containerBgColor = Color(0xFFDCFCE7)
            moodList.addAll(
                listOf(
                    ExpressiveMoodItem("🎄", "Festive", Color(0xFFDCFCE7), Color(0xFF14532D)),
                    ExpressiveMoodItem("🎅", "Merry", Color(0xFFFEE2E2), Color(0xFF991B1B)),
                    ExpressiveMoodItem("🎁", "Generous", Color(0xFFFEF3C7), Color(0xFF78350F)),
                    ExpressiveMoodItem("❄️", "Cozy", Color(0xFFE0F2FE), Color(0xFF075985))
                )
            )
        } else if (month == Calendar.OCTOBER && day >= 20) {
            activeHolidayName = "Spooky Halloween Season"
            holidayEmoji = "🎃"
            accentColor = Color(0xFFC2410C)
            containerBgColor = Color(0xFFFFEDD5)
            moodList.addAll(
                listOf(
                    ExpressiveMoodItem("🎃", "Spooky", Color(0xFFFFEDD5), Color(0xFF9A3412)),
                    ExpressiveMoodItem("👻", "Playful", Color(0xFFF3E8FF), Color(0xFF6B21A8)),
                    ExpressiveMoodItem("🧙", "Thrilled", Color(0xFFE0E7FF), Color(0xFF3730A3)),
                    ExpressiveMoodItem("🍬", "Excited", Color(0xFFFCE7F3), Color(0xFF9D174D))
                )
            )
        } else if (currentMillis in easterStartMillis..easterEndMillis) {
            activeHolidayName = "Easter Season"
            holidayEmoji = "🐣"
            accentColor = Color(0xFFD97706)
            containerBgColor = Color(0xFFFEF3C7)
            moodList.addAll(
                listOf(
                    ExpressiveMoodItem("🐣", "Joyful", Color(0xFFFEF3C7), Color(0xFF78350F)),
                    ExpressiveMoodItem("🐰", "Egg-cited", Color(0xFFFCE7F3), Color(0xFF9D174D)),
                    ExpressiveMoodItem("🌸", "Renewed", Color(0xFFE0F2FE), Color(0xFF0369A1)),
                    ExpressiveMoodItem("🥚", "Blessed", Color(0xFFDCFCE7), Color(0xFF15803D))
                )
            )
        }

        // Fill remaining with time-of-day contextual suggestions
        when (timePeriod) {
            TimeOfDayPeriod.MORNING -> {
                if (moodList.size < 4) {
                    moodList.add(ExpressiveMoodItem("🌅", "Energized", Color(0xFFFEF3C7), Color(0xFFB45309)))
                    moodList.add(ExpressiveMoodItem("☕", "Focused", Color(0xFFE0E7FF), Color(0xFF3730A3)))
                    moodList.add(ExpressiveMoodItem("🕊️", "Peaceful", Color(0xFFDCFCE7), Color(0xFF15803D)))
                    moodList.add(ExpressiveMoodItem("😴", "Sleepy", Color(0xFFEDE9FE), Color(0xFF5B21B6)))
                }
            }
            TimeOfDayPeriod.AFTERNOON -> {
                if (moodList.size < 4) {
                    moodList.add(ExpressiveMoodItem("☀️", "Productive", Color(0xFFE0F2FE), Color(0xFF0369A1)))
                    moodList.add(ExpressiveMoodItem("🎨", "Creative", Color(0xFFECFDF5), Color(0xFF047857)))
                    moodList.add(ExpressiveMoodItem("😃", "Upbeat", Color(0xFFFEF3C7), Color(0xFFB45309)))
                    moodList.add(ExpressiveMoodItem("🍕", "Hungry", Color(0xFFFFEDD5), Color(0xFFC2410C)))
                }
            }
            TimeOfDayPeriod.EVENING -> {
                if (moodList.size < 4) {
                    moodList.add(ExpressiveMoodItem("🌆", "Relaxed", Color(0xFFFAE8FF), Color(0xFF86198F)))
                    moodList.add(ExpressiveMoodItem("🍷", "Cozy", Color(0xFFFCE7F3), Color(0xFF9D174D)))
                    moodList.add(ExpressiveMoodItem("💬", "Chatty", Color(0xFFFEF3C7), Color(0xFFB45309)))
                    moodList.add(ExpressiveMoodItem("🛋️", "Unwinding", Color(0xFFE0F2FE), Color(0xFF0369A1)))
                }
            }
            TimeOfDayPeriod.NIGHT -> {
                if (moodList.size < 4) {
                    moodList.add(ExpressiveMoodItem("🌙", "Restful", Color(0xFFE0E7FF), Color(0xFF3730A3)))
                    moodList.add(ExpressiveMoodItem("😴", "Sleepy", Color(0xFFEDE9FE), Color(0xFF5B21B6)))
                    moodList.add(ExpressiveMoodItem("🌌", "Reflective", Color(0xFFF1F5F9), Color(0xFF334155)))
                    moodList.add(ExpressiveMoodItem("🕯️", "Quiet", Color(0xFFFEF3C7), Color(0xFF78350F)))
                }
            }
        }

        val bannerTitle = if (activeHolidayName != null) {
            "$holidayEmoji $activeHolidayName Mood Check-In"
        } else {
            "${timePeriod.icon} ${timePeriod.greeting} Check-In"
        }

        val bannerSubtitle = if (activeHolidayName != null) {
            "Special holiday moods & time of day feelings (${timePeriod.title})"
        } else {
            "Time of day: ${timePeriod.title} (${SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)})"
        }

        return MoodContextResult(
            timeOfDayPeriod = timePeriod,
            activeHolidayName = activeHolidayName,
            holidayEmoji = holidayEmoji,
            bannerTitle = bannerTitle,
            bannerSubtitle = bannerSubtitle,
            accentColor = accentColor,
            containerBgColor = containerBgColor,
            contextualMoods = moodList.take(6)
        )
    }
}
