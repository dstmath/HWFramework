package com.android.server.wifi;

import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkInfo;
import android.net.StaticIpConfiguration;
import android.net.ip.IpClientManager;
import android.os.Message;
import com.android.internal.util.State;

public interface IHwWifiStateMachineInner {
    State getConnectedState();

    State getDisconnectedState();

    IpClientManager getIpClient();

    String getLastBssid();

    LinkProperties getLinkProperties();

    NetworkAgent getNetworkAgent();

    NetworkInfo getNetworkInfo();

    NetworkAgent getNewWifiNetworkAgent();

    State getObtainingIpState();

    int getSelfCureNetworkId();

    WifiNative getWifiNative();

    void handleStaticIpConfig(IpClientManager ipClientManager, WifiNative wifiNative, StaticIpConfiguration staticIpConfiguration);

    void hwHandleNetworkDisconnect();

    void hwSendConnectedState();

    void hwSendNetworkStateChangeBroadcast(String str);

    boolean hwSetNetworkDetailedState(NetworkInfo.DetailedState detailedState);

    void hwStopIpClient();

    boolean ignoreEnterConnectedState();

    boolean isWifiProEvaluatingAP();

    void notifySelfCureComplete(boolean z, int i);

    void notifyWifiConnectedBackgroundReady();

    void reportPortalNetworkStatus();

    void sendUpdateDnsServersRequest(Message message, LinkProperties linkProperties);

    void setWifiBackgroundStatus(boolean z);

    void startSelfCureReconnect();

    void startSelfCureWifiReassoc(int i);

    void startSelfCureWifiReset();

    void stopSelfCureWifi(int i);

    void triggerInvalidlinkNetworkMonitor();

    void updateNetworkConcurrently();

    void updateWifiBackgroudStatus(int i);

    void updateWifiproWifiConfiguration(Message message);

    void wifiNetworkExplicitlySelected();

    void wifiNetworkExplicitlyUnselected();
}
