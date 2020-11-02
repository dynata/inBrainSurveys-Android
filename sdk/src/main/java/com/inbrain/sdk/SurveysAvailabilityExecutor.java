package com.inbrain.sdk;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Locale;

class SurveysAvailabilityExecutor {
    void areSurveysAvailable(final Context context, final String token, final boolean stagingMode,
                             final SurveysAvailableExecutorCallback callback, final String appUserId,
                             final String deviceId) {
        if (isUserFromUs(context)) {
            callback.onSurveysAvailable(true);
            return;
        }
        new AllowedCountriesExecutor().getAllowedCountries(new AllowedCountriesExecutor.AllowedCountriesCallback() {
            @Override
            public void onCountriesAvailable(List<String> countries) {
                Locale current = context.getResources().getConfiguration().locale;
                for (String country : countries) {
                    if (current.getCountry().equalsIgnoreCase(country)) {
                        checkSurveysAvailable(token, stagingMode, appUserId, deviceId, callback);
                        return;
                    }
                }
                callback.onSurveysAvailable(false);
            }

            @Override
            public void onFailToLoadAllowedCountries(Exception ex) {
                callback.onSurveysAvailable(false);
            }
        });
    }

    private void checkSurveysAvailable(String token, boolean stagingMode, String appUserId, String deviceId,
                                       final SurveysAvailableExecutorCallback callback) {
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

    private boolean isUserFromUs(Context context) {
        Locale current = context.getResources().getConfiguration().locale;
        return current.getCountry().equals("US");
    }

    public interface SurveysAvailableExecutorCallback {
        void onSurveysAvailable(boolean available);

        void onFailToLoadSurveysAvailability(Exception ex);
    }
}
