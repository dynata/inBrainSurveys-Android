package com.inbrain.sdk;

import org.json.JSONException;
import org.json.JSONObject;

class TokenExecutor {
    private static final String FIELD_NAME_ACCESS_TOKEN = "access_token";
    private final String clientId;
    private final String clientSecret;

    TokenExecutor(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    void getToken(final TokenCallback callback) {
        TokenPostRequest tokenRequest = new TokenPostRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    JSONObject jObj = new JSONObject(output);
                    String token = jObj.getString(FIELD_NAME_ACCESS_TOKEN);
                    callback.onGetToken(token);
                } catch (JSONException e) {
                    callback.onFailToLoadToken(e);
                }
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailToLoadToken(ex);
            }
        }, clientId, clientSecret);
        tokenRequest.execute();
    }

    interface TokenCallback {
        void onGetToken(String token);
        void onFailToLoadToken(Throwable t);
    }
}