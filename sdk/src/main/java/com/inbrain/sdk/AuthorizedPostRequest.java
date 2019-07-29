package com.inbrain.sdk;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.inbrain.sdk.Constants.REQUEST_TIMEOUT_MS;

class AuthorizedPostRequest extends AsyncTask<String, Void, String> {
    private final AsyncResponse callback;
    private boolean success = false;
    private Exception exception;

    AuthorizedPostRequest(AsyncResponse callback) {
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
        String postDataParams = params[2];
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(REQUEST_TIMEOUT_MS);
            con.setConnectTimeout(REQUEST_TIMEOUT_MS);
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setDoInput(true);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postDataParams);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                success = true;
                return sb.toString();
            }
            return con.getResponseMessage();
        } catch (Exception ex) {
            exception = ex;
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (success) {
            callback.processFinish(s);
        } else {
            callback.onError(exception);
        }
    }
}