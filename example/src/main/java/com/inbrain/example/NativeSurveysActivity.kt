package com.inbrain.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inbrain.sdk.InBrain
import com.inbrain.sdk.callback.StartSurveysCallback
import com.inbrain.sdk.model.Survey

class NativeSurveysActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "InBrainExample"

        fun intent(context: Context): Intent = Intent(context, NativeSurveysActivity::class.java)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: NativeSurveysAdapter

    private var surveys: List<Survey> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_surveys)

        title = getString(R.string.native_surveys_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerSurveys)
        progressBar = findViewById(R.id.progressBar)

        adapter = NativeSurveysAdapter { survey -> openSurvey(survey) }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        loadSurveys()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadSurveys() {
        setLoading(true)
        recyclerView.animate().alpha(0f).setDuration(300).start()

        InBrain.getInstance().getNativeSurveys { surveyList ->
            surveys = surveyList
            adapter.submit(surveyList)
            setLoading(false)

            if (surveyList.isEmpty()) {
                showToast(getString(R.string.error_no_surveys))
                recyclerView.animate().alpha(0f).setDuration(300).start()
            } else {
                recyclerView.animate().alpha(1f).setDuration(300).start()
            }
        }
    }

    private fun openSurvey(survey: Survey) {
        InBrain.getInstance().sessionId = "TestSessionID"
        InBrain.getInstance().showNativeSurvey(this, survey, false, object : StartSurveysCallback {
            override fun onSuccess() {
                Log.d(LOG_TAG, "Successfully started native survey")
            }

            override fun onFail(message: String) {
                showToast(message.ifEmpty { getString(R.string.error_generic) })
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
