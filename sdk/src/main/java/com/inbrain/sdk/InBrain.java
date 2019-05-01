package com.inbrain.sdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.inbrain.sdk.ui.SurveysActivity;

public abstract class InBrain {

    static String clientId = null;
    static String clientSecret = null;
    static String appUserId = null;

    private InBrain() {}

    public static void init(String clientId, String clientSecret) {
        InBrain.clientId = clientId;
        InBrain.clientSecret = clientSecret;
    }

    public static void setAppUserId(String id) {
        appUserId = id;
    }

    public static void showSurveys(Context context) {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call init() method with client id and client secret.");
            return;
        }
        SurveysActivity.start(context);
    }

}
