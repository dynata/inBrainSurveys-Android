package com.inbrain.example

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.inbrain.sdk.InBrain
import com.inbrain.sdk.callback.GetCurrencySaleCallback
import com.inbrain.sdk.callback.GetRewardsCallback
import com.inbrain.sdk.callback.InBrainCallback
import com.inbrain.sdk.callback.StartSurveysCallback
import com.inbrain.sdk.config.StatusBarConfig
import com.inbrain.sdk.config.ToolBarConfig
import com.inbrain.sdk.model.CurrencySale
import com.inbrain.sdk.model.Reward
import com.inbrain.sdk.model.WallOption

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "InBrainExample"

        private const val API_CLIENT_ID = "YOUR_CLIENT_ID"   // Client Id
        private const val API_SECRET = "YOUR_CLIENT_SECRET"  // Client Secret
        private const val USER_ID = "YOUR_USER_ID_QA"        // Unique User_id provided by your app
    }

    private lateinit var pointsLabel: TextView
    private lateinit var currencySaleLabel: TextView

    private var totalPoints = 0f

    private val callback = InBrainCallback { _, _ ->
        Log.d(LOG_TAG, "Surveys closed")
        getInBrainRewards()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pointsLabel = findViewById(R.id.pointsLabel)
        currencySaleLabel = findViewById(R.id.currencySaleLabel)

        findViewById<MaterialButton>(R.id.btnOpenSurveyWall).setOnClickListener { openSurveyWall() }
        findViewById<MaterialButton>(R.id.btnNativeSurveys).setOnClickListener {
            startActivity(NativeSurveysActivity.intent(this))
        }
        findViewById<MaterialButton>(R.id.btnNativeOffers).setOnClickListener {
            startActivity(NativeOffersActivity.intent(this))
        }

        updatePoints()
        initInBrain()
        checkForCurrencySale()
    }

    override fun onDestroy() {
        InBrain.getInstance().removeCallback(callback)
        super.onDestroy()
    }

    private fun initInBrain() {
        // This line MUST be called prior to utilizing any other inBrain functions.
        // You can optionally pass in USER_ID param to the function.
        InBrain.getInstance().setInBrain(this, API_CLIENT_ID, API_SECRET, false)
        InBrain.getInstance().setUserID(this, USER_ID)

        InBrain.getInstance().addCallback(callback)
        applyUiCustomization()
    }

    private fun openSurveyWall() {
        InBrain.getInstance().openWall(this, WallOption.ALL, object : StartSurveysCallback {
            override fun onSuccess() {
                Log.d(LOG_TAG, "Survey Wall displayed successfully")
            }

            override fun onFail(message: String) {
                showToast(message.ifEmpty { getString(R.string.error_generic) })
            }
        })
    }

    private fun checkForCurrencySale() {
        InBrain.getInstance().getCurrencySale(GetCurrencySaleCallback { sale ->
            showCurrencySale(sale)
        })
    }

    private fun showCurrencySale(sale: CurrencySale?) {
        if (sale == null) {
            currencySaleLabel.animate().alpha(0f).setDuration(300).withEndAction {
                currencySaleLabel.visibility = View.INVISIBLE
            }.start()
            return
        }

        val multiplierValue = ((sale.multiplier * 10).toInt() / 10f)
        val multiplier = "${multiplierValue}x"
        val fullText = getString(R.string.currency_sale_earnings, multiplier)
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(multiplier)
        if (start >= 0) {
            val end = start + multiplier.length
            spannable.setSpan(
                AbsoluteSizeSpan(14, true),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        currencySaleLabel.text = spannable
        currencySaleLabel.visibility = View.VISIBLE
        currencySaleLabel.animate().alpha(1f).setDuration(300).start()
    }

    private fun getInBrainRewards() {
        InBrain.getInstance().getRewards(object : GetRewardsCallback {
            override fun handleRewards(rewards: List<Reward>): Boolean {
                if (rewards.isEmpty()) return true
                for (reward in rewards) {
                    totalPoints += reward.amount
                }
                updatePoints()
                return true
            }

            override fun onFailToLoadRewards(t: Throwable) {
                showToast(getString(R.string.error_generic))
            }
        })
    }

    private fun updatePoints() {
        pointsLabel.text = getString(R.string.total_points, totalPoints)
    }

    private fun applyUiCustomization() {
        val toolBarConfig = ToolBarConfig()
            .setToolbarColor(ContextCompat.getColor(this, R.color.main_color))
            .setBackButtonColor(ContextCompat.getColor(this, R.color.white))
        toolBarConfig.titleColor = ContextCompat.getColor(this, R.color.white)
        toolBarConfig.isElevationEnabled = false
        InBrain.getInstance().setToolbarConfig(toolBarConfig)

        val statusBarConfig = StatusBarConfig()
            .setStatusBarColor(ContextCompat.getColor(this, R.color.main_color))
            .setStatusBarIconsLight(true)
        InBrain.getInstance().setStatusBarConfig(statusBarConfig)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
