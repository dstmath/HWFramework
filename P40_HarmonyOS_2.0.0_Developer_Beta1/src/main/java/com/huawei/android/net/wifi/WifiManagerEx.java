package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.MacAddress;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.IWifiCfgCallback;
import android.net.wifi.IWifiRepeaterConfirmListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import com.huawei.annotation.HwSystemApi;
import java.util.List;

public class WifiManagerEx {
    public static final int INTERFACE_GET_WIFI_INVISABLE_INFO = 81;
    public static final String SERVICE_NAME = "WIFIPRO_SERVICE";
    public static final int TCP_TYPE = 6;
    public static final int TYPE_OF_GET_SELF_CONFIG = 1;
    public static final int TYPE_OF_SET_PEER_CONFIG = 1;
    public static final int TYPE_OF_SET_PEER_STATE_CHANGE = 2;
    public static final int UDP_TYPE = 17;
    @HwSystemApi
    public static final int WIFI_AP_STATE_DISABLED = 11;
    @HwSystemApi
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_MODE_ASSOCIATE_ASSISTANTE = 1002;
    public static final int WIFI_MODE_HWSHARE_LARGE_FILE = 1001;
    public static final int WIFI_MODE_RESET = 0;

    public interface ActionListenerEx {
        void onFailure(int i);

        void onSuccess();
    }

    public interface WifiCfgCallback {
        void onNotifyWifiCfg(int i, byte[] bArr);
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

    public static Bundle ctrlHwWifiNetwork(String pkgName, int interfaceId, Bundle data) {
        return HwFrameworkFactory.getHwInnerWifiManager().ctrlHwWifiNetwork(pkgName, interfaceId, data);
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
        HwFrameworkFactory.getHwInnerWifiManager().enableHiLinkHandshake(uiEnable, bssid, (WifiConfiguration) null);
    }

