package com.huawei.android.content.pm;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;

public class ServiceInfoEx {
    private ServiceInfo serviceInfo;

    public ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo2) {
        this.serviceInfo = serviceInfo2;
    }

    public String getName() {
        return this.serviceInfo.name;
    }

    public static ComponentName getComponentName(ServiceInfo serviceInfo2) {
        if (serviceInfo2 == null) {
            return null;
        }
        return serviceInfo2.getComponentName();
    }
}
