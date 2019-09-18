package com.huawei.android.feature.install;

public interface IFetchFeature {
    void fetch(InstallSessionState installSessionState, FeatureFetchListener featureFetchListener);

    void fetch(InstallSessionState installSessionState, InstallSessionStateNotifier installSessionStateNotifier);
}
