package com.inbrain.sdk.callback;

public interface ConfirmRewardsCallback {
    void onSuccessfullyConfirmRewards();
    void onFailToConfirmRewards(Throwable t);
}