package com.inbrain.sdk;

interface ConfirmRewardsCallback {
    void onSuccessfullyConfirmRewards();
    void onFailToConfirmRewards(Throwable t);
}
