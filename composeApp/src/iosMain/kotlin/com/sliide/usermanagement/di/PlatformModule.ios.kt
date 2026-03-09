package com.sliide.usermanagement.di

import com.sliide.usermanagement.data.local.DatabaseDriverFactory
import com.sliide.usermanagement.data.preferences.AppPreferences
import org.koin.dsl.module

val platformModule = module {
    single { DatabaseDriverFactory() }
    single { AppPreferences() }
}
