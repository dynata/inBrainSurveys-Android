package com.inbrain.sdk;

interface TokenCallback {
    void onGetToken(String token);
    void onFailToLoadToken(Throwable t);
}
