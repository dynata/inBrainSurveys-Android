package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface GetRewardsCallback {
    void onGetRewards(List<Reward> rewards, ReceivedRewardsListener confirmRewardsCallback);
    void onFailToLoadRewards(Throwable t);
}
