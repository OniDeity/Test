# Moshi reflection-based adapters are not used (codegen only), but keep
# generated JsonAdapter classes and their targets discoverable when minifying.
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonClass class *
-keepnames class * extends com.squareup.moshi.JsonAdapter
