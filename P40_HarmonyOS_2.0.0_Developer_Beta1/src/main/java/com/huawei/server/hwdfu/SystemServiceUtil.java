package com.huawei.server.hwdfu;

import android.util.Log;
import com.huawei.android.hwdfu.LocalServicesUtil;
import com.huawei.android.hwdfu.ServiceManagerUtil;

public class SystemServiceUtil {
    private static final String TAG = "SystemServiceUtil";

    private SystemServiceUtil() {
    }

    public static void removeBinderService(String name, boolean isLazy) {
        ServiceManagerUtil.removeService(name, isLazy);
        Log.i(TAG, "removeBinderService: " + name);
    }

    public static void releaseBinderService(String name) {
        ServiceManagerUtil.releaseService(name);
        Log.i(TAG, "releaseBinderService: " + name);
    }

    public static void removeLocalService(Class<?> type, boolean isLazy) {
        LocalServicesUtil.removeService(type, isLazy);
        Log.i(TAG, "removeLocalService: " + type);
    }

    public static void releaseLocalService(Class<?> type) {
        LocalServicesUtil.releaseService(type);
        Log.i(TAG, "releaseLocalService: " + type);
    }
}
