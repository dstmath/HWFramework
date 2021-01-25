package android.net.wifi;

import android.content.Context;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import java.util.HashMap;
import java.util.List;

public interface HwInnerWifiManager {
    int calculateSignalLevelHW(int i);

    int calculateSignalLevelHW(int i, int i2);

    void confirmWifiRepeater(int i, IWifiRepeaterConfirmListener iWifiRepeaterConfirmListener);

    boolean controlHidataOptimize(String str, int i, boolean z);

    Bundle ctrlHwWifiNetwork(String str, int i, Bundle bundle);

    void dcConnect(WifiConfiguration wifiConfiguration, IWifiActionListener iWifiActionListener);

    boolean dcDisconnect();

    boolean disableWifiFilter(IBinder iBinder, Context context);

    void enableHiLinkHandshake(boolean z, String str, WifiConfiguration wifiConfiguration);

    boolean enableWifiFilter(IBinder iBinder, Context context);

    boolean evaluateNetworkQuality(IHwQoECallback iHwQoECallback);

    void extendWifiScanPeriodForP2p(Context context, boolean z, int i);

    byte[] fetchWifiSignalInfoForVoWiFi();

    int getApBandwidth();

    List<String> getApLinkedStaList();

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    int[] getChannelListFor5G();

    String getConnectionRawPsk();

    boolean getHwMeteredHint(Context context);

    LinkProperties getLinkPropertiesForSlaveWifi();

    NetworkInfo getNetworkInfoForSlaveWifi();

    int getP2pRecommendChannel(String str);

    PPPOEInfo getPPPOEInfo();

    byte[] getSelfWifiCfgInfo(String str, int i);

    WifiInfo getSlaveWifiConnectionInfo();

    WifiDetectConfInfo getVoWifiDetectMode();

    int getVoWifiDetectPeriod();

    int[] getWideBandwidthChannels();

    byte[] getWifiAwareInfo(Context context, int i, byte[] bArr);

    int getWifiMode(String str);

    int getWifiRepeaterMode();

    String getWpaSuppConfig();

    int handleInterferenceParams(int i, int i2);

    void hwSetWifiAnt(Context context, String str, int i, int i2);

    boolean isRSDBSupported();

    boolean isSupportDualWifi();

    boolean isSupportVoWifiDetect();

    boolean isWideBandwidthSupported(int i);

    boolean reduceTxPower(boolean z);

    void refreshPackageWhitelist(int i, List<String> list);

    int registerWifiCfgCallback(String str, IWifiCfgCallback iWifiCfgCallback);

    boolean reportSpeedMeasureResult(String str);

    boolean requestWifiAware(Context context, int i);

    boolean requestWifiEnable(boolean z, String str);

    int setPeerWifiCfgInfo(String str, int i, byte[] bArr);

    boolean setPerformanceMode(int i);

    void setSlaveWifiNetworkSelectionPara(int i, int i2, int i3);

    void setSoftapDisassociateSta(String str);

    void setSoftapMacFilter(String str);

    boolean setVoWifiDetectMode(WifiDetectConfInfo wifiDetectConfInfo);

    boolean setVoWifiDetectPeriod(int i);

    void setWifiApEvaluateEnabled(boolean z);

    boolean setWifiAwareInfo(Context context, int i, byte[] bArr);

    boolean setWifiEnterpriseConfigEapMethod(int i, HashMap<String, String> hashMap);

    boolean setWifiMode(String str, int i);

    void setWifiSlicing(int i, int i2, int i3);

    boolean setWifiTxPower(int i);

    boolean startHwQoEMonitor(int i, int i2, IHwQoECallback iHwQoECallback);

    void startPPPOE(PPPOEConfig pPPOEConfig);

    boolean startPacketKeepalive(Message message);

    boolean stopHwQoEMonitor(int i);

    void stopPPPOE();

    boolean stopPacketKeepalive(Message message);

    int unregisterWifiCfgCallback(String str);

    boolean updateAppExperienceStatus(int i, int i2, long j, int i3);

    boolean updateAppRunningStatus(int i, int i2, int i3, int i4, int i5);

    boolean updateLimitSpeedStatus(int i, int i2, int i3);

    boolean updateVOWIFIStatus(int i);

    boolean updateWaveMapping(int i, int i2);

    void userHandoverWifi();
}
