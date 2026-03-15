package com.sliide.usermanagement.ui.strings

internal object AppStrings {
    // Screen titles
    const val USERS_TITLE = "Users"

    // FAB
    const val FAB_ADD_USER = "Add User"
    const val FAB_ADD_USER_CD = "Add user"

    // Hints
    const val HINT_LONG_PRESS_DELETE = "Long-press to delete"

    // Empty state
    const val EMPTY_TITLE = "No users yet"
    const val EMPTY_SUBTITLE = "Tap + to add your first user"

    // Tablet — no selection
    const val DETAIL_PLACEHOLDER = "Select a user to view details"

    // Error block titles
    const val ERROR_NO_INTERNET_TITLE = "No Internet Connection"
    const val ERROR_SERVER_TITLE = "Server Error"

    // Error messages
    const val ERROR_NO_INTERNET_MSG = "Check your connection and try again."
    const val ERROR_NO_INTERNET_SNACKBAR = "No internet connection"
    const val ERROR_SERVER_SNACKBAR = "Couldn't refresh — server error"

    // Retry button
    const val BTN_TRY_AGAIN = "TRY AGAIN"

    // Snackbar
    const val SNACKBAR_UNDO = "Undo"
    fun deletedMessage(name: String) = "$name deleted"

    // Detail panel labels
    const val LABEL_EMAIL = "Email"
    const val LABEL_GENDER = "Gender"
    const val LABEL_STATUS = "Status"
    const val LABEL_MEMBER_SINCE = "Member Since"

    // Navigation
    const val CD_BACK = "Back"

    // Add user dialog
    const val DIALOG_ADD_TITLE = "New User"
    const val FIELD_FULL_NAME = "Full Name"
    const val FIELD_EMAIL = "Email"
    const val LABEL_GENDER_SECTION = "Gender"
    const val LABEL_STATUS_SECTION = "Status"
    const val BTN_CREATE_USER = "CREATE USER"
    const val BTN_CANCEL = "Cancel"
    const val ERROR_INVALID_NAME = "Enter a valid full name"
    const val ERROR_INVALID_EMAIL = "Enter a valid email address"
    const val ERROR_ADD_NO_INTERNET = "No internet connection. Check your network and try again."
    const val ERROR_ADD_DUPLICATE_EMAIL = "This email is already registered. Please use a different one."
    const val ERROR_ADD_SERVER = "Server error. Please try again later."
    const val ERROR_ADD_UNKNOWN = "Something went wrong. Please try again."
    const val ERROR_GENERIC_FALLBACK = "Invalid input"

    // Animation labels
    const val ANIM_CHIP_GROUP_BORDER = "chipGroupBorder"

    // Delete confirm dialog
    const val DIALOG_DELETE_TITLE = "Delete User"
    const val BTN_DELETE = "DELETE"
    fun deleteWarning(name: String) =
        "This will permanently remove $name from the system. This action cannot be undone."
}
