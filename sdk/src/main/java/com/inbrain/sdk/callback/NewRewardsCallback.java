package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface NewRewardsCallback {
    /**
     * Notifies the application about new rewards.
     * You need to confirm rewards that were processed in your application.
     * For simple synchronous confirmation just return true from this method.
     * For advanced async confirmation return false here and then call
     * confirmRewards separately.
     *
     * @param rewards new rewards to handle (including previous unconfirmed rewards)
     * @return true if rewards were handled by the app and should be confirmed automatically,
     *         false otherwise
     */
    boolean handleRewards(List<Reward> rewards);
}
