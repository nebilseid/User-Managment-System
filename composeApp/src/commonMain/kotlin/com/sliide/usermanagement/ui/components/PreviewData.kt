package com.sliide.usermanagement.ui.components

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import com.sliide.usermanagement.domain.model.User

internal val previewUser = User(
    id = 1L,
    name = "Jane Smith",
    email = "jane.smith@verylongemailprovider.example.com",
    gender = "female",
    status = "active",
    createdAt = 1_705_312_800_000L // 2024-01-15
)

internal val previewUserInactive = User(
    id = 2L,
    name = "Bob Johnson",
    email = "bob.johnson@example.com",
    gender = "male",
    status = "inactive",
    createdAt = 1_690_000_000_000L // 2023-07-22
)

internal fun fakeSnackbarData(message: String, actionLabel: String? = null): SnackbarData =
    object : SnackbarData {
        override val visuals = object : SnackbarVisuals {
            override val message = message
            override val actionLabel = actionLabel
            override val withDismissAction = false
            override val duration = SnackbarDuration.Short
        }
        override fun dismiss() {}
        override fun performAction() {}
    }
