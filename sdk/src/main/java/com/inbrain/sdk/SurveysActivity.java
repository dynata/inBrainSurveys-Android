package com.inbrain.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.inbrain.sdk.model.Configuration;

import java.io.IOException;
import java.util.HashMap;

import static com.inbrain.sdk.Constants.DOMAIN;
import static com.inbrain.sdk.Constants.INTERFACE_NAME;
import static com.inbrain.sdk.Constants.JS_LOG_TAG;
import static com.inbrain.sdk.Constants.LOG_TAG;
import static com.inbrain.sdk.Constants.STAGING_DOMAIN;

public class SurveysActivity extends Activity {
    private static final String EXTRA_STAGING_MODE = "15213412";
    private static final String EXTRA_CLIENT_ID = "368234109";
    private static final String EXTRA_CLIENT_SECRET = "6388991";
    private static final String EXTRA_SESSION_UID = "64548792";
    private static final String EXTRA_DATA_POINTS = "15895132";
    private static final String EXTRA_APP_USER_ID = "29678234";
    private static final String EXTRA_DEVICE_ID = "97497286";
    private static final String EXTRA_LANGUAGE = "51211232";
    private static final String EXTRA_TOOLBAR_TEXT = "64587132";
    private static final String EXTRA_TOOLBAR_COLOR = "67584922";
    private static final String EXTRA_BACK_BUTTON_COLOR = "13645898";
    private static final int UPDATE_REWARDS_DELAY_MS = 10000;

    private WebView webView;
    private ImageView backImageView;
    private TextView toolbarTextView;

    private String configurationUrl;

    private String clientId;
    private String clientSecret;
    private String sessionUid;
    private HashMap<String, String> dataPoints;
    private String appUserId;
    private String deviceId;
    private String language;
    private boolean stagingMode;

    private boolean surveyActive;
    private Handler updateRewardsHandler = new Handler();
    private AlertDialog inBrainErrorDialog;
    private AlertDialog abortSurveyDialog;
    private boolean finishedFromPage;

    static void start(Context context, boolean stagingMode, String clientId, String clientSecret, String sessionUid,
                      String appUserId, String deviceId, HashMap<String, String> dataPoints,
                      String language, String title, int toolbarColor, int backButtonColor) {
        Intent intent = new Intent(context, SurveysActivity.class);
        intent.putExtra(EXTRA_STAGING_MODE, stagingMode);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        intent.putExtra(EXTRA_CLIENT_SECRET, clientSecret);
        intent.putExtra(EXTRA_SESSION_UID, sessionUid);
        intent.putExtra(EXTRA_DATA_POINTS, dataPoints);
        intent.putExtra(EXTRA_APP_USER_ID, appUserId);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        if (!TextUtils.isEmpty(language)) {
            intent.putExtra(EXTRA_LANGUAGE, language);
        }
        if (title != null) {
            intent.putExtra(EXTRA_TOOLBAR_TEXT, title);
        }
        if (toolbarColor != 0) {
            intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        }
        if (backButtonColor != 0) {
            intent.putExtra(EXTRA_BACK_BUTTON_COLOR, backButtonColor);
        }
        context.startActivity(intent);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveys);

