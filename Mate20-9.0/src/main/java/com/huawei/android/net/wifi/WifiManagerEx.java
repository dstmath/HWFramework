package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import java.util.List;

public class WifiManagerEx {

    public interface ActionListenerEx {
        void onFailure(int i);

        void onSuccess();
    }

    private static class ActionListenerInner implements WifiManager.ActionListener {
        private final ActionListenerEx inner;

        ActionListenerInner(ActionListenerEx actionListener) {
            this.inner = actionListener;
        }

        public void onSuccess() {
            this.inner.onSuccess();
        }

        public void onFailure(int reason) {
            this.inner.onFailure(reason);
        }
    }

    public static List<String> getApLinkedStaList(WifiManager obj) {
        return HwFrameworkFactory.getHwInnerWifiManager().getApLinkedStaList();
    }

    public static void setSoftapMacFilter(WifiManager obj, String macFilter) {
        HwFrameworkFactory.getHwInnerWifiManager().setSoftapMacFilter(macFilter);
    }

    public static void setSoftapDisassociateSta(WifiManager obj, String mac) {
        HwFrameworkFactory.getHwInnerWifiManager().setSoftapDisassociateSta(mac);
    }

    public static boolean setWifiApMaxSCB(WifiManager obj, WifiConfiguration wifiConfig, int maxNum) {
        return false;
    }

    public static boolean isSupportConnectManager(WifiManager obj) {
        return true;
    }

    public static boolean isSupportChannel(WifiManager obj) {
        return true;
    }

    public static void userHandoverWifi(WifiManager obj) {
        HwFrameworkFactory.getHwInnerWifiManager().userHandoverWifi();
    }

    public static void setWifiApEvaluateEnabled(WifiManager obj, boolean enabled) {
        HwFrameworkFactory.getHwInnerWifiManager().setWifiApEvaluateEnabled(enabled);
    }

    public static boolean registerVoWifiSignalDetectInterrupt(WifiManager obj, WifiManager.VoWifiSignalDetectInterruptCallback callback) {
        return obj.registerVoWifiSignalDetectInterrupt(callback);
    }

    public static boolean unregisterVoWifiSignalDetectInterrupt(WifiManager obj) {
        return obj.unregisterVoWifiSignalDetectInterrupt();
    }

    public static boolean setVoWifiDetectMode(WifiManager obj, WifiDetectConfInfo info) {
        return obj.setVoWifiDetectMode(info);
    }

    public static byte[] fetchWifiSignalInfoForVoWiFi() {
        return HwFrameworkFactory.getHwInnerWifiManager().fetchWifiSignalInfoForVoWiFi();
    }

    public static WifiDetectConfInfo getVoWifiDetectMode() {
        return HwFrameworkFactory.getHwInnerWifiManager().getVoWifiDetectMode();
    }

    public static boolean setVoWifiDetectPeriod(int period) {
        return HwFrameworkFactory.getHwInnerWifiManager().setVoWifiDetectPeriod(period);
    }

    public static int getVoWifiDetectPeriod() {
        return HwFrameworkFactory.getHwInnerWifiManager().getVoWifiDetectPeriod();
    }

    public static boolean isSupportVoWifiDetect() {
        return HwFrameworkFactory.getHwInnerWifiManager().isSupportVoWifiDetect();
    }

