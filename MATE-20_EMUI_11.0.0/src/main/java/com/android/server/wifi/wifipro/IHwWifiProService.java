package com.android.server.wifi.wifipro;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import java.util.List;

public interface IHwWifiProService {
    boolean allowAutoJoinDisabledNetworkAgain(WifiConfiguration wifiConfiguration);

    boolean allowCheckPortalNetwork(String str, String str2);

    void disconnectePoorWifi();

    String getCurrentPackageNameFromWifiPro();

    int getNetwoksHandoverType();

    long getRttDuration(int i, int i2);

    long getRttSegs(int i, int i2);

    List<String> getScanfrequencys();

    Bundle getWifiDisplayInfo(NetworkInfo networkInfo);

    void handleWiFiConnected(WifiConfiguration wifiConfiguration, boolean z);

    void handleWiFiDisconnected();

    boolean isAutoJoinAllowedSetTargetBssid(WifiConfiguration wifiConfiguration, String str);

    boolean isBssidMatchedBlacklist(String str);

    boolean isDualbandScanning();

    boolean isPortalNotifyOn();

    boolean isWifiEvaluating();

    void notifyApkChangeWifiStatus(boolean z, String str);

    void notifyAutoConnectManagerDisconnected();

    void notifyEnableSameNetworkId(int i);

    void notifyForegroundAppChanged(String str);

    void notifyNetworkRoamingCompleted(String str);

    void notifyNetworkUserConnect(boolean z);

    void notifyScanGenieEvent(int i);

    void notifyTcpStatResult(List<String> list);

    void notifyUseFullChannels();

    void notifyWifiConnFailedInfo(WifiConfiguration wifiConfiguration, String str, int i, int i2);

    void notifyWifiConnectedBackground();

    void notifyWifiConnectivityRoamingCompleted();

    void notifyWifiDisconnected(Intent intent);

    void notifyWifiMonitorDisconnected();

    void notifyWifiRoamingStarted();

    void releaseBlackListBssid(WifiConfiguration wifiConfiguration, boolean z);

    void sendMessageToHwDualBandStateMachine(int i);

    void setWiFiProScanResultList(List<ScanResult> list);

    void setWifiApEvaluateEnabled(boolean z);

    void updateDualBandSwitchEvent();

    void updatePopUpNetworkRssi(String str, int i);

    void userHandoverWifi();
}
