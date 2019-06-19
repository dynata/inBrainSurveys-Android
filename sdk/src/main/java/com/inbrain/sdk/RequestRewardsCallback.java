package com.inbrain.sdk;

import com.inbrain.sdk.callback.ReceivedRewardsListener;
import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface RequestRewardsCallback {
    void onGetRewards(List<Reward> rewards);
    void onFailToLoadRewards(Throwable t);
}
