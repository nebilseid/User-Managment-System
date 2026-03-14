package com.sliide.usermanagement.ui.util

import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Long.toMemberSince(): String? {
    if (this <= 0L) return null
    val date = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$month ${date.year}"
}

fun Long.toRelativeTime(): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - this
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> { val m = diff / 60_000L; if (m == 1L) "1 minute ago" else "$m minutes ago" }
        diff < 86_400_000L -> { val h = diff / 3_600_000L; if (h == 1L) "1 hour ago" else "$h hours ago" }
        else -> { val d = diff / 86_400_000L; if (d == 1L) "1 day ago" else "$d days ago" }
    }
}
