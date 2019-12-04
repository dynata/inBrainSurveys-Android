package com.inbrain.sdk;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import static com.inbrain.sdk.Constants.ERROR_INVALID_CLIENT;
import static com.inbrain.sdk.Constants.GRANT_TYPE_CLIENT_CREDENTIALS;
import static com.inbrain.sdk.Constants.TOKEN_SCOPE;
import static com.inbrain.sdk.Constants.TOKEN_URL;

class TokenPostRequest extends AsyncTask<Void, Void, String> {
    private final AsyncResponse callback;
    private final String clientId;
    private final String clientSecret;

    TokenPostRequest(AsyncResponse callback, String clientId, String clientSecret) {
        this.callback = callback;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String encodedClientId = "";
        String encodedClientSecret = "";
        try {
            encodedClientId = URLEncoder.encode(clientId, "UTF-8");
            encodedClientSecret = URLEncoder.encode(clientSecret, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String urlParameters = String.format("client_id=%s&client_secret=%s&scope=%s&grant_type=%s",
                encodedClientId, encodedClientSecret, TOKEN_SCOPE, GRANT_TYPE_CLIENT_CREDENTIALS);

        try {
            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(urlParameters);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                return sb.toString();
            } else {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();
                    String errorText = sb.toString();
                    if (errorText.equals(ERROR_INVALID_CLIENT)) {
                        callback.onError(new InvalidClientException());
                        return "";
                    }
                } catch (Exception ex) {
                    if (BuildConfig.DEBUG) {
                        ex.printStackTrace();
                    }
                }
            }
            callback.onError(new IllegalStateException(conn.getResponseMessage()));
            return "";
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
            callback.onError(ex);
            return "";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!TextUtils.isEmpty(s)) callback.processFinish(s);
    }
}
