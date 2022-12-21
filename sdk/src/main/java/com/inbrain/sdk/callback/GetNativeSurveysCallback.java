package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.InBrainNativeSurvey;

import java.util.List;

public interface GetNativeSurveysCallback {
    void nativeSurveysReceived(List<InBrainNativeSurvey> surveyList);
}
