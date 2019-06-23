package com.inbrain.sdk;

import android.util.Log;

import org.json.JSONArray;

import java.util.Set;

class ConfirmRewardsExecutor {
    void confirmRewards(String token, Set<Long> rewardsIds, final ConfirmRewardsCallback callback, String appUserId, String deviceId) {
        String rewardsUrl = getConfirmRewardsUrl(appUserId, deviceId);
        String rewardsBody = getConfirmRewardsBody(rewardsIds);
        AuthorizedPostRequest confirmTransactionsRequest = new AuthorizedPostRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                callback.onSuccessfullyConfirmedRewards();
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailToConfirmRewards(ex);
            }
        });
        if (BuildConfig.DEBUG) Log.d("RewardsExecutor", "token is:" + token);
        if (BuildConfig.DEBUG) Log.d("RewardsExecutor", "rewardsUrl is:" + rewardsUrl);
        if (BuildConfig.DEBUG) Log.d("RewardsExecutor", "rewardsBody is:" + rewardsBody);
        confirmTransactionsRequest.execute(rewardsUrl, token, rewardsBody);
    }

    private String getConfirmRewardsUrl(String appUserId, String deviceId) {
        return String.format("%s%s/%s/%s", Constants.BASE_URL, Constants.CONFIRM_TRANSACTIONS, appUserId, deviceId);
    }

    private String getConfirmRewardsBody(Set<Long> rewardsIds) {
        JSONArray array = new JSONArray();
        for (Long id : rewardsIds) array.put(id);
        return array.toString();
    }

    public interface ConfirmRewardsCallback {
        void onSuccessfullyConfirmedRewards();
        void onFailToConfirmRewards(Throwable t);
    }
}
