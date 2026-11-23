# ProGuard rules for Lark Player

# Keep all model classes for serialization
-keep class com.larkplayer.app.data.model.** { *; }

# Keep Room database classes
-keep class com.larkplayer.app.data.source.local.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }
-keep class androidx.media3.** { *; }

# Keep Coil classes
-keep class coil.** { *; }
-keep class io.coil-kt.** { *; }

# Keep Lottie classes
-keep class com.airbnb.lottie.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep serialization
-keep class kotlinx.serialization.** { *; }

# Keep navigation
-keep class androidx.navigation.** { *; }

# Keep Material components
-keep class com.google.android.material.** { *; }

# Keep MediaSession
-keep class androidx.media3.session.** { *; }

# Keep ViewBinding
-keep class com.larkplayer.app.databinding.** { *; }

# Keep generated Hilt components
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class * extends dagger.hilt.android.HiltApplication { *; }

# Keep all parcelable implementations
-keep class * implements android.os.Parcelable { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Don't obfuscate resource references
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}

# Keep JNI classes
-keep class * { native <methods>; }

# Suppress warnings
-dontwarn com.google.android.exoplayer2.**
-dontwarn androidx.media3.**
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.serialization.**
-dontwarn coil.**
-dontwarn com.airbnb.lottie.**
-dontwarn dagger.hilt.**