package com.huawei.android.server;

import com.android.server.LocalServices;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public final class LocalServicesEx {
    public static <T> T getService(Class<T> type) {
        return (T) LocalServices.getService(type);
    }
}
