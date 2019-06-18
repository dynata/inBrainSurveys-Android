-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}


-keep class com.inbrain.sdk.InBrain {
    public <methods>;
}

-keep interface com.inbrain.sdk.callback.** { *; }
-keep class com.inbrain.sdk.model.** { *; }