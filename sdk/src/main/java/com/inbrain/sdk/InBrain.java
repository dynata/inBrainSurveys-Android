package com.inbrain.sdk;

import static com.inbrain.sdk.Constants.LOG_TAG;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_1;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_2;
import static com.inbrain.sdk.Constants.MINIMUM_WEBVIEW_VERSION_GROUP_3;

import android.content.Context;
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
import com.inbrain.sdk.model.InBrainSurveyReward;
import com.inbrain.sdk.model.Reward;
import com.inbrain.sdk.model.Survey;
import com.inbrain.sdk.model.SurveyCategory;
import com.inbrain.sdk.model.SurveyFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InBrain {
    private static InBrain instance;

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
    private Handler handler;

    private final APIExecutor apiExecutor;

    private InBrain() {
        handler = new Handler(Looper.getMainLooper());
        apiExecutor = new APIExecutor(handler);
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
        if (TextUtils.isEmpty(apiClientID)) {
            Log.e(Constants.LOG_TAG, "API_CLIENT_ID can't be null or empty!");
            return;
        }
        if (TextUtils.isEmpty(apiSecret)) {
            Log.e(Constants.LOG_TAG, "API_SECRET can't be null or empty!");
            return;
        }
        apiExecutor.setApiClientId(apiClientID.trim());
        apiExecutor.setApiSecret(apiSecret.trim());
        apiExecutor.setIsS2S(isS2S);
        setUserID(context, userID);
    }

    public void setUserID(Context context, String userID) {
        PreferenceUtil.INSTANCE.init(context);
        String deviceId = PreferenceUtil.INSTANCE.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            PreferenceUtil.INSTANCE.saveDeviceId(deviceId);
        }
        apiExecutor.setDeviceId(deviceId);
        if (TextUtils.isEmpty(userID)) {
            apiExecutor.setUserId(deviceId);
        } else {
            apiExecutor.setUserId(userID);
        }
    }

    public void addCallback(InBrainCallback callback) {
        apiExecutor.addCallback(callback);
    }

    public void removeCallback(InBrainCallback callback) {
        apiExecutor.removeCallback(callback);
    }

    /**
     * @deprecated(forRemoval=true) This method has been deprecated.
     * Please build a habit to set sessionID and dataOptions separately using {@link #setSessionId(String)} and {@link #setDataOptions(HashMap)}
     */
    @Deprecated
    public void setInBrainValuesFor(String sessionID, HashMap<String, String> dataOptions) {
        this.sessionUid = sessionID;
        this.dataOptions = dataOptions;
    }

    public void setSessionId(String sessionID) {
        this.sessionUid = sessionID;
    }

    public String getSessionId() {
        return this.sessionUid;
    }

    public void setDataOptions(HashMap<String, String> dataOptions) {
        this.dataOptions = dataOptions;
    }

    public HashMap<String, String> getDataOptions() {
        return this.dataOptions;
    }

    /**
     * @deprecated(forRemoval=true) This method has been deprecated.
     */
    @Deprecated
    public void setLanguage(String language) {
        this.language = language;
        this.langManuallySet = true;
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

    private boolean canStartSurveys(Context context, final StartSurveysCallback callback) {
        if (!apiExecutor.checkForInit()) {
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

    /**
     * Opens survey wall
     */
    public void showSurveys(Context context, final StartSurveysCallback callback) {
        if (!canStartSurveys(context, callback)) {
            return;
        }

        prepareConfig(context);

        try {
            SurveysActivity.start(context, APIExecutor.stagingMode, apiExecutor.getApiClientId(), apiExecutor.getApiSecret(), apiExecutor.getIsS2S(),
                    sessionUid, apiExecutor.getUserId(), apiExecutor.getDeviceId(), dataOptions, language, title, toolbarColor,
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
            SurveysActivity.start(context, APIExecutor.stagingMode, apiExecutor.getApiClientId(), apiExecutor.getApiSecret(), apiExecutor.getIsS2S(),
                    sessionUid, apiExecutor.getUserId(), apiExecutor.getDeviceId(), surveyId, searchId, dataOptions, language, title, toolbarColor,
                    backButtonColor, titleColor, statusBarColor, enableToolbarElevation, lightStatusBarIcons);
            handler.post(callback::onSuccess);
        } catch (final Exception ex) {
            handler.post(() -> callback.onFail("Failed to start SDK:" + ex));
        }
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
            toolbarColor = context.getResources().getColor(R.color.azure);
        }

        if (backButtonColor == 0) {
            backButtonColor = context.getResources().getColor(R.color.main_text);
        }

        if (titleColor == 0) {
            titleColor = context.getResources().getColor(R.color.main_text);
        }

        if (statusBarColor == 0) {
            statusBarColor = context.getResources().getColor(R.color.azure);
        }

        if (lightStatusBarIcons == null) {
            lightStatusBarIcons = false;
        }

        if (enableToolbarElevation == null) {
            enableToolbarElevation = false;
        }

        if (TextUtils.isEmpty(title)) {
            title = context.getResources().getString(R.string.inbrain_surveys);
        }
    }

    /**
     * Requests rewards manually. Returns result through global callback set in setListener().
     */
    public void getRewards() {
        getRewards(null);
    }

    /**
     * Requests rewards manually.
     *
     * @see InBrainCallback
     */
    public void getRewards(final GetRewardsCallback callback) {
        apiExecutor.execute(RequestType.GET_REWARDS, true, callback);
    }

    /**
     * Confirms rewards manually.
     *
     * @param rewards list of rewards which need to be confirmed
     */
    public void confirmRewards(final List<Reward> rewards) {
        Set<Long> rewardsIds = getRewardsIds(rewards);
        confirmRewardsById(rewardsIds);
    }

    /**
     * Confirms rewards manually.
     *
     * @param transactionIds list of transactionIds which need to be confirmed
     */
    public void confirmRewards(final long[] transactionIds) {
        Set<Long> rewardsIds = getRewardsIds(transactionIds);
        confirmRewardsById(rewardsIds);
    }

    private Set<Long> getRewardsIds(List<Reward> rewards) {
        Set<Long> rewardsIds = new HashSet<>(rewards.size());
        for (Reward reward : rewards) {
            rewardsIds.add(reward.transactionId);
        }
        return rewardsIds;
    }

    private Set<Long> getRewardsIds(long[] transactionIds) {
        Set<Long> rewardsIds = new HashSet<>(transactionIds.length);
        for (long transactionId : transactionIds) {
            rewardsIds.add(transactionId);
        }
        return rewardsIds;
    }

    private void confirmRewardsById(final Set<Long> rewardIds) {
        final Set<Long> pendingRewardIds = PreferenceUtil.INSTANCE.getPendingRewardIds();
        pendingRewardIds.addAll(rewardIds);
        PreferenceUtil.INSTANCE.savePendingRewards(pendingRewardIds);
        apiExecutor.execute(RequestType.CONFIRM_REWARDS, true, pendingRewardIds);
    }

    void onClosed(boolean byWebView, List<InBrainSurveyReward> rewards) {
        apiExecutor.onClosed(byWebView, rewards);
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(apiExecutor.getApiClientId()) || TextUtils.isEmpty(apiExecutor.getApiSecret())) {
            Log.e(Constants.LOG_TAG, "Please first call setInBrain() method!");
            return "";
        }

        return apiExecutor.getDeviceId();
    }

    public void areSurveysAvailable(final Context context, final SurveysAvailableCallback callback) {
        apiExecutor.execute(RequestType.ARE_SURVEYS_AVAILABLE, true, callback);
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
        apiExecutor.execute(RequestType.GET_NATIVE_SURVEYS, true, placeId, includeCategoryIds, excludeCategoryIds, callback);
    }

    public void getCurrencySale(final GetCurrencySaleCallback callback) {
        apiExecutor.execute(RequestType.GET_CURRENCY_SALE, true, callback);
    }
}