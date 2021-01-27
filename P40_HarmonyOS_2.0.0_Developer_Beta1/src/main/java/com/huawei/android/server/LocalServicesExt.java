package com.huawei.android.server;

import com.android.server.LocalServices;

public class LocalServicesExt {
    public static <T> T getService(Class<T> type) {
        return (T) LocalServices.getService(type);
    }

    public static <T> void addService(Class<T> type, T service) {
        LocalServices.addService(type, service);
    }
}
