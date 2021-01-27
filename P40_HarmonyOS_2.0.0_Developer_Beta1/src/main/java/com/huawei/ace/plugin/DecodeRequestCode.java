package com.huawei.ace.plugin;

public final class DecodeRequestCode {
    public static String getPluginName(int i) {
        return (i < RequestCodeMapping.GEOLOCATIONSTART.getCode() || i > RequestCodeMapping.GEOLOCATIONEND.getCode()) ? "" : "com.huawei.ace.systemplugin.geolocation.GeolocationPlugin";
    }
}
