# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.yomusensei.data.model.** { *; }
