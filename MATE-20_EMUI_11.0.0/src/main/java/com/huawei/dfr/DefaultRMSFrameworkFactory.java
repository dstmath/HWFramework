package com.huawei.dfr;

import android.rms.HwSysResource;
import com.huawei.dfr.rms.DefaultHwSysResource;
import com.huawei.dfr.rms.resource.DefaultPidsResource;

public class DefaultRMSFrameworkFactory {
    private static final String TAG = "DefaultRMSFrameworkFactory";
    private static DefaultRMSFrameworkFactory instance;

    public static synchronized DefaultRMSFrameworkFactory getInstance() {
        DefaultRMSFrameworkFactory defaultRMSFrameworkFactory;
        synchronized (DefaultRMSFrameworkFactory.class) {
            if (instance == null) {
                instance = new DefaultRMSFrameworkFactory();
            }
            defaultRMSFrameworkFactory = instance;
        }
        return defaultRMSFrameworkFactory;
    }

    public HwSysResource getHwSysResource(int sourceType) {
        return DefaultHwSysResource.getDefault();
    }

    public DefaultHwSysResource getPidsResource() {
        return DefaultPidsResource.getPidsResource();
    }
}
