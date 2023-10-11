package com.inbrain.example

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inbrain.sdk.model.Survey

class NativeSurveysAdapter(private val listener: NativeSurveysClickListener, private val surveys: List<Survey>) :
    RecyclerView.Adapter<NativeSurveysAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setupSurvey(surveys[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_survey, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return surveys.size
    }

    interface NativeSurveysClickListener {
        fun surveyClicked(survey: Survey)
    }

    inner class ViewHolder(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
        private val rankTextView: TextView = rootView.findViewById(R.id.rank_text_view)
        private val valueTextView: TextView = rootView.findViewById(R.id.value_text_view)
        private val timeTextView: TextView = rootView.findViewById(R.id.time_text_view)
        private val currencySaleTextView: TextView = rootView.findViewById(R.id.currency_text_view)
        private val multiplierTextView: TextView = rootView.findViewById(R.id.multiplier_text_view)
        private val conversionTextView: TextView = rootView.findViewById(R.id.conversion_threshold_text_view)
        private val categoriesTextView: TextView = rootView.findViewById(R.id.categories_text_view)
        private val searchIdTextView: TextView = rootView.findViewById(R.id.search_id_text_view)
        private val isProfilerTextView: TextView = rootView.findViewById(R.id.is_profiler_text_view)

        @SuppressLint("SetTextI18n")
        fun setupSurvey(survey: Survey) {
            rankTextView.text = "Rank: " + survey.rank
            valueTextView.text = "Value: " + survey.value
            timeTextView.text = "Time: " + survey.time
            currencySaleTextView.text = "CurrencySale: " + survey.currencySale
            multiplierTextView.text = "Multiplier: " + survey.multiplier
            conversionTextView.text = "Conversion: " + survey.conversionLevel
            categoriesTextView.text = "Categories: " + survey.categories
            searchIdTextView.text = "SearchID: " + survey.searchId
            isProfilerTextView.text = "isProfiler: " + survey.isProfilerSurvey
            rootView.setOnClickListener { listener.surveyClicked(survey) }
        }
    }
}