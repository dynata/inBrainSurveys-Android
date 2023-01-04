package com.inbrain.example

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val QA = BuildConfig.DEBUG // Set to {true} if you want to test on QA

        private const val API_CLIENT_ID: String = if (QA) BuildConfig.QA_CLIENT_ID else BuildConfig.PROD_CLIENT_ID // Client Id

        private const val API_SECRET: String = if (QA) BuildConfig.QA_CLIENT_SECRET else BuildConfig.PROD_CLIENT_SECRET // Client Secret

        private const val USER_ID: String = if (QA) BuildConfig.QA_USER_ID else BuildConfig.PROD_USER_ID // Unique User_id provided by your app

        private const val PLACEMENT_ID: String? = null // Used for custom placements with Native Surveys
    }

    private var nativeSurveys: List<Survey>? = null

    private val callback: InBrainCallback = object : InBrainCallback() {
        fun surveysClosed(b: Boolean, list: List<InBrainSurveyReward>) {
            /**
             * Called upon dismissal of inBrainWebView.
             * If you are using Native Surveys - please, ensure the surveys reloaded after some survey(s) completed.
             *
             * @param byWebView: **true** means closed by WebView's command; **false** - closed by user;
             * @param rewards: **NOTE:** At the moment only first** Native Survey reward is delivered.
             * That means if the user complete a Native Survey, proceed to Survey Wall and complete one more survey - only first
             * reward will be delivered. In case of Survey Wall usage only - no rewards will be delivered.
             */
            Log.d("MainActivity", "Surveys closed")
            getInBrainRewards()
        }

        fun didReceiveInBrainRewards(rewards: List<Reward>): Boolean {
            // THIS METHOD IS DEPRECATED...USE getInBrainRewards() INSTEAD
            // note: this method can be called during SDK usage while your activity is in 'onStop' state
            return false //this should always be false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnOpenSurveyWall).setOnClickListener { openSurveyWall() }
        findViewById<View>(R.id.btnShowNativeSurveys).setOnClickListener { showNativeSurveys() }
        findViewById<View>(R.id.btnFetchCurrencySale).setOnClickListener { fetchCurrencySale() }

        initInBrain()
    }

    override fun onResume() {
        super.onResume()

        // (1) Fetch Native Surveys from inBrain
        // ============================================
        /*InBrain.getInstance().getNativeSurveys(surveyList -> {
            nativeSurveys = surveyList;
            Log.d("MainActivity", "Count of Native Surveys returned:" + surveyList.size());
        });*/

        // (2) Fetch Native Surveys from inBrain based on the given SurveyFilter
        // ============================================
        val incCategories: List<SurveyCategory> = ArrayList<SurveyCategory>()
        //        incCategories.add(SurveyCategory.Home);
//        incCategories.add(SurveyCategory.PersonalCare);
        val excCategories: List<SurveyCategory> = ArrayList<SurveyCategory>()
        /*excCategories.add(SurveyCategory.SmokingTobacco);*/
        val filter = SurveyFilter()
        filter.placementId = MainActivity.PLACEMENT_ID
        filter.includeCategories = incCategories
        filter.excludeCategories = excCategories
        InBrain.getInstance().getNativeSurveys(filter, GetNativeSurveysCallback { surveyList: List<Survey?> ->
            nativeSurveys = surveyList
            Log.d("MainActivity", "Count of Native Surveys returned:" + surveyList.size)
        })
    }

    override fun onDestroy() {
        InBrain.getInstance().removeCallback(callback) // unsubscribe from events & rewards update
        super.onDestroy()
    }

    private fun initInBrain() {
        //this line must be called prior to utilizing any other inBrain functions
        InBrain.getInstance().setInBrain(this, MainActivity.API_CLIENT_ID, MainActivity.API_SECRET, false, MainActivity.USER_ID)
        InBrain.getInstance().addCallback(callback) // subscribe to events and new rewards

        //Here we are applying some custom UI settings for inBrain
        applyUiCustomization()

        //Checking if Surveys are Available
        InBrain.getInstance().areSurveysAvailable(this, SurveysAvailableCallback { available: Boolean ->
            Log.d(
                "MainActivity",
                "Surveys available:$available"
            )
        })
    }

    /**
     * Open the Survey Wall
     */
    private fun openSurveyWall() {
        InBrain.getInstance().showSurveys(this, object : StartSurveysCallback() {
            fun onSuccess() {
                Log.d("MainActivity", "Survey Wall Display Successfully")
            }

            fun onFail(message: String) {
                Log.e("MainActivity", "Failed to Show inBrain Survey Wall: $message")
                Toast.makeText(
                    this@MainActivity,  // show some message or dialog to user
                    "Sorry, something went wrong!",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Fetch ongoing currency sale details
     */
    private fun fetchCurrencySale() {
        InBrain.getInstance().getCurrencySale(GetCurrencySaleCallback { currencySale: CurrencySale? ->
            if (currencySale == null) {
                Toast.makeText(this, "There's no ongoing currency sale now.", Toast.LENGTH_SHORT).show()
            } else {
                val currencySaleInfo: String = (currencySale.description
                        + "\n" + currencySale.startOn
                        + "\n" + currencySale.endOn
                        + "\n" + currencySale.multiplier)
                Toast.makeText(this, currencySaleInfo, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Show Native Surveys in your own App
     */
    private fun showNativeSurveys() {
        //Checking if there are any nativeSurveys returned
        if (nativeSurveys == null || nativeSurveys.isEmpty()) {
            Toast.makeText(this@MainActivity, "Sorry, no native surveys available", Toast.LENGTH_LONG).show()
            return
        }

        //Just some custom logic for this demo app
        findViewById<View>(R.id.btnShowNativeSurveys).visibility = View.GONE
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerNativeSurveys)
        recyclerView.visibility = View.VISIBLE
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        //THIS IS THE LINE THAT CALLS showNativeSurveys private function to load the Survey via inBrain function
        val adapter = NativeSurveysAdapter({ survey: Survey? -> this.showNativeSurvey(survey) }, nativeSurveys)
        recyclerView.adapter = adapter
    }

    private fun showNativeSurvey(survey: Survey) {
        InBrain.getInstance().showNativeSurvey(this, survey, object : StartSurveysCallback() {
            fun onSuccess() {
                Log.d("MainActivity", "Successfully started InBrain")
            }

            fun onFail(message: String) {
                Log.e("MainActivity", "Failed to start inBrain:$message")
                Toast.makeText(
                    this@MainActivity,  // show some message or dialog to user
                    "Sorry, InBrain isn't supported on your device", Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Use this if you need to force check for rewards that may not have been returned in delegate method
     */
    private fun getInBrainRewards() {
        InBrain.getInstance().getRewards(object : GetRewardsCallback() {
            fun handleRewards(rewards: List<Reward>): Boolean {
                Log.d("MainActivity", "Received rewards:" + Arrays.toString(rewards.toTypedArray()))
                processRewards(rewards)
                return true //be sure to return true here. This will automatically confirm rewards on the inBrain server side
            }

            fun onFailToLoadRewards(t: Throwable) {
                Log.e("MainActivity", "onFailToLoadRewards:$t")
            }
        })
    }

    private fun processRewards(rewards: List<Reward>) {
        var total = 0f
        for (reward in rewards) {
            total += reward.amount
        }
        if (rewards.isEmpty()) {
            Toast.makeText(
                this@MainActivity,
                "You have no new rewards!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this@MainActivity,
                String.format(
                    Locale.getDefault(),
                    "You have received %d new rewards for a total of %.1f %s!",
                    rewards.size,
                    total,
                    rewards[0].currency
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun applyUiCustomization() {
        val toolBarConfig = ToolBarConfig()
        toolBarConfig.setTitle(getString(R.string.app_name)) // set title
        val useResourceId = false
        if (useResourceId) {
            toolBarConfig.setToolbarColorResId(R.color.colorAccent) // set toolbar color with status bar
                .setBackButtonColorResId(android.R.color.white) // set icon color
                .setTitleColorResId(android.R.color.white) //  set toolbar text
        } else {
            toolBarConfig.setToolbarColor(resources.getColor(R.color.colorAccent))
                .setBackButtonColor(resources.getColor(android.R.color.white))
                .setTitleColor(resources.getColor(android.R.color.white))
        }
        toolBarConfig.setElevationEnabled(false)
        InBrain.getInstance().setToolbarConfig(toolBarConfig)
        val statusBarConfig = StatusBarConfig()
        if (useResourceId) {
            statusBarConfig.setStatusBarColorResId(android.R.color.white)
                .setStatusBarIconsLight(false)
        } else {
            statusBarConfig.setStatusBarColor(resources.getColor(android.R.color.white))
                .setStatusBarIconsLight(false)
        }
        InBrain.getInstance().setStatusBarConfig(statusBarConfig)
    }
}