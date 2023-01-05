package com.inbrain.example

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inbrain.sdk.InBrain
import com.inbrain.sdk.callback.*
import com.inbrain.sdk.config.StatusBarConfig
import com.inbrain.sdk.config.ToolBarConfig
import com.inbrain.sdk.model.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * Set this to {true} if you want to test with QA credentials.
         * Also, make sure InBrain.stagingMode is set to true as well, so the sdk indicates QA BE correctly.
         */
        private const val QA = false

        private const val LOG_TAG = "InBrainExample"
    }

    private val apiClientKey: String = if (QA) BuildConfig.QA_CLIENT_ID else BuildConfig.PROD_CLIENT_ID // Client Id

    private val apiSecret: String = if (QA) BuildConfig.QA_CLIENT_SECRET else BuildConfig.PROD_CLIENT_SECRET // Client Secret

    private val userId: String = if (QA) BuildConfig.QA_USER_ID else BuildConfig.PROD_USER_ID // Unique User_id provided by your app

    private val placementId: String? = null // Used for custom placements with Native Surveys


    private var nativeSurveys: List<Survey>? = null

    private val callback: InBrainCallback = object : InBrainCallback {
        override fun surveysClosed(byWebView: Boolean, rewards: MutableList<InBrainSurveyReward>?) {
            Log.d(LOG_TAG, "Surveys closed")
            getInBrainRewards()
        }

        override fun didReceiveInBrainRewards(rewards: List<Reward>): Boolean {
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
//        InBrain.getInstance().getNativeSurveys {
//            nativeSurveys = it
//            if (nativeSurveys != null) {
//                Log.d(LOG_TAG, "Count of Native Surveys returned:" + nativeSurveys!!.size)
//            }
//        }

        // (2) Fetch Native Surveys from inBrain based on the given SurveyFilter
        // ============================================
        val incCategories: MutableList<SurveyCategory> = ArrayList<SurveyCategory>()
//        incCategories.add(SurveyCategory.Home)
//        incCategories.add(SurveyCategory.PersonalCare)
        val excCategories: MutableList<SurveyCategory> = ArrayList<SurveyCategory>()
//        excCategories.add(SurveyCategory.SmokingTobacco)
        val filter = SurveyFilter()
        filter.placementId = placementId
        filter.includeCategories = incCategories
        filter.excludeCategories = excCategories
        InBrain.getInstance().getNativeSurveys(filter) { surveyList: List<Survey> ->
            nativeSurveys = surveyList
            Log.d(LOG_TAG, "Count of Native Surveys returned:" + surveyList.size)
        }
    }

    override fun onDestroy() {
        InBrain.getInstance().removeCallback(callback) // unsubscribe from events & rewards update
        super.onDestroy()
    }

    private fun initInBrain() {
        //this line must be called prior to utilizing any other inBrain functions
        InBrain.getInstance().setInBrain(this, apiClientKey, apiSecret, false, userId)

        InBrain.getInstance().addCallback(callback) // subscribe to events and new rewards

        //Here we are applying some custom UI settings for inBrain
        applyUiCustomization()

        //Checking if Surveys are Available
        InBrain.getInstance().areSurveysAvailable(this) { available: Boolean ->
            Log.d(
                LOG_TAG,
                "Surveys available:$available"
            )
        }
    }

    /**
     * Open the Survey Wall
     */
    private fun openSurveyWall() {
        InBrain.getInstance().showSurveys(this, object : StartSurveysCallback {
            override fun onSuccess() {
                Log.d(LOG_TAG, "Survey Wall Display Successfully")
            }

            override fun onFail(message: String) {
                Log.e(LOG_TAG, "Failed to Show inBrain Survey Wall: $message")
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
        InBrain.getInstance().getCurrencySale { currencySale: CurrencySale? ->
            if (currencySale == null) {
                Toast.makeText(this, "There's no ongoing currency sale now.", Toast.LENGTH_SHORT).show()
            } else {
                val currencySaleInfo: String = (currencySale.description
                        + "\n" + currencySale.startOn
                        + "\n" + currencySale.endOn
                        + "\n" + currencySale.multiplier)
                Toast.makeText(this, currencySaleInfo, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Show Native Surveys in your own App
     */
    private fun showNativeSurveys() {
        //Checking if there are any nativeSurveys returned
        if (nativeSurveys == null || nativeSurveys!!.isEmpty()) {
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
        val adapter = NativeSurveysAdapter(object : NativeSurveysAdapter.NativeSurveysClickListener {
            override fun surveyClicked(survey: Survey) {
                showNativeSurvey(survey)
            }
        }, nativeSurveys!!)
        recyclerView.adapter = adapter
    }

    private fun showNativeSurvey(survey: Survey) {
        InBrain.getInstance().showNativeSurvey(this, survey, object : StartSurveysCallback {
            override fun onSuccess() {
                Log.d(LOG_TAG, "Successfully started InBrain")
            }

            override fun onFail(message: String) {
                Log.e(LOG_TAG, "Failed to start inBrain:$message")
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
        InBrain.getInstance().getRewards(object : GetRewardsCallback {
            override fun handleRewards(rewards: List<Reward>): Boolean {
                Log.d(LOG_TAG, "Received rewards:" + rewards.toTypedArray().contentToString())
                processRewards(rewards)
                return true //be sure to return true here. This will automatically confirm rewards on the inBrain server side
            }

            override fun onFailToLoadRewards(t: Throwable) {
                Log.e(LOG_TAG, "onFailToLoadRewards:$t")
            }
        })
    }

    private fun processRewards(rewards: List<Reward>) {
        var total = 0f
        for (reward in rewards) {
            total += reward.amount
        }
        if (rewards.isEmpty()) {
            Toast.makeText(this@MainActivity, "You have no new rewards!", Toast.LENGTH_LONG).show()
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
        toolBarConfig.title = getString(R.string.app_name) // set title
        val useResourceId = false
        if (useResourceId) {
            toolBarConfig.setToolbarColorResId(R.color.colorAccent) // set toolbar color with status bar
                .setBackButtonColorResId(R.color.white)
                .titleColorResId = R.color.white //  set toolbar text
        } else {
            toolBarConfig.setToolbarColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setBackButtonColor(ContextCompat.getColor(this, R.color.white))
                .titleColor = ContextCompat.getColor(this, R.color.white)
        }
        toolBarConfig.isElevationEnabled = false
        InBrain.getInstance().setToolbarConfig(toolBarConfig)
        val statusBarConfig = StatusBarConfig()
        if (useResourceId) {
            statusBarConfig.setStatusBarColorResId(R.color.white)
                .setStatusBarIconsLight(false)
        } else {
            statusBarConfig.setStatusBarColor(ContextCompat.getColor(this, R.color.white))
                .setStatusBarIconsLight(false)
        }
        InBrain.getInstance().setStatusBarConfig(statusBarConfig)
    }
}