package android.net.wifi;

import android.content.Context;
import android.net.wifi.HwQoE.IHwQoECallback;
import java.util.HashMap;
import java.util.List;

public interface HwInnerWifiManager {
    int calculateSignalLevelHW(int i);

    int calculateSignalLevelHW(int i, int i2);

    void enableHiLinkHandshake(boolean z, String str);

    boolean evaluateNetworkQuality(IHwQoECallback iHwQoECallback);

    void extendWifiScanPeriodForP2p(Context context, boolean z, int i);

    byte[] fetchWifiSignalInfoForVoWiFi();

    List<String> getApLinkedStaList();

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    int[] getChannelListFor5G();

    String getConnectionRawPsk();

    boolean getHwMeteredHint(Context context);

    PPPOEInfo getPPPOEInfo();

    WifiDetectConfInfo getVoWifiDetectMode();

    int getVoWifiDetectPeriod();

    String getWpaSuppConfig();

    boolean isRSDBSupported();

    boolean isSupportVoWifiDetect();

    void refreshPackageWhitelist(int i, List<String> list);

    boolean requestWifiEnable(boolean z, String str);

    void setSoftapDisassociateSta(String str);

    void setSoftapMacFilter(String str);

    boolean setVoWifiDetectMode(WifiDetectConfInfo wifiDetectConfInfo);

    boolean setVoWifiDetectPeriod(int i);

    void setWifiApEvaluateEnabled(boolean z);

    boolean setWifiEnterpriseConfigEapMethod(int i, HashMap<String, String> hashMap);

    boolean setWifiTxPower(int i);

    boolean startHwQoEMonitor(int i, int i2, IHwQoECallback iHwQoECallback);

    void startPPPOE(PPPOEConfig pPPOEConfig);

    boolean stopHwQoEMonitor(int i);

    void stopPPPOE();

    boolean updateAppExperienceStatus(int i, int i2, long j, int i3);

    boolean updateAppRunningStatus(int i, int i2, int i3, int i4, int i5);

    boolean updateVOWIFIStatus(int i);

    void userHandoverWifi();
}
