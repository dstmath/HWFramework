package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwUtil.HwConstantUtils;
import com.android.server.wifi.HwUtil.HwDevicePolicyManager;
import com.android.server.wifi.HwUtil.HwLogCollectManager;
import com.android.server.wifi.HwUtil.HwTelphonyUtils;
import com.android.server.wifi.HwUtil.IHwDevicePolicyManagerEx;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.MSS.HwMSSHandler;
import com.android.server.wifi.hwUtil.IHwConstantUtilsEx;
import com.android.server.wifi.hwUtil.IHwLogCollectManagerEx;
import com.android.server.wifi.hwUtil.IHwTelphonyUtilsEx;
import com.android.server.wifi.hwcoex.HiCoexManagerImpl;
import com.android.server.wifi.p2p.HwSupplicantP2pIfaceCallbackExt;
import com.android.server.wifi.p2p.HwSupplicantP2pIfaceHalEx;
import com.android.server.wifi.p2p.HwWifiP2pMonitorExt;
import com.android.server.wifi.p2p.HwWifiP2pNativeEx;
import com.android.server.wifi.p2p.HwWifiP2pServiceTvEx;
import com.android.server.wifi.p2p.IHwSupplicantP2pIfaceCallbackExt;
import com.android.server.wifi.p2p.IHwSupplicantP2pIfaceHalEx;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorExt;
import com.android.server.wifi.p2p.IHwWifiP2pMonitorInner;
import com.android.server.wifi.p2p.IHwWifiP2pNativeEx;
import com.android.server.wifi.p2p.IHwWifiP2pServiceInner;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
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

    public HwSupplicantHeartBeat getHwSupplicantHeartBeat(ClientModeImpl wifiStateMachine, WifiNative wifiNative) {
        return SupplicantHeartBeat.createHwSupplicantHeartBeat(wifiStateMachine, wifiNative);
    }

    public HwWifiDevicePolicy getHwWifiDevicePolicy() {
        return HwWifiDevicePolicyImpl.getDefault();
    }

    public HwMSSHandlerManager getHwMSSHandlerManager(Context context, WifiNative wifiNative, WifiInfo wifiInfo) {
        return HwMSSHandler.getDefault(context, wifiNative, wifiInfo);
    }

    public IHwSupplicantStaIfaceHalEx getHwSupplicantStaIfaceHalEx(IHwSupplicantStaIfaceHalInner supplicantStaIfaceHal, WifiMonitor wifiMonitor) {
        return HwSupplicantStaIfaceHalEx.createHwSupplicantStaIfaceHalEx(supplicantStaIfaceHal, wifiMonitor);
    }

    public IHwSupplicantStaNetworkHalEx getHwSupplicantStaNetworkHalEx(IHwSupplicantStaNetworkHalInner supplicantStaNetworkHal, WifiMonitor wifiMonitor, String ifaceName) {
        return HwSupplicantStaNetworkHalEx.createHwSupplicantStaNetworkHalEx(supplicantStaNetworkHal, wifiMonitor, ifaceName);
    }

    public IHwWifiNativeEx getHwWifiNativeEx(IHwWifiNativeInner hwWifiNativeInner, SupplicantStaIfaceHal staIfaceHal) {
        return HwWifiNativeEx.createHwWifiNativeEx(hwWifiNativeInner, staIfaceHal);
    }

    public IHwWifiApConfigStoreEx getHwWifiApConfigStoreEx() {
        return HwWifiApConfigStoreEx.getDefault();
    }

    public IHwSupplicantP2pIfaceHalEx getHwSupplicantP2pIfaceHalEx(SupplicantP2pIfaceHal supplicantP2pIfaceHal, WifiP2pMonitor monitor) {
        return HwSupplicantP2pIfaceHalEx.createHwSupplicantP2pIfaceHalEx(supplicantP2pIfaceHal, monitor);
    }

    public IHwWifiP2pNativeEx getHwWifiP2pNativeEx(SupplicantP2pIfaceHal p2pIfaceHal) {
        return HwWifiP2pNativeEx.createHwWifiP2pNativeEx(p2pIfaceHal);
    }

    public IHwWifiConfigManagerEx getHwWifiConfigManagerEx(IHwWifiConfigManagerInner hwWifiCofigManagerInner, Context context) {
        return HwWifiConfigManagerEx.createHwWifiConfigManagerEx(hwWifiCofigManagerInner, context);
    }

    public IHwWifiScoreReportEx getHwWifiScoreReportEx() {
        return HwWifiScoreReportEx.getDefault();
    }

    public IHwSoftApManagerEx getHwSoftApManagerEx(IHwSoftApManagerInner hwSoftApManagerInner, Context context) {
        return HwSoftApManagerEx.createHwSoftApManagerEx(hwSoftApManagerInner, context);
    }

    public IHwWificondScannerImplEx getHwWificondScannerImplEx(Context context) {
        return HwWificondScannerImplEx.createHwWificondScannerImplEx(context);
    }

    public IHwScanRequestProxyEx getHwScanRequestProxyEx(IHwScanRequestProxyInner hwScanRequestProxyInner, Context context, WifiInjector wifiInjector) {
        return HwScanRequestProxyEx.createHwScanRequestProxyEx(hwScanRequestProxyInner, context, wifiInjector);
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

    public IHwWifiScanningServiceImplEx getHwWifiScanningServiceImplEx(Context contex) {
        return HwWifiScanningServiceImplEx.createHwWifiScanningServiceImplEx(contex);
    }

    public IHwWifiStateMachineEx getHwWifiStateMachineEx(IHwWifiStateMachineInner hwWifiStateMachineInner) {
        return HwWifiStateMachineEx.createHwWifiStateMachineEx(hwWifiStateMachineInner);
    }

    public HiCoexManager getHiCoexManager() {
        return HiCoexManagerImpl.getInstance();
    }

    public IHwSupplicantP2pIfaceCallbackExt getHwSupplicantP2pIfaceCallbackExt(String iface, WifiP2pMonitor wifiMonitor) {
        return HwSupplicantP2pIfaceCallbackExt.createHwSupplicantP2pIfaceCallbackExt(iface, wifiMonitor);
    }

    public IHwWifiP2pMonitorExt getHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner wifiP2pMonitor, WifiInjector wifiInjector) {
        return HwWifiP2pMonitorExt.createHwWifiP2pMonitorExt(wifiP2pMonitor, wifiInjector);
    }

    public HwWifiP2pServiceTvEx getHwWifiP2pServiceTvEx(Context context, StateMachine p2pStateMachine, WifiInjector wifiInjector, IHwWifiP2pServiceInner hwWifiP2pServiceInner) {
        return HwWifiP2pServiceTvEx.createHwWifiP2pServiceTvEx(context, p2pStateMachine, wifiInjector, hwWifiP2pServiceInner);
    }

    public IHwWifiSettingsStoreEx getHwWifiSettingsStoreEx(Context context) {
        return HwWifiSettingsStore.createHwWifiSettingsStore(context);
    }
}
