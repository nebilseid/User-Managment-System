package com.sliide.usermanagement.data.preferences

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

actual class AppPreferences(context: Context) : DeleteHintStore {
    private val settings = SharedPreferencesSettings(
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    )

    actual override fun hasSeenDeleteHint(): Boolean = settings.getBoolean(KEY_DELETE_HINT, false)
    actual override fun markDeleteHintSeen() { settings.putBoolean(KEY_DELETE_HINT, true) }

    companion object {
        private const val KEY_DELETE_HINT = "delete_hint_seen"
    }
}
