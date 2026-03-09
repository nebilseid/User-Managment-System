package com.sliide.usermanagement.di

import android.content.Context
import com.sliide.usermanagement.data.local.DatabaseDriverFactory
import com.sliide.usermanagement.data.preferences.AppPreferences
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single { DatabaseDriverFactory(context) }
    single { AppPreferences(context) }
}
