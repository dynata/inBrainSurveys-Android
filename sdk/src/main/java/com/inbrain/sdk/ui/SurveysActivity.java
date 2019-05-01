package com.inbrain.sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private WebView webView;

    public static void start(Context context) {
        Intent intent = new Intent(context, SurveysActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveys);

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
        }
        webView.setInitialScale(1);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals(Constants.CONFIGURATION_URL)) {
                    webView.loadUrl("javascript:" +
                            "setConfiguration({" +
                            "'client_id':''," +
                            "'client_secret':''," +
                            "'device_id':'1a2b-1234-abcd'," +
                            "'app_uid':''" +
                            "});");
                }
            }
        });
        webView.loadUrl(Constants.CONFIGURATION_URL);
    }

    public void onExitClick(View view) {
        finish();
    }
}
