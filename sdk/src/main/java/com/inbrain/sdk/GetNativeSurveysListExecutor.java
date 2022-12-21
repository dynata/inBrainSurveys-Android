package com.inbrain.sdk;

import android.util.Log;

import com.inbrain.sdk.model.InBrainNativeSurvey;
import com.inbrain.sdk.model.SurveyCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class GetNativeSurveysListExecutor {
    void getNativeSurveysList(final String token, final boolean stagingMode,
                              final NativeSurveysExecutorCallback callback, final String appUserId, final String deviceId,
                              final String placeId, final List<SurveyCategory> includeCategoryIds, final List<SurveyCategory> excludeCategoryIds) {
        String nativeSurveysUrl = getNativeSurveysUrl(stagingMode,
                Constants.getNativeSurveysUrl(appUserId, deviceId, placeId, includeCategoryIds, excludeCategoryIds));
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOG_TAG, "getNativeSurveysList() url: " + nativeSurveysUrl);
        }
        AuthorizedGetRequest getNativeSurveysRequest = new AuthorizedGetRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                onNativeSurveysReceived(callback, output);
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailToLoadNativeSurveysList(ex);
            }
        });
        getNativeSurveysRequest.execute(nativeSurveysUrl, token);
    }

    private String getNativeSurveysUrl(boolean stagingMode, String areSurveysAvailableUrl) {
        String baseUrl;
        if (stagingMode) {
            baseUrl = Constants.STAGING_BASE_URL_V2;
        } else {
            baseUrl = Constants.BASE_URL_V2;
        }
        return String.format("%s%s", baseUrl, areSurveysAvailableUrl);
    }

    private void onNativeSurveysReceived(NativeSurveysExecutorCallback callback, String output) {
        try {
            if (BuildConfig.DEBUG) {
                Log.d(Constants.LOG_TAG, "onNativeSurveysReceived: " + output);
            }
            callback.onNativeSurveysAvailable(parseSurveys(output));
        } catch (JSONException e) {
            callback.onFailToLoadNativeSurveysList(e);
        }
    }

    private List<InBrainNativeSurvey> parseSurveys(String data) throws JSONException {
        List<InBrainNativeSurvey> surveys = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(data);
        int searchId = jsonObject.getInt("searchId");
        JSONArray jsonarray = (JSONArray) jsonObject.get("surveys");
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            String id = jsonobject.getString("id");
            long rank = jsonobject.getLong("rank");
            long time = jsonobject.getLong("time");
            float value = (float) jsonobject.getDouble("value");
            boolean currencySale = jsonobject.getBoolean("currencySale");
            float multiplier = (float) jsonobject.getDouble("multiplier");
            int conversionThreshold = jsonobject.getInt("conversionThreshold");
            JSONArray idsArray = null;
            Object categoryIdsObj = jsonobject.get("categoryIds");
            if (categoryIdsObj instanceof JSONArray) {
                idsArray = (JSONArray) categoryIdsObj;
            }
            List<SurveyCategory> categories = new ArrayList<>();
            if (idsArray != null) {
                for (int j = 0; j < idsArray.length(); j++) {
                    categories.add(SurveyCategory.fromId(idsArray.getInt(j)));
                }
            }
            surveys.add(new InBrainNativeSurvey(id, rank, time, value, currencySale, multiplier, conversionThreshold, String.valueOf(searchId), categories));
        }
        return surveys;
    }

    public interface NativeSurveysExecutorCallback {
        void onNativeSurveysAvailable(List<InBrainNativeSurvey> surveys);

        void onFailToLoadNativeSurveysList(Exception ex);
    }
}
