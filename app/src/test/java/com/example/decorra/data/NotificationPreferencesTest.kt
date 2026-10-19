package com.example.decorra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPreferencesTest {

    private fun notification(type: String) =
        AppNotification(id = "n1", timeMillis = 0L, type = type)

    @Test
    fun typesMapToTheirCategories() {
        assertEquals(NotificationCategory.QUOTE_UPDATES, notification(NotificationType.APPROVED).category())
        assertEquals(NotificationCategory.QUOTE_UPDATES, notification(NotificationType.DECLINED).category())
        assertEquals(NotificationCategory.PAYMENTS, notification(NotificationType.DEPOSIT).category())
        assertEquals(NotificationCategory.NEW_REQUESTS, notification(NotificationType.NEW_REQUEST).category())
        assertEquals(
            NotificationCategory.CLIENT_RESPONSES,
            notification(NotificationType.CLIENT_APPROVED).category()
        )
    }

    @Test
    fun untypedNotificationsAlwaysShow() {
        assertNull(notification("").category())
        assertTrue(emptyMap<String, Set<String>>().allows("a@b.com", null))
    }

    @Test
    fun everythingIsOnByDefault() {
        val prefs = emptyMap<String, Set<String>>()
        NotificationCategory.entries.forEach {
            assertTrue(prefs.allows("a@b.com", it))
        }
    }

    @Test
    fun aSwitchedOffCategoryIsHiddenForThatPersonOnly() {
        val prefs = mapOf("a@b.com" to setOf("payments"))
        assertFalse(prefs.allows("a@b.com", NotificationCategory.PAYMENTS))
        assertTrue(prefs.allows("a@b.com", NotificationCategory.QUOTE_UPDATES))
        assertTrue(prefs.allows("b@b.com", NotificationCategory.PAYMENTS))
    }

    @Test
    fun emailMatchingIgnoresCase() {
        val prefs = mapOf("a@b.com" to setOf("countdown"))
        assertFalse(prefs.allows("A@B.com", NotificationCategory.COUNTDOWN))
    }
}