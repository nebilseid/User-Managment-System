package com.sliide.usermanagement

import androidx.compose.ui.window.ComposeUIViewController
import com.sliide.usermanagement.di.appModule
import com.sliide.usermanagement.di.platformModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    App()
}

private var koinStarted = false

fun initKoin() {
    if (koinStarted) return
    koinStarted = true
    try {
        startKoin {
            modules(appModule, platformModule)
        }
    } catch (e: Throwable) {
        println("Koin init FAILED: ${e::class.simpleName}: ${e.message}")
        e.printStackTrace()
        throw e
    }
}