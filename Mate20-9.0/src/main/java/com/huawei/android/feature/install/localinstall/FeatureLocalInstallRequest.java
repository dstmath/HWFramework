package com.huawei.android.feature.install.localinstall;

public class FeatureLocalInstallRequest {
    private String mFeatureName;
    private String mLocalFeaturePath;
    private String mLocalSignature;

    public FeatureLocalInstallRequest(String str, String str2, String str3) {
        this.mLocalFeaturePath = str2;
        this.mLocalSignature = str3;
        this.mFeatureName = str;
    }

    public String getFeatureName() {
        return this.mFeatureName;
    }

    public String getPath() {
        return this.mLocalFeaturePath;
    }

    public String getSignature() {
        return this.mLocalSignature;
    }
}
