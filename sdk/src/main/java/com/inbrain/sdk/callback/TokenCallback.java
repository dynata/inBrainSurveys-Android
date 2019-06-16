package com.inbrain.sdk.callback;

public interface TokenCallback {
    void onGetToken(String token);
    void onFailToLoadToken(Throwable t);
}
