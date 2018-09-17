package com.android.server.wifi;

import android.net.wifi.SupplicantState;
import java.util.List;

public interface HwWifiStatStore {
    public static final int HW_DHCP_AUTO_IP = 16;
    public static final int HW_DHCP_FAIL = 4;
    public static final int HW_DHCP_RENEW_FAIL = 5;
    public static final int HW_DHCP_RENEW_START = 10;
    public static final int HW_DHCP_RENEW_SUCC = 3;
    public static final int HW_DHCP_START = 0;
    public static final int HW_DHCP_STATIC_IP = 8;
    public static final int HW_DHCP_STATIC_IP_SUCC = 9;
    public static final int HW_DHCP_STOP = 1;
    public static final int HW_DHCP_SUCC = 2;

    void checkScanResults(List<ScanDetail> list);

    void clearStatInfo();

    void getTemperatureCtrlParam();

    void getWifiDFT2StabilitySsidStat(List<HwWifiDFT2StabilitySsidStat> list);

    void getWifiDFT2StabilityStat(HwWifiDFT2StabilityStat hwWifiDFT2StabilityStat);

    void getWifiDFTAPKAction(List<HwWifiDFTAPKAction> list);

    int getWifiOpenStatDura();

    void handleSupplicantStateChange(SupplicantState supplicantState, boolean z);

    void handleWiFiDnsStats(int i);

    void incrAccessWebRecord(int i, boolean z, boolean z2);

    void incrWebSpeedStatus(int i, int i2);

    boolean isConnectToNetwork();

    void readWifiCHRStat();

    void setAPSSID(String str);

    void setAbDisconnectFlg(String str, int i);

    void setAccessWebFlag(String str);

    void setApFreqParam(short s);

    void setApMac(String str);

    void setApRoamingParam(byte b);

    void setApVendorInfo(String str);

    void setApencInfo(String str, String str2);

    void setBackgroundScanReq(boolean z);

    void setCHRConnectingSartTimestamp(long j);

    void setGameKogScene(int i);

    void setIPv4SuccFlag();

    void setMultiGWCount(byte b);

    void setRouterModelParam(String str);

    void setRssi(int i);

    void setWeChatScene(int i);

    void setWifiConnectType(String str);

    void triggerCHRConnectingDuration(long j);

    void triggerConnectedDuration();

    void triggerTotalConnetedDuration(int i);

    void triggerTotalTrafficBytes();

    void txPwrBoostChrStatic(Boolean bool, int i, int i2, int i3, int i4, int i5);

    void updataWeChartStatic(int i, int i2, int i3, int i4, int i5);

    void updateABSTime(String str, int i, int i2, long j, long j2, long j3, long j4);

    void updateApkChangewWifiStatus(int i, String str, int i2);

    void updateAssocByABS();

    void updateCHRConnectFailedCount(int i);

    void updateConnectCnt();

    void updateConnectInternetFailedType(String str);

    @Deprecated
    void updateConnectState(boolean z);

    void updateCurrentConnectType(int i);

    void updateDhcpState(int i);

    void updateDisconnectCnt();

    void updateGameBoostStatic(String str, boolean z);

    void updateMssSucCont(int i, int i2);

    void updateReasonCode(int i, int i2);

    void updateScCHRCount(int i);

    void updateWifiState(boolean z, boolean z2);

    void updateWifiTriggerState(boolean z);
}
