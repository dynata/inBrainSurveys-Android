package com.inbrain.sdk;

class Constants {
    static final String DOMAIN = "https://www.surveyb.in";

    static final String CONFIGURATION_URL = DOMAIN + "/configuration";

    static final String LOG_TAG = "InBrainSDK";

    static final String INTERFACE_NAME = "androidInterface";

    static final String JS_LOG_TAG = LOG_TAG + " JS";

    static final String BASE_URL = "https://api.surveyb.in/api/v1/external-surveys/";

    static final String TOKEN_URL = "https://auth.surveyb.in/connect/token";

    static final String TOKEN_SCOPE = "inbrain-api:integration";

    static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    static final String REWARDS = "rewards";

    static final String CONFIRM_TRANSACTIONS = "confirm-transactions";

    static final int REQUEST_TIMEOUT_MS = 30000;
}