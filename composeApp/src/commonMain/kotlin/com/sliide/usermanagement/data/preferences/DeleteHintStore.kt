package com.sliide.usermanagement.data.preferences

interface DeleteHintStore {
    fun hasSeenDeleteHint(): Boolean
    fun markDeleteHintSeen()
}
