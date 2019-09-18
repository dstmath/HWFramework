package com.android.server.hidata.mplink;

import android.net.NetworkInfo;
import android.telephony.ServiceState;

public interface IMpLinkStateObserverCallback {
    void onMobileDataSwitchChange(boolean z);

    void onMpLinkRequestTimeout(int i);

    void onMplinkSwitchChange(boolean z);

    void onSimulateHiBrainRequestForDemo(boolean z);

    void onTelephonyDataConnectionChanged(String str, String str2, int i);

    void onTelephonyDefaultDataSubChanged(int i);

    void onTelephonyServiceStateChanged(ServiceState serviceState, int i);

    void onVpnStateChange(boolean z);

    void onWifiNetworkStateChanged(NetworkInfo networkInfo);
}
