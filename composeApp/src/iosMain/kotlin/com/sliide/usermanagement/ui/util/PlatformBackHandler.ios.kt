package com.sliide.usermanagement.ui.util

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no hardware back button; navigation is handled via the TopAppBar back arrow
}
