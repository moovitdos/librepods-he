# Add project specific ProGuard rules here.
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

-keep class me.kavishdevar.librepods.utils.KotlinModule { *; }

# --- Android 13 Parcelable crash (librepods-org/librepods#592) ---
# Intent.getParcelableArrayListExtra("data", Battery::class.java) NPEs in
# Parcel.readParcelableCreatorInternal when the @Parcelize class is obfuscated.
-keep class me.kavishdevar.librepods.data.** { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
# Navigation routes are @Serializable and matched by class identity
-keep class me.kavishdevar.librepods.presentation.navigation.Screen** { *; }
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
