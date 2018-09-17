package com.huawei.android.net;

import android.net.ConnectivityManager;
import huawei.android.net.HwConnectivityExManager;
import java.net.InetAddress;

public class ConnectivityManagerEx {
    public static final int TYPE_NONE = -1;
    private static ConnectivityManagerEx mInstace = new ConnectivityManagerEx();

    public static ConnectivityManagerEx getDefault() {
        return mInstace;
    }

    public void setSmartKeyguardLevel(String level) {
        HwConnectivityExManager.getDefault().setSmartKeyguardLevel(level);
    }

    public void setUseCtrlSocket(boolean state) {
        HwConnectivityExManager.getDefault().setUseCtrlSocket(state);
    }

    public static boolean isNetworkSupported(int networkType, ConnectivityManager connectivityManager) {
        return connectivityManager.isNetworkSupported(networkType);
    }

    public static boolean requestRouteToHostAddress(ConnectivityManager cm, int networkType, InetAddress hostAddress) {
        return cm.requestRouteToHostAddress(networkType, hostAddress);
    }

    public static int stopUsingNetworkFeature(ConnectivityManager cm, int networkType, String feature) {
        return cm.stopUsingNetworkFeature(networkType, feature);
    }

    public static int startUsingNetworkFeature(ConnectivityManager cm, int networkType, String feature) {
        return cm.startUsingNetworkFeature(networkType, feature);
    }
}
