package com.android.server.wifi;

import android.util.Log;

public class HwWifiCHRNative {
    public static final int NETLINK_MSG_WIFI_TCP_START = 1;
    public static final int NETLINK_MSG_WIFI_TCP_STOP = 0;
    private static String TAG = "HwWifiCHRNative";

    private static native int registerNatives();

    public static native void setTcpMonitorStat(int i);

    static {
        try {
            System.loadLibrary("wifichrstat");
            Log.i(TAG, "loadLibrary: libwifichrstat.so successful");
            registerNatives();
        } catch (UnsatisfiedLinkError err) {
            Log.e(TAG, "loadLibrary: libwifichrstat.so, failure!!!");
            err.printStackTrace();
        }
    }
}
