package com.android.server;

import android.util.ArrayMap;
import com.android.internal.annotations.VisibleForTesting;

public final class LocalServices {
    private static final ArrayMap<Class<?>, Object> sLocalServiceObjects = new ArrayMap<>();

    private LocalServices() {
    }

    public static <T> T getService(Class<T> type) {
        T t;
        synchronized (sLocalServiceObjects) {
            t = sLocalServiceObjects.get(type);
        }
        return t;
    }

    public static <T> void addService(Class<T> type, T service) {
        synchronized (sLocalServiceObjects) {
            if (!sLocalServiceObjects.containsKey(type)) {
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
}
