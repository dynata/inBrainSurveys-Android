package com.inbrain.sdk;

import android.text.TextUtils;

class Constants {
    static final String STAGING_DOMAIN = "https://inbrainwebview-qa.azureedge.net";

    static final String DOMAIN = "https://www.surveyb.in";

    static final String LOG_TAG = "InBrainSDK";

    static final String INTERFACE_NAME = "androidInterface";

    static final String JS_LOG_TAG = LOG_TAG + " JS";

    static final String BASE_URL = "https://api.surveyb.in/api/v1/";

    static final String BASE_URL_EXTERNAL_SURVEYS = BASE_URL + "external-surveys/";

    static final String STAGING_BASE_URL = "https://inbrain-api-qa.azurewebsites.net/api/v1/";

    static final String STAGING_BASE_URL_EXTERNAL_SURVEYS = STAGING_BASE_URL + "external-surveys/";

    static final String TOKEN_URL = "https://auth.surveyb.in/connect/token";

    static final String STAGING_TOKEN_URL = "https://inbrain-auth-qa.azurewebsites.net/connect/token";

    static final String ALLOWED_COUNTRIES_URL = "https://inbrainbackend.blob.core.windows.net/misc/allowedCountries.json";

    static final String TOKEN_SCOPE = "inbrain-api:integration";

    static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    static final String REWARDS = "rewards";

    static final String CONFIRM_TRANSACTIONS = "confirm-transactions";

    static final int REQUEST_TIMEOUT_MS = 30000;

    static final String ERROR_INVALID_CLIENT = "{\"error\":\"invalid_client\"}";

    static final int MINIMUM_WEBVIEW_VERSION_GROUP_1 = 51;

    static final int MINIMUM_WEBVIEW_VERSION_GROUP_2 = 0;

    static final int MINIMUM_WEBVIEW_VERSION_GROUP_3 = 2704;

    public static String getAreSurveysAvailableUrl(String appUserId, String deviceId) {
        return "external-panelist/" +
                appUserId +
                "/" +
                deviceId +
                "/surveys-available";
    }

    public static String getNativeSurveysUrl(String appUserId, String deviceId, String placeId) {
        StringBuilder sb = new StringBuilder("external-panelist/");
        sb.append(appUserId)
                .append("/")
                .append(deviceId)
                .append("/native-surveys");
        if (!TextUtils.isEmpty(placeId)) {
            sb.append("?placementId=").append(placeId);
        }
        return sb.toString();
    }
}