package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import com.android.server.wifi.HwUtil.HwConstantUtils;
import com.android.server.wifi.HwUtil.HwDevicePolicyManager;
import com.android.server.wifi.HwUtil.HwLogCollectManager;
import com.android.server.wifi.HwUtil.HwTelphonyUtils;
import com.android.server.wifi.HwUtil.IHwConstantUtilsEx;
import com.android.server.wifi.HwUtil.IHwDevicePolicyManagerEx;
import com.android.server.wifi.HwUtil.IHwLogCollectManagerEx;
import com.android.server.wifi.HwUtil.IHwTelphonyUtilsEx;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.MSS.HwMSSHandler;
import com.android.server.wifi.p2p.HwSupplicantP2pIfaceCallbackExt;
import com.android.server.wifi.p2p.HwWifiP2pMonitorExt;
import com.android.server.wifi.p2p.IHwSupplicantP2pIfaceCallbackExt;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorExt;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorInner;
import com.android.server.wifi.p2p.WifiP2pMonitor;

public class HwWifiServiceFactoryImpl implements HwWifiServiceFactory.Factory {
    private static final String TAG = "HwWifiServiceFactoryImpl";

    public HwWifiServiceManager getHwWifiServiceManager() {
        return HwWifiServiceManagerImpl.getDefault();
    }

    public HwWifiCHRService getHwWifiCHRService() {
        return HwWifiCHRServiceImpl.getInstance();
    }

    public void initWifiCHRService(Context cxt) {
        HwWifiCHRServiceImpl.init(cxt);
    }

    public HwWifiMonitor getHwWifiMonitor() {
        return HwWifiMonitorImpl.getDefault();
    }

    public HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        return SupplicantHeartBeat.createHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
    }

    public HwWifiDevicePolicy getHwWifiDevicePolicy() {
        return HwWifiDevicePolicyImpl.getDefault();
    }

    public HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo) {
        return HwMSSHandler.getDefault(context, wifiNative, wifiInfo);
    }

    public IHwTelphonyUtilsEx getHwTelphonyUtils() {
        return HwTelphonyUtils.getDefault();
    }

    public IHwLogCollectManagerEx getHwLogCollectManager(Context context) {
        return HwLogCollectManager.createHwLogCollectManager(context);
    }

    public IHwConstantUtilsEx getHwConstantUtils() {
        return HwConstantUtils.getDefault();
    }

    public IHwDevicePolicyManagerEx getHwDevicePolicyManager() {
        return HwDevicePolicyManager.createHwDevicePolicyManager();
    }

    public IHwSupplicantP2pIfaceCallbackExt getHwSupplicantP2pIfaceCallbackExt(String iface, WifiP2pMonitor wifiMonitor) {
        return HwSupplicantP2pIfaceCallbackExt.createHwSupplicantP2pIfaceCallbackExt(iface, wifiMonitor);
    }

    public IHwWifiP2pMonitorExt getHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner wifiP2pMonitor, WifiInjector wifiInjector) {
        return HwWifiP2pMonitorExt.createHwWifiP2pMonitorExt(wifiP2pMonitor, wifiInjector);
    }
}
