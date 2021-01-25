package com.android.server;

import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.huawei.android.hwdfu.LocalServicesUtil;

public final class LocalServices {
    private static final ArrayMap<Class<?>, Object> sLocalServiceObjects = new ArrayMap<>();

    private LocalServices() {
    }

    public static <T> T getService(Class<T> type) {
        T t;
        synchronized (sLocalServiceObjects) {
            LocalServicesUtil.registerService(type);
            t = (T) sLocalServiceObjects.get(type);
        }
        return t;
    }

    public static <T> void addService(Class<T> type, T service) {
        synchronized (sLocalServiceObjects) {
            if (!sLocalServiceObjects.containsKey(type)) {
                LocalServicesUtil.addService(type);
                sLocalServiceObjects.put(type, service);
            } else {
                throw new IllegalStateException("Overriding service registration");
            }
        }
    }

    @VisibleForTesting
    public static <T> void removeServiceForTest(Class<T> type) {
        synchronized (sLocalServiceObjects) {
            sLocalServiceObjects.remove(type);
        }
    }

    public static void removeServiceImpl(Class<?> type) {
        synchronized (sLocalServiceObjects) {
            int index = sLocalServiceObjects.indexOfKey(type);
            if (index >= 0) {
                sLocalServiceObjects.removeAt(index);
                Log.i("LocalServices", "removeServiceImpl: " + type);
            }
        }
    }
}
