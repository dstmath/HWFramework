package com.android.server.wifi;

import android.content.Context;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

public abstract class HwWifiCHRStateManager {
    public static final String MAIN_IFACE = "wlan0";
    public static final int SCAN_FAILED = 10;
    public static final String TYPE_AP_VENDOR = "0";
    public static final String WIFI_CHR_STR = "CHR_EVENT";
    public static final String WIFI_STATE_PATH = "/sys/devices/platform/bcmdhd_wlan.1/wifi_open_state";

    public abstract void LinkPropertiesUpdate(RouteInfo routeInfo);

    public abstract void checkAppName(WifiConfiguration wifiConfiguration, Context context);

    public abstract void clearDisconnectData();

    public abstract int getDhcpSerialNo();

    public abstract ScanResult getScanResultByBssid(String str);

    public abstract void handleCHREvents(String str);

    public abstract void handleIPv4SuccessException(Inet4Address inet4Address);

    public abstract void handleSupplicantException();

    public abstract void reportHwCHRAccessNetworkEventInfoList(int i);

    public abstract void resetWhenDisconnect();

    public abstract void setCurrentMsgIface(String str);

    public abstract void setLastNetIdFromUI(int i);

    public abstract void setLastNetIdFromUI(WifiConfiguration wifiConfiguration, int i);

    public abstract void syncSetScanResultsList(List<ScanDetail> list);

    public abstract void updateAP(int i, SupplicantState supplicantState, String str, int i2, WifiNative wifiNative);

    public abstract void updateAPMAC(String str);

    public abstract void updateAPSsid(SupplicantState supplicantState, WifiInfo wifiInfo);

    public abstract void updateAPSsid(String str);

    public abstract void updateAPSsidByEvent(String str);

    public abstract void updateAccessWebException(int i, int i2);

    public abstract void updateAccessWebException(int i, String str);

    public abstract void updateApMessage(String str, String str2, String str3, String str4, String str5, String str6);

    public abstract void updateApVendorInfo(String str);

    public abstract void updateApkChangewWifiStatus(int i, String str, int i2, String str2);

    public abstract void updateBSSID(String str);

    public abstract void updateChannel(int i);

    public abstract void updateConnectSuccessTime(long j);

    public abstract void updateConnectThreadName(String str);

    public abstract void updateConnectType(String str);

    public abstract void updateDNS(Collection<InetAddress> collection);

    public abstract void updateDhcpSerialNo(int i);

    public abstract void updateDisableThreadName(String str);

    public abstract void updateGateWay(String str);

    public abstract void updateLeaseIP(long j);

    public abstract void updateLinkSpeed(int i);

    public abstract void updateMultiGWCount(byte b);

    public abstract void updateRSSI(int i);

    public abstract void updateSSID(String str);

    public abstract void updateScreenState(boolean z);

    public abstract void updateSignalLevel(int i, int i2, int i3);

    public abstract void updateSpeedInfo(String str, String str2, int i, int i2);

    public abstract void updateStaMAC(String str);

    public abstract void updateStrucRoutes(String str);

    public abstract void updateTimeStampSessionFinish(long j);

    public abstract void updateTimeStampSessionFirstConnect(long j);

    public abstract void updateTimeStampSessionStart(long j);

    public abstract void updateWifiDirverException(String str);

    public abstract int updateWifiException(int i);

    public abstract void updateWifiException(int i, String str);

    public abstract void updateWifiExceptionByWifipro(int i, String str);

    public abstract void updateWifiIp(String str);

    public abstract void uploadAssocRejectExc(int i);

    public abstract void uploadAssocRejectException(int i);

    public abstract void uploadAssocRejectException(int i, String str);

    public abstract void uploadAssocRejectException(String str);

    public abstract void uploadConnectFailed();

    public abstract void uploadConnectFailed(int i, int i2);

    public abstract void uploadDFTEvent(int i);

    public abstract void uploadDhcpException(int i, int i2);

    public abstract void uploadDhcpException(String str);

    public abstract void uploadDisconnectExc(String str);

    public abstract void uploadDisconnectException(String str);

    public abstract void uploadFailureIfFailed(int i, int i2);

    public abstract void uploadTls12Stat(int i, int i2);

    public abstract void uploadUserConnectFailed(int i);

    public abstract void uploadWifiStat();

    public abstract void waitForDhcpStopping(String str);
}
