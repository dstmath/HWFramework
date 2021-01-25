package com.huawei.android.content.pm;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;

public class ServiceInfoEx {
    public static ComponentName getComponentName(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return null;
        }
        return serviceInfo.getComponentName();
    }
}
