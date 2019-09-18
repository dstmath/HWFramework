package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import java.util.ArrayList;

public interface HwWifiCHRService {
    void addRepeaterConnFailedCount(int i);

    void addWifiRepeaterOpenedCount(int i);

    void connectFromUserByConfig(WifiConfiguration wifiConfiguration);

    void handleSupplicantException();

    void handleWiFiDnsStats(int i);

    void incrAccessWebRecord(int i, boolean z, boolean z2);

    void reportHwCHRAccessNetworkEventInfoList(int i);

    void setBackgroundScanReq(boolean z);

    void setGameKogScene(int i);

    void setIpType(int i);

    void setRepeaterMaxClientCount(int i);

    void setWeChatScene(int i);

    void setWifiRepeaterFreq(int i);

    void setWifiRepeaterStatus(boolean z);

    void setWifiRepeaterWorkingTime(long j);

    void txPwrBoostChrStatic(Boolean bool, int i, int i2, int i3, int i4, int i5);

    void updataWeChartStatic(int i, int i2, int i3, int i4, int i5);

    void updateABSTime(String str, int i, int i2, long j, long j2, long j3, long j4);

    void updateAPOpenState();

    void updateAPVendorInfo(String str);

    void updateAccessWebException(int i, String str);

    void updateApkChangeWifiConfig(int i, String str, WifiConfiguration wifiConfiguration, WifiConfiguration wifiConfiguration2, Bundle bundle);

    void updateApkChangewWifiStatus(int i, String str);

    void updateArpSummery(boolean z, int i, int i2);

    void updateAssocByABS();

    void updateConnectType(String str);

    void updateDhcpState(int i);

    void updateGameBoostLag(String str, String str2, int i, int i2);

    void updateGameBoostStatic(String str, boolean z);

    void updateMSSCHR(int i, int i2, int i3, ArrayList arrayList);

    void updateMSSState(String str);

    void updateMssSucCont(int i, int i2);

    void updateMultiGWCount(byte b);

    void updatePortalConnection(int i);

    void updatePortalStatus(int i);

    void updateRepeaterOpenOrCloseError(int i, int i2, String str);

    void updateScCHRCount(int i);

    void updateWIFIConfiguraionByConfig(WifiConfiguration wifiConfiguration);

    void updateWifiAuthFailEvent(String str, int i);

    void updateWifiException(int i, String str);

    void updateWifiTriggerState(boolean z);

    void uploadAssocRejectException(int i);

    void uploadDFTEvent(int i, Bundle bundle);

    void uploadDFTEvent(int i, String str);

    void uploadDhcpException(String str);

    void uploadDisconnectException(int i);
}
