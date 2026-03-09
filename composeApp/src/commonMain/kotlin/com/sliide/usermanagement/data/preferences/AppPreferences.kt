package com.sliide.usermanagement.data.preferences

expect class AppPreferences : DeleteHintStore {
    override fun hasSeenDeleteHint(): Boolean
    override fun markDeleteHintSeen()
}
