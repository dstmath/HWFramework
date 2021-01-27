package android.net.wifi;

import android.content.Context;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.NetworkInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import java.util.HashMap;
import java.util.List;

public class DummyHwInnerWifiManager implements HwInnerWifiManager {
    private static HwInnerWifiManager mInstance = new DummyHwInnerWifiManager();

    public HwInnerWifiManager getDefault() {
        return mInstance;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        return "";
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int calculateSignalLevelHW(int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int calculateSignalLevelHW(int freq, int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public String getWpaSuppConfig() {
        return "";
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setWifiEnterpriseConfigEapMethod(int eapMethod, HashMap<String, String> hashMap) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean getHwMeteredHint(Context context) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public PPPOEInfo getPPPOEInfo() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void startPPPOE(PPPOEConfig config) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void stopPPPOE() {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public List<String> getApLinkedStaList() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void setSoftapMacFilter(String macFilter) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void setSoftapDisassociateSta(String mac) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void userHandoverWifi() {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public Bundle ctrlHwWifiNetwork(String pkgName, int interfaceId, Bundle data) {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int[] getChannelListFor5G() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void setWifiApEvaluateEnabled(boolean isEnabled) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public byte[] fetchWifiSignalInfoForVoWiFi() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public WifiDetectConfInfo getVoWifiDetectMode() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setVoWifiDetectPeriod(int period) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int getVoWifiDetectPeriod() {
        return -1;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean isSupportVoWifiDetect() {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void enableHiLinkHandshake(boolean uiEnable, String bssid, WifiConfiguration config) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public String getConnectionRawPsk() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean requestWifiEnable(boolean flag, String reason) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setWifiTxPower(int power) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean startHwQoEMonitor(int monitorType, int period, IHwQoECallback callback) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean stopHwQoEMonitor(int monitorType) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean evaluateNetworkQuality(IHwQoECallback callback) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean updateVOWIFIStatus(int state) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean updateLimitSpeedStatus(int mode, int reserve1, int reserve2) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean controlHidataOptimize(String pkgName, int action, boolean enable) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean updateAppExperienceStatus(int uid, int experience, long rtt, int reserved) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void extendWifiScanPeriodForP2p(Context context, boolean isExtend, int iTimes) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void refreshPackageWhitelist(int type, List<String> list) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean isRSDBSupported() {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void hwSetWifiAnt(Context context, String iface, int mode, int operation) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean disableWifiFilter(IBinder token, Context context) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean enableWifiFilter(IBinder token, Context context) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean startPacketKeepalive(Message msg) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean stopPacketKeepalive(Message msg) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean updateWaveMapping(int location, int action) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int getWifiRepeaterMode() {
        return -1;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void confirmWifiRepeater(int mode, IWifiRepeaterConfirmListener listener) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean isWideBandwidthSupported(int type) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean reduceTxPower(boolean enable) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int getApBandwidth() {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int[] getWideBandwidthChannels() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean requestWifiAware(Context ctx, int requestId) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setWifiAwareInfo(Context ctx, int queryId, byte[] content) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public byte[] getWifiAwareInfo(Context ctx, int queryId, byte[] filter) {
        return new byte[0];
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void dcConnect(WifiConfiguration configuration, IWifiActionListener listener) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean dcDisconnect() {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean reportSpeedMeasureResult(String info) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setPerformanceMode(int mode) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean setWifiMode(String packageName, int mode) {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int getWifiMode(String packageName) {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int handleInterferenceParams(int type, int value) {
        return -1;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void setWifiSlicing(int uid, int protocolType, int mode) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public boolean isSupportDualWifi() {
        return false;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public void setSlaveWifiNetworkSelectionPara(int signalLevel, int callerUid, int needInternet) {
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public WifiInfo getSlaveWifiConnectionInfo() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public LinkProperties getLinkPropertiesForSlaveWifi() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public NetworkInfo getNetworkInfoForSlaveWifi() {
        return null;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public byte[] getSelfWifiCfgInfo(String packageName, int cfgType) {
        return new byte[0];
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int setPeerWifiCfgInfo(String packageName, int cfgType, byte[] cfgData) {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int registerWifiCfgCallback(String packageName, IWifiCfgCallback callback) {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int unregisterWifiCfgCallback(String packageName) {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int getP2pRecommendChannel(String packageName) {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int setWifiLowLatencyMode(String packageName, boolean enabled) {
        return 0;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int queryCsi(String packageName, boolean enable, List<MacAddress> list, int mask, IWifiActionListener listener) {
        return -1;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public int querySniffer(String packageName, int id, MacAddress filterMac, String channel, IWifiActionListener listener) {
        return -1;
    }

    @Override // android.net.wifi.HwInnerWifiManager
    public String queryArp(String packageName, List<String> list) {
        return "";
    }
}
