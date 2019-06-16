package com.inbrain.sdk.api;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.inbrain.sdk.callback.AsyncResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import static com.inbrain.sdk.Constants.GRANT_TYPE_CLIENT_CREDENTIALS;
import static com.inbrain.sdk.Constants.TOKEN_SCOPE;
import static com.inbrain.sdk.Constants.TOKEN_URL;

public class TokenPostRequest extends AsyncTask<Void, Void, String> {
    private final AsyncResponse callback;
    private final String clientId;
    private final String clientSecret;

    public TokenPostRequest(AsyncResponse callback, String clientId, String clientSecret) {
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
        String urlParameters = String.format("client_id=%s&client_secret=%s&scope=%s&grant_type=%s",
                clientId, clientSecret, TOKEN_SCOPE, GRANT_TYPE_CLIENT_CREDENTIALS);

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
                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            }
            callback.onError(new IllegalStateException(conn.getResponseMessage()));
            return "";
        } catch (Exception ex) {
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