package com.inbrain.sdk;

class Constants {
    static final String DOMAIN = "https://inbrainwebview-staging.azureedge.net";

    static final String CONFIGURATION_URL = DOMAIN + "/configuration";

    static final String LOG_TAG = "InBrainSDK";

    static final String INTERFACE_NAME = "androidInterface";

    static final String JS_LOG_TAG = LOG_TAG + " JS";

    static final String BASE_URL = " https://inbrain-api-staging.azurewebsites.net/api/v1/external-surveys/";
    //public static final String BASE_URL = " https://inbrain-api.azurewebsites.net/api/v1/external-surveys/"; // PRODUCTION

    static final String TOKEN_URL = "https://inbrain-auth-staging.azurewebsites.net/connect/token";
    //public static final String TOKEN_URL = "https://inbrain-auth.azurewebsites.net/connect/token"; // PRODUCTION

    static final String TOKEN_SCOPE = "inbrain-api:integration";

    static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    static final String REWARDS = "rewards";

    static final String CONFIRM_TRANSACTIONS = "confirm-transactions";

    static final int REQUEST_TIMEOUT_MS = 10000;
}