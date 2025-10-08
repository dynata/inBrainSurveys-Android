package com.inbrain.example;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inbrain.sdk.model.Survey;

import java.util.List;

public class NativeSurveysAdapter extends RecyclerView.Adapter<NativeSurveysAdapter.ViewHolder> {

    private final NativeSurveysClickListener listener;
    private final List<Survey> surveys;

    public NativeSurveysAdapter(NativeSurveysClickListener listener, List<Survey> surveys) {
        this.listener = listener;
        this.surveys = surveys;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupSurvey(surveys.get(position));
    }

    @NonNull
    @Override
    public NativeSurveysAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return surveys.size();
    }

    interface NativeSurveysClickListener {
        void surveyClicked(Survey survey);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final TextView rankTextView;
        private final TextView valueTextView;
        private final TextView timeTextView;
        private final TextView currencySaleTextView;
        private final TextView multiplierTextView;
        private final TextView conversionTextView;
        private final TextView categoriesTextView;
        private final TextView searchIdTextView;
        private final TextView isProfilerTextView;

        ViewHolder(View v) {
            super(v);
            rootView = v;
            rankTextView = v.findViewById(R.id.rank_text_view);
            valueTextView = v.findViewById(R.id.value_text_view);
            timeTextView = v.findViewById(R.id.time_text_view);
            currencySaleTextView = v.findViewById(R.id.currency_text_view);
            multiplierTextView = v.findViewById(R.id.multiplier_text_view);
            conversionTextView = v.findViewById(R.id.conversion_threshold_text_view);
            categoriesTextView = v.findViewById(R.id.categories_text_view);
            searchIdTextView = v.findViewById(R.id.search_id_text_view);
            isProfilerTextView = v.findViewById(R.id.is_profiler_text_view);
        }

        @SuppressLint("SetTextI18n")
        public void setupSurvey(final Survey survey) {
            rankTextView.setText("Rank: " + survey.rank);
            valueTextView.setText("Value: " + survey.value);
            timeTextView.setText("Time: " + survey.time);
            currencySaleTextView.setText("CurrencySale: " + survey.currencySale);
            multiplierTextView.setText("Multiplier: " + survey.multiplier);
            conversionTextView.setText("Conversion: " + survey.conversionLevel);
            categoriesTextView.setText("Categories: " + survey.categories);
            searchIdTextView.setText("SearchID: " + survey.searchId);
            isProfilerTextView.setText("isProfiler: " + survey.isProfilerSurvey);
            rootView.setOnClickListener(v -> listener.surveyClicked(survey));
        }
    }
}