package com.inbrain.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.inbrain.sdk.Constants.DOMAIN;
import static com.inbrain.sdk.Constants.INTERFACE_NAME;
import static com.inbrain.sdk.Constants.JS_LOG_TAG;
import static com.inbrain.sdk.Constants.LOG_TAG;

public class SurveysActivity extends Activity {
    private static final String EXTRA_CLIENT_ID = "368234109";
    private static final String EXTRA_CLIENT_SECRET = "6388991";
    private static final String EXTRA_APP_USER_ID = "29678234";
    private static final String EXTRA_DEVICE_ID = "97497286";
    private static final int UPDATE_REWARDS_DELAY_MS = 10000;

    private WebView webView;

    private String clientId;
    private String clientSecret;
    private String appUserId;
    private String deviceId;

    private boolean surveyActive;
    private Handler updateRewardsHandler = new Handler();

    public static void start(Context context, String clientId, String clientSecret, String appUserId, String deviceId) {
        Intent intent = new Intent(context, SurveysActivity.class);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        intent.putExtra(EXTRA_CLIENT_SECRET, clientSecret);
        intent.putExtra(EXTRA_APP_USER_ID, appUserId);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        context.startActivity(intent);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveys);

        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.darker_background));
        }

        clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        clientSecret = getIntent().getStringExtra(EXTRA_CLIENT_SECRET);
        appUserId = getIntent().getStringExtra(EXTRA_APP_USER_ID);
        deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);

        webView = findViewById(R.id.web_view);
        View backView = findViewById(R.id.back_image);

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (surveyActive) showAbortSurveyDialog();
                else finish();
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
                if (url.equals(Constants.CONFIGURATION_URL)) {
                    String newUrl = "javascript:" +
                            "setConfiguration({" +
                            "\"client_id\":\"" + clientId + "\"," +
                            "\"client_secret\":\"" + clientSecret + "\"," +
                            "\"device_id\":\"" + deviceId + "\"," +
                            "\"app_uid\":\"" + appUserId + "\"" +
                            "});";
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG_TAG, "URL: " + newUrl);
                    }
                    webView.loadUrl(newUrl);
                }
            }
        });
        webView.addJavascriptInterface(new SurveyJavaScriptInterface(), INTERFACE_NAME);

        webView.clearHistory();

        webView.loadUrl(Constants.CONFIGURATION_URL);

        updateRewards(false);
    }

    private void setSurveyActive(boolean surveyActive) {
        if (this.surveyActive == surveyActive) return;
        this.surveyActive = surveyActive;
    }

    private void showAbortSurveyDialog() {
        new AlertDialog.Builder(this)
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
        // ignore because we don't have agreed navigation in web view and we don't want to leave immediately
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
        InBrain.getInstance().onAdClosed();
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
        public void toggleNativeButtons(boolean toggle) {
            if (BuildConfig.DEBUG) Log.i(JS_LOG_TAG, "toggleNativeButtons:" + toggle);
            setSurveyActive(toggle);
        }
    }
}