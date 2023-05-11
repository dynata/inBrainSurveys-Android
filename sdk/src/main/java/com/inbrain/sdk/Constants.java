package com.inbrain.sdk;

import android.text.TextUtils;

import com.inbrain.sdk.model.SurveyCategory;

import java.util.List;

class Constants {
    static final String STAGING_DOMAIN = "https://qa.surveyb.in";

    static final String DOMAIN = "https://www.surveyb.in";

    static final String LOG_TAG = "InBrainSDK";

    static final String INTERFACE_NAME = "androidInterface";

    static final String JS_LOG_TAG = LOG_TAG + " JS";

    static final String BASE_URL = "https://api.surveyb.in/api/v1/";

    static final String BASE_URL_V2 = "https://api.surveyb.in/api/v2/";

    static final String BASE_URL_EXTERNAL_SURVEYS = BASE_URL + "external-surveys/";

    static final String STAGING_BASE_URL = "https://inbrain-api-qa.azurewebsites.net/api/v1/";

    static final String STAGING_BASE_URL_V2 = "https://inbrain-api-qa.azurewebsites.net/api/v2/";

    static final String STAGING_BASE_URL_EXTERNAL_SURVEYS = STAGING_BASE_URL + "external-surveys/";

    static final String TOKEN_URL = "https://auth.surveyb.in/connect/token";

    static final String STAGING_TOKEN_URL = "https://inbrain-auth-qa.azurewebsites.net/connect/token";

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

    public static String getNativeSurveysUrl(String appUserId, String deviceId,
                                             String placeId, List<SurveyCategory> includeCategoryIds, List<SurveyCategory> excludeCategoryIds) {
        String separator = "?";
        StringBuilder sb = new StringBuilder("external-panelist/");
        sb.append(appUserId)
                .append("/")
                .append(deviceId)
                .append("/native-surveys");
        if (!TextUtils.isEmpty(placeId)) {
            sb.append(separator).append("placementId=").append(placeId);
            separator = "&";
        }
        if (includeCategoryIds != null && includeCategoryIds.size() > 0) {
            sb.append(separator).append("categoryIds=").append(includeCategoryIds.get(0).getId());
            for (int i = 1; i < includeCategoryIds.size(); i++) {
                sb.append(",").append(includeCategoryIds.get(i).getId());
            }
            separator = "&";
        }
        if (excludeCategoryIds != null && excludeCategoryIds.size() > 0) {
            sb.append(separator).append("excludeCategoryIds=").append(excludeCategoryIds.get(0).getId());
            for (int i = 1; i < excludeCategoryIds.size(); i++) {
                sb.append(",").append(excludeCategoryIds.get(i).getId());
            }
        }
        return sb.toString();
    }

    public static String getCurrencySaleUrl() {
        return "external-panelist/publisher/currency-sale";
    }
}