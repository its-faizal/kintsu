package com.kintsu.app.util

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object KintsuDateFormatter {
    fun formatHeaderDate(date: LocalDate = LocalDate.now(), locale: Locale = Locale.getDefault()): String {
        val pattern = try {
            DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd")
        } catch (_: Exception) {
            "EEEE, MMMM d"
        }
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }
}
