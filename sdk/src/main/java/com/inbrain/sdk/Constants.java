package com.inbrain.sdk;

public class Constants {
    static final String DOMAIN = "https://inbrainwebview-staging.azureedge.net";

    static final String CONFIGURATION_URL = DOMAIN + "/configuration";

    static final String LOG_TAG = "InBrainSDK";

    static final String INTERFACE_NAME = "androidInterface";

    static final String JS_LOG_TAG = LOG_TAG + " JS";

    public static final String BASE_URL = " https://inbrain-api-staging.azurewebsites.net/api/v1/external-surveys/";
    //public static final String BASE_URL = " https://inbrain-api.azurewebsites.net/api/v1/external-surveys/"; // PRODUCTION

    public static final String TOKEN_URL = "https://inbrain-auth-staging.azurewebsites.net/connect/token";
    //public static final String TOKEN_URL = "https://inbrain-auth.azurewebsites.net/connect/token"; // PRODUCTION

    public static final String TOKEN_SCOPE = "inbrain-api:integration";

    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    public static final String REWARDS = "rewards";

    public static final String CONFIRM_TRANSACTIONS = "confirm-transactions";
}