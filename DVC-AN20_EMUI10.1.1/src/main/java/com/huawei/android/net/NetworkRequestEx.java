package com.huawei.android.net;

import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class NetworkRequestEx {
    public static NetworkCapabilities getNetworkCapabilities(NetworkRequest networkRequest) {
        return networkRequest.networkCapabilities;
    }
}
