package com.inbrain.sdk;

import static com.inbrain.sdk.Constants.DOMAIN;
import static com.inbrain.sdk.Constants.INTERFACE_NAME;
import static com.inbrain.sdk.Constants.JS_LOG_TAG;
import static com.inbrain.sdk.Constants.LOG_TAG;
import static com.inbrain.sdk.Constants.STAGING_DOMAIN;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inbrain.sdk.model.Configuration;

import java.io.IOException;
import java.util.HashMap;

public class SurveysActivity extends Activity {
    private static final String EXTRA_STAGING_MODE = "15213412";
    private static final String EXTRA_CLIENT_ID = "368234109";
    private static final String EXTRA_CLIENT_SECRET = "6388991";
    private static final String EXTRA_SESSION_UID = "64548792";
    private static final String EXTRA_DATA_POINTS = "15895132";
    private static final String EXTRA_APP_USER_ID = "29678234";
    private static final String EXTRA_DEVICE_ID = "97497286";
    private static final String EXTRA_SURVEY_ID = "56238743";
    private static final String EXTRA_PLACE_ID = "56238744";
    private static final String EXTRA_S2S = "71263886";
    private static final String EXTRA_LANGUAGE = "51211232";
    private static final String EXTRA_TOOLBAR_TEXT = "64587132";
    private static final String EXTRA_TOOLBAR_COLOR = "67584922";
    private static final String EXTRA_BACK_BUTTON_COLOR = "13645898";
    private static final String EXTRA_TITLE_COLOR = "12343214";
    private static final String EXTRA_STATUS_BAR_COLOR = "89732498";
    private static final String EXTRA_ENABLE_ELEVATION = "46782388";
    private static final String EXTRA_LIGHT_STATUS_BAR_ICONS = "81237412";
    private static final int UPDATE_REWARDS_DELAY_MS = 10000;

    private ViewGroup webViewsContainer;
    private WebView mainWebView;
    private WebView secondaryWebView;
    private ProgressBar loadingIndicator;
    private ImageView backImageView;
    private TextView toolbarTextView;

    private String configurationUrl;

    private boolean stagingMode;
    private String clientId;
    private String clientSecret;
    private boolean isS2S;
    private String sessionUid;
    private HashMap<String, String> dataPoints;
    private String appUserId;
    private String deviceId;
    private String surveyId;
    private String placeId;
    private String language;

    private boolean surveyActive;
    private final Handler updateRewardsHandler = new Handler();
    private AlertDialog inBrainErrorDialog;
    private AlertDialog abortSurveyDialog;
    private boolean finishedFromPage;
    private NetworkBroadcastReceiver networkStateReceiver;
    private boolean connectionLost;

    static void start(Context context, boolean stagingMode, String clientId, String clientSecret,
                      boolean isS2S, String sessionUid, String appUserId, String deviceId,
                      HashMap<String, String> dataPoints, String language, String title,
                      int toolbarColor, int backButtonColor, int titleColor, int statusBarColor,
                      boolean enableElevation, boolean lightStatusBarColor) {
        Intent startingIntent = getLaunchingIntent(context, stagingMode, clientId, clientSecret,
                isS2S, sessionUid, appUserId, deviceId, dataPoints, language, title, toolbarColor,
                backButtonColor, titleColor, statusBarColor, enableElevation, lightStatusBarColor);
        context.startActivity(startingIntent);
    }

    public static void start(Context context, boolean stagingMode, String clientId, String clientSecret,
                             boolean isS2S, String sessionUid, String appUserId, String deviceId,
                             String surveyId, String placeId, HashMap<String, String> dataPoints, String language,
                             String title, int toolbarColor, int backButtonColor, int titleColor,
                             int statusBarColor, boolean enableElevation, boolean lightStatusBarColor) {
        Intent startingIntent = getLaunchingIntent(context, stagingMode, clientId, clientSecret,
                isS2S, sessionUid, appUserId, deviceId, dataPoints, language, title, toolbarColor,
                backButtonColor, titleColor, statusBarColor, enableElevation, lightStatusBarColor);
        startingIntent.putExtra(EXTRA_SURVEY_ID, surveyId);
        if (!TextUtils.isEmpty(placeId)) {
            startingIntent.putExtra(EXTRA_PLACE_ID, placeId);
        }
        context.startActivity(startingIntent);
    }

