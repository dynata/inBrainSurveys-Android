package com.inbrain.sdk;

import static com.inbrain.sdk.Constants.LOG_TAG;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_1;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_2;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.inbrain.sdk.callback.GetCurrencySaleCallback;
import com.inbrain.sdk.callback.GetNativeSurveysCallback;
import com.inbrain.sdk.callback.GetRewardsCallback;
import com.inbrain.sdk.callback.InBrainCallback;
import com.inbrain.sdk.callback.StartSurveysCallback;
import com.inbrain.sdk.callback.SurveysAvailableCallback;
import com.inbrain.sdk.config.StatusBarConfig;
import com.inbrain.sdk.config.ToolBarConfig;
import com.inbrain.sdk.model.CurrencySale;
import com.inbrain.sdk.model.Reward;
import com.inbrain.sdk.model.Survey;
import com.inbrain.sdk.model.SurveyCategory;
import com.inbrain.sdk.model.SurveyFilter;

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

public class InBrain {
    private static final String PREFERENCES = "SharedPreferences_inBrain25930";
    private static final String PREFERENCE_DEVICE_ID = "529826892";
    private static final String PREFERENCE_PENDING_REWARDS = "372131_f4lied";

    private static InBrain instance;

    private final Set<Long> confirmedRewardsIds = new HashSet<>();
    private Set<Reward> lastReceivedRewards = new HashSet<>();

    private String apiClientID = null;
    private String apiSecret = null;
    private boolean isS2S = false;
    private final List<InBrainCallback> callbacksList = new ArrayList<>();
    private String userID = null;
    private String deviceId = null;
    private SharedPreferences preferences;
    private String sessionUid;
    private HashMap<String, String> dataOptions;
    private String language;
    private boolean langManuallySet = false;
    private String title;
    private int toolbarColorResId;
    private int toolbarColor;
    private int backButtonColorResId;
    private int backButtonColor;
    private int titleColorResId;
    private int titleColor;
    private Boolean enableToolbarElevation;
    private Boolean lightStatusBarIcons;
    private int statusBarColorResId;
    private int statusBarColor;
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

    public void setInBrain(Context context, String apiClientID, String apiSecret, boolean isS2S) {
        setInBrain(context, apiClientID, apiSecret, isS2S, null);
    }

    public void setInBrain(Context context, String apiClientID, String apiSecret, boolean isS2S, String userID) {
        boolean isUiThread = Looper.getMainLooper().getThread() == Thread.currentThread();
        if (!isUiThread) {
            Log.e(Constants.LOG_TAG, "Method must be called from main thread!");
            return;
        }
        handler = new Handler(Looper.getMainLooper());
        if (TextUtils.isEmpty(apiClientID)) {
            Log.e(Constants.LOG_TAG, "API_CLIENT_ID can't be null or empty!");
            return;
        }
        if (TextUtils.isEmpty(apiSecret)) {
            Log.e(Constants.LOG_TAG, "API_SECRET can't be null or empty!");
            return;
        }
        this.apiClientID = apiClientID.trim();
        this.apiSecret = apiSecret.trim();
        this.isS2S = isS2S;
        wrongClientIdError = false;
        setUserID(context, userID);
    }

    public void setUserID(Context context, String userID) {
        preferences = getPreferences(context);
        if (preferences.contains(PREFERENCE_DEVICE_ID)) {
            deviceId = preferences.getString(PREFERENCE_DEVICE_ID, null);
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            preferences.edit().putString(PREFERENCE_DEVICE_ID, deviceId).apply();
        }
        if (TextUtils.isEmpty(userID)) {
            this.userID = deviceId;
        } else {
            this.userID = userID;
        }
    }

    private boolean checkForInit() {
        if (TextUtils.isEmpty(apiClientID) || TextUtils.isEmpty(apiSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call setInBrain() method!");
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

    public void setInBrainValuesFor(String sessionID, HashMap<String, String> dataOptions) {
        this.sessionUid = sessionID;
        this.dataOptions = dataOptions;
    }

    public String getSessionUid() {
        return this.sessionUid;
    }

    public HashMap<String, String> getDataOptions() {
        return this.dataOptions;
    }

    public void setLanguage(String language) {
        this.language = language;
        this.langManuallySet = true;
    }

    public void setStagingMode(boolean stagingMode) {
        this.stagingMode = stagingMode;
    }

    public void setToolbarConfig(ToolBarConfig config) {
        if (config == null) {
            Log.e(Constants.LOG_TAG, "ToolBarConfig can't be null! Don't call this method if you don't need customization");
            return;
        }
        this.title = config.getTitle();
        this.toolbarColorResId = config.getToolbarColorResId();
        this.toolbarColor = config.getToolbarColor();
        this.backButtonColorResId = config.getBackButtonColorResId();
        this.backButtonColor = config.getBackButtonColor();
        this.titleColorResId = config.getTitleColorResId();
        this.titleColor = config.getTitleColor();
        this.enableToolbarElevation = config.isElevationEnabled();
    }

    public void setStatusBarConfig(StatusBarConfig config) {
        if (config == null) {
            Log.e(Constants.LOG_TAG, "StatusBarConfig can't be null! Don't call this method if you don't need customization");
            return;
        }
        this.lightStatusBarIcons = config.isStatusBarIconsLight();
        this.statusBarColorResId = config.getStatusBarColorResId();
        this.statusBarColor = config.getStatusBarColor();
    }

    /**
     * Opens survey wall
     */
    public void showSurveys(Context context, final StartSurveysCallback callback) {
        if (!canStartSurveys(context, callback)) {
            return;
        }

        prepareConfig(context);

        try {
            SurveysActivity.start(context, stagingMode, apiClientID, apiSecret, isS2S,
                    sessionUid, userID, deviceId, dataOptions, language, title, toolbarColor,
                    backButtonColor, titleColor, statusBarColor, enableToolbarElevation, lightStatusBarIcons);
            handler.post(callback::onSuccess);
        } catch (final Exception ex) {
            handler.post(() -> callback.onFail("Failed to start SDK:" + ex));
        }
    }

    public void showNativeSurvey(Context context, Survey survey, final StartSurveysCallback callback) {
        showNativeSurveyWith(context, survey.id, survey.searchId, callback);
    }

    public void showNativeSurveyWith(Context context, String surveyId, String searchId, final StartSurveysCallback callback) {
        if (!canStartSurveys(context, callback)) {
            return;
        }

        prepareConfig(context);

        try {
            SurveysActivity.start(context, stagingMode, apiClientID, apiSecret, isS2S,
                    sessionUid, userID, deviceId, surveyId, searchId, dataOptions, language, title, toolbarColor,
                    backButtonColor, titleColor, statusBarColor, enableToolbarElevation, lightStatusBarIcons);
            handler.post(callback::onSuccess);
        } catch (final Exception ex) {
            handler.post(() -> callback.onFail("Failed to start SDK:" + ex));
        }
    }

    private boolean canStartSurveys(Context context, final StartSurveysCallback callback) {
        if (!checkForInit()) {
            handler.post(() -> callback.onFail("SDK not initialized"));
            return false;
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
                            handler.post(() -> callback.onFail("Android System WebView version isn't supported"));
                            return false;
                        } else if (!group2Matches) {
                            handler.post(() -> callback.onFail("Android System WebView version isn't supported"));
                            return false;
                        }
                    } else {
                        handler.post(() -> callback.onFail("Android System WebView version isn't supported"));
                        return false;
                    }
                } else {
                    handler.post(() -> callback.onFail("Failed to check webview version, can't start SDK"));
                    return false;
                }
            } catch (Exception ex) {
                handler.post(() -> callback.onFail("Failed to check webview version, can't start SDK"));
                return false;
            }
        }
        return true;
    }

    private void prepareConfig(Context context) {
        if (!langManuallySet || language == null) {
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
                if (BuildConfig.DEBUG)
                    Log.d(LOG_TAG, "lang=" + language);
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

        if (titleColorResId != 0) {
            try {
                titleColor = context.getResources().getColor(titleColorResId);
            } catch (Resources.NotFoundException e) {
                Log.e(LOG_TAG, "Can't find color resource for title text:" + titleColorResId);
            }
        }

        if (statusBarColorResId != 0) {
            try {
                statusBarColor = context.getResources().getColor(statusBarColorResId);
            } catch (Resources.NotFoundException e) {
                Log.e(LOG_TAG, "Can't find color resource for status bar:" + statusBarColorResId);
            }
        }

        if (toolbarColor == 0) {
            toolbarColor = context.getResources().getColor(R.color.default_toolbar);
        }

        if (backButtonColor == 0) {
            backButtonColor = context.getResources().getColor(R.color.main_text);
        }

        if (titleColor == 0) {
            titleColor = context.getResources().getColor(R.color.main_text);
        }

        if (statusBarColor == 0) {
            statusBarColor = context.getResources().getColor(R.color.default_toolbar);
        }

        if (lightStatusBarIcons == null) {
            lightStatusBarIcons = true;
        }

        if (enableToolbarElevation == null) {
            enableToolbarElevation = false;
        }

        if (title == null) {
            title = context.getResources().getString(R.string.inbrain_surveys);
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
                    handler.post(() -> callback.onFailToLoadRewards(t));
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
                                handler.post(() -> callback.onFailToLoadRewards(t));
                            }
                        });
                    } else {
                        handler.post(() -> callback.onFailToLoadRewards(t));
                    }
                } else {
                    handler.post(() -> callback.onFailToLoadRewards(t));
                }
            }
        }, userID, deviceId);
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
        }, userID, deviceId);
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
            boolean processed = false;
            for (InBrainCallback callback : callbacksList) {
                if (callback.didReceiveInBrainRewards(rewards)) {
                    processed = true;
                }
            }
            return processed; // confirm by subscriber
        }
        return false; // no subscriptions for rewards, leave rewards for next call
    }

    private void refreshToken(final TokenExecutor.TokenCallback tokenCallback) {
        TokenExecutor executor = new TokenExecutor(stagingMode, apiClientID, apiSecret);
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

    /**
     * Confirms rewards manually.
     *
     * @param transactionIds list of transactionIds which need to be confirmed
     */
    public void confirmRewards(final long[] transactionIds) {
        if (!checkForInit()) {
            return;
        }
        Set<Long> rewardsIds = getRewardsIds(transactionIds);
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

    private Set<Long> getRewardsIds(long[] transactionIds) {
        Set<Long> rewardsIds = new HashSet<>(transactionIds.length);
        for (long transactionId : transactionIds) {
            if (confirmedRewardsIds.contains(transactionId)) continue;
            rewardsIds.add(transactionId);
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
                            }, userID, deviceId);
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
        }, userID, deviceId);
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
                    handler.post(callback::surveysClosedFromPage);
                } else {
                    handler.post(callback::surveysClosed);
                }
            }
        }
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(apiClientID) || TextUtils.isEmpty(apiSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call setInBrain() method!");
            return "";
        }

        return deviceId;
    }

    public void areSurveysAvailable(final Context context,
                                    final SurveysAvailableCallback callback) {
        if (!checkForInit()) {
            return;
        }
        if (BuildConfig.DEBUG) Log.d(Constants.LOG_TAG, "External check for available surveys");
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    requestSurveysAvailabilityWithTokenUpdate(context, callback, false);
                }

                @Override
                public void onFailToLoadToken(Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                    handler.post(() -> callback.onSurveysAvailable(false));
                }
            });
        } else {
            requestSurveysAvailabilityWithTokenUpdate(context, callback, true);
        }
    }

    private void requestSurveysAvailabilityWithTokenUpdate(final Context context,
                                                           final SurveysAvailableCallback callback,
                                                           final boolean updateToken) {
        SurveysAvailabilityExecutor surveysAvailabilityExecutor = new SurveysAvailabilityExecutor();
        surveysAvailabilityExecutor.areSurveysAvailable(context, token, stagingMode,
                new SurveysAvailabilityExecutor.SurveysAvailableExecutorCallback() {
                    @Override
                    public void onSurveysAvailable(boolean available) {
                        callback.onSurveysAvailable(available);
                    }

                    @Override
                    public void onFailToLoadSurveysAvailability(Exception t) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Failed to load surveys availability");
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
                                        requestSurveysAvailabilityWithTokenUpdate(context, callback, false);
                                    }

                                    @Override
                                    public void onFailToLoadToken(final Throwable t) {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(Constants.LOG_TAG, "Failed to load token");
                                            t.printStackTrace();
                                        }
                                        handler.post(() -> callback.onSurveysAvailable(false));
                                    }
                                });
                            } else {
                                handler.post(() -> callback.onSurveysAvailable(false));
                            }
                        } else {
                            handler.post(() -> callback.onSurveysAvailable(false));
                        }
                    }
                }, userID, deviceId);
    }

    public void getNativeSurveys(final GetNativeSurveysCallback callback) {
        getNativeSurveys(null, callback);
    }

    public void getNativeSurveys(final SurveyFilter filter, final GetNativeSurveysCallback callback) {
        if (filter == null)
            getNativeSurveys(null, null, null, callback);
        else
            getNativeSurveys(filter.placementId, filter.includeCategories, filter.excludeCategories, callback);
    }

    private void getNativeSurveys(final String placeId, final List<SurveyCategory> includeCategoryIds, final List<SurveyCategory> excludeCategoryIds,
                                  final GetNativeSurveysCallback callback) {
        if (!checkForInit()) {
            return;
        }
        if (BuildConfig.DEBUG)
            Log.d(Constants.LOG_TAG, "External get for native surveys, token: " + token);
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    Log.d(Constants.LOG_TAG, "onGetToken: " + token);
                    requestNativeSurveysWithTokenUpdate(placeId, includeCategoryIds, excludeCategoryIds, callback, false);
                }

                @Override
                public void onFailToLoadToken(Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                    handler.post(() -> callback.nativeSurveysReceived(new ArrayList<>()));
                }
            });
        } else {
            requestNativeSurveysWithTokenUpdate(placeId, includeCategoryIds, excludeCategoryIds, callback, true);
        }
    }

    private void requestNativeSurveysWithTokenUpdate(final String placeId, final List<SurveyCategory> includeCategoryIds, final List<SurveyCategory> excludeCategoryIds,
                                                     final GetNativeSurveysCallback callback, final boolean updateToken) {
        GetNativeSurveysListExecutor getNativeSurveysListExecutor = new GetNativeSurveysListExecutor();
        getNativeSurveysListExecutor.getNativeSurveysList(token, stagingMode,
                new GetNativeSurveysListExecutor.NativeSurveysExecutorCallback() {
                    @Override
                    public void onNativeSurveysAvailable(List<Survey> surveys) {
                        callback.nativeSurveysReceived(surveys);
                    }

                    @Override
                    public void onFailToLoadNativeSurveysList(Exception t) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Failed to load native surveys: " + t);
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
                                        if (BuildConfig.DEBUG) {
                                            Log.d(Constants.LOG_TAG, "onGetToken: " + token);
                                        }
                                        requestNativeSurveysWithTokenUpdate(placeId, includeCategoryIds, excludeCategoryIds, callback, false);
                                    }

                                    @Override
                                    public void onFailToLoadToken(final Throwable t) {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(Constants.LOG_TAG, "Failed to load token");
                                            t.printStackTrace();
                                        }
                                        handler.post(() -> callback.nativeSurveysReceived(new ArrayList<>()));
                                    }
                                });
                            } else {
                                handler.post(() -> callback.nativeSurveysReceived(new ArrayList<>()));
                            }
                        } else {
                            handler.post(() -> callback.nativeSurveysReceived(new ArrayList<>()));
                        }
                    }
                }, userID, deviceId, placeId, includeCategoryIds, excludeCategoryIds);
    }

    public void getCurrencySale(final GetCurrencySaleCallback callback) {
        if (!checkForInit()) {
            return;
        }
        if (BuildConfig.DEBUG)
            Log.d(Constants.LOG_TAG, "External get for ongoing currency sale data, token: " + token);
        if (TextUtils.isEmpty(token)) {
            refreshToken(new TokenExecutor.TokenCallback() {
                @Override
                public void onGetToken(String token) {
                    Log.d(Constants.LOG_TAG, "onGetToken: " + token);
                    fetchCurrencySaleWithTokenUpdate(callback, false);
                }

                @Override
                public void onFailToLoadToken(Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.e(Constants.LOG_TAG, "Failed to load token");
                        t.printStackTrace();
                    }
                    handler.post(() -> callback.currencySaleReceived(null));
                }
            });
        } else {
            fetchCurrencySaleWithTokenUpdate(callback, true);
        }
    }

    private void fetchCurrencySaleWithTokenUpdate(final GetCurrencySaleCallback callback, final boolean updateToken) {
        FetchCurrencySaleExecutor fetchCurrencySaleExecutor = new FetchCurrencySaleExecutor();
        fetchCurrencySaleExecutor.fetchCurrencySale(token, stagingMode,
                new FetchCurrencySaleExecutor.CurrencySaleExecutorCallback() {
                    @Override
                    public void onCurrencySaleAvailable(CurrencySale currencySale) {
                        callback.currencySaleReceived(currencySale);
                    }

                    @Override
                    public void onFailedToFetchCurrencySale(Exception t) {
                        if (BuildConfig.DEBUG) {
                            Log.e(Constants.LOG_TAG, "Failed to fetch currency sale: " + t);
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
                                        if (BuildConfig.DEBUG) {
                                            Log.d(Constants.LOG_TAG, "onGetToken: " + token);
                                        }
                                        fetchCurrencySaleWithTokenUpdate(callback, false);
                                    }

                                    @Override
                                    public void onFailToLoadToken(final Throwable t) {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(Constants.LOG_TAG, "Failed to load token");
                                            t.printStackTrace();
                                        }
                                        handler.post(() -> callback.currencySaleReceived(null));
                                    }
                                });
                            } else {
                                handler.post(() -> callback.currencySaleReceived(null));
                            }
                        } else {
                            handler.post(() -> callback.currencySaleReceived(null));
                        }
                    }
                });
    }
}