package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Reward;

import java.util.List;

/**
 * Callback which is used for notifying your application about new events
 */
public interface InBrainCallback {
    /**
     * Called when InBrain Activity is finished.
     */
    void onClosed();

    /**
     * Called when InBrain Activity is finished from JS interface.
     */
    void onClosedFromPage();
}