    private static Intent getLaunchingIntent(Context context, boolean stagingMode, String clientId,
                                             String clientSecret, boolean isS2S, String sessionUid,
                                             String appUserId, String deviceId,
                                             HashMap<String, String> dataPoints, String language,
                                             String title, int toolbarColor, int backButtonColor,
                                             int titleColor, int statusBarColor, boolean enableElevation,
                                             boolean lightStatusBarColor) {
        Intent intent = new Intent(context, SurveysActivity.class);
        intent.putExtra(EXTRA_STAGING_MODE, stagingMode);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        intent.putExtra(EXTRA_CLIENT_SECRET, clientSecret);
        intent.putExtra(EXTRA_S2S, isS2S);
        intent.putExtra(EXTRA_SESSION_UID, sessionUid);
        intent.putExtra(EXTRA_DATA_POINTS, dataPoints);
        intent.putExtra(EXTRA_APP_USER_ID, appUserId);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        intent.putExtra(EXTRA_TOOLBAR_TEXT, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        intent.putExtra(EXTRA_BACK_BUTTON_COLOR, backButtonColor);
        intent.putExtra(EXTRA_TITLE_COLOR, titleColor);
        intent.putExtra(EXTRA_STATUS_BAR_COLOR, statusBarColor);
        intent.putExtra(EXTRA_ENABLE_ELEVATION, enableElevation);
        intent.putExtra(EXTRA_LIGHT_STATUS_BAR_ICONS, lightStatusBarColor);
        if (!TextUtils.isEmpty(language)) {
            intent.putExtra(EXTRA_LANGUAGE, language);
        }

        return intent;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.InBrainTheme);
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_surveys);

