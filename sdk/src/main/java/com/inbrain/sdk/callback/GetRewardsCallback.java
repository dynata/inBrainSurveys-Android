package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

public interface GetRewardsCallback {
    int ERROR_CODE_UNKNOWN = 0;

    /**
     * @param rewards
     * @return handle rewards by user, in case false will be handled by user
     */
    boolean handleRewards(List<Reward> rewards);

    void onFailToLoadRewards(int errorCode);
}
