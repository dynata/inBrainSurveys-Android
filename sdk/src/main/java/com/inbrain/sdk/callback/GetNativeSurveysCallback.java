package com.inbrain.sdk.callback;

import com.inbrain.sdk.model.Survey;

import java.util.List;

public interface GetNativeSurveysCallback {
    void nativeSurveysReceived(List<Survey> surveyList);
}