        backImageView = findViewById(R.id.back_image);
        toolbarTextView = findViewById(R.id.toolbar_title_text);

        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));

        Intent intent = getIntent();
        stagingMode = intent.getBooleanExtra(EXTRA_STAGING_MODE, false);
        clientId = intent.getStringExtra(EXTRA_CLIENT_ID);
        clientSecret = intent.getStringExtra(EXTRA_CLIENT_SECRET);
        isS2S = intent.getBooleanExtra(EXTRA_S2S, false);
        sessionUid = intent.getStringExtra(EXTRA_SESSION_UID);
        dataPoints = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA_POINTS);
        appUserId = intent.getStringExtra(EXTRA_APP_USER_ID);
        deviceId = intent.getStringExtra(EXTRA_DEVICE_ID);
        surveyId = intent.getStringExtra(EXTRA_SURVEY_ID);
        if (intent.hasExtra(EXTRA_PLACE_ID)) {
            placeId = intent.getStringExtra(EXTRA_PLACE_ID);
        }

        configurationUrl = String.format("%s/configuration", stagingMode ? STAGING_DOMAIN : DOMAIN);

        if (intent.hasExtra(EXTRA_LANGUAGE)) {
            language = intent.getStringExtra(EXTRA_LANGUAGE);
        }

        if (intent.hasExtra(EXTRA_TOOLBAR_TEXT)) {
            toolbarTextView.setText(intent.getStringExtra(EXTRA_TOOLBAR_TEXT));
        }

        if (intent.hasExtra(EXTRA_TOOLBAR_COLOR)) {
            int color = intent.getIntExtra(EXTRA_TOOLBAR_COLOR, 0);
            findViewById(R.id.toolbar).setBackgroundColor(color);
        }

        if (intent.hasExtra(EXTRA_BACK_BUTTON_COLOR)) {
            int color = intent.getIntExtra(EXTRA_BACK_BUTTON_COLOR, getResources().getColor(R.color.main_text));
            backImageView.setColorFilter(color);
        }

        if (intent.hasExtra(EXTRA_TITLE_COLOR)) {
            int color = intent.getIntExtra(EXTRA_TITLE_COLOR, getResources().getColor(R.color.main_text));
            toolbarTextView.setTextColor(color);
        }

        if (intent.hasExtra(EXTRA_STATUS_BAR_COLOR)) {
            int color = intent.getIntExtra(EXTRA_STATUS_BAR_COLOR, getResources().getColor(R.color.main_text));
            setStatusBarColor(color);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (intent.hasExtra(EXTRA_ENABLE_ELEVATION)) {
                boolean enableElevation = intent.getBooleanExtra(EXTRA_ENABLE_ELEVATION, false);
                if (enableElevation) {
                    findViewById(R.id.toolbar).setElevation(getResources().getDimension(R.dimen.elevation));
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (intent.hasExtra(EXTRA_LIGHT_STATUS_BAR_ICONS)) {
                boolean lightStatusBarIcons = intent.getBooleanExtra(EXTRA_LIGHT_STATUS_BAR_ICONS, false);
                if (!lightStatusBarIcons) {
                    int flags = getWindow().getDecorView().getSystemUiVisibility();
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }
        }

        loadingIndicator = findViewById(R.id.progress_loader);
        loadingIndicator.setVisibility(View.VISIBLE);

        webViewsContainer = findViewById(R.id.web_views_container);
        mainWebView = findViewById(R.id.web_view);
        networkStateReceiver = new NetworkBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, intentFilter);

        backImageView.setOnClickListener(v -> handleBackButton(false));

        setupWebView(mainWebView);
        mainWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (view.getProgress() < 100) {
                    return;
                }

                loadingIndicator.setVisibility(View.GONE);

                if (!url.equals(configurationUrl)) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "Entering configuration loading");
                }
                setConfiguration();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    if (BuildConfig.DEBUG) {
                        Log.w(LOG_TAG, "error for main frame:" + error.getDescription());
                    }
                    onFailedToLoadInBrainSurveys();
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.w(LOG_TAG, "error for secondary frame:" + error.getDescription());
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "old api error for main frame:" + errorCode + ", " + description);
                }
                onFailedToLoadInBrainSurveys();
            }
        });
        mainWebView.addJavascriptInterface(new SurveyJavaScriptInterface(), INTERFACE_NAME);

        mainWebView.clearHistory();

        mainWebView.loadUrl(configurationUrl);

        updateRewards(false);
    }

    private boolean onCreateWebviewWindow(WebView view) {
        WebView.HitTestResult result = view.getHitTestResult();
        String url = result.getExtra();
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "onCreateWebviewWindow with url: " + url);
        }
        if (secondaryWebView == null) {
            secondaryWebView = new WebView(this);
            setupWebView(secondaryWebView);
            secondaryWebView.setWebViewClient(new WebViewClient());
            webViewsContainer.addView(secondaryWebView);
        }
        secondaryWebView.loadUrl(url);
        return true;
    }

    private void setConfiguration() {
        try {
            String newUrl = getConfigurationUrl();
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG, "Configuration URL: " + newUrl);
            }
            mainWebView.loadUrl(newUrl);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            onFailedToLoadInBrainSurveys();
        }
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    private String getConfigurationUrl() throws IOException {
        Configuration configuration = new Configuration(clientId, clientSecret, appUserId, deviceId,
                surveyId, placeId, sessionUid, dataPoints, language);
        return String.format("javascript:setConfiguration(%s);", configuration.toJson());
    }

    private void setSurveyActive(final boolean surveyActive) {
        this.surveyActive = surveyActive;
        runOnUiThread(() -> {
            if (surveyActive) setToolbarVisible(true);
            toolbarTextView.setVisibility(surveyActive ? View.GONE : View.VISIBLE);
        });
    }

    private void setToolbarVisible(final boolean visible) {
        runOnUiThread(() -> {
            backImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
            toolbarTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        });
    }

    private void showAbortSurveyDialog() {
        if (abortSurveyDialog != null && abortSurveyDialog.isShowing()) {
            return;
        }
        abortSurveyDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dont_abandon_the_survey_title)
                .setMessage(getString(R.string.dont_abandon_the_survey_message))
                .setPositiveButton(R.string.abort_survey, (dialog, which) -> abortSurvey())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void abortSurvey() {
        setSurveyActive(false);
        String url = String.format("%s/feedback", stagingMode ? STAGING_DOMAIN : DOMAIN);
        mainWebView.loadUrl(url);
    }

    private void updateRewards(boolean withDelay) {
        if (isS2S) {
            return;
        }
        if (withDelay) {
            updateRewardsHandler.postDelayed(() -> InBrain.getInstance().getRewards(), UPDATE_REWARDS_DELAY_MS);
        } else {
            InBrain.getInstance().getRewards();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackButton(true);
    }

    private void handleBackButton(boolean hardware) {
        if (secondaryWebView != null) {
            webViewsContainer.removeView(secondaryWebView);
            destroyWebView(secondaryWebView);
            secondaryWebView = null;
            return;
        }
        if (surveyActive) {
            if (hardware) {
                return;
            } else {
                showAbortSurveyDialog();
            }
        } else {
            finish();
        }
    }

    private void onFailedToLoadInBrainSurveys() {
        mainWebView.setVisibility(View.INVISIBLE);
        showInBrainErrorDialog();
    }

    private void showInBrainErrorDialog() {
        if (inBrainErrorDialog != null && inBrainErrorDialog.isShowing()) {
            return;
        }
        inBrainErrorDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.error_inbrain_unavailable_title)
                .setMessage(getString(R.string.error_inbrain_unavailable_message))
                .setPositiveButton(R.string.quit, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView) {
        webView.setLongClickable(false);
        webView.setOnLongClickListener(v -> true);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);

        if (BuildConfig.DEBUG) {
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.i(LOG_TAG, consoleMessage.message());
                    return true;
                }

                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                                              Message resultMsg) {
                    return onCreateWebviewWindow(view);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        } else {
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                                              Message resultMsg) {
                    return onCreateWebviewWindow(view);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(networkStateReceiver);
        updateRewards(true);
        if (secondaryWebView != null) {
            destroyWebView(secondaryWebView);
        }
        mainWebView.removeJavascriptInterface(INTERFACE_NAME);
        destroyWebView(mainWebView);
        super.onDestroy();
        InBrain.getInstance().onClosed(finishedFromPage);
    }

    private void destroyWebView(WebView webView) {
        webView.setWebViewClient(null);
        webView.clearView();
        webView.freeMemory();
        webView.removeAllViews();
        webView.destroy();
    }

    private class SurveyJavaScriptInterface {
        @JavascriptInterface
        public void surveyOpened() {
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "surveyOpened");
            setSurveyActive(true);
        }

        @JavascriptInterface
        public void surveyClosed() {
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "surveyClosed");
            setSurveyActive(false);
            updateRewards(true);
        }

        @JavascriptInterface
        public void dismissWebView() {
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "dismissWebView");
            onSurveysClosed();
        }

        @JavascriptInterface
        public void nativeSurveyClosed() {
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "nativeSurveyClosed");
            onSurveysClosed();
        }

        private void onSurveysClosed() {
            finishedFromPage = true;
            finish();
        }
    }

    private class NetworkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
                if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                    if (connectionLost) {
                        connectionLost = false;
                      /*  reset configuration in case network error during loading to avoid infinite blue hud
                        calling setConfiguration on any other urls is not going to be a problem because it's not defined anywhere other than /configuration url */
                        setConfiguration();
                    }
                } else if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    connectionLost = true;
                    if (BuildConfig.DEBUG) Log.w(LOG_TAG, "There's no network connectivity");
                }
            }
        }
    }
}