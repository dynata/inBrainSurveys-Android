package com.inbrain.sdk.executor;

import android.util.Log;

import com.inbrain.sdk.Constants;
import com.inbrain.sdk.api.AuthorizedGetRequest;
import com.inbrain.sdk.callback.AsyncResponse;
import com.inbrain.sdk.callback.RewardsCallback;

public class RewardsExecutor {
    public void getRewards(String token, final RewardsCallback callback, String appUserId, String deviceId) {
        String rewardsUrl = String.format("%s%s/%s/%s", Constants.BASE_URL, Constants.REWARDS, appUserId, deviceId);
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
        Log.d("RewardsExecutor", "token is:" + token);
        Log.d("RewardsExecutor", "rewardsUrl is:" + rewardsUrl);
        getRewardsRequest.execute(rewardsUrl, token);
    }

    private void onGotRewardsData(RewardsCallback callback, String output) {
        Log.d("RewardsExecutor", "Result is:" + output);
    }
}
