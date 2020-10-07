package com.inbrain.sdk;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.inbrain.sdk.Constants.ALLOWED_COUNTRIES_URL;

class AllowedCountriesExecutor {
    void getAllowedCountries(final AllowedCountriesCallback callback) {
        GetRequest countriesRequest = new GetRequest(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    callback.onCountriesAvailable(parseCountries(output));
                } catch (JSONException e) {
                    callback.onFailToLoadAllowedCountries(e);
                }
            }

            @Override
            public void onError(Exception ex) {
                callback.onFailToLoadAllowedCountries(ex);
            }
        });
        countriesRequest.execute(ALLOWED_COUNTRIES_URL);
    }

    private List<String> parseCountries(String data) throws JSONException {
        List<String> countries = new ArrayList<>();
        JSONArray jsonarray = new JSONArray(data);
        for (int i = 0; i < jsonarray.length(); i++) {
            String country = jsonarray.getString(i);
            countries.add(country);
        }
        return countries;
    }

    public interface AllowedCountriesCallback {
        void onCountriesAvailable(List<String> countries);

        void onFailToLoadAllowedCountries(Exception ex);
    }
}
