package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface ReceivedRewardsListener {
    void confirmRewardsReceived(List<Reward> rewards);
}
