package com.sliide.usermanagement

import androidx.compose.runtime.Composable
import com.sliide.usermanagement.ui.screen.UsersScreen
import com.sliide.usermanagement.ui.theme.SliideTheme

@Composable
fun App() {
    SliideTheme {
        UsersScreen()
    }
}
