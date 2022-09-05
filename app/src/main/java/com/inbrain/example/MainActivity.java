package com.inbrain.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inbrain.sdk.InBrain;
import com.inbrain.sdk.callback.GetRewardsCallback;
import com.inbrain.sdk.callback.InBrainCallback;
import com.inbrain.sdk.callback.StartSurveysCallback;
import com.inbrain.sdk.config.StatusBarConfig;
import com.inbrain.sdk.config.ToolBarConfig;
import com.inbrain.sdk.model.Reward;
import com.inbrain.sdk.model.Survey;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String API_CLIENT_ID = "{CLIENT_ID}"; // Client Id
    private static final String API_SECRET = "{CLIENT_SECRET}";// Client Secret
    private static final String PLACEMENT_ID = null; // Used for custom placements with Native Surveys
    private static final String USER_ID = "{USER_ID}"; // Unique User_id provided by your app
    private List<Survey> nativeSurveys;

    private final InBrainCallback callback = new InBrainCallback() {
        @Override
        public void surveysClosed() {
            Log.d("MainActivity", "Survey Wall Closed");
            // User Closed The Survey Wall and Control returned to App
            // Get Rewards earned from Survey Wall Flow
            getInBrainRewards();
        }

        @Override
        public void surveysClosedFromPage() {
            Log.d("MainActivity", "Native Survey Closed");
            // Native Survey Flow Finished and Control returned to App
            // Get Rewards earned from Native Survey Flow
            getInBrainRewards();
        }

        @Override
        public boolean didReceiveInBrainRewards(List<Reward> rewards) {
            // THIS METHOD IS DEPRECATED...USE getInBrainRewards() INSTEAD
            // note: this method can be called during SDK usage while your activity is in 'onStop' state
            return false; //this should always be false
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnOpenSurveyWall).setOnClickListener(view -> openSurveyWall());
        findViewById(R.id.btnShowNativeSurveys).setOnClickListener(view -> showNativeSurveys());
        findViewById(R.id.btnFetchCurrencySale).setOnClickListener(view -> fetchCurrencySale());

        initInBrain();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Request Native Surveys from inBrain
//        InBrain.getInstance().getNativeSurveys(PLACEMENT_ID, surveyList -> {
//            nativeSurveys = surveyList;
//            Log.d("MainActivity", "Count of Native Surveys returned:" + surveyList.size());
//        });
    }

    @Override
    protected void onDestroy() {
        InBrain.getInstance().removeCallback(callback); // unsubscribe from events & rewards update
        super.onDestroy();
    }

    private void initInBrain() {
        //this line must be called prior to utilizing any other inBrain functions
//        InBrain.getInstance().setInBrain(this, API_CLIENT_ID, API_SECRET, false, USER_ID);

//        InBrain.getInstance().addCallback(callback); // subscribe to events and new rewards

        //You can also pass tracking data as well as demographic data.  Note: THIS IS OPTIONAL
        /*String trackingData = "{ someCustom: 'tracking_data'}"; //this will be passed to your server via S2S postbacks
        HashMap<String, String> dataOptions = new HashMap<>();
        dataOptions.put("gender", "female");
        dataOptions.put("age", "26");
        InBrain.getInstance().setInBrainValuesFor(trackingData, dataOptions);*/

        //Here we are applying some custom UI settings for inBrain
        applyUiCustomization();

        //Checking if Surveys are Available
//        InBrain.getInstance().areSurveysAvailable(this, available -> Log.d("MainActivity", "Surveys available:" + available));
    }

    /**
     * Open the Survey Wall
     */
    private void openSurveyWall() {
        InBrain.getInstance().setInBrain(this, API_CLIENT_ID, API_SECRET, true, USER_ID);
        InBrain.getInstance().setInBrainValuesFor("ololo", null);
        InBrain.getInstance().showSurveys(this, new StartSurveysCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "Survey Wall Display Successfully");
            }

            @Override
            public void onFail(String message) {
                Log.e("MainActivity", "Failed to Show inBrain Survey Wall: " + message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Fetch ongoing currency sale details
     */
    private void fetchCurrencySale() {
        InBrain.getInstance().getCurrencySale(currencySale -> {
            if (currencySale == null) {
                Toast.makeText(this, "There's no ongoing currency sale now.", Toast.LENGTH_SHORT).show();
            } else {
                String currencySaleInfo = currencySale.description
                        + "\n" + currencySale.startOn
                        + "\n" + currencySale.endOn
                        + "\n" + currencySale.multiplier;
                Toast.makeText(this, currencySaleInfo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show Native Surveys in your own App
     */
    private void showNativeSurveys() {

        //Checking if there are any nativeSurveys returned
        if (nativeSurveys == null || nativeSurveys.isEmpty()) {
            Toast.makeText(MainActivity.this, "Sorry, no native surveys available", Toast.LENGTH_LONG).show();
            return;
        }

        //Just some custom logic for this demo app
        findViewById(R.id.btnShowNativeSurveys).setVisibility(View.GONE);
        RecyclerView recyclerView = findViewById(R.id.recyclerNativeSurveys);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //THIS IS THE LINE THAT CALLS showNativeSurveys private function to load the Survey via inBrain function
        NativeSurveysAdapter adapter = new NativeSurveysAdapter(this::showNativeSurvey, nativeSurveys);
        recyclerView.setAdapter(adapter);
    }

    private void showNativeSurvey(String surveyId) {
        InBrain.getInstance().showNativeSurveyWith(this, surveyId, PLACEMENT_ID, new StartSurveysCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "Successfully started InBrain");
            }

            @Override
            public void onFail(String message) {
                Log.e("MainActivity", "Failed to start inBrain:" + message);
                Toast.makeText(MainActivity.this, // show some message or dialog to user
                        "Sorry, InBrain isn't supported on your device", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Use this if you need to force check for rewards that may not have been returned in delegate method
     */
    private void getInBrainRewards() {
        InBrain.getInstance().getRewards(new GetRewardsCallback() {
            @Override
            public boolean handleRewards(List<Reward> rewards) {
                Log.d("MainActivity", "Received rewards:" + Arrays.toString(rewards.toArray()));
                processRewards(rewards);
                return true; //be sure to return true here. This will automatically confirm rewards on the inBrain server side
            }

            @Override
            public void onFailToLoadRewards(Throwable t) {
                Log.e("MainActivity", "onFailToLoadRewards:" + t);
            }
        });
    }

    private void processRewards(List<Reward> rewards) {
        float total = 0;
        for (Reward reward : rewards) {
            total += reward.amount;
        }

        if (rewards.isEmpty()) {
            Toast.makeText(MainActivity.this,
                    "You have no new rewards!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this,
                    String.format(Locale.getDefault(), "You have received %d new rewards for a total of %.1f %s!", rewards.size(), total, rewards.get(0).currency),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void applyUiCustomization() {
        ToolBarConfig toolBarConfig = new ToolBarConfig();
        toolBarConfig.setTitle(getString(R.string.app_name)); // set title

        boolean useResourceId = false;
        if (useResourceId) {
            toolBarConfig.setToolbarColorResId(R.color.colorAccent) // set toolbar color with status bar
                    .setBackButtonColorResId(android.R.color.white) // set icon color
                    .setTitleColorResId(android.R.color.white); //  set toolbar text
        } else {
            toolBarConfig.setToolbarColor(getResources().getColor(R.color.colorAccent))
                    .setBackButtonColor(getResources().getColor(android.R.color.white))
                    .setTitleColor(getResources().getColor(android.R.color.white));
        }
        toolBarConfig.setElevationEnabled(false);
        InBrain.getInstance().setToolbarConfig(toolBarConfig);

        StatusBarConfig statusBarConfig = new StatusBarConfig();
        if (useResourceId) {
            statusBarConfig.setStatusBarColorResId(android.R.color.white)
                    .setStatusBarIconsLight(false);
        } else {
            statusBarConfig.setStatusBarColor(getResources().getColor(android.R.color.white))
                    .setStatusBarIconsLight(false);
        }
        InBrain.getInstance().setStatusBarConfig(statusBarConfig);
    }

}
