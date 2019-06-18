package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface InBrainCallback {
    void onAdClosed();
    void onRewardReceived(List<Reward> rewards, ReceivedRewardsListener callback);
}
