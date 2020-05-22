package com.inbrain.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.inbrain.sdk.callback.GetRewardsCallback;
import com.inbrain.sdk.callback.InBrainCallback;
import com.inbrain.sdk.callback.StartSurveysCallback;
import com.inbrain.sdk.model.Reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.inbrain.sdk.Constants.LOG_TAG;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_1;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_2;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_3;

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
    private String sessionUid;
    private HashMap<String, String> dataPoints;
    private String language;
    private String title;
    private int toolbarColorResId;
    private int toolbarColor;
    private int backButtonColorResId;
    private int backButtonColor;
    private String token;
    private boolean wrongClientIdError;
    private boolean stagingMode;
    private Handler handler;

    private InBrain() {
    }

    public static InBrain getInstance() {
        if (instance == null) {
            instance = new InBrain();
        }
        return instance;
    }

    public void init(Context context, String clientId, String clientSecret) {
        boolean isUiThread = Looper.getMainLooper().getThread() == Thread.currentThread();
        if (!isUiThread) {
            Log.e(Constants.LOG_TAG, "Method must be called from main thread!");
            return;
        }
        handler = new Handler(Looper.getMainLooper());
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        wrongClientIdError = false;
        preferences = getPreferences(context);
        if (preferences.contains(PREFERENCE_DEVICE_ID)) {
            deviceId = preferences.getString(PREFERENCE_DEVICE_ID, null);
        }
        if (TextUtils.isEmpty(appUserId) && preferences.contains(PREFERENCE_APP_USER_ID)) {
            appUserId = preferences.getString(PREFERENCE_APP_USER_ID, null);
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
        }
        preferences.edit().putString(PREFERENCE_DEVICE_ID, deviceId).apply();
    }

    private boolean checkForInit() {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call init() method!");
            return false;
        }
        if (wrongClientIdError) {
            Log.e(Constants.LOG_TAG, "Wrong client id!");
            return false;
        }
        return true;
    }

    public void addCallback(InBrainCallback callback) {
        callbacksList.add(callback);
    }

    public void removeCallback(InBrainCallback callback) {
        callbacksList.remove(callback);
    }

    public void setAppUserId(String id) {
        if (!checkForInit()) {
            return;
        }
        appUserId = id;
        preferences.edit().putString(PREFERENCE_APP_USER_ID, appUserId).apply();
        token = null;
    }

    public void setSessionUid(String sessionUid) {
        this.sessionUid = sessionUid;
    }

    public void setDataPoints(HashMap<String, String> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setStagingMode(boolean stagingMode) {
        this.stagingMode = stagingMode;
    }

    public void setToolbarTitle(String title) {
        this.title = title;
    }

    public void setToolbarColorResId(int toolbarColorResId) {
        this.toolbarColorResId = toolbarColorResId;
    }

    public void setToolbarColor(int toolbarColor) {
        this.toolbarColor = toolbarColor;
    }

    public void setTitleTextColorResId(int backButtonColorResId) {
        this.backButtonColorResId = backButtonColorResId;
    }

    public void setTitleTextColor(int backButtonColor) {
        this.backButtonColor = backButtonColor;
    }

    /**
     * Opens survey wall
     */
    public void showSurveys(Context context, final StartSurveysCallback callback) {
        if (!checkForInit()) {
            return;
        }
        // todo pay attention to minimal required chrome version, old devices may have updates
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                WebView webView = new WebView(context);
                String userAgent = webView.getSettings().getUserAgentString();
                Pattern pattern = Pattern.compile("chrome/(\\d+)\\.(\\d+)\\.(\\d+)",
                        Pattern.CASE_INSENSITIVE);
                Matcher m = pattern.matcher(userAgent);
                ArrayList<String> matches = new ArrayList<>();
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        matches.add(m.group(i));
                    }
                }
                if (matches.size() > 2) {
                    int group0 = Integer.parseInt(matches.get(0));
                    int group1 = Integer.parseInt(matches.get(1));
                    int group2 = Integer.parseInt(matches.get(2));
                    boolean group0Matches = group0 >= MINIMUM_WEBVIEW_VERSION_GROUP_1;
                    boolean group1Matches = group1 >= MINIMUM_WEBVIEW_VERSION_GROUP_2;
                    boolean group2Matches = group2 >= MINIMUM_WEBVIEW_VERSION_GROUP_3;
                    if (group0Matches) {
                        if (!group1Matches) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFail("Android System WebView version isn't supported");
                                }
                            });
                            return;
                        } else if (!group2Matches) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFail("Android System WebView version isn't supported");
                                }
                            });
                            return;
                        }
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail("Android System WebView version isn't supported");
                            }
                        });
                        return;
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFail("Failed to check webview version, can't start SDK");
                        }
                    });
                    return;
                }
            } catch (Exception ex) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFail("Failed to check webview version, can't start SDK");
                    }
                });
                return;
            }
        }

        if (language == null) {
            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            if (locale != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    language = locale.toLanguageTag().toLowerCase();
                } else {
                    String lang = locale.getLanguage();
                    String country = locale.getCountry().toLowerCase();
                    language = lang + "-" + country;
                }
            }
        }

        if (toolbarColorResId != 0) {
            try {
                toolbarColor = context.getResources().getColor(toolbarColorResId);
            } catch (Resources.NotFoundException e) {
                Log.e(LOG_TAG, "Can't find color resource for toolbar:" + toolbarColorResId);
            }
        }

        if (backButtonColorResId != 0) {
            try {
                backButtonColor = context.getResources().getColor(backButtonColorResId);
            } catch (Resources.NotFoundException e) {
                Log.e(LOG_TAG, "Can't find color resource for back button:" + backButtonColorResId);
            }
        }

        try {
            SurveysActivity.start(context, stagingMode, clientId, clientSecret, sessionUid, appUserId, deviceId,
                    dataPoints, language, title, toolbarColor, backButtonColor);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess();
                }
            });
        } catch (final Exception ex) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFail("Failed to start SDK:" + ex);
                }
            });
            return;
        }
    }

    /**
     * Requests rewards manually.
     *
     * @see InBrainCallback
     */
    public void getRewards(final GetRewardsCallback callback) {
        if (!checkForInit()) {
            return;
        }
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "External get rewards");
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    requestRewardsWithTokenUpdate(callback, false);
                }

                @Override
                public void onFailToLoadToken(final Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailToLoadRewards(t);
                        }
                    });
                }
            });
        } else {
            requestRewardsWithTokenUpdate(callback, true);
        }
    }

    private void requestRewardsWithTokenUpdate(final GetRewardsCallback callback, final boolean updateToken) {
        RewardsExecutor rewardsExecutor = new RewardsExecutor();
        rewardsExecutor.getRewards(stagingMode, token, new RewardsExecutor.RequestRewardsCallback() {
            @Override
            public void onGetRewards(List<Reward> rewards) {
                onGetRewardsSuccess(callback, rewards);
            }

            @Override
            public void onFailToLoadRewards(final Throwable t) {
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
                            public void onFailToLoadToken(final Throwable t) {
                                if (BuildConfig.DEBUG) {
                                    Log.e(Constants.LOG_TAG, "Failed to load token");
                                    t.printStackTrace();
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onFailToLoadRewards(t);
                                    }
                                });
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailToLoadRewards(t);
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailToLoadRewards(t);
                        }
                    });
                }
            }
        }, appUserId, deviceId);
    }

    private void onGetRewardsSuccess(GetRewardsCallback callback, List<Reward> rewards) {
        lastReceivedRewards = new HashSet<>(rewards);
        if (shouldConfirmNewRewards(rewards, callback)) {
            confirmRewards(rewards);
        }
    }

    /**
     * Requests rewards manually. Returns result through global callback set in setListener().
     */
    public void getRewards() {
        if (!checkForInit()) {
            return;
        }
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
        rewardsExecutor.getRewards(stagingMode, token, new RewardsExecutor.RequestRewardsCallback() {
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
        if (shouldConfirmNewRewards(rewards, null)) {
            confirmRewards(rewards);
        }
    }

    private boolean checkRewardsAreSame(Set<Reward> newRewards) {
        boolean firstContainsAll = lastReceivedRewards.containsAll(newRewards);
        boolean secondContainsAll = newRewards.containsAll(lastReceivedRewards);
        return firstContainsAll && secondContainsAll;
    }

    private boolean shouldConfirmNewRewards(List<Reward> rewards,
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
        return !rewards.isEmpty();
    }

    private void refreshToken(final TokenExecutor.TokenCallback tokenCallback) {
        TokenExecutor executor = new TokenExecutor(stagingMode, clientId, clientSecret);
        executor.getToken(new TokenExecutor.TokenCallback() {
            @Override
            public void onGetToken(String token) {
                InBrain.this.token = token;
                tokenCallback.onGetToken(token);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                if (t instanceof InvalidClientException) {
                    token = null;
                    wrongClientIdError = true;
                    Log.w(Constants.LOG_TAG, "Invalid client");
                }
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
        if (!checkForInit()) {
            return;
        }
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
        confirmRewardsExecutor.confirmRewards(stagingMode, token, pendingRewardIds, new ConfirmRewardsExecutor.ConfirmRewardsCallback() {
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
                            confirmRewardsExecutor.confirmRewards(stagingMode, token, pendingRewardIds, new ConfirmRewardsExecutor.ConfirmRewardsCallback() {
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

    void onClosed(boolean finishedFromPage) {
        if (!callbacksList.isEmpty()) {
            for (final InBrainCallback callback : callbacksList) {
                if (finishedFromPage) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onClosedFromPage();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onClosed();
                        }
                    });
                }
            }
        }
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call init() method!");
            return "";
        }

        return deviceId;
    }
}