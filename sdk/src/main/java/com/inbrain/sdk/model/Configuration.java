package com.inbrain.sdk.model;

import android.text.TextUtils;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    final String clientId;
    final String clientSecret;
    final String appUserId;
    final String deviceId;
    final String sessionUid;
    final HashMap<String, String> dataPoints;

    public Configuration(String clientId, String clientSecret, String appUserId, String deviceId,
                         String sessionUid, HashMap<String, String> dataPoints) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.appUserId = appUserId;
        this.deviceId = deviceId;
        this.sessionUid = sessionUid;
        this.dataPoints = dataPoints;
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
        if (!TextUtils.isEmpty(sessionUid)) {
            writer.name("session_uid").value(sessionUid);
        }
        if (dataPoints != null) {
            writer.name("data_points").beginObject();
            for (Map.Entry<String, String> entry : dataPoints.entrySet()) {
                writer.name(entry.getKey()).value(entry.getValue());
            }
            writer.endObject();
        }
        writer.endObject();
    }
}
