package com.insurancebazaar.app.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val DAY_MS = 24L * 60 * 60 * 1000
private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** Today's calendar date as UTC-midnight millis (matches what the Material date picker returns). */
fun startOfToday(): Long {
    val local = Calendar.getInstance()
    val utc = Calendar.getInstance(UTC)
    utc.clear()
    utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    return utc.timeInMillis
}

/** [start, end) of the current month in UTC-midnight millis. */
fun currentMonthRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance(UTC)
    cal.timeInMillis = startOfToday()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    return start to cal.timeInMillis
}

enum class PolicyStatus(val label: String) {
    ACTIVE("Active"), EXPIRING("Expiring soon"), EXPIRED("Expired")
}

fun Policy.status(now: Long = startOfToday()): PolicyStatus = when {
    expiryDate < now -> PolicyStatus.EXPIRED
    expiryDate < now + 30 * DAY_MS -> PolicyStatus.EXPIRING
    else -> PolicyStatus.ACTIVE
}

object Fmt {
    fun date(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply { timeZone = UTC }.format(Date(millis))

    fun money(value: Double): String = "₹" + String.format(Locale.getDefault(), "%,.0f", value)

    fun month(yearMonth: String): String = try {
        val parsed = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth)!!
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(parsed)
    } catch (e: Exception) {
        yearMonth
    }
}
