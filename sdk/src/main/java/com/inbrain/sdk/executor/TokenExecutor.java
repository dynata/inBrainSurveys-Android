package com.inbrain.sdk.executor;

import com.inbrain.sdk.api.TokenPostRequest;
import com.inbrain.sdk.callback.AsyncResponse;
import com.inbrain.sdk.callback.TokenCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenExecutor {
    private final String clientId;
    private final String clientSecret;

    public TokenExecutor(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void getToken(final TokenCallback callback) {
        TokenPostRequest tokenRequest = new TokenPostRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    JSONObject jObj = new JSONObject(output);
                    String token = jObj.getString("access_token");
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
}