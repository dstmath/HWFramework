package com.huawei.server;

import android.util.Log;
import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.BroadcastQueueEx;
import com.android.server.am.DefaultHwMtmBroadcastResourceManagerImpl;
import com.huawei.annotation.HwSystemApi;
import com.huawei.server.util.FactoryLoader;

@HwSystemApi
public class HwPartIawareServiceFactory {
    public static final String IAWARE_SERVICE_FACTORY_IMPL_NAME = "com.huawei.server.HwPartIawareServiceFactoryImpl";
    private static final String TAG = "HwPartIawareServiceFactory";
    private static HwPartIawareServiceFactory sFactory;

    public static HwPartIawareServiceFactory loadFactory(String factoryName) {
        HwPartIawareServiceFactory hwPartIawareServiceFactory = sFactory;
        if (hwPartIawareServiceFactory != null) {
            return hwPartIawareServiceFactory;
        }
        Object object = FactoryLoader.loadFactory(factoryName);
        if (object == null || !(object instanceof HwPartIawareServiceFactory)) {
            sFactory = new HwPartIawareServiceFactory();
        } else {
            sFactory = (HwPartIawareServiceFactory) object;
        }
        if (sFactory != null) {
            Log.i(TAG, "add " + factoryName + " to memory.");
            return sFactory;
        }
        throw new RuntimeException("can't load any iaware service factory");
    }

    public AbsHwMtmBroadcastResourceManager getHwMtmBroadcastResourceManagerImpl(BroadcastQueueEx queue) {
        return new DefaultHwMtmBroadcastResourceManagerImpl(queue);
    }
}
