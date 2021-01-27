package com.android.server.wifi.wifi2;

import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;

public interface IHwWifi2Service {
    LinkProperties getLinkPropertiesForSlaveWifi();

    NetworkInfo getNetworkInfoForSlaveWifi();

    WifiInfo getSlaveWifiConnectionInfo();

    void handleP2pConnectCommand(int i);

    void setSlaveWifiNetworkSelectionPara(int i, int i2, int i3);

    boolean setWifi2Enable(boolean z, int i);
}
