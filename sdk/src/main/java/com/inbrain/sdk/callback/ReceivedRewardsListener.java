package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface ReceivedRewardsListener {
    /**
     * Notifies Ad Service about processed rewards.
     *
     * @param rewards list of rewards processed by your application
     */
    void confirmRewardsReceived(List<Reward> rewards);
}
