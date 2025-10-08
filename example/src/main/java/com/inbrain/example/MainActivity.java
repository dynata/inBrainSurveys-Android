package com.inbrain.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.inbrain.sdk.model.InBrainSurveyReward;
import com.inbrain.sdk.model.Reward;
import com.inbrain.sdk.model.Survey;
import com.inbrain.sdk.model.SurveyCategory;
import com.inbrain.sdk.model.SurveyFilter;
import com.inbrain.sdk.model.WallOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "InBrainExample";

    private static final String API_CLIENT_ID = "YOUR_CLIENT_ID";   // Client Id
    private static final String API_SECRET = "YOUR_CLIENT_SECRET";  // Client Secret
    private static final String USER_ID = "YOUR_USER_ID_QA";        // Unique User_id provided by your app
    private static final String PLACEMENT_ID = null;                // Used for custom placements with Native Surveys

    private List<Survey> nativeSurveys;

    private final InBrainCallback callback = (byWebView, rewards) -> {
        Log.d(LOG_TAG, "Surveys closed");

        StringBuilder outText = new StringBuilder("Survey outcome from callback:");
        if (rewards != null) {
            for (InBrainSurveyReward reward : rewards) {
                outText.append("\n")
                        .append("Survey(")
                        .append(reward.getSurveyId())
                        .append(") has been ")
                        .append(reward.getOutcomeType().name())
                        .append(" with reward ")
                        .append(reward.getUserReward())
                        .append(".");
            }
            Toast.makeText(MainActivity.this, outText.toString(), Toast.LENGTH_LONG).show();
        }

        // Manually check rewards received
        getInBrainRewards();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnOpenSurveyWall).setOnClickListener(view -> openSurveyWall());
        findViewById(R.id.btnShowNativeSurveys).setOnClickListener(view -> showNativeSurveys());
        findViewById(R.id.btnFetchCurrencySale).setOnClickListener(view -> fetchCurrencySale());

        CheckBox chbSessionId = findViewById(R.id.chbSessionId);
        EditText edtSessionId = findViewById(R.id.edtSessionId);
        chbSessionId.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                String sessionId = String.valueOf(edtSessionId.getText());
                if (!sessionId.isEmpty()) {
                    InBrain.getInstance().setSessionId(sessionId);
                    return;
                }
            }
            InBrain.getInstance().setSessionId(null);
        });

        initInBrain();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // (1) Fetch Native Surveys from inBrain
        // ============================================
        /*InBrain.getInstance().getNativeSurveys(surveyList -> {
            nativeSurveys = surveyList;
            Log.d(LOG_TAG, "Count of Native Surveys returned:" + surveyList.size());
        });*/

        // (2) Fetch Native Surveys from inBrain based on the given SurveyFilter
        // ============================================
        List<SurveyCategory> incCategories = new ArrayList<>();
        /*incCategories.add(SurveyCategory.Home);
        incCategories.add(SurveyCategory.PersonalCare);*/
        List<SurveyCategory> excCategories = new ArrayList<>();
        /*excCategories.add(SurveyCategory.SmokingTobacco);*/
        SurveyFilter filter = new SurveyFilter();
        filter.placementId = PLACEMENT_ID;
        filter.includeCategories = incCategories;
        filter.excludeCategories = excCategories;
        InBrain.getInstance().getNativeSurveys(filter, surveyList -> {
            nativeSurveys = surveyList;
            Log.d(LOG_TAG, "Count of Native Surveys returned:" + surveyList.size());
        });
    }

    @Override
    protected void onDestroy() {
        InBrain.getInstance().removeCallback(callback); // unsubscribe from events & rewards update
        super.onDestroy();
    }

    private void initInBrain() {
        // This line MUST be called prior to utilizing any other inBrain functions. You can optionally pass in USER_ID param to the function.
//        InBrain.getInstance().setInBrain(this, API_CLIENT_ID, API_SECRET, false, USER_ID);
        InBrain.getInstance().setInBrain(this, API_CLIENT_ID, API_SECRET, false);
        InBrain.getInstance().setUserID(this, USER_ID);

        InBrain.getInstance().addCallback(callback); // subscribe to events and new rewards

        //Here we are applying some custom UI settings for inBrain
        applyUiCustomization();
    }

    /**
     * Open the Survey Wall
     */
    private void openSurveyWall() {
        InBrain.getInstance().openWall(this, WallOption.ALL, new StartSurveysCallback() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Survey Wall Displayed Successfully");
            }

            @Override
            public void onFail(String message) {
                Log.e(LOG_TAG, "Failed to Show inBrain Survey Wall: " + message);
                Toast.makeText(MainActivity.this,
                        "Sorry, something went wrong!",
                        Toast.LENGTH_LONG).show();
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

    private void showNativeSurvey(Survey survey) {
        InBrain.getInstance().showNativeSurvey(this, survey, true, new StartSurveysCallback() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Successfully started InBrain");
            }

            @Override
            public void onFail(String message) {
                Log.e(LOG_TAG, "Failed to start inBrain:" + message);
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
                Log.d(LOG_TAG, "Received rewards:" + Arrays.toString(rewards.toArray()));
                processRewards(rewards);
                return true; //be sure to return true here. This will automatically confirm rewards on the inBrain server side
            }

            @Override
            public void onFailToLoadRewards(Throwable t) {
                Log.e(LOG_TAG, "onFailToLoadRewards:" + t);
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
                    "Force check rewards:\nYou have no new rewards!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this,
                    String.format(Locale.getDefault(), "Force check rewards:\nYou have received %d new rewards for a total of %.1f %s!",
                            rewards.size(), total, rewards.get(0).currency),
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
