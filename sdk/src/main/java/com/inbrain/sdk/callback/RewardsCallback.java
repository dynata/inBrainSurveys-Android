package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface RewardsCallback {
    void onGetRewards(List<Reward> rewards);
    void onFailToLoadRewards(Throwable t);
}
