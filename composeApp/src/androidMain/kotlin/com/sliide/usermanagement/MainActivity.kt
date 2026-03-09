package com.sliide.usermanagement

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sliide.usermanagement.di.appModule
import com.sliide.usermanagement.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SliideApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SliideApp)
            modules(appModule, platformModule(this@SliideApp))
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ shows an automatic system splash — remove it immediately
        // so only our custom Compose splash is visible.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { it.remove() }
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}