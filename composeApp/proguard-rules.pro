# Keep source info for crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature

# kotlinx.serialization
-keepattributes InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sliide.usermanagement.**$$serializer { *; }
-keepclassmembers class com.sliide.usermanagement.** { *** Companion; }
-keepclasseswithmembers class com.sliide.usermanagement.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Domain models and DTOs (serialized / reflected)
-keep class com.sliide.usermanagement.data.remote.dto.** { *; }
-keep class com.sliide.usermanagement.domain.model.** { *; }

# Ktor
-keep class io.ktor.** { *; }

# Koin
-keep class org.koin.** { *; }
-keepnames class * extends org.koin.core.module.Module

# Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# SQLDelight generated code
-keep class com.sliide.usermanagement.data.local.db.** { *; }
