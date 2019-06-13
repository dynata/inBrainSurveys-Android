package com.inbrain.sdk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.inbrain.sdk.BuildConfig;
import com.inbrain.sdk.Constants;
import com.inbrain.sdk.R;

public class SurveysActivity extends Activity {

    private static final String EXTRA_CLIENT_ID = "368234109";
    private static final String EXTRA_CLIENT_SECRET = "6388991";
    private static final String EXTRA_APP_USER_ID = "29678234";
    private static final String EXTRA_DEVICE_ID = "97497286";

    private WebView webView;

    private String clientId;
    private String clientSecret;
    private String appUserId;
    private String deviceId;

    public static void start(Context context, String clientId, String clientSecret, String appUserId, String deviceId) {
        Intent intent = new Intent(context, SurveysActivity.class);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        intent.putExtra(EXTRA_CLIENT_SECRET, clientSecret);
        intent.putExtra(EXTRA_APP_USER_ID, appUserId);
        intent.putExtra(EXTRA_DEVICE_ID, deviceId);
        context.startActivity(intent);
    }

    @SuppressLint("SetJavaScriptEnabled")
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
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (BuildConfig.DEBUG) {
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.i(Constants.LOG_TAG, consoleMessage.message());
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
                            "'client_id':'" + clientId + "'," +
                            "'client_secret':'" + clientSecret + "'," +
                            "'device_id':'" + deviceId + "'," +
                            "'app_uid':'" + appUserId + "'" +
                            "});";
                    if (BuildConfig.DEBUG) {
                        Log.i(Constants.LOG_TAG, "URL: " + newUrl);
                    }
                    webView.loadUrl(newUrl);
                }
            }
        });
        webView.loadUrl(Constants.CONFIGURATION_URL);
    }

    public void onExitClick(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        // ignore because we don't have agreed navigation in web view and we don't want to leave immediately
    }

}
