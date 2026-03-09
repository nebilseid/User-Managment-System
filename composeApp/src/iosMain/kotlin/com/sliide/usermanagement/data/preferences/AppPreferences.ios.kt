package com.sliide.usermanagement.data.preferences

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

actual class AppPreferences : DeleteHintStore {
    private val settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)

    actual override fun hasSeenDeleteHint(): Boolean = settings.getBoolean(KEY_DELETE_HINT, false)
    actual override fun markDeleteHintSeen() { settings.putBoolean(KEY_DELETE_HINT, true) }

    companion object {
        private const val KEY_DELETE_HINT = "delete_hint_seen"
    }
}