    public static void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        HwFrameworkFactory.getHwInnerWifiManager().enableHiLinkHandshake(uiEnable, bssid);
    }

    public static String getConnectionRawPsk() {
        return HwFrameworkFactory.getHwInnerWifiManager().getConnectionRawPsk();
    }

    public static boolean requestWifiEnable(boolean flag, String reason) {
        return HwFrameworkFactory.getHwInnerWifiManager().requestWifiEnable(flag, reason);
    }

    public static boolean setWifiTxPower(int power) {
        return HwFrameworkFactory.getHwInnerWifiManager().setWifiTxPower(power);
    }

    public static boolean startHwQoEMonitor(int monitorType, int period, IHwQoECallback callback) {
        return HwFrameworkFactory.getHwInnerWifiManager().startHwQoEMonitor(monitorType, period, callback);
    }

    public static boolean stopHwQoEMonitor(int monitorType) {
        return HwFrameworkFactory.getHwInnerWifiManager().stopHwQoEMonitor(monitorType);
    }

    public static boolean evaluateNetworkQuality(IHwQoECallback callback) {
        return HwFrameworkFactory.getHwInnerWifiManager().evaluateNetworkQuality(callback);
    }

    public static boolean updateVOWIFIStatus(int state) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateVOWIFIStatus(state);
    }

    public static boolean limitSpeed(int mode, int reserve1, int reserve2) {
        return HwFrameworkFactory.getHwInnerWifiManager().updatelimitSpeedStatus(mode, reserve1, reserve2);
    }

    public static List<String> getMplinkSupportAppList() {
        return HwFrameworkFactory.getHwInnerWifiManager().getSupportList();
    }

    public static void notifyUserActionEvent(int event) {
        HwFrameworkFactory.getHwInnerWifiManager().notifyUIEvent(event);
    }

    public static boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateAppRunningStatus(uid, type, status, scene, reserved);
    }

    public static boolean updateAppExperienceStatus(int uid, int experience, long rtt, int reserved) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateAppExperienceStatus(uid, experience, rtt, reserved);
    }

    public static void extendWifiScanPeriodForP2p(Context context, boolean bExtend, int iTimes) {
        HwFrameworkFactory.getHwInnerWifiManager().extendWifiScanPeriodForP2p(context, bExtend, iTimes);
    }

    public static void hwSetWifiAnt(Context context, String iface, int mode, int operation) {
        HwFrameworkFactory.getHwInnerWifiManager().hwSetWifiAnt(context, iface, mode, operation);
    }

    public static void save(WifiManager wifiMgr, WifiConfiguration config, ActionListenerEx listener) {
        wifiMgr.save(config, new ActionListenerInner(listener));
    }

    public static WifiConfiguration getWifiConfigurationForNetworkId(WifiManager wifiMgr, int networkId) {
        List<WifiConfiguration> configs = wifiMgr.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (networkId == config.networkId && (!config.selfAdded || config.numAssociation != 0)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static void stopSoftAp(WifiManager wifiManager) {
        if (wifiManager != null) {
            wifiManager.stopSoftAp();
        }
    }

    public static boolean isWifiApEnabled(WifiManager wifiManager) {
        if (wifiManager == null) {
            return false;
        }
        return wifiManager.isWifiApEnabled();
    }

    public static String getWifiApStateChangedAction() {
        return "android.net.wifi.WIFI_AP_STATE_CHANGED";
    }

    public static String getExtraWifiApState() {
        return "wifi_state";
    }

    public static int getWifiApStateDisabling() {
        return 10;
    }

    public static int getWifiApStateDisabled() {
        return 11;
    }

    public static int getWifiApStateEnabled() {
        return 13;
    }

    public static boolean disableWifiFilter(IBinder token, Context context) {
        return HwFrameworkFactory.getHwInnerWifiManager().disableWifiFilter(token, context);
    }

    public static boolean enableWifiFilter(IBinder token, Context context) {
        return HwFrameworkFactory.getHwInnerWifiManager().enableWifiFilter(token, context);
    }

    public static boolean startPacketKeepalive(Message msg) {
        return HwFrameworkFactory.getHwInnerWifiManager().startPacketKeepalive(msg);
    }

    public static boolean stopPacketKeepalive(Message msg) {
        return HwFrameworkFactory.getHwInnerWifiManager().stopPacketKeepalive(msg);
    }

    public static boolean updateWaveMapping(int location, int action) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateWaveMapping(location, action);
    }

    public static int getWifiApStateEnabling() {
        return 12;
    }

    public static int getWifiApState(WifiManager wifiManager) {
        return wifiManager.getWifiApState();
    }

    public static final int getDateActivityNotification() {
        return 1;
    }

    public static Messenger getWifiServiceMessenger(WifiManager wifiManager) {
        return wifiManager.getWifiServiceMessenger();
    }

    public static int getDataActivityIn() {
        return 1;
    }

    public static int getDataActivityInOut() {
        return 3;
    }

    public static int getDataActivityOut() {
        return 2;
    }

    public static void connect(WifiManager wifiMgr, int networkId, ActionListenerEx listener) {
        wifiMgr.connect(networkId, new ActionListenerInner(listener));
    }

    public static void connect(WifiManager wifiMgr, WifiConfiguration configuration, ActionListenerEx listener) {
        wifiMgr.connect(configuration, new ActionListenerInner(listener));
    }
}
