package com.huawei.android.hwdfu;

import android.util.Log;
import com.android.server.LocalServices;
import java.util.concurrent.ConcurrentHashMap;

public final class LocalServicesUtil {
    private static final String TAG = "LocalServicesUtil";
    private static ConcurrentHashMap<Class<?>, ServiceInfo> sLocalServiceInfos = new ConcurrentHashMap<>();

    private LocalServicesUtil() {
    }

    public static void registerService(Class<?> type) {
        ServiceInfo serviceInfo = sLocalServiceInfos.get(type);
        if (serviceInfo != null) {
            serviceInfo.acquire();
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "registerService: " + type);
        }
    }

    public static void releaseService(Class<?> type) {
        ServiceInfo serviceInfo = sLocalServiceInfos.get(type);
        if (serviceInfo != null) {
            serviceInfo.release();
            if (serviceInfo.isNeedStop()) {
                LocalServices.removeServiceImpl(type);
                sLocalServiceInfos.remove(type);
            }
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "releaseService: " + type);
        }
    }

    public static void addService(Class<?> type) {
        ServiceInfo serviceInfo = sLocalServiceInfos.get(type);
        if (serviceInfo == null) {
            ServiceInfo serviceInfo2 = new ServiceInfo(type.toString());
            serviceInfo2.setServiceStart();
            sLocalServiceInfos.put(type, serviceInfo2);
        } else {
            serviceInfo.setServiceStart();
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "addService: " + type);
        }
    }

    public static void removeService(Class<?> type, boolean isLazy) {
        ServiceInfo serviceInfo = sLocalServiceInfos.get(type);
        if (serviceInfo != null) {
            serviceInfo.setServiceStop(isLazy);
            if (serviceInfo.isNeedStop()) {
                LocalServices.removeServiceImpl(type);
                sLocalServiceInfos.remove(type);
            }
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "removeService: " + type);
        }
    }
}
