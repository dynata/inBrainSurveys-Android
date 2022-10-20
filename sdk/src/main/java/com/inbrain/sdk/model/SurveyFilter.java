package com.inbrain.sdk.model;

import java.io.Serializable;
import java.util.List;

public class SurveyFilter implements Serializable {
    public String placementId;
    public List<SurveyCategory> includeCategories;
    public List<SurveyCategory> excludeCategories;

    public SurveyFilter() {
        this(null, null, null);
    }

    public SurveyFilter(String placementId) {
        this(placementId, null, null);
    }

    public SurveyFilter(List<SurveyCategory> includeCategories) {
        this(null, includeCategories, null);
    }

    public SurveyFilter(List<SurveyCategory> includeCategories, List<SurveyCategory> excludeCategories) {
        this(null, includeCategories, excludeCategories);
    }

    public SurveyFilter(String placementId, List<SurveyCategory> includeCategories) {
        this(placementId, includeCategories, null);
    }

    public SurveyFilter(String placementId, List<SurveyCategory> includeCategories, List<SurveyCategory> excludeCategories) {
        this.placementId = placementId;
        this.includeCategories = includeCategories;
        this.excludeCategories = excludeCategories;
    }
}