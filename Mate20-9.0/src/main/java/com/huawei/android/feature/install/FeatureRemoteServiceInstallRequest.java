package com.huawei.android.feature.install;

public class FeatureRemoteServiceInstallRequest {
    private String mFeatureName;
    private String mFeatureSignInfo;

    public FeatureRemoteServiceInstallRequest(String str, String str2) {
        this.mFeatureName = str;
        this.mFeatureSignInfo = str2;
    }

    public String getExceptSignature() {
        return this.mFeatureSignInfo;
    }

    public String getFeatureName() {
        return this.mFeatureName;
    }
}
