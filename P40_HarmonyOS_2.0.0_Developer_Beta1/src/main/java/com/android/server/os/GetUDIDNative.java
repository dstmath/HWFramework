package com.android.server.os;

public class GetUDIDNative {
    public static final native String getBtMacAddress();

    public static final native String getEmmcId();

    public static final native String getUDID();

    public static final native String getWifiMacAddress();

    static {
        System.loadLibrary("udidjni");
    }
}