    public static void enableHiLinkHandshake(boolean uiEnable, String bssid, WifiConfiguration config) {
        HwFrameworkFactory.getHwInnerWifiManager().enableHiLinkHandshake(uiEnable, bssid, config);
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

    public static boolean controlHidataOptimize(String pkgName, int action, boolean enable) {
        return HwFrameworkFactory.getHwInnerWifiManager().controlHidataOptimize(pkgName, action, enable);
    }

    public static boolean limitSpeed(int mode, int reserve1, int reserve2) {
        return HwFrameworkFactory.getHwInnerWifiManager().updateLimitSpeedStatus(mode, reserve1, reserve2);
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

    public static WifiConfiguration getWifiConfigurationForNetworkId(WifiManager wifiMgr, int networkId) {
        List<WifiConfiguration> configs = wifiMgr.getConfiguredNetworks();
        if (configs == null) {
            return null;
        }
        for (WifiConfiguration config : configs) {
            if (networkId == config.networkId && !(config.selfAdded && config.numAssociation == 0)) {
                return config;
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
        return wifiManager.getHwWifiServiceMessenger();
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

    public static class WifiRepeaterMode {
        public static final int ALLOWED_BUT_LBS_DISABLED = 11;
        public static final int ALLOWED_BUT_P2P_CONNECTED = 10;
        public static final int ALLOWED_BUT_SSID_SAME_WITH_STA = 12;
        public static final int ALLOWED_BUT_STA_DFS_CHANNEL = 13;
        public static final int ALLOWED_WITHOUT_CONSTRAINT = 20;
        public static final int DISALLOWED_AS_NOT_SUPPORT_REPEATER = 3;
        public static final int DISALLOWED_AS_WIFI_DISABLED = 0;
        public static final int DISALLOWED_AS_WIFI_DISCONNECTED = 1;
        public static final int DISALLOWED_AS_WIFI_EAP_TLS = 2;

        private WifiRepeaterMode() {
        }
    }

    public static int getWifiRepeaterMode() {
        return HwFrameworkFactory.getHwInnerWifiManager().getWifiRepeaterMode();
    }

    public static void confirmWifiRepeater(int mode, IWifiRepeaterConfirmListener listener) {
        HwFrameworkFactory.getHwInnerWifiManager().confirmWifiRepeater(mode, listener);
    }

    public static boolean isWideBandwidthSupported(int type) {
        return HwFrameworkFactory.getHwInnerWifiManager().isWideBandwidthSupported(type);
    }

    public static int getApBandwidth() {
        return HwFrameworkFactory.getHwInnerWifiManager().getApBandwidth();
    }

    public static boolean reduceTxPower(boolean enable) {
        return HwFrameworkFactory.getHwInnerWifiManager().reduceTxPower(enable);
    }

    public static int[] getWideBandwidthChannels() {
        return HwFrameworkFactory.getHwInnerWifiManager().getWideBandwidthChannels();
    }

    public static boolean requestWifiAware(Context ctx, int requestId) {
        if (ctx == null) {
            return false;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().requestWifiAware(ctx, requestId);
    }

    public static boolean setWifiAwareInfo(Context ctx, int queryId, byte[] content) {
        if (ctx == null) {
            return false;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().setWifiAwareInfo(ctx, queryId, content);
    }

    public static byte[] getWifiAwareInfo(Context ctx, int queryId, byte[] filter) {
        if (ctx == null) {
            return new byte[0];
        }
        return HwFrameworkFactory.getHwInnerWifiManager().getWifiAwareInfo(ctx, queryId, filter);
    }

    public static void dcConnect(WifiConfiguration configuration, IWifiActionListener listener) {
        HwFrameworkFactory.getHwInnerWifiManager().dcConnect(configuration, listener);
    }

    public static boolean dcDisconnect() {
        return HwFrameworkFactory.getHwInnerWifiManager().dcDisconnect();
    }

    public static boolean reportSpeedMeasureResult(String info) {
        return HwFrameworkFactory.getHwInnerWifiManager().reportSpeedMeasureResult(info);
    }

    public static boolean setPerformanceMode(int mode) {
        return HwFrameworkFactory.getHwInnerWifiManager().setPerformanceMode(mode);
    }

    public static boolean setWifiMode(Context context, int mode) {
        if (context == null) {
            return false;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().setWifiMode(context.getOpPackageName(), mode);
    }

    public static int getWifiMode(Context context) {
        if (context == null) {
            return 0;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().getWifiMode(context.getOpPackageName());
    }

    public static int handleInterferenceParams(int type, int value) {
        return HwFrameworkFactory.getHwInnerWifiManager().handleInterferenceParams(type, value);
    }

    public static void setWifiSlicing(int uid, int protocolType, int mode) {
        if (protocolType == 6 || protocolType == 17) {
            HwFrameworkFactory.getHwInnerWifiManager().setWifiSlicing(uid, protocolType, mode);
        }
    }

    public static byte[] getSelfWifiCfgInfo(Context context, int cfgType) {
        if (context == null) {
            return new byte[0];
        }
        return HwFrameworkFactory.getHwInnerWifiManager().getSelfWifiCfgInfo(context.getOpPackageName(), cfgType);
    }

    public static int setPeerWifiCfgInfo(Context context, int cfgType, byte[] cfgData) {
        if (context == null) {
            return -1;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().setPeerWifiCfgInfo(context.getOpPackageName(), cfgType, cfgData);
    }

    public static int registerWifiCfgCallback(Context context, final WifiCfgCallback callback) {
        if (context == null || callback == null) {
            return -1;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().registerWifiCfgCallback(context.getOpPackageName(), new IWifiCfgCallback.Stub() {
            /* class com.huawei.android.net.wifi.WifiManagerEx.AnonymousClass1 */

            public void onNotifyWifiCfg(int cfgType, byte[] data) {
                WifiCfgCallback.this.onNotifyWifiCfg(cfgType, data);
            }
        });
    }

    public static int unregisterWifiCfgCallback(Context context) {
        if (context == null) {
            return -1;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().unregisterWifiCfgCallback(context.getOpPackageName());
    }

    public static int getP2pRecommendChannel(Context context) {
        if (context == null) {
            return 0;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().getP2pRecommendChannel(context.getOpPackageName());
    }

    public static int setWifiLowLatencyMode(Context context, boolean enabled) {
        if (context == null) {
            return -1;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().setWifiLowLatencyMode(context.getOpPackageName(), enabled);
    }

    public static int queryCsi(Context ctx, boolean enable, List<MacAddress> taList, int mask, IWifiActionListener listener) {
        if (ctx == null) {
            return -1;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().queryCsi(ctx.getOpPackageName(), enable, taList, mask, listener);
    }

    public static int querySniffer(Context ctx, int id, MacAddress filterMac, String channel, IWifiActionListener listener) {
        if (ctx == null) {
            return -1;
        }
        return HwFrameworkFactory.getHwInnerWifiManager().querySniffer(ctx.getOpPackageName(), id, filterMac, channel, listener);
    }

    public static String queryArp(Context ctx, List<String> filterIp) {
        if (ctx == null) {
            return "";
        }
        return HwFrameworkFactory.getHwInnerWifiManager().queryArp(ctx.getOpPackageName(), filterIp);
    }
}
