package com.android.server.pm.auth.deviceid;

public interface DeviceId {
    public static final String TAG_IMEI = "IMEI/";
    public static final String TAG_MEID = "MEID/";
    public static final String TAG_WIFIMAC = "WIFIMAC/";

    void addDeviceId(String str);

    void append(StringBuffer stringBuffer);

    boolean contain(String str);

    boolean isEmpty();
}
