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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InBrain {
    private static final String PREFERENCES = "SharedPreferences_inBrain25930";
    private static final String PREFERENCE_DEVICE_ID = "529826892";
    private static final String PREFERENCE_APP_USER_ID = "378294761";
    private static final String PREFERENCE_FAILED_REWARDS = "372131_f4lied";
    static InBrainCallback callback;
    private static Context appContext = null;
    private static String clientId = null;
    private static String clientSecret = null;
    private static String appUserId = null;
    private static String deviceId = null;
    private static SharedPreferences preferences;

    private static Set<Long> confirmedRewards;
    private static Set<Reward> lastReceivedRewards;

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
        preferences = getPreferences(appContext);
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
        confirmedRewards = new HashSet<>();
        lastReceivedRewards = new HashSet<>();
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
        if (callback == null) {
            Log.e(Constants.LOG_TAG, "Please first call init() method with callback.");
            return;
        }
        SurveysActivity.start(context, clientId, clientSecret, appUserId, deviceId);
    }

    public static void getRewards(final GetRewardsCallback callback) {
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "External get rewards");
        getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                RewardsExecutor rewardsExecutor = new RewardsExecutor();
                rewardsExecutor.getRewards(token, new RewardsExecutor.RequestRewardsCallback() {
                    @Override
                    public void onGetRewards(List<Reward> rewards) {
                        lastReceivedRewards = new HashSet<>(rewards);
                        onNewRewardsReceived(rewards, callback, new ReceivedRewardsListener() {
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

    static void getRewards() {
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "Get rewards");
        getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                RewardsExecutor rewardsExecutor = new RewardsExecutor();
                rewardsExecutor.getRewards(token, new RewardsExecutor.RequestRewardsCallback() {
                    @Override
                    public void onGetRewards(List<Reward> rewards) {
                        Set<Reward> newRewards = new HashSet<>(rewards);
                        if (checkRewardsAreSame(newRewards)) {
                            if (BuildConfig.DEBUG) Log.w(Constants.LOG_TAG, "Rewards are same");
                            return;
                        }
                        lastReceivedRewards = newRewards;
                        onNewRewardsReceived(rewards, null, new ReceivedRewardsListener() {
                            @Override
                            public void confirmRewardsReceived(List<Reward> rewards) {
                                confirmRewards(rewards);
                            }
                        });
                    }

                    @Override
                    public void onFailToLoadRewards(Throwable t) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Failed to load rewards:" + t);
                        }
                    }
                }, appUserId, deviceId);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                if (BuildConfig.DEBUG) Log.e(Constants.LOG_TAG, "Failed to load token:" + t);
            }
        });
    }

    private static boolean checkRewardsAreSame(Set<Reward> newRewards) {
        boolean firstContainsAll = lastReceivedRewards.containsAll(newRewards);
        boolean secondContainsAll = newRewards.containsAll(lastReceivedRewards);
        return firstContainsAll && secondContainsAll;
    }

    private static void onNewRewardsReceived(List<Reward> rewards,
                                             GetRewardsCallback externalCallback,
                                             ReceivedRewardsListener receivedRewardsListener) {
        Iterator<Reward> iterator = rewards.iterator();
        while (iterator.hasNext()) {
            Reward reward = iterator.next();
            for (Long rewardId : confirmedRewards) {
                if (reward.transactionId == rewardId) {
                    if (BuildConfig.DEBUG) {
                        Log.w(Constants.LOG_TAG, "New reward has been already confirmed");
                    }
                    iterator.remove();
                    break;
                }
            }
        }
        if (!rewards.isEmpty()) {
            if (externalCallback != null) {
                externalCallback.onGetRewards(rewards, receivedRewardsListener); // notify by request
            } else {
                InBrain.callback.onRewardReceived(rewards, receivedRewardsListener); // notify by subscription
            }
        }
    }

    private static void getToken(TokenExecutor.TokenCallback tokenCallback) {
        TokenExecutor executor = new TokenExecutor(clientId, clientSecret);
        executor.getToken(tokenCallback);
    }

    public static void confirmRewards(final List<Reward> rewards, final ConfirmRewardsCallback callback) {
        Set<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds, callback);
    }

    private static void confirmRewards(List<Reward> rewards) {
        Set<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds, null);
    }

    private static Set<Long> getRewardsIds(List<Reward> rewards) {
        Set<Long> rewardsIds = new HashSet<>(rewards.size());
        for (Reward reward : rewards) rewardsIds.add(reward.transactionId);
        return rewardsIds;
    }

    private static void confirmRewardsById(final Set<Long> rewardsIds, final ConfirmRewardsCallback callback) {
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
                        confirmedRewards.addAll(rewardsIds);
                        saveFailedToConfirmRewards(null);
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
                if (callback != null) callback.onFailToConfirmRewards(t);
            }
        });
    }

    private static void saveFailedToConfirmRewards(Set<Long> rewardsIds) {
        if (rewardsIds == null) {
            preferences.edit()
                    .putStringSet(PREFERENCE_FAILED_REWARDS, null)
                    .apply();
            return;
        }
        Set<String> set = new HashSet<>();
        for (Long id : rewardsIds) set.add(id.toString());
        preferences.edit()
                .putStringSet(PREFERENCE_FAILED_REWARDS, set)
                .apply();
    }

    private static List<Long> getFailedToConfirmRewardsIds() {
        Set<String> set = preferences.getStringSet(PREFERENCE_FAILED_REWARDS, null);
        if (set == null) return null;
        List<Long> ids = new ArrayList<>(set.size());
        for (String stringNumber : set) {
            try {
                ids.add(Long.parseLong(stringNumber));
            } catch (Exception ignored) {
            }
        }
        return ids;
    }
}
