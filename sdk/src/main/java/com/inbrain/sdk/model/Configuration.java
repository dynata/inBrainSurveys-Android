package com.inbrain.sdk.model;

import android.text.TextUtils;
import android.util.JsonWriter;

import com.inbrain.sdk.InBrain.WallOption;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    final String clientId;
    final String clientSecret;
    final String appUserId;
    final String deviceId;
    final String surveyId;
    final String searchId;
    final String sessionUid;
    final HashMap<String, String> dataPoints;
    final String language;
    final WallOption option;

    public Configuration(String clientId, String clientSecret, String appUserId, String deviceId,
                         String surveyId, String searchId, String sessionUid, HashMap<String, String> dataPoints,
                         String language, WallOption option) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.appUserId = appUserId;
        this.deviceId = deviceId;
        this.surveyId = surveyId;
        this.searchId = searchId;
        this.sessionUid = sessionUid;
        this.dataPoints = dataPoints;
        this.language = language;
        this.option = option;
    }

    public String toJson() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writeConfiguration(writer);
        writer.close();
        return stringWriter.toString();
    }

    private void writeConfiguration(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("client_id").value(clientId);
        writer.name("client_secret").value(clientSecret);
        writer.name("app_uid").value(appUserId);
        writer.name("device_id").value(deviceId);
        if (!TextUtils.isEmpty(surveyId)) {
            writer.name("survey_id").value(surveyId);
        }
        if (!TextUtils.isEmpty(searchId)) {
            writer.name("search_id").value(searchId);
        }
        if (!TextUtils.isEmpty(sessionUid)) {
            writer.name("session_uid").value(sessionUid);
        }
        if (dataPoints != null) {
            writer.name("data_points").beginArray();
            for (Map.Entry<String, String> entry : dataPoints.entrySet()) {
                writer.beginObject();
                writer.name(entry.getKey()).value(entry.getValue());
                writer.endObject();
            }
            writer.endArray();
        }
        if (!TextUtils.isEmpty(language)) {
            writer.name("language").value(language);
        }
        switch (option) {
            case ALL:
                writer.name("surveys_enabled").value(true);
                writer.name("offers_enabled").value(true);
                break;
            case SURVEYS:
                writer.name("offers_enabled").value(false);
                break;
            case OFFERS:
                writer.name("surveys_enabled").value(false);
                break;
        }

        writer.endObject();
    }
}
