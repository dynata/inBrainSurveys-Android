package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.InBrainSurveyReward;
import com.inbrain.sdk.model.Reward;

import java.util.List;
import java.util.Optional;

/**
 * Callback which is used for notifying your application about new events
 */
public interface InBrainCallback {
    /**
     Called upon dismissal of inBrainWebView.
     If you are using Native Surveys - please, ensure the surveys reloaded after some survey(s) completed.

     @param byWebView: **true** means closed by WebView's command; **false** - closed by user;
     @param rewards: **NOTE:** At the moment only first** Native Survey reward is delivered.
            That means if the user complete a Native Survey, proceed to Survey Wall and complete one more survey - only first
            reward will be delivered. In case of Survey Wall usage only - no rewards will be delivered.
     */
    void surveysClosed(boolean byWebView, Optional<List<InBrainSurveyReward>> rewards);

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
    boolean didReceiveInBrainRewards(List<Reward> rewards);

    //region -DEPRECATED---------------
    /**
     * @deprecated(forRemoval=true) Use {@link #surveysClosed(boolean, List)} instead.
     */
    @Deprecated
    default void surveysClosed() {};

    /**
     * @deprecated(forRemoval=true) Use {@link #surveysClosed(boolean, List)} instead.
     */
    @Deprecated
    default void surveysClosedFromPage() {};
    //endregion
}
