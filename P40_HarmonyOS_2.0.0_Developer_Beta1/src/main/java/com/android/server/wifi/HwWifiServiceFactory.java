package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwUtil.IHwDevicePolicyManagerEx;
import com.android.server.wifi.hwUtil.IHwConstantUtilsEx;
import com.android.server.wifi.hwUtil.IHwLogCollectManagerEx;
import com.android.server.wifi.hwUtil.IHwTelphonyUtilsEx;
import com.android.server.wifi.p2p.IHwSupplicantP2pIfaceCallbackExt;
import com.android.server.wifi.p2p.IHwSupplicantP2pIfaceHalEx;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorExt;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorInner;
import com.android.server.wifi.p2p.IHwWifiP2pNativeEx;
import com.android.server.wifi.p2p.IHwWifiP2pServiceEx;
import com.android.server.wifi.p2p.IHwWifiP2pServiceInner;
import com.android.server.wifi.p2p.IHwWifiP2pServiceTvEx;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
import com.android.server.wifi.p2p.WifiP2pMonitor;

public class HwWifiServiceFactory {
    private static final String TAG = "HwWifiServiceFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        HiCoexManager getHiCoexManager();

        IHwConstantUtilsEx getHwConstantUtils();

        IHwDevicePolicyManagerEx getHwDevicePolicyManager();

        IHwLogCollectManagerEx getHwLogCollectManager(Context context);

        HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo);

        IHwScanRequestProxyEx getHwScanRequestProxyEx(IHwScanRequestProxyInner iHwScanRequestProxyInner, Context context, WifiInjector wifiInjector);

        IHwSoftApManagerEx getHwSoftApManagerEx(IHwSoftApManagerInner iHwSoftApManagerInner, Context context);

        HwSupplicantHeartBeat getHwSupplicantHeartBeat(ClientModeImpl clientModeImpl, WifiNative wifiNative);

        IHwSupplicantP2pIfaceCallbackExt getHwSupplicantP2pIfaceCallbackExt(String str, WifiP2pMonitor wifiP2pMonitor);

        IHwSupplicantP2pIfaceHalEx getHwSupplicantP2pIfaceHalEx(SupplicantP2pIfaceHal supplicantP2pIfaceHal, WifiP2pMonitor wifiP2pMonitor);

        IHwSupplicantStaIfaceHalEx getHwSupplicantStaIfaceHalEx(IHwSupplicantStaIfaceHalInner iHwSupplicantStaIfaceHalInner, WifiMonitor wifiMonitor);

        IHwSupplicantStaNetworkHalEx getHwSupplicantStaNetworkHalEx(IHwSupplicantStaNetworkHalInner iHwSupplicantStaNetworkHalInner, WifiMonitor wifiMonitor, String str);

        IHwTelphonyUtilsEx getHwTelphonyUtils();

        IHwWifiApConfigStoreEx getHwWifiApConfigStoreEx();

        HwWifiCHRService getHwWifiCHRService();

        IHwWifiConfigManagerEx getHwWifiConfigManagerEx(IHwWifiConfigManagerInner iHwWifiConfigManagerInner, Context context);

        HwWifiDevicePolicy getHwWifiDevicePolicy();

        HwWifiMonitor getHwWifiMonitor();

        IHwWifiNativeEx getHwWifiNativeEx(IHwWifiNativeInner iHwWifiNativeInner, SupplicantStaIfaceHal supplicantStaIfaceHal);

        IHwWifiP2pMonitorExt getHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner iHwWifiP2pMonitorInner, WifiInjector wifiInjector);

        IHwWifiP2pNativeEx getHwWifiP2pNativeEx(SupplicantP2pIfaceHal supplicantP2pIfaceHal);

        IHwWifiP2pServiceEx getHwWifiP2pServiceEx(Context context);

        IHwWifiP2pServiceTvEx getHwWifiP2pServiceTvEx(Context context, StateMachine stateMachine, WifiInjector wifiInjector, IHwWifiP2pServiceInner iHwWifiP2pServiceInner);

        IHwWifiScanningServiceImplEx getHwWifiScanningServiceImplEx(Context context);

        IHwWifiScoreReportEx getHwWifiScoreReportEx();

        HwWifiServiceManager getHwWifiServiceManager();

        IHwWifiSettingsStoreEx getHwWifiSettingsStoreEx(Context context);

        IHwWifiStateMachineEx getHwWifiStateMachineEx(IHwWifiStateMachineInner iHwWifiStateMachineInner);

        IHwWificondScannerImplEx getHwWificondScannerImplEx(Context context);

        void initWifiCHRService(Context context);
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        Object allImplObj = Class.forName("com.android.server.wifi.HwWifiServiceFactoryImpl").newInstance();
                        if (allImplObj instanceof Factory) {
                            obj = (Factory) allImplObj;
                        }
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        Log.e(TAG, ": reflection exception in getImplObject()");
                    }
                }
            }
        }
        Log.v(TAG, "get AllImpl object = " + obj);
        return obj;
    }

    public static HwWifiServiceManager getHwWifiServiceManager() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiServiceManager();
        }
        return DummyHwWifiServiceManager.getDefault();
    }

    public static HwWifiCHRService getHwWifiCHRService() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiCHRService();
        }
        return null;
    }

    public static void initWifiCHRService(Context cxt) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            wifiServiceFactory.initWifiCHRService(cxt);
        }
    }

    public static HwWifiMonitor getHwWifiMonitor() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiMonitor();
        }
        return null;
    }

    public static HwSupplicantHeartBeat getHwSupplicantHeartBeat(ClientModeImpl wifiStateMachine, WifiNative wifiNative) {
        Log.d(TAG, "getHwSupplicantHeartBeat() is callled");
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
        }
        return null;
    }

    public static HwWifiDevicePolicy getHwWifiDevicePolicy() {
        Log.d(TAG, "getHwWifiDevicePolicy() is callled");
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiDevicePolicy();
        }
        return null;
    }

    public static HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwMSSHandlerManager(context, wifiNative, wifiInfo);
        }
        return null;
    }

    public static IHwSupplicantStaIfaceHalEx getHwSupplicantStaIfaceHalEx(IHwSupplicantStaIfaceHalInner supplicantStaIfaceHal, WifiMonitor wifiMonitor) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwSupplicantStaIfaceHalEx(supplicantStaIfaceHal, wifiMonitor);
        }
        return null;
    }

    public static IHwSupplicantStaNetworkHalEx getHwSupplicantStaNetworkHalEx(IHwSupplicantStaNetworkHalInner supplicantStaNetworkHal, WifiMonitor wifiMonitor, String ifaceName) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwSupplicantStaNetworkHalEx(supplicantStaNetworkHal, wifiMonitor, ifaceName);
        }
        return null;
    }

    public static IHwWifiNativeEx getHwWifiNativeEx(IHwWifiNativeInner hwWifiNativeInner, SupplicantStaIfaceHal staIfaceHal) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiNativeEx(hwWifiNativeInner, staIfaceHal);
        }
        return null;
    }

    public static IHwWifiApConfigStoreEx getHwWifiApConfigStoreEx() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiApConfigStoreEx();
        }
        return null;
    }

    public static IHwSupplicantP2pIfaceHalEx getHwSupplicantP2pIfaceHalEx(SupplicantP2pIfaceHal supplicantP2pIfaceHal, WifiP2pMonitor monitor) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwSupplicantP2pIfaceHalEx(supplicantP2pIfaceHal, monitor);
        }
        return null;
    }

    public static IHwWifiP2pNativeEx getHwWifiP2pNativeEx(SupplicantP2pIfaceHal p2pIfaceHal) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiP2pNativeEx(p2pIfaceHal);
        }
        return null;
    }

    public static IHwWifiConfigManagerEx getHwWifiConfigManagerEx(IHwWifiConfigManagerInner hwWifiConfigManagerInner, Context context) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiConfigManagerEx(hwWifiConfigManagerInner, context);
        }
        return null;
    }

    public static IHwWifiScoreReportEx getHwWifiScoreReportEx() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiScoreReportEx();
        }
        return null;
    }

    public static IHwSoftApManagerEx getHwSoftApManagerEx(IHwSoftApManagerInner hwSoftApManagerInner, Context context) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwSoftApManagerEx(hwSoftApManagerInner, context);
        }
        return null;
    }

    public static IHwWificondScannerImplEx getHwWificondScannerImplEx(Context context) {
        Log.d(TAG, "getHwWificondScannerImplEx() is callled");
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWificondScannerImplEx(context);
        }
        return null;
    }

    public static IHwScanRequestProxyEx getHwScanRequestProxyEx(IHwScanRequestProxyInner hwScanRequestProxyInner, Context context, WifiInjector wifiInjector) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwScanRequestProxyEx(hwScanRequestProxyInner, context, wifiInjector);
        }
        return null;
    }

    public static IHwTelphonyUtilsEx getHwTelphonyUtils() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwTelphonyUtils();
        }
        return null;
    }

    public static IHwLogCollectManagerEx getHwLogCollectManager(Context context) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwLogCollectManager(context);
        }
        return null;
    }

    public static IHwConstantUtilsEx getHwConstantUtils() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwConstantUtils();
        }
        return null;
    }

    public static IHwDevicePolicyManagerEx getHwDevicePolicyManager() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwDevicePolicyManager();
        }
        return null;
    }

    public static IHwWifiScanningServiceImplEx getHwWifiScanningServiceImplEx(Context context) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiScanningServiceImplEx(context);
        }
        return null;
    }

    public static IHwWifiStateMachineEx getHwWifiStateMachineEx(IHwWifiStateMachineInner hwWifiStateMachineInner) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiStateMachineEx(hwWifiStateMachineInner);
        }
        return null;
    }

    public static HiCoexManager getHiCoexManager() {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHiCoexManager();
        }
        return null;
    }

    public static IHwSupplicantP2pIfaceCallbackExt getHwSupplicantP2pIfaceCallbackExt(String iface, WifiP2pMonitor wifiMonitor) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwSupplicantP2pIfaceCallbackExt(iface, wifiMonitor);
        }
        return null;
    }

    public static IHwWifiP2pMonitorExt getHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner wifiP2pMonitor, WifiInjector wifiInjector) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiP2pMonitorExt(wifiP2pMonitor, wifiInjector);
        }
        return null;
    }

    public static IHwWifiP2pServiceTvEx getHwWifiP2pServiceTvEx(Context context, StateMachine p2pStateMachine, WifiInjector wifiInjector, IHwWifiP2pServiceInner hwWifiP2pServiceInner) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwWifiP2pServiceTvEx(context, p2pStateMachine, wifiInjector, hwWifiP2pServiceInner);
        }
        return null;
    }

    public static IHwWifiSettingsStoreEx getHwWifiSettingsStoreEx(Context context) {
        Factory wifiServiceFactory = getImplObject();
        if (wifiServiceFactory != null) {
            return wifiServiceFactory.getHwWifiSettingsStoreEx(context);
        }
        return null;
    }

    public static IHwWifiP2pServiceEx getHwWifiP2pServiceEx(Context context) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwWifiP2pServiceEx(context);
        }
        return null;
    }
}
