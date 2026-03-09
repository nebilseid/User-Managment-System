package com.sliide.usermanagement

import platform.Foundation.NSBundle

// iOS: add SUPABASE_KEY as a String entry in Info.plist, reading from an .xcconfig that
// is excluded from version control (mirrors the Android local.properties approach).
actual val supabaseApiKey: String
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("SUPABASE_KEY") as? String ?: ""

// iOS release builds set DEBUG=0 via the default Xcode configuration.
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean get() = Platform.isDebugBinary
