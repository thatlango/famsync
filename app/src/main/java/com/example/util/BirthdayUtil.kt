package com.example.util

import com.example.data.entities.FamilyMember
import java.util.Calendar

object BirthdayUtil {
    fun getDaysUntilBirthday(member: FamilyMember, currentDate: Calendar = Calendar.getInstance()): Int? {
        if (member.birthdayMonth !in 1..12 || member.birthdayDay !in 1..31) return null

        val currentYear = currentDate.get(Calendar.YEAR)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetBirthday = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, member.birthdayMonth - 1)
            set(Calendar.DAY_OF_MONTH, member.birthdayDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (targetBirthday.before(todayStart)) {
            targetBirthday.set(Calendar.YEAR, currentYear + 1)
        }

        val diffMs = targetBirthday.timeInMillis - todayStart.timeInMillis
        return (diffMs / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getMonthName(month: Int): String {
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return if (month in 1..12) months[month - 1] else ""
    }
}
