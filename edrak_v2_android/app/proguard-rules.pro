# Proguard rules for Edrak V2

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Room entities
-keep class me.edrakai.core.database.entity.** { *; }

# Keep Retrofit models
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class me.edrakai.**$$serializer { *; }
-keepclassmembers class me.edrakai.** {
    *** Companion;
}
-keepclasseswithmembers class me.edrakai.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep WorkManager workers
-keep class me.edrakai.core.** extends androidx.work.Worker { *; }
-keep class me.edrakai.core.** extends androidx.work.CoroutineWorker { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Timber
-dontwarn org.jetbrains.annotations.**
