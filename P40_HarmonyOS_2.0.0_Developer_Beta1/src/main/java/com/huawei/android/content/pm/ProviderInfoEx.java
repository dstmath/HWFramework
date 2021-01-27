package com.huawei.android.content.pm;

import android.content.ComponentName;
import android.content.pm.ProviderInfo;

public class ProviderInfoEx {
    private ProviderInfo providerInfo;

    public ProviderInfo getProviderInfo() {
        return this.providerInfo;
    }

    public void setProviderInfo(ProviderInfo providerInfo2) {
        this.providerInfo = providerInfo2;
    }

    public String getName() {
        return this.providerInfo.name;
    }

    public static ComponentName getComponentName(ProviderInfo providerInfo2) {
        if (providerInfo2 == null) {
            return null;
        }
        return providerInfo2.getComponentName();
    }
}
