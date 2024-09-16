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
import androidx.annotation.RequiresApi

open class IntentHandlerWebViewClient : WebViewClient() {
    open fun intentOpened() {}

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        return checkForOverrideUrlLoading(view!!, url)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return checkForOverrideUrlLoading(view, url)
    }

    private fun checkForOverrideUrlLoading(view: WebView, url: String?): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }

        if (url.contains("gotooffer.inbrain")) {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            view.context.startActivity(intent)
            intentOpened()

            return true
        }

        return false
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError) {
        super.onReceivedError(view, request, error)
        if (BuildConfig.DEBUG) {
            Log.w(Constants.LOG_TAG, "error for main frame:" + error.description)
        }
    }

    @Deprecated("Deprecated in Java")
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