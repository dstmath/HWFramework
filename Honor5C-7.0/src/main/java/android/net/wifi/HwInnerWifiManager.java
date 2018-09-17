package android.net.wifi;

import android.content.Context;
import java.util.HashMap;
import java.util.List;

public interface HwInnerWifiManager {
    int calculateSignalLevelHW(int i);

    void enableHiLinkHandshake(boolean z, String str);

    byte[] fetchWifiSignalInfoForVoWiFi();

    List<String> getApLinkedStaList();

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    int[] getChannelListFor5G();

    boolean getHwMeteredHint(Context context);

    PPPOEInfo getPPPOEInfo();

    WifiDetectConfInfo getVoWifiDetectMode();

    int getVoWifiDetectPeriod();

    String getWpaSuppConfig();

    boolean isSupportVoWifiDetect();

    void setSoftapDisassociateSta(String str);

    void setSoftapMacFilter(String str);

    boolean setVoWifiDetectMode(WifiDetectConfInfo wifiDetectConfInfo);

    boolean setVoWifiDetectPeriod(int i);

    void setWifiApEvaluateEnabled(boolean z);

    boolean setWifiEnterpriseConfigEapMethod(int i, HashMap<String, String> hashMap);

    void startPPPOE(PPPOEConfig pPPOEConfig);

    void stopPPPOE();

    void userHandoverWifi();
}
