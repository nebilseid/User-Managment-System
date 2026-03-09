package com.sliide.usermanagement

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean get() = Platform.isDebugBinary
