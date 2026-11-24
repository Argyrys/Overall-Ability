# Add project specific ProGuard rules here.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.oaa.livingresume.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.oaa.livingresume.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
