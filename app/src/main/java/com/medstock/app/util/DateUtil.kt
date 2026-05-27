package com.medstock.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtil {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val displayFormatTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    fun format(millis: Long): String =
        if (millis <= 0L) "—" else displayFormat.format(Date(millis))

    fun formatWithTime(millis: Long): String =
        if (millis <= 0L) "—" else displayFormatTime.format(Date(millis))

    fun monthYear(millis: Long): String =
        if (millis <= 0L) "—" else monthYearFormat.format(Date(millis))

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun endOfDay(millis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        return c.timeInMillis
    }

    fun daysFromNow(days: Int): Long =
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days.toLong())

    /** Negative if expired; positive if remaining. */
    fun daysBetween(from: Long, to: Long): Long =
        TimeUnit.MILLISECONDS.toDays(to - from)
}
