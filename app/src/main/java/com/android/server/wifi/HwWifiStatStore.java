package com.android.server.wifi;

import android.net.wifi.SupplicantState;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import java.util.Date;
import java.util.List;

public interface HwWifiStatStore {
    public static final int HW_DHCP_AUTO_IP = 16;
    public static final int HW_DHCP_FAIL = 4;
    public static final int HW_DHCP_RENEW_FAIL = 5;
    public static final int HW_DHCP_RENEW_SUCC = 3;
    public static final int HW_DHCP_START = 0;
    public static final int HW_DHCP_STATIC_IP = 8;
    public static final int HW_DHCP_STOP = 1;
    public static final int HW_DHCP_SUCC = 2;

    void getWifiStabilitySsidStat(List<HwWifiDFTStabilitySsidStat> list);

    void getWifiStabilityStat(HwWifiDFTStabilityStat hwWifiDFTStabilityStat);

    List<ChrLogBaseModel> getWifiStatModel(Date date);

    void handleSupplicantStateChange(SupplicantState supplicantState, boolean z);

    void handleWiFiDnsStats(int i);

    void incrAccessWebRecord(int i, boolean z, boolean z2);

    void incrWebSpeedStatus(int i, int i2);

    boolean isConnectToNetwork();

    void readWifiCHRStat();

    void setAPSSID(String str);

    void setAbDisconnectFlg(String str);

    void setApMac(String str);

    void setApVendorInfo(String str);

    void setApencInfo(String str, String str2, String str3, String str4, String str5, String str6);

    void setCHRConnectingSartTimestamp(long j);

    void setMultiGWCount(byte b);

    void triggerCHRConnectingDuration(long j);

    void triggerConnectedDuration();

    void triggerTotalConnetedDuration(int i);

    void triggerTotalTrafficBytes();

    void updateAssocByABS();

    void updateCHRConnectFailedCount(int i);

    void updateConnectInternetFailedType(String str);

    void updateConnectState(boolean z);

    void updateCurrentConnectType(int i);

    void updateDhcpState(int i);

    void updateDisconnectCnt();

    void updateReasonCode(int i, int i2);

    void updateScCHRCount(int i);

    void updateScreenState(boolean z);

    @Deprecated
    void updateUserType(boolean z);

    void updateWifiState(int i);

    void updateWifiState(boolean z, boolean z2);

    void updateWifiTriggerState(boolean z);
}
