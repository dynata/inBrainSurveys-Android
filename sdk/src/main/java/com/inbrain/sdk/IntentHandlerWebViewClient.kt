package com.inbrain.sdk

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

open class IntentHandlerWebViewClient : WebViewClient() {
    // TODO: Add modern version of the function lateer
    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        if (url == null || url.startsWith("http://") || url.startsWith("https://")) {
            return false
        }

        val uri = Uri.parse(url)

        if (url.startsWith("market://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                view.context.startActivity(intent)
            } catch (e: Exception) {
                val finalUri = "https://play.google.com/store/apps/" + uri.host + "?" + uri.query
                openURLAsIntent(finalUri, view)
            }
            return true
        }

        if (url.startsWith("intent://")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                view.context.startActivity(intent)
            } catch (e: Exception) {
                val finalUri = "https://play.google.com/store/apps/details?" + uri.query
                openURLAsIntent(finalUri, view)
            }
            return true
        }

        return false
    }

    private fun openURLAsIntent(url: String, webView: WebView) {
        try {
            // Try to open the link in the system's browser
            val uri = Uri.parse(url)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            webView.context.startActivity(browserIntent)
        } catch (error: Exception) {
            // Try to load the link at WebView if the system browser attempt failed;
            webView.loadUrl(url)

            if (BuildConfig.DEBUG) {
                Log.w(Constants.LOG_TAG, "Unable to start intent: " + error.message)
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError ) {
        super.onReceivedError(view, request, error)
        if (BuildConfig.DEBUG) {
            Log.w(Constants.LOG_TAG, "error for main frame:" + error.description)
        }
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        if (BuildConfig.DEBUG) {
            Log.w(
                Constants.LOG_TAG,
                "old api error for main frame:$errorCode, $description"
            )
        }
    }
}