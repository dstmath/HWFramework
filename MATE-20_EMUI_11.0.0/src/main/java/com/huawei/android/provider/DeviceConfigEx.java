package com.huawei.android.provider;

import android.provider.DeviceConfig;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DeviceConfigEx {
    @HwSystemApi
    public static final String NAMESPACE_PRIVACY = "privacy";

    @HwSystemApi
    public static boolean getBoolean(String namespace, String name, boolean defaultValue) {
        return DeviceConfig.getBoolean(namespace, name, defaultValue);
    }

    @HwSystemApi
    public static boolean setProperty(String namespace, String name, String value, boolean makeDefault) {
        return DeviceConfig.setProperty(namespace, name, value, makeDefault);
    }
}
