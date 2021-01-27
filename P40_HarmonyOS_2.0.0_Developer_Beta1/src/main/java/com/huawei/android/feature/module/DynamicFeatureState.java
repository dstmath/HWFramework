package com.huawei.android.feature.module;

public class DynamicFeatureState {
    String featureName;
    int featureState;

    public DynamicFeatureState(String str, int i) {
        this.featureState = i;
        this.featureName = str;
    }
}
