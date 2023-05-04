package com.inbrain.sdk;

import android.util.Log;

class SurveysAvailabilityExecutor {
    void areSurveysAvailable(final String token, final boolean stagingMode,
                             final SurveysAvailableExecutorCallback callback, final String appUserId,
                             final String deviceId) {

        String surveysAvailableUrl = getSurveysAvailableUrl(stagingMode,
                Constants.getAreSurveysAvailableUrl(appUserId, deviceId));
        AuthorizedGetRequest areSurveysAvailableRequest = new AuthorizedGetRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                callback.onSurveysAvailable(true);
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailToLoadSurveysAvailability(ex);
            }
        });
        if (BuildConfig.DEBUG) Log.d("SurveysAvailability", "url is:" + surveysAvailableUrl);
        areSurveysAvailableRequest.execute(surveysAvailableUrl, token);
    }

    private String getSurveysAvailableUrl(boolean stagingMode, String areSurveysAvailableUrl) {
        String baseUrl;
        if (stagingMode) {
            baseUrl = Constants.STAGING_BASE_URL;
        } else {
            baseUrl = Constants.BASE_URL;
        }
        return String.format("%s%s", baseUrl, areSurveysAvailableUrl);
    }

    public interface SurveysAvailableExecutorCallback {
        void onSurveysAvailable(boolean available);

        void onFailToLoadSurveysAvailability(Exception ex);
    }
}
