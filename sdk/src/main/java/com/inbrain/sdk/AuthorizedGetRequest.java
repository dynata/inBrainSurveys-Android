package com.inbrain.sdk;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.inbrain.sdk.Constants.REQUEST_TIMEOUT_MS;

class AuthorizedGetRequest extends AsyncTask<String, Void, String> {
    static final int RESPONSE_CODE_UNAUTHORIZED = 401;
    static final String RESPONSE_MESSAGE_UNAUTHORIZED = "Unauthorized";
    private final AsyncResponse callback;

    AuthorizedGetRequest(AsyncResponse callback) {
        super();
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
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else if (con.getResponseCode() == RESPONSE_CODE_UNAUTHORIZED
                    & con.getResponseMessage().equals(RESPONSE_MESSAGE_UNAUTHORIZED)) {
                callback.onError(new TokenExpiredException(con.getResponseMessage()));
                return null;
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
        if (s != null) {
            callback.processFinish(s);
        }
    }
}