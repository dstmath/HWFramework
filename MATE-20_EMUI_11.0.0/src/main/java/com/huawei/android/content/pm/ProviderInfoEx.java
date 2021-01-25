package com.huawei.android.content.pm;

import android.content.ComponentName;
import android.content.pm.ProviderInfo;

public class ProviderInfoEx {
    public static ComponentName getComponentName(ProviderInfo providerInfo) {
        if (providerInfo == null) {
            return null;
        }
        return providerInfo.getComponentName();
    }
}
