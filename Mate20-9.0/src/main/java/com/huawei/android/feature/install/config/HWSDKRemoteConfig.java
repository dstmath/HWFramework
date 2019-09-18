package com.huawei.android.feature.install.config;

public class HWSDKRemoteConfig extends RemoteConfig {
    public String getBindRemoteServiceAction() {
        return "com.huawei.vending.feature.service";
    }

    public String getBindRemoteServicePkgName() {
        return "com.huawei.android.feature.servicesample";
    }

    public String getReceiveBroadcastAction() {
        return "com.huawei.android.dynamicinstallservicesample";
    }
}
