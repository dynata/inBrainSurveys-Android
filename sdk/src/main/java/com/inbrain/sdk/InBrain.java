package com.inbrain.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.inbrain.sdk.callback.ConfirmRewardsCallback;
import com.inbrain.sdk.callback.GetRewardsCallback;
import com.inbrain.sdk.callback.InBrainCallback;
import com.inbrain.sdk.callback.ReceivedRewardsListener;
import com.inbrain.sdk.model.Reward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InBrain {
    private static final String PREFERENCES = "SharedPreferences_inBrain25930";
    private static final String PREFERENCE_DEVICE_ID = "529826892";
    private static final String PREFERENCE_APP_USER_ID = "378294761";
    static InBrainCallback callback;
    private static Context appContext = null;
    private static String clientId = null;
    private static String clientSecret = null;
    private static String appUserId = null;
    private static String deviceId = null;

    private InBrain() {
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void init(Context context, String clientId, String clientSecret,
                            InBrainCallback callback) {
        InBrain.appContext = context.getApplicationContext();
        InBrain.clientId = clientId;
        InBrain.clientSecret = clientSecret;
        InBrain.callback = callback;
        SharedPreferences preferences = getPreferences(appContext);
        if (preferences.contains(PREFERENCE_DEVICE_ID)) {
            InBrain.deviceId = preferences.getString(PREFERENCE_DEVICE_ID, null);
        }
        if (TextUtils.isEmpty(InBrain.appUserId) && preferences.contains(PREFERENCE_APP_USER_ID)) {
            InBrain.appUserId = preferences.getString(PREFERENCE_APP_USER_ID, null);
        }
        if (TextUtils.isEmpty(InBrain.deviceId)) {
            String androidId = Settings.Secure.ANDROID_ID;
            if (!TextUtils.isEmpty(androidId)) {
                InBrain.deviceId = UUID.nameUUIDFromBytes(androidId.getBytes()).toString();
            }
        }
        if (TextUtils.isEmpty(InBrain.deviceId)) {
            InBrain.deviceId = UUID.randomUUID().toString();
        }
        preferences.edit().putString(PREFERENCE_DEVICE_ID, InBrain.deviceId).apply();
    }

    public static void setAppUserId(String id) {
        appUserId = id;
        getPreferences(appContext).edit().putString(PREFERENCE_APP_USER_ID, InBrain.appUserId).apply();
    }

    public static void showSurveys(Context context) {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call init() method with client id and client secret.");
            return;
        }
        SurveysActivity.start(context, clientId, clientSecret, appUserId, deviceId);
    }

    public static void getRewards(final GetRewardsCallback callback) {
        TokenExecutor executor = new TokenExecutor(clientId, clientSecret);
        executor.getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                RewardsExecutor rewardsExecutor = new RewardsExecutor();
                rewardsExecutor.getRewards(token, new RewardsExecutor.RequestRewardsCallback() {
                    @Override
                    public void onGetRewards(List<Reward> rewards) {
                        callback.onGetRewards(rewards, new ReceivedRewardsListener() {
                            @Override
                            public void confirmRewardsReceived(List<Reward> rewards) {
                                confirmRewards(rewards);
                            }
                        });
                    }

                    @Override
                    public void onFailToLoadRewards(Throwable t) {
                        callback.onFailToLoadRewards(t);
                    }
                }, appUserId, deviceId);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                callback.onFailToLoadRewards(t);
            }
        });
    }

    public static void confirmRewards(final List<Reward> rewards, final ConfirmRewardsCallback callback) {
        List<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds, callback);
    }

    private static void confirmRewards(List<Reward> rewards) {
        List<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds, null);
    }

    private static List<Long> getRewardsIds(List<Reward> rewards) {
        List<Long> rewardsIds = new ArrayList<>(rewards.size());
        for (Reward reward : rewards) rewardsIds.add(reward.transactionId);
        return rewardsIds;
    }

    private static void confirmRewardsById(final List<Long> rewardsIds, final ConfirmRewardsCallback callback) {
        List<Long> failedToConfirmRewards = getFailedToConfirmRewardsIds();
        if (failedToConfirmRewards != null) {
            rewardsIds.addAll(failedToConfirmRewards);
        }
        TokenExecutor executor = new TokenExecutor(clientId, clientSecret);
        executor.getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                ConfirmRewardsExecutor confirmRewardsExecutor = new ConfirmRewardsExecutor();
                confirmRewardsExecutor.confirmRewards(token, rewardsIds, new ConfirmRewardsExecutor.ConfirmRewardsCallback() {
                    @Override
                    public void onSuccessfullyConfirmedRewards() {
                        if (callback != null) callback.onSuccessfullyConfirmRewards();
                    }

                    @Override
                    public void onFailToConfirmRewards(Throwable t) {
                        if (callback != null) callback.onFailToConfirmRewards(t);
                        saveFailedToConfirmRewards(rewardsIds);
                    }
                }, appUserId, deviceId);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                callback.onFailToConfirmRewards(t);
            }
        });
    }

    private static void saveFailedToConfirmRewards(List<Long> rewardsIds) {

    }

    private static List<Long> getFailedToConfirmRewardsIds() {
        return null;
    }
}
