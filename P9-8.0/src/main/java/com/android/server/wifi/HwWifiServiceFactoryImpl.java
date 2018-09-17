package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import com.android.server.wifi.HwWifiServiceFactory.Factory;
import com.android.server.wifi.MSS.HwMSSHandler;

public class HwWifiServiceFactoryImpl implements Factory {
    private static final String TAG = "HwWifiServiceFactoryImpl";

    public HwWifiServiceManager getHwWifiServiceManager() {
        return HwWifiServiceManagerImpl.getDefault();
    }

    public HwWifiCHRStateManager getHwWifiCHRStateManager() {
        return HwWifiCHRStateManagerImpl.getDefault();
    }

    public HwWifiCHRService getHwWifiCHRService() {
        return HwWifiCHRServiceImpl.getDefault();
    }

    public HwWifiCHRConst getHwWifiCHRConst() {
        return HwWifiCHRConstImpl.getDefault();
    }

    public HwWifiStatStore getHwWifiStatStore() {
        return HwWifiStatStoreImpl.getDefault();
    }

    public void initWifiCHRService(Context cxt) {
        HwWifiCHRServiceImpl.init(cxt);
    }

    public void initWifiCHRStateManager(Context cxt) {
        HwWifiCHRStateManagerImpl.init(cxt);
    }

    public void initWifiStatStore(Context cxt) {
        HwWifiStatStoreImpl.init(cxt);
    }

    public HwWifiMonitor getHwWifiMonitor() {
        return HwWifiMonitorImpl.getDefault();
    }

    public HwIsmCoexWifiStateTrack getIsmCoexWifiStateTrack(Context context, WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        return IsmCoexWifiStateTrack.createIsmCoexWifiStateTrack(context, wifiStateMachine, wifiNative);
    }

    public HwSupplicantHeartBeat getHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        return SupplicantHeartBeat.createHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
    }

    public HwWifiDFTUtil getHwWifiDFTUtil() {
        return HwWifiDFTUtilImpl.getDefault();
    }

    public HwWifiDevicePolicy getHwWifiDevicePolicy() {
        return HwWifiDevicePolicyImpl.getDefault();
    }

    public HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo) {
        return HwMSSHandler.getDefault(context, wifiNative, wifiInfo);
    }
}