        backImageView = findViewById(R.id.back_image);
        toolbarTextView = findViewById(R.id.toolbar_title_text);

        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));

        Intent intent = getIntent();
        stagingMode = intent.getBooleanExtra(EXTRA_STAGING_MODE, false);
        clientId = intent.getStringExtra(EXTRA_CLIENT_ID);
        clientSecret = intent.getStringExtra(EXTRA_CLIENT_SECRET);
        sessionUid = intent.getStringExtra(EXTRA_SESSION_UID);
        dataPoints = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA_POINTS);
        appUserId = intent.getStringExtra(EXTRA_APP_USER_ID);
        deviceId = intent.getStringExtra(EXTRA_DEVICE_ID);

        if (stagingMode) {
            configurationUrl = STAGING_DOMAIN + "/configuration";
        } else {
            configurationUrl = DOMAIN + "/configuration";
        }

        if (intent.hasExtra(EXTRA_LANGUAGE)) {
            language = intent.getStringExtra(EXTRA_LANGUAGE);
        }

        if (intent.hasExtra(EXTRA_TOOLBAR_TEXT)) {
            toolbarTextView.setText(intent.getStringExtra(EXTRA_TOOLBAR_TEXT));
        }

        if (intent.hasExtra(EXTRA_TOOLBAR_COLOR)) {
            int color = intent.getIntExtra(EXTRA_TOOLBAR_COLOR, 0);
            if (color == 0) {
                setStatusBarColor(getResources().getColor(R.color.background));
            } else {
                findViewById(R.id.toolbar).setBackgroundColor(color);
                setStatusBarColor(color);
            }
        } else {
            setStatusBarColor(R.color.background);
        }

        if (intent.hasExtra(EXTRA_BACK_BUTTON_COLOR)) {
            int color = intent.getIntExtra(EXTRA_BACK_BUTTON_COLOR, getResources().getColor(android.R.color.black));
            backImageView.setColorFilter(color);
            toolbarTextView.setTextColor(color);
        }

        webView = findViewById(R.id.web_view);
        webView.clearCache(true);
        webView.clearHistory();

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackButton(false);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (BuildConfig.DEBUG) {
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.i(LOG_TAG, consoleMessage.message());
                    return true;
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals(configurationUrl)) {
                    try {
                        String newUrl = getConfigurationUrl();
                        if (BuildConfig.DEBUG) {
                            Log.i(LOG_TAG, "URL: " + newUrl);
                        }
                        webView.loadUrl(newUrl);
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                        onFailedToLoadInBrainSurveys();
                    }
                }
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
        webView.addJavascriptInterface(new SurveyJavaScriptInterface(), INTERFACE_NAME);

        webView.clearHistory();

        webView.loadUrl(configurationUrl);

        updateRewards(false);
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (shouldInvertStatusBarIconsColor(color)) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.darker_background));
        }
    }

    private boolean shouldInvertStatusBarIconsColor(int color) {
        float red = Color.red(color);
        float green = Color.green(color);
        float blue = Color.red(color);
        return (red * 0.299 + green * 0.587 + blue * 0.114) > 186;
    }

    private String getConfigurationUrl() throws IOException {
        Configuration configuration = new Configuration(clientId, clientSecret, appUserId, deviceId,
                sessionUid, dataPoints, language);
        return String.format("javascript:setConfiguration(%s);", configuration.toJson());
    }

    private void setSurveyActive(final boolean surveyActive) {
        this.surveyActive = surveyActive;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (surveyActive) setToolbarVisible(true);
                toolbarTextView.setVisibility(surveyActive ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void setToolbarVisible(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                backImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
                toolbarTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showAbortSurveyDialog() {
        if (abortSurveyDialog != null && abortSurveyDialog.isShowing()) {
            return;
        }
        abortSurveyDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dont_abandon_the_survey_title)
                .setMessage(getString(R.string.dont_abandon_the_survey_message))
                .setPositiveButton(R.string.abort_survey, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateBackToSurveys();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void navigateBackToSurveys() {
        setSurveyActive(false);
        webView.loadUrl(DOMAIN);
    }

    private void updateRewards(boolean withDelay) {
        if (withDelay) {
            updateRewardsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InBrain.getInstance().getRewards();
                }
            }, UPDATE_REWARDS_DELAY_MS);
        } else {
            InBrain.getInstance().getRewards();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackButton(true);
    }

    private void handleBackButton(boolean hardware) {
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
        webView.setVisibility(View.INVISIBLE);
        showInBrainErrorDialog();
    }

    private void showInBrainErrorDialog() {
        if (inBrainErrorDialog != null && inBrainErrorDialog.isShowing()) {
            return;
        }
        inBrainErrorDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.error_inbrain_unavailable_title)
                .setMessage(getString(R.string.error_inbrain_unavailable_message))
                .setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        updateRewards(true);
        webView.removeJavascriptInterface(INTERFACE_NAME);
        webView.setWebViewClient(null);
        webView.clearView();
        webView.freeMemory();
        webView.removeAllViews();
        webView.destroy();
        super.onDestroy();
        InBrain.getInstance().onClosed(finishedFromPage);
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
        public void toggleNativeButtons(String toggle) {
            boolean visible = Boolean.parseBoolean(toggle);
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "toggleNativeButtons:" + toggle);
            setToolbarVisible(visible);
        }

        @JavascriptInterface
        public void dismissWebView() {
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "dismissWebView");
            finishedFromPage = true;
            finish();
        }
    }
}