package com.huawei.android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import com.huawei.annotation.HwSystemApi;
import huawei.android.net.HwConnectivityExManager;
import java.net.InetAddress;

public class ConnectivityManagerEx {
    @HwSystemApi
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    @HwSystemApi
    public static final int CALLBACK_AVAILABLE = 524290;
    @HwSystemApi
    public static final int CALLBACK_BLK_CHANGED = 524299;
    @HwSystemApi
    public static final int CALLBACK_CAP_CHANGED = 524294;
    @HwSystemApi
    public static final int CALLBACK_IP_CHANGED = 524295;
    @HwSystemApi
    public static final int CALLBACK_LOSING = 524291;
    @HwSystemApi
    public static final int CALLBACK_LOST = 524292;
    @HwSystemApi
    public static final int CALLBACK_PRECHECK = 524289;
    @HwSystemApi
    public static final int CALLBACK_RESUMED = 524298;
    @HwSystemApi
    public static final int CALLBACK_SUSPENDED = 524297;
    @HwSystemApi
    public static final int CALLBACK_UNAVAIL = 524293;
    @HwSystemApi
    public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    @HwSystemApi
    public static final String EXTRA_ACTIVE_TETHER = "tetherArray";
    @HwSystemApi
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_NONE = -1;
    @HwSystemApi
    public static final int TYPE_PROXY = 16;
    public static final int USB_P2P_STATE_DISCONNECTED = 0;
    public static final int USB_P2P_STATE_P2P = 1;
    public static final int USB_P2P_STATE_TETHERED = 2;
    private static ConnectivityManagerEx mInstace = new ConnectivityManagerEx();

    @HwSystemApi
    public static String getCallbackName(int whichCallback) {
        return ConnectivityManager.getCallbackName(whichCallback);
    }

    public static ConnectivityManagerEx getDefault() {
        return mInstace;
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

    @HwSystemApi
    public static String[] getTetherableWifiRegexs(Context context) {
        ConnectivityManager connectivityManager;
        if (context == null || (connectivityManager = (ConnectivityManager) context.getSystemService("connectivity")) == null) {
            return new String[0];
        }
        return connectivityManager.getTetherableWifiRegexs();
    }

    @HwSystemApi
    public static String[] getTetherableUsbRegexs(Context context) {
        ConnectivityManager connectivityManager;
        if (context == null || (connectivityManager = (ConnectivityManager) context.getSystemService("connectivity")) == null) {
            return new String[0];
        }
        return connectivityManager.getTetherableUsbRegexs();
    }

    @HwSystemApi
    public static String[] getTetherableBluetoothRegexs(Context context) {
        ConnectivityManager connectivityManager;
        if (context == null || (connectivityManager = (ConnectivityManager) context.getSystemService("connectivity")) == null) {
            return new String[0];
        }
        return connectivityManager.getTetherableBluetoothRegexs();
    }

    public void setSmartKeyguardLevel(String level) {
        HwConnectivityExManager.getDefault().setSmartKeyguardLevel(level);
    }

    public void setUseCtrlSocket(boolean state) {
        HwConnectivityExManager.getDefault().setUseCtrlSocket(state);
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

    public int getUsbP2pState() {
        return HwConnectivityExManager.getDefault().getUsbP2pState();
    }

    public void requestUsbP2p(IUsbP2pCallback callback) {
        HwConnectivityExManager.getDefault().requestUsbP2p(callback);
    }

    public void registerUsbP2pCallback(IUsbP2pCallback callback) {
        HwConnectivityExManager.getDefault().registerUsbP2pCallback(callback);
    }

    public void unregisterUsbP2pCallback(IUsbP2pCallback callback) {
        HwConnectivityExManager.getDefault().unregisterUsbP2pCallback(callback);
    }
}
