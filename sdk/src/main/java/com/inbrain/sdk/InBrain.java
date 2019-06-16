package com.inbrain.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.inbrain.sdk.callback.InBrainCallback;
import com.inbrain.sdk.callback.RewardsCallback;
import com.inbrain.sdk.callback.TokenCallback;
import com.inbrain.sdk.executor.RewardsExecutor;
import com.inbrain.sdk.executor.TokenExecutor;

import java.util.UUID;

public class InBrain {
    private static final String PREFERENCES = "SharedPreferences_inBrain25930";
    private static final String PREFERENCE_DEVICE_ID = "529826892";
    private static final String PREFERENCE_APP_USER_ID = "378294761";

    public static Context appContext = null;
    static InBrainCallback callback;

    private static String clientId = null;
    private static String clientSecret = null;
    private static String appUserId = null;
    private static String deviceId = null;

    private InBrain() {
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void init(Context context, String clientId, String clientSecret,
                            InBrainCallback callback) {
        InBrain.appContext = context.getApplicationContext();
        InBrain.clientId = clientId;
        InBrain.clientSecret = clientSecret;
        InBrain.callback = callback;
        SharedPreferences preferences = getPreferences(appContext);
        if (preferences.contains(PREFERENCE_DEVICE_ID)) {
            InBrain.deviceId = preferences.getString(PREFERENCE_DEVICE_ID, null);
        }
        if (TextUtils.isEmpty(InBrain.appUserId) && preferences.contains(PREFERENCE_APP_USER_ID)) {
            InBrain.appUserId = preferences.getString(PREFERENCE_APP_USER_ID, null);
        }
        if (TextUtils.isEmpty(InBrain.deviceId)) {
            String androidId = Settings.Secure.ANDROID_ID;
            if (!TextUtils.isEmpty(androidId)) {
                InBrain.deviceId = UUID.nameUUIDFromBytes(androidId.getBytes()).toString();
            }
        }
        if (TextUtils.isEmpty(InBrain.deviceId)) {
            InBrain.deviceId = UUID.randomUUID().toString();
        }
        preferences.edit().putString(PREFERENCE_DEVICE_ID, InBrain.deviceId).apply();
    }

    public static void setAppUserId(String id) {
        appUserId = id;
        getPreferences(appContext).edit().putString(PREFERENCE_APP_USER_ID, InBrain.appUserId).apply();
    }

    public static void showSurveys(Context context) {
        if (TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            Log.e(Constants.LOG_TAG, "Please first call init() method with client id and client secret.");
            return;
        }
        SurveysActivity.start(context, clientId, clientSecret, appUserId, deviceId);
    }

    public static void getRewards(final RewardsCallback callback) {
        TokenExecutor executor = new TokenExecutor(clientId, clientSecret);
        executor.getToken(new TokenCallback() {
            @Override
            public void onGetToken(String token) {
                RewardsExecutor rewardsExecutor = new RewardsExecutor();
                rewardsExecutor.getRewards(token, callback, appUserId, deviceId);
            }

            @Override
            public void onFailToLoadToken(Throwable t) {
                callback.onFailToLoadRewards(t);
            }
        });
    }

//    public static void confirmRewards(final List<String> rewardsIds, final ConfirmRewardsCallback callback) {
//        apiProvider.getToken(clientId, clientSecret, new TokenCallback() {
//            @Override
//            public void onGetToken(String token) {
//                apiProvider.confirmRewards(token, callback, rewardsIds);
//            }
//
//            @Override
//            public void onFailToLoadToken(Throwable t) {
//                callback.onFailToConfirmRewards(t);
//            }
//        });
//    }
}
