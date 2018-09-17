package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;
import com.huawei.connectivitylog.ConnectivityLogManager;

public class HwWifiServiceFactory {
    private static final String TAG = "HwWifiServiceFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo);

        HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative);

        HwWifiCHRConst getHwWifiCHRConst();

        HwWifiCHRService getHwWifiCHRService();

        HwWifiCHRStateManager getHwWifiCHRStateManager();

        HwWifiDFTUtil getHwWifiDFTUtil();

        HwWifiDevicePolicy getHwWifiDevicePolicy();

        HwWifiMonitor getHwWifiMonitor();

        HwWifiServiceManager getHwWifiServiceManager();

        HwWifiStatStore getHwWifiStatStore();

        HwIsmCoexWifiStateTrack getIsmCoexWifiStateTrack(Context context, WifiStateMachine wifiStateMachine, WifiNative wifiNative);

        void initWifiCHRService(Context context);

        void initWifiCHRStateManager(Context context);

        void initWifiStatStore(Context context);
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.wifi.HwWifiServiceFactoryImpl").newInstance();
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
        }
        Log.v(TAG, "get AllImpl object = " + obj);
        return obj;
    }

    public static HwWifiServiceManager getHwWifiServiceManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiServiceManager();
        }
        return DummyHwWifiServiceManager.getDefault();
    }

    public static HwWifiCHRStateManager getHwWifiCHRStateManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiCHRStateManager();
        }
        return null;
    }

    public static HwWifiCHRConst getHwWifiCHRConst() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiCHRConst();
        }
        return null;
    }

    public static HwWifiStatStore getHwWifiStatStore() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiStatStore();
        }
        return null;
    }

    public static HwWifiCHRService getHwWifiCHRService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiCHRService();
        }
        return null;
    }

    public static void initWifiCHRService(Context cxt) {
        ConnectivityLogManager.init(cxt);
        Factory obj = getImplObject();
        if (obj != null) {
            obj.initWifiCHRService(cxt);
        }
    }

    public static void initWifiCHRStateManager(Context cxt) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.initWifiCHRStateManager(cxt);
        }
    }

    public static void initWifiStatStore(Context cxt) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.initWifiStatStore(cxt);
        }
    }

    public static HwWifiMonitor getHwWifiMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiMonitor();
        }
        return null;
    }

    public static HwIsmCoexWifiStateTrack getIsmCoexWifiStateTrack(Context context, WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        Log.d(TAG, "getIsmCoexWifiStateTrack() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getIsmCoexWifiStateTrack(context, wifiStateMachine, wifiNative);
        }
        return null;
    }

    public static HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        Log.d(TAG, "getHwSupplicantHeartBeat() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
        }
        return null;
    }

    public static HwWifiDFTUtil getHwWifiDFTUtil() {
        Log.d(TAG, "getHwWifiDFTUtil() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiDFTUtil();
        }
        return null;
    }

    public static HwWifiDevicePolicy getHwWifiDevicePolicy() {
        Log.d(TAG, "getHwWifiDevicePolicy() is callled");
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWifiDevicePolicy();
        }
        return null;
    }

    public static HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMSSHandlerManager(context, wifiNative, wifiInfo);
        }
        return null;
    }
}
