package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;
import com.android.server.wifi.HwUtil.IHwConstantUtilsEx;
import com.android.server.wifi.HwUtil.IHwDevicePolicyManagerEx;
import com.android.server.wifi.HwUtil.IHwLogCollectManagerEx;
import com.android.server.wifi.HwUtil.IHwTelphonyUtilsEx;
import com.android.server.wifi.p2p.IHwSupplicantP2pIfaceCallbackExt;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorExt;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorInner;
import com.android.server.wifi.p2p.WifiP2pMonitor;

public class HwWifiServiceFactory {
    private static final String TAG = "HwWifiServiceFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        IHwConstantUtilsEx getHwConstantUtils();

        IHwDevicePolicyManagerEx getHwDevicePolicyManager();

        IHwLogCollectManagerEx getHwLogCollectManager(Context context);

        HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo);

        HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative);

        IHwSupplicantP2pIfaceCallbackExt getHwSupplicantP2pIfaceCallbackExt(String str, WifiP2pMonitor wifiP2pMonitor);

        IHwTelphonyUtilsEx getHwTelphonyUtils();

        HwWifiCHRService getHwWifiCHRService();

        HwWifiDevicePolicy getHwWifiDevicePolicy();

        HwWifiMonitor getHwWifiMonitor();

        IHwWifiP2pMonitorExt getHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner iHwWifiP2pMonitorInner, WifiInjector wifiInjector);

        HwWifiServiceManager getHwWifiServiceManager();

        void initWifiCHRService(Context context);
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
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiServiceManager();
        }
        return DummyHwWifiServiceManager.getDefault();
    }

    public static HwWifiCHRService getHwWifiCHRService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiCHRService();
        }
        return null;
    }

    public static void initWifiCHRService(Context cxt) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.initWifiCHRService(cxt);
        }
    }

    public static HwWifiMonitor getHwWifiMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiMonitor();
        }
        return null;
    }

    public static HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        Log.d(TAG, "getHwSupplicantHeartBeat() is callled");
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
        }
        return null;
    }

    public static HwWifiDevicePolicy getHwWifiDevicePolicy() {
        Log.d(TAG, "getHwWifiDevicePolicy() is callled");
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiDevicePolicy();
        }
        return null;
    }

    public static HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMSSHandlerManager(context, wifiNative, wifiInfo);
        }
        return null;
    }

    public static IHwTelphonyUtilsEx getHwTelphonyUtils() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwTelphonyUtils();
        }
        return null;
    }

    public static IHwLogCollectManagerEx getHwLogCollectManager(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLogCollectManager(context);
        }
        return null;
    }

    public static IHwConstantUtilsEx getHwConstantUtils() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwConstantUtils();
        }
        return null;
    }

    public static IHwDevicePolicyManagerEx getHwDevicePolicyManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDevicePolicyManager();
        }
        return null;
    }

    public static IHwSupplicantP2pIfaceCallbackExt getHwSupplicantP2pIfaceCallbackExt(String iface, WifiP2pMonitor wifiMonitor) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSupplicantP2pIfaceCallbackExt(iface, wifiMonitor);
        }
        return null;
    }

    public static IHwWifiP2pMonitorExt getHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner wifiP2pMonitor, WifiInjector wifiInjector) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiP2pMonitorExt(wifiP2pMonitor, wifiInjector);
        }
        return null;
    }
}
