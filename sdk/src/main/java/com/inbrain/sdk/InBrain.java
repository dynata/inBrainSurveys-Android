package com.inbrain.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.inbrain.sdk.callback.GetRewardsCallback;
import com.inbrain.sdk.callback.InBrainCallback;
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

    private static InBrain instance;

    private Set<Long> confirmedRewardsIds = new HashSet<>();
    private Set<Reward> lastReceivedRewards = new HashSet<>();

    private Context appContext = null;
    private String clientId = null;
    private String clientSecret = null;
    private InBrainCallback callback;
    private String appUserId = null;
    private String deviceId = null;
    private SharedPreferences preferences;


    private InBrain() {
    }

    public static InBrain getInstance() {
        if (instance == null) {
            instance = new InBrain();
        }
        return instance;
    }

    public void init(Context context, String clientId, String clientSecret,
                     InBrainCallback callback) {
        appContext = context.getApplicationContext();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callback = callback;
        preferences = getPreferences(appContext);
        if (preferences.contains(PREFERENCE_DEVICE_ID)) {
            deviceId = preferences.getString(PREFERENCE_DEVICE_ID, null);
        }
        if (TextUtils.isEmpty(appUserId) && preferences.contains(PREFERENCE_APP_USER_ID)) {
            appUserId = preferences.getString(PREFERENCE_APP_USER_ID, null);
        }
        if (TextUtils.isEmpty(deviceId)) {
            String androidId = Settings.Secure.ANDROID_ID;
            if (!TextUtils.isEmpty(androidId)) {
                deviceId = UUID.nameUUIDFromBytes(androidId.getBytes()).toString();
            }
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
        }
        preferences.edit().putString(PREFERENCE_DEVICE_ID, deviceId).apply();
    }

    public void setAppUserId(String id) {
        appUserId = id;
        getPreferences(appContext).edit().putString(PREFERENCE_APP_USER_ID, appUserId).apply();
    }

    /**
     * Starts Ad Screen
     *
     * @param context
     */
    public void showSurveys(Context context) {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret) || callback == null) {
            Log.e(Constants.LOG_TAG, "Please first call init() method with client id, client secret and callback.");
            return;
        }
        SurveysActivity.start(context, clientId, clientSecret, appUserId, deviceId);
    }

    /**
     * Requests rewards manually.
     *
     * @see InBrainCallback
     */
    public void getRewards(final GetRewardsCallback callback) {
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "External get rewards");
        getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                RewardsExecutor rewardsExecutor = new RewardsExecutor();
                rewardsExecutor.getRewards(token, new RewardsExecutor.RequestRewardsCallback() {
                    @Override
                    public void onGetRewards(List<Reward> rewards) {
                        lastReceivedRewards = new HashSet<>(rewards);
                        if (onNewRewardsReceived(rewards, callback)) confirmRewards(rewards);
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

    void getRewards() {
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
                        if (onNewRewardsReceived(rewards, null)) confirmRewards(rewards);
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

    private boolean checkRewardsAreSame(Set<Reward> newRewards) {
        boolean firstContainsAll = lastReceivedRewards.containsAll(newRewards);
        boolean secondContainsAll = newRewards.containsAll(lastReceivedRewards);
        return firstContainsAll && secondContainsAll;
    }

    private boolean onNewRewardsReceived(List<Reward> rewards,
                                         GetRewardsCallback externalCallback) {
        Iterator<Reward> iterator = rewards.iterator();
        while (iterator.hasNext()) {
            Reward reward = iterator.next();
            for (Long rewardId : confirmedRewardsIds) {
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
                return externalCallback.handleRewards(rewards); // notify by request
            } else {
                return callback.handleRewards(rewards); // notify by subscription
            }
        }
        return false;
    }

    private void getToken(TokenExecutor.TokenCallback tokenCallback) {
        TokenExecutor executor = new TokenExecutor(clientId, clientSecret);
        executor.getToken(tokenCallback);
    }

    /**
     * Confirms rewards manually
     *
     * @param rewards list of rewards which need to be confirmed
     * @return success of confirmed rewards result
     */
    public boolean confirmRewards(final List<Reward> rewards) {
        Set<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds);
        return !rewardsIds.isEmpty();
    }

    private Set<Long> getRewardsIds(List<Reward> rewards) {
        Set<Long> rewardsIds = new HashSet<>(rewards.size());
        for (Reward reward : rewards) {
            if (confirmedRewardsIds.contains(reward.transactionId)) continue;
            rewardsIds.add(reward.transactionId);
        }
        return rewardsIds;
    }

    private void confirmRewardsById(final Set<Long> rewardsIds) {
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
                    public void onSuccess() {
                        if (BuildConfig.DEBUG) {
                            Log.d(Constants.LOG_TAG, "Successfully confirmed rewards");
                        }
                        confirmedRewardsIds.addAll(rewardsIds);
                        saveFailedToConfirmRewards(null);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "On failed to confirm rewards:" + t);
                        }
                        saveFailedToConfirmRewards(rewardsIds);
                    }
                }, appUserId, deviceId);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                if (BuildConfig.DEBUG) {
                    Log.e(Constants.LOG_TAG, "On failed to load token:" + t);
                }
            }
        });
    }

    private void saveFailedToConfirmRewards(Set<Long> rewardsIds) {
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

    private List<Long> getFailedToConfirmRewardsIds() {
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

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    void onAdClosed() {
        if (callback != null) callback.onAdClosed();
    }
}