package com.inbrain.sdk;

import android.util.Log;

import com.inbrain.sdk.model.Reward;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class RewardsExecutor {
    private String getRewardsUrl(String appUserId, String deviceId) {
        return String.format("%s%s/%s/%s", Constants.BASE_URL, Constants.REWARDS, appUserId, deviceId);
    }

    void getRewards(String token, final RequestRewardsCallback callback, String appUserId, String deviceId) {
        String rewardsUrl = getRewardsUrl(appUserId, deviceId);
        AuthorizedGetRequest getRewardsRequest = new AuthorizedGetRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                onGotRewardsData(callback, output);
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailToLoadRewards(ex);
            }
        });
        if (BuildConfig.DEBUG) Log.d("RewardsExecutor", "token is:" + token);
        if (BuildConfig.DEBUG) Log.d("RewardsExecutor", "rewardsUrl is:" + rewardsUrl);
        getRewardsRequest.execute(rewardsUrl, token);
    }

    private void onGotRewardsData(RequestRewardsCallback callback, String data) {
        try {
            List<Reward> rewards = parseRewards(data);
            callback.onGetRewards(rewards);
        } catch (JSONException e) {
            callback.onFailToLoadRewards(e);
        }
    }

    private List<Reward> parseRewards(String data) throws JSONException {
        List<Reward> rewards = new ArrayList<>();
        JSONArray jsonarray = new JSONArray(data);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            long transactionId = jsonobject.getLong("transactionId");
            double amount = jsonobject.getDouble("amount");
            String currency = jsonobject.getString("currency");
            int transactionType = jsonobject.getInt("transactionType");
            rewards.add(new Reward(transactionId, (float) amount, currency, transactionType));
        }
        return rewards;
    }

    public interface RequestRewardsCallback {
        void onGetRewards(List<Reward> rewards);
        void onFailToLoadRewards(Throwable t);
    }
}
