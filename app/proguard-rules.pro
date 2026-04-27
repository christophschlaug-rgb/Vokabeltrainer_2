# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.vokabeltrainer.**$$serializer { *; }
-keepclassmembers class com.example.vokabeltrainer.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.vokabeltrainer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
