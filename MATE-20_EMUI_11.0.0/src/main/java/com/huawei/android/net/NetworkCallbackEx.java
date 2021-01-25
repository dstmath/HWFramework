package com.huawei.android.net;

import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkCallbackEx extends ConnectivityManager.NetworkCallback {
    public void onPreCheck(Network network) {
        super.onPreCheck(network);
    }

    public void onAvailable(Network network, NetworkCapabilities networkCapabilities, LinkProperties linkProperties, boolean isBlocked) {
        super.onAvailable(network, networkCapabilities, linkProperties, isBlocked);
    }

    public void onNetworkSuspended(Network network) {
        super.onNetworkSuspended(network);
    }

    public void onNetworkResumed(Network network) {
        super.onNetworkResumed(network);
    }
}
