package com.huawei.android.feature.install.localinstall;

public interface IFeatureLocalInstall {
    void onInstallFeatureBegin();

    void onInstallFeatureEnd();

    void onInstallProgressUpdate(String str, int i);
}
