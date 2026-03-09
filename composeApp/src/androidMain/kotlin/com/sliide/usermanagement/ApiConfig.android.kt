package com.sliide.usermanagement

actual val supabaseApiKey: String get() = BuildConfig.API_KEY

actual val isDebugBuild: Boolean get() = BuildConfig.DEBUG
