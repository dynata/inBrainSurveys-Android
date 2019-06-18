package com.inbrain.sdk;

interface AsyncResponse {
    void processFinish(String output);
    void onError(Exception ex);
}