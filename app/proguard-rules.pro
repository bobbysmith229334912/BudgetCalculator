# Add project-specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep PendingIntent class and methods
-keep class android.app.PendingIntent { *; }

# Keep WorkManager classes (if you are using WorkManager)
-keep class androidx.work.impl.utils.ForceStopRunnable { *; }
-keep class androidx.work.impl.utils.SerialExecutor$Task { *; }

# Keep Firebase classes (if you are using Firebase)
-keep class com.google.firebase.** { *; }
-keepnames class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** { *; }

# Keep ML Kit classes (if you are using ML Kit)
-keep class com.google.mlkit.** { *; }
-keepnames class com.google.mlkit.** { *; }
-keepclassmembers class com.google.mlkit.** { *; }

# Keep Plaid SDK classes (if you are using Plaid SDK)
-keep class com.plaid.link.** { *; }
-keepnames class com.plaid.link.** { *; }
-keepclassmembers class com.plaid.link.** { *; }

# Keep WorkManager classes (if you are using WorkManager)
-keep class androidx.work.impl.utils.ForceStopRunnable { *; }
-keep class androidx.work.impl.utils.SerialExecutor$Task { *; }

# Keep Volley classes (if you are using Volley)
-keep class com.android.volley.toolbox.** { *; }
-keepnames class com.android.volley.toolbox.** { *; }
-keepclassmembers class com.android.volley.toolbox.** { *; }

# General rules to keep all models, adapters, and view holders (you can customize as needed)
-keep class com.hardcoreamature.budgetcalculatoreno.model.** { *; }
-keep class com.hardcoreamature.budgetcalculatoreno.adapter.** { *; }
-keep class com.hardcoreamature.budgetcalculatoreno.viewholder.** { *; }

# General rule to keep all enums
-keepclassmembers enum * { *; }

# Rule to keep attributes used by libraries
-keepattributes Signature
-keepattributes *Annotation*
