package com.sliide.usermanagement.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class TimeUtilTest {

    private val now get() = Clock.System.now().toEpochMilliseconds()

    @Test
    fun `0 ms ago returns just now`() {
        assertEquals("Just now", now.toRelativeTime())
    }

    @Test
    fun `30 seconds ago returns just now`() {
        val ts = now - 30_000L
        assertEquals("Just now", ts.toRelativeTime())
    }

    @Test
    fun `59 seconds ago returns just now`() {
        val ts = now - 59_999L
        assertEquals("Just now", ts.toRelativeTime())
    }

    @Test
    fun `1 minute ago returns 1 minute ago`() {
        val ts = now - 60_000L
        assertEquals("1 minute ago", ts.toRelativeTime())
    }

    @Test
    fun `5 minutes ago returns 5 minutes ago`() {
        val ts = now - 5 * 60_000L
        assertEquals("5 minutes ago", ts.toRelativeTime())
    }

    @Test
    fun `59 minutes ago returns 59 minutes ago`() {
        val ts = now - 59 * 60_000L
        assertEquals("59 minutes ago", ts.toRelativeTime())
    }

    @Test
    fun `1 hour ago returns 1 hour ago`() {
        val ts = now - 3_600_000L
        assertEquals("1 hour ago", ts.toRelativeTime())
    }

    @Test
    fun `3 hours ago returns 3 hours ago`() {
        val ts = now - 3 * 3_600_000L
        assertEquals("3 hours ago", ts.toRelativeTime())
    }

    @Test
    fun `23 hours ago returns 23 hours ago`() {
        val ts = now - 23 * 3_600_000L
        assertEquals("23 hours ago", ts.toRelativeTime())
    }

    @Test
    fun `1 day ago returns 1 day ago`() {
        val ts = now - 86_400_000L
        assertEquals("1 day ago", ts.toRelativeTime())
    }

    @Test
    fun `7 days ago returns 7 days ago`() {
        val ts = now - 7 * 86_400_000L
        assertEquals("7 days ago", ts.toRelativeTime())
    }
}
