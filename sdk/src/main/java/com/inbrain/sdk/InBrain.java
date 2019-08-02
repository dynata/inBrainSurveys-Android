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
    private static final String PREFERENCE_PENDING_REWARDS = "372131_f4lied";

    private static InBrain instance;

    private Set<Long> confirmedRewardsIds = new HashSet<>();
    private Set<Reward> lastReceivedRewards = new HashSet<>();

    private String clientId = null;
    private String clientSecret = null;
    private List<InBrainCallback> callbacksList = new ArrayList<>();
    private String appUserId = null;
    private String deviceId = null;
    private SharedPreferences preferences;
    private String token;


    private InBrain() {
    }

    public static InBrain getInstance() {
        if (instance == null) {
            instance = new InBrain();
        }
        return instance;
    }

    public void init(Context context, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        preferences = getPreferences(context);
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

    private void requireInit() {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call init() method!");
            throw new IllegalStateException();
        }
    }

    public void setAppUserId(String id) {
        requireInit();
        appUserId = id;
        preferences.edit().putString(PREFERENCE_APP_USER_ID, appUserId).apply();
        token = null;
    }

    public void addCallback(InBrainCallback callback) {
        callbacksList.add(callback);
    }

    public void removeCallback(InBrainCallback callback) {
        callbacksList.remove(callback);
    }

    /**
     * Opens survey wall
     *
     * @param context
     */
    public void showSurveys(Context context) {
        requireInit();
        SurveysActivity.start(context, clientId, clientSecret, appUserId, deviceId);
    }

    /**
     * Requests rewards manually.
     *
     * @see InBrainCallback
     */
    public void getRewards(final GetRewardsCallback callback) {
        requireInit();
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "External get rewards");
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    requestRewardsWithTokenUpdate(callback, false);
                }

                @Override
                public void onFailToLoadToken(Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                    callback.onFailToLoadRewards(GetRewardsCallback.ERROR_CODE_UNKNOWN);
                }
            });
        } else {
            requestRewardsWithTokenUpdate(callback, true);
        }
    }

    private void requestRewardsWithTokenUpdate(final GetRewardsCallback callback, final boolean updateToken) {
        RewardsExecutor rewardsExecutor = new RewardsExecutor();
        rewardsExecutor.getRewards(token, new RewardsExecutor.RequestRewardsCallback() {
            @Override
            public void onGetRewards(List<Reward> rewards) {
                onGetRewardsSuccess(callback, rewards);
            }

            @Override
            public void onFailToLoadRewards(Throwable t) {
                if (BuildConfig.DEBUG) {
                    Log.e(Constants.LOG_TAG, "Failed to load rewards");
                    t.printStackTrace();
                }
                if (t instanceof TokenExpiredException) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Token expired");
                    }
                    if (updateToken) {
                        refreshToken(new TokenExecutor.TokenCallback() {
                            @Override
                            public void onGetToken(String token) {
                                requestRewardsWithTokenUpdate(callback, false);
                            }

                            @Override
                            public void onFailToLoadToken(Throwable t) {
                                if (BuildConfig.DEBUG) {
                                    Log.e(Constants.LOG_TAG, "Failed to load token");
                                    t.printStackTrace();
                                }
                                callback.onFailToLoadRewards(GetRewardsCallback.ERROR_CODE_UNKNOWN);
                            }
                        });
                    } else {
                        callback.onFailToLoadRewards(GetRewardsCallback.ERROR_CODE_UNKNOWN);
                    }
                } else {
                    callback.onFailToLoadRewards(GetRewardsCallback.ERROR_CODE_UNKNOWN);
                }
            }
        }, appUserId, deviceId);
    }

    private void onGetRewardsSuccess(GetRewardsCallback callback, List<Reward> rewards) {
        lastReceivedRewards = new HashSet<>(rewards);
        if (onNewRewardsReceived(rewards, callback)) confirmRewards(rewards);
    }

    /**
     * Requests rewards manually. Returns result through global callback set in setListener().
     */
    public void getRewards() {
        requireInit();
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "Get rewards");
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    requestRewardsWithTokenUpdate(false);
                }

                @Override
                public void onFailToLoadToken(Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                }
            });
        } else {
            requestRewardsWithTokenUpdate(true);
        }
    }

    private void requestRewardsWithTokenUpdate(final boolean updateToken) {
        RewardsExecutor rewardsExecutor = new RewardsExecutor();
        rewardsExecutor.getRewards(token, new RewardsExecutor.RequestRewardsCallback() {
            @Override
            public void onGetRewards(List<Reward> rewards) {
                onGetRewardsSuccess(rewards);
            }

            @Override
            public void onFailToLoadRewards(Throwable t) {
                if (t instanceof TokenExpiredException) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Token expired");
                    }
                    if (updateToken) {
                        refreshToken(new TokenExecutor.TokenCallback() {
                            @Override
                            public void onGetToken(String token) {
                                requestRewardsWithTokenUpdate(false);
                            }

                            @Override
                            public void onFailToLoadToken(Throwable t) {
                                if (BuildConfig.DEBUG) {
                                    Log.e(Constants.LOG_TAG, "Failed to load token");
                                    t.printStackTrace();
                                }
                            }
                        });
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Token is expired, but not gonna update");
                        }
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load rewards");
                        t.printStackTrace();
                    }
                }
            }
        }, appUserId, deviceId);
    }

    private void onGetRewardsSuccess(List<Reward> rewards) {
        Set<Reward> newRewards = new HashSet<>(rewards);
        if (checkRewardsAreSame(newRewards)) {
            if (BuildConfig.DEBUG) Log.w(Constants.LOG_TAG, "Rewards are same");
            return;
        }
        lastReceivedRewards = newRewards;
        if (onNewRewardsReceived(rewards, null)) confirmRewards(rewards);
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
            } else if (!callbacksList.isEmpty()) {
                boolean handleBySelf = false;
                for (InBrainCallback callback : callbacksList) {
                    if (callback.handleRewards(rewards)) {
                        handleBySelf = true;
                        break;
                    }
                }
                return handleBySelf; // notify by subscription
            }
        }
        return true;
    }

    private void refreshToken(final TokenExecutor.TokenCallback tokenCallback) {
        TokenExecutor executor = new TokenExecutor(clientId, clientSecret);
        executor.getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                InBrain.this.token = token;
                tokenCallback.onGetToken(token);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                tokenCallback.onFailToLoadToken(t);
            }
        });
    }

    /**
     * Confirms rewards manually.
     *
     * @param rewards list of rewards which need to be confirmed
     */
    public void confirmRewards(final List<Reward> rewards) {
        requireInit();
        Set<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds);
    }

    private Set<Long> getRewardsIds(List<Reward> rewards) {
        Set<Long> rewardsIds = new HashSet<>(rewards.size());
        for (Reward reward : rewards) {
            if (confirmedRewardsIds.contains(reward.transactionId)) continue;
            rewardsIds.add(reward.transactionId);
        }
        return rewardsIds;
    }

    private void confirmRewardsById(final Set<Long> rewardIds) {
        final Set<Long> pendingRewardIds = getPendingRewardIds();
        pendingRewardIds.addAll(rewardIds);
        savePendingRewards(pendingRewardIds);
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    confirmRewards(token, pendingRewardIds);
                }

                @Override
                public void onFailToLoadToken(Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                }
            });
        } else {
            confirmRewards(token, pendingRewardIds);
        }
    }

    private void confirmRewards(String token, final Set<Long> pendingRewardIds) {
        ConfirmRewardsExecutor confirmRewardsExecutor = new ConfirmRewardsExecutor();
        confirmRewardsExecutor.confirmRewards(token, pendingRewardIds, new ConfirmRewardsExecutor.ConfirmRewardsCallback() {
            @Override
            public void onSuccess() {
                if (BuildConfig.DEBUG) {
                    Log.d(Constants.LOG_TAG, "Successfully confirmed rewards");
                }
                confirmedRewardsIds.addAll(pendingRewardIds);
                Set<Long> newPendingRewardIds = getPendingRewardIds(); // It might have changed
                newPendingRewardIds.removeAll(pendingRewardIds);
                savePendingRewards(newPendingRewardIds);
            }

            @Override
            public void onFailed(Throwable t) {
                if (t instanceof TokenExpiredException) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Token expired");
                    }
                    refreshToken(new TokenExecutor.TokenCallback() {
                        @Override
                        public void onGetToken(String token) {
                            ConfirmRewardsExecutor confirmRewardsExecutor = new ConfirmRewardsExecutor();
                            confirmRewardsExecutor.confirmRewards(token, pendingRewardIds, new ConfirmRewardsExecutor.ConfirmRewardsCallback() {
                                @Override
                                public void onSuccess() {
                                    if (BuildConfig.DEBUG) {
                                        Log.d(Constants.LOG_TAG, "Successfully confirmed rewards");
                                    }
                                    confirmedRewardsIds.addAll(pendingRewardIds);
                                    Set<Long> newPendingRewardIds = getPendingRewardIds(); // It might have changed
                                    newPendingRewardIds.removeAll(pendingRewardIds);
                                    savePendingRewards(newPendingRewardIds);
                                }

                                @Override
                                public void onFailed(Throwable t) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(Constants.LOG_TAG, "On failed to confirm rewards:" + t);
                                    }
                                }
                            }, appUserId, deviceId);
                        }

                        @Override
                        public void onFailToLoadToken(Throwable t) {
                            if (BuildConfig.DEBUG) {
                                Log.e(Constants.LOG_TAG, "Failed to load token");
                                t.printStackTrace();
                            }
                        }
                    });
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "On failed to confirm rewards:" + t);
                    }
                }
            }
        }, appUserId, deviceId);
    }

    private void savePendingRewards(Set<Long> rewardsIds) {
        if (rewardsIds == null) {
            preferences.edit()
                    .putStringSet(PREFERENCE_PENDING_REWARDS, null)
                    .apply();
            return;
        }
        Set<String> set = new HashSet<>();
        for (Long id : rewardsIds) set.add(id.toString());
        preferences.edit()
                .putStringSet(PREFERENCE_PENDING_REWARDS, set)
                .apply();
    }

    private Set<Long> getPendingRewardIds() {
        Set<String> set = preferences.getStringSet(PREFERENCE_PENDING_REWARDS, null);
        Set<Long> ids = new HashSet<>();
        if (set != null) {
            for (String stringNumber : set) {
                try {
                    ids.add(Long.parseLong(stringNumber));
                } catch (Exception ignored) {
                }
            }
        }
        return ids;
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    void onClosed() {
        if (!callbacksList.isEmpty()) {
            for (InBrainCallback callback : callbacksList) callback.onClosed();
        }
    }
}