package com.android.server.wifi.wifipro;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import java.util.List;

public interface IHwSelfCureService {
    int getNetworkProbeRuslt(boolean z);

    boolean isDhcpFailedBssid(String str);

    boolean isDhcpFailedConfigKey(String str);

    void notifyChrEvent(int i, String str, String str2, int i2);

    void notifyDhcpResultsInternetOk(String str);

    void notifyFirstConnectProbeResult(int i);

    void notifySefCureCompleted(int i);

    void notifySelfCureIpConfigCompleted();

    boolean notifySelfCureIpConfigLostAndHandle(WifiConfiguration wifiConfiguration);

    void notifySelfCureWifiConnectedBackground();

    void notifySelfCureWifiDisconnected();

    void notifySelfCureWifiRoamingCompleted(String str);

    void notifySelfCureWifiScanResultsAvailable(boolean z);

    void requestChangeWifiStatus(boolean z);

    ScanResult updateScanDetailByWifiPro(ScanResult scanResult);

    List<ScanResult> updateScanResultByWifiPro(List<ScanResult> list);

    void uploadDisconnectedEvent(String str);
}
