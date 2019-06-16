package com.inbrain.sdk.callback;

public interface AsyncResponse {
    void processFinish(String output);
    void onError(Exception ex);
}
