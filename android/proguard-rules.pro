# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android Gradle Plugin's default proguard-android.txt.

# Keep the vendor UHF/RFID SDK completely — it's a closed-source .jar and
# minification/R8 renaming it silently breaks reflection-based calls at
# runtime with no compile-time warning. Needed only if you ever flip
# minifyEnabled to true for this module or the consuming app.
-keep class com.rscja.** { *; }
-dontwarn com.rscja.**

# jxl.jar (Excel handling) — same reasoning.
-keep class jxl.** { *; }
-dontwarn jxl.**

# Keep the native module + package so RN's reflection-based bridge/interop
# lookup can still find them after obfuscation.
-keep class com.uhfrfidlibrary.uhf.C72RfidScannerModule { *; }
-keep class com.uhfrfidlibrary.uhf.C72RfidScannerPackage { *; }
