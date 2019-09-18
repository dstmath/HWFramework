package com.huawei.android.feature.install.config;

import com.huawei.android.feature.BuildConfig;

public class HWAppBundleRemoteConfig extends RemoteConfig {
    public String getBindRemoteServiceAction() {
        return BuildConfig.FLAVOR;
    }

    public String getBindRemoteServicePkgName() {
        return BuildConfig.FLAVOR;
    }

    public String getReceiveBroadcastAction() {
        return BuildConfig.FLAVOR;
    }
}
