package com.android.server.wifi;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Message;

public interface IHwWifiStateMachineEx {
    boolean checkForceReconnect(NetworkInfo networkInfo, WifiConfiguration wifiConfiguration, boolean z);

    boolean handleHwPrivateMsgInConnectModeState(Message message);

    boolean handleHwPrivateMsgInConnectedState(Message message);

    boolean handleHwPrivateMsgInDefaultState(Message message);

    boolean handleHwPrivateMsgInObtainingIpState(Message message);

    void handleWifiproPrivateStatus(int i);

    WifiInfo hwSyncRequestConnectionInfo(WifiInfo wifiInfo);

    boolean ignoreEnterConnectedStateByWifipro();

    boolean isNeedIgnoreConnect();

    boolean isTempCreated(WifiConfiguration wifiConfiguration);
}
