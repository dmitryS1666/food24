#########################
# 1. Сохраняем Activities / Fragments / Services
#########################
-keep class * extends android.app.Activity
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver

#########################
# 2. Gson — сериализация
#########################
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

#########################
# 3. Glide
#########################
-keep public class com.bumptech.glide.** { *; }
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class * extends com.bumptech.glide.request.target.CustomTarget
-dontwarn com.bumptech.glide.**

#########################
# 4. OkHttp
#########################
-dontwarn okhttp3.**
-dontwarn okio.**

#########################
# 5. Media3
#########################
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

#########################
# 6. MPAndroidChart
#########################
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

#########################
# 7. Jetpack Compose
#########################
# Compose часто не требует ручных правил, но чтобы избежать проблем:
-dontwarn kotlin.Unit
-keep class androidx.compose.** { *; }
-keep class androidx.activity.ComponentActivity
-keep class androidx.lifecycle.ViewModel
-dontwarn androidx.compose.**

#########################
# 8. Точки входа
#########################
-keep class com.food24.track.ui.onboarding.SplashActivity { *; }
-keep class com.food24.track.MainActivity { *; }

#########################
# 9. Обработка исключений
#########################
# Сохраняем line number info (для крэшей)
-keepattributes SourceFile,LineNumberTable
