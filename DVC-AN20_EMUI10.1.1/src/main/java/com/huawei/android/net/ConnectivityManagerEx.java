package com.huawei.android.net;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import com.huawei.annotation.HwSystemApi;
import huawei.android.net.HwConnectivityExManager;
import java.net.InetAddress;

public class ConnectivityManagerEx {
    @HwSystemApi
    public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    @HwSystemApi
    public static final int TYPE_MOBILE = 0;
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

    public static NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(ConnectivityManager cm, int userId) {
        return cm.getDefaultNetworkCapabilitiesForUser(userId);
    }

    public static String getInetConditionAction() {
        return "android.net.conn.INET_CONDITION_ACTION";
    }

    public boolean bindUidProcessToNetwork(int netId, int uid) {
        return HwConnectivityExManager.getDefault().bindUidProcessToNetwork(netId, uid);
    }

    public boolean unbindAllUidProcessToNetwork(int netId) {
        return HwConnectivityExManager.getDefault().unbindAllUidProcessToNetwork(netId);
    }

    public boolean isUidProcessBindedToNetwork(int netId, int uid) {
        return HwConnectivityExManager.getDefault().isUidProcessBindedToNetwork(netId, uid);
    }

    public boolean isAllUidProcessUnbindToNetwork(int netId) {
        return HwConnectivityExManager.getDefault().isAllUidProcessUnbindToNetwork(netId);
    }

    public int getNetIdBySlotId(int slotId) {
        return HwConnectivityExManager.getDefault().getNetIdBySlotId(slotId);
    }
}
