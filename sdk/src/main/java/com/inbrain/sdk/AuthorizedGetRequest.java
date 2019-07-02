package com.inbrain.sdk;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.inbrain.sdk.Constants.REQUEST_TIMEOUT_MS;

class AuthorizedGetRequest extends AsyncTask<String, Void, String> {
    private final AsyncResponse callback;

    AuthorizedGetRequest(AsyncResponse callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String token = params[1];
        try {
            URL obj = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setReadTimeout(REQUEST_TIMEOUT_MS);
            con.setConnectTimeout(REQUEST_TIMEOUT_MS);
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + token);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                callback.onError(new IllegalStateException(con.getResponseMessage()));
                return null;
            }
        } catch (Exception ex) {
            callback.onError(ex);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!TextUtils.isEmpty(s)) callback.processFinish(s);
    }
}