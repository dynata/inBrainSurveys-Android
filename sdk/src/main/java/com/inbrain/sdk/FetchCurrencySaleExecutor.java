package com.inbrain.sdk;

import android.util.Log;

import com.inbrain.sdk.model.CurrencySale;

import org.json.JSONException;
import org.json.JSONObject;

class FetchCurrencySaleExecutor {
    void fetchCurrencySale(final String token, final boolean stagingMode,
                           final CurrencySaleExecutorCallback callback) {
        String fetchCurrencySaleUrl = getCurrencySaleFullUrl(stagingMode, Constants.getCurrencySaleUrl());
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOG_TAG, "fetchCurrencySale() url: " + fetchCurrencySaleUrl);
        }
        AuthorizedGetRequest fetchCurrencySaleRequest = new AuthorizedGetRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                onCurrencySaleReceived(callback, output);
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailedToFetchCurrencySale(ex);
            }
        });
        fetchCurrencySaleRequest.execute(fetchCurrencySaleUrl, token);
    }

    private String getCurrencySaleFullUrl(boolean stagingMode, String currencySaleUrl) {
        String baseUrl;
        if (stagingMode) {
            baseUrl = Constants.STAGING_BASE_URL;
        } else {
            baseUrl = Constants.BASE_URL;
        }
        return String.format("%s%s", baseUrl, currencySaleUrl);
    }

    private void onCurrencySaleReceived(CurrencySaleExecutorCallback callback, String output) {
        try {
            if (BuildConfig.DEBUG) {
                Log.d(Constants.LOG_TAG, "onCurrencySaleReceived: " + output);
            }
            callback.onCurrencySaleAvailable(parseCurrencySale(output));
        } catch (JSONException e) {
            callback.onFailedToFetchCurrencySale(e);
        }
    }

    private CurrencySale parseCurrencySale(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);

        String startOn = jsonObject.getString("startOn");
        String endOn = jsonObject.getString("endOn");
        String description = jsonObject.getString("description");
        float multiplier = (float) jsonObject.getDouble("multiplier");

        return new CurrencySale(startOn, endOn, description, multiplier);
    }

    public interface CurrencySaleExecutorCallback {
        void onCurrencySaleAvailable(CurrencySale currencySale);

        void onFailedToFetchCurrencySale(Exception ex);
    }
}
