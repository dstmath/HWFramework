package com.android.server.wifi;

import android.content.Context;
import android.net.DhcpResults;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import java.util.ArrayList;
import java.util.List;

public interface HwWifiCHRStateManager {
    public static final String MAIN_IFACE = "wlan0";
    public static final int SCAN_FAILED = 10;
    public static final String TYPE_AP_VENDOR = "0";
    public static final String WIFI_CHR_STR = "CHR_EVENT";
    public static final String WIFI_STATE_PATH = "/sys/devices/platform/bcmdhd_wlan.1/wifi_open_state";

    void LinkPropertiesUpdate(RouteInfo routeInfo);

    void checkAppName(WifiConfiguration wifiConfiguration, Context context);

    int getDiffFreqStationRepeaterDuration();

    int getRepeaterConnFailedCount();

    int getRepeaterMaxClientCount();

    ScanResult getScanResultByBssid(String str);

    int getWifiRepeaterOpenedCount();

    long getWifiRepeaterWorkingTime();

    void handleIPv4SuccessException(DhcpResults dhcpResults);

    void handleSupplicantException();

    void isHiddenSsid(boolean z);

    void notifyNcSTATDftEvent();

    void reportHwCHRAccessNetworkEventInfoList(int i);

    void setCountryCode(String str);

    void setLastNetIdFromUI(int i);

    void setLastNetIdFromUI(WifiConfiguration wifiConfiguration, int i);

    void syncSetScanResultsList(List<ScanDetail> list);

    void updateAP(String str, int i, SupplicantState supplicantState, String str2);

    void updateAPOpenState();

    void updateAPSsid(String str);

    void updateAccessWebException(int i, int i2);

    void updateAccessWebException(int i, String str);

    void updateApkChangewWifiStatus(int i, String str, int i2, String str2);

    void updateBSSID(String str);

    void updateChannel(int i);

    void updateConnectType(String str);

    void updateDisableThreadName(String str);

    void updateGameBoostLag(String str, String str2, int i, int i2);

    void updateLeaseIP(long j);

    void updateLinkSpeed(int i);

    void updateMSSCHR(int i, int i2, int i3, ArrayList arrayList);

    void updateMultiGWCount(byte b);

    void updateRSSI(int i);

    void updateSSID(String str);

    void updateScreenState(boolean z);

    void updateSpeedInfo(String str, String str2, int i, int i2);

    void updateStaMAC(String str);

    void updateTimeStampSessionFinish(long j);

    void updateTimeStampSessionFirstConnect(long j);

    void updateTimeStampSessionStart(long j);

    void updateWifiAuthFailEvent(String str, int i);

    void updateWifiDirverException(String str);

    int updateWifiException(int i);

    void updateWifiException(int i, String str);

    void uploadAssocRejectException(int i);

    void uploadAssocRejectException(int i, String str);

    void uploadDFTEvent(int i, String str);

    void uploadDhcpException(String str);

    void uploadDisconnectException(int i);

    void uploadUserConnectFailed(int i);

    void uploadWifiStat();
}
