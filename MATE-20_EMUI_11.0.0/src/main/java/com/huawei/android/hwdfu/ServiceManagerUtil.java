package com.huawei.android.hwdfu;

import android.os.Binder;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import java.util.concurrent.ConcurrentHashMap;

@HwSystemApi
public final class ServiceManagerUtil {
    private static final String TAG = "ServiceManagerUtil";
    private static ConcurrentHashMap<String, ServiceInfo> sServiceInfos = new ConcurrentHashMap<>();

    private ServiceManagerUtil() {
    }

    public static void registerService(String name) {
        ServiceInfo serviceInfo = sServiceInfos.get(name);
        if (serviceInfo != null) {
            serviceInfo.acquire();
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "registerService, " + name);
        }
    }

    public static void releaseService(String name) {
        ServiceInfo serviceInfo = sServiceInfos.get(name);
        if (serviceInfo != null) {
            serviceInfo.release();
            if (serviceInfo.isNeedStop()) {
                removeServiceImpl(name);
                sServiceInfos.remove(name);
            }
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "releaseService, " + name);
        }
    }

    public static void addService(String name) {
        ServiceInfo serviceInfo = sServiceInfos.get(name);
        if (serviceInfo == null) {
            ServiceInfo serviceInfo2 = new ServiceInfo(name);
            serviceInfo2.setServiceStart();
            sServiceInfos.put(name, serviceInfo2);
        } else {
            serviceInfo.setServiceStart();
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "addService, " + name);
        }
    }

    public static void removeService(String name, boolean isLazy) {
        ServiceInfo serviceInfo = sServiceInfos.get(name);
        if (serviceInfo != null) {
            serviceInfo.setServiceStop(isLazy);
            if (serviceInfo.isNeedStop()) {
                removeServiceImpl(name);
                sServiceInfos.remove(name);
            }
        }
        if (ServiceInfo.DEBUG) {
            Log.i(TAG, "removeService, " + name);
        }
    }

    public static boolean isServiceEnabled(String name) {
        ServiceInfo serviceInfo = sServiceInfos.get(name);
        if (serviceInfo != null) {
            return serviceInfo.isEnabled();
        }
        return false;
    }

    private static void removeServiceImpl(String name) {
        if (Binder.getCallingUid() >= 10000) {
            Log.e(TAG, "removeServiceImpl permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        ServiceManager.addService(name, null);
        Log.i(TAG, "removeServiceImpl success, " + name);
    }
}
