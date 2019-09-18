package android.net.wifi;

import android.content.Context;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.os.IBinder;
import android.os.Message;
import java.util.HashMap;
import java.util.List;

public interface HwInnerWifiManager {
    int calculateSignalLevelHW(int i);

    int calculateSignalLevelHW(int i, int i2);

    boolean disableWifiFilter(IBinder iBinder, Context context);

    void enableHiLinkHandshake(boolean z, String str);

    boolean enableWifiFilter(IBinder iBinder, Context context);

    boolean evaluateNetworkQuality(IHwQoECallback iHwQoECallback);

    void extendWifiScanPeriodForP2p(Context context, boolean z, int i);

    byte[] fetchWifiSignalInfoForVoWiFi();

    List<String> getApLinkedStaList();

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    int[] getChannelListFor5G();

    String getConnectionRawPsk();

    boolean getHwMeteredHint(Context context);

    PPPOEInfo getPPPOEInfo();

    List<String> getSupportList();

    WifiDetectConfInfo getVoWifiDetectMode();

    int getVoWifiDetectPeriod();

    String getWpaSuppConfig();

    void hwSetWifiAnt(Context context, String str, int i, int i2);

    boolean isRSDBSupported();

    boolean isSupportVoWifiDetect();

    void notifyUIEvent(int i);

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

    boolean startPacketKeepalive(Message message);

    boolean stopHwQoEMonitor(int i);

    void stopPPPOE();

    boolean stopPacketKeepalive(Message message);

    boolean updateAppExperienceStatus(int i, int i2, long j, int i3);

    boolean updateAppRunningStatus(int i, int i2, int i3, int i4, int i5);

    boolean updateVOWIFIStatus(int i);

    boolean updateWaveMapping(int i, int i2);

    boolean updatelimitSpeedStatus(int i, int i2, int i3);

    void userHandoverWifi();
}
