package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

/**
 * Callback which is used for notifying your application about new events
 */
public interface InBrainCallback {
    /**
     * Calls when InBrain Ad Activity is finished
     */
    void onAdClosed();

    /**
     * Notifies your application about new rewards.
     * You need to confirm receipt after processing rewards in your application.
     *
     * @param rewards  new rewards
     * @param callback callback which is need to be called after processing rewards to confirm them.
     */
    void onRewardReceived(List<Reward> rewards, ReceivedRewardsListener callback);
}
