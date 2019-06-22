package com.inbrain.sdk;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class AuthorizedGetRequest extends AsyncTask<String, Void, String> {
    private static final String ERROR = "412error752";
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
        OutputStream out = null;
        try {
            URL obj = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
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
                return ERROR;
            }
        } catch (Exception ex) {
            callback.onError(ex);
            return ERROR;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!s.equals(ERROR)) callback.processFinish(s);
    }
}