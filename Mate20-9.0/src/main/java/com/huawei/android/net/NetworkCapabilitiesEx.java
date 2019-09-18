package com.huawei.android.net;

import android.net.NetworkCapabilities;

public class NetworkCapabilitiesEx {
    public static int[] getTransportTypes(NetworkCapabilities nc) {
        return nc.getTransportTypes();
    }
}
