-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}


-keep class com.inbrain.sdk.InBrain {
    public <methods>;
    private void switchToMode(android.content.Context, java.lang.String);
}

-keep interface com.inbrain.sdk.callback.** { *; }
-keep class com.inbrain.sdk.model.** { *; }
-keep class com.inbrain.sdk.config.** { *; }

-keeppackagenames com.inbrain.sdk

-dontwarn java.lang.invoke.StringConcatFactory

-keepparameternames