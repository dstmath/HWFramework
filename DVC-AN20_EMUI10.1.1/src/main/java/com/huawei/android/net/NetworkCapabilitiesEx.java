package com.huawei.android.net;

import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import com.huawei.annotation.HwSystemApi;

public class NetworkCapabilitiesEx {
    private NetworkCapabilities mNetworkCapabilities;

    @HwSystemApi
    public NetworkCapabilitiesEx(NetworkCapabilities networkCapabilities) {
        this.mNetworkCapabilities = networkCapabilities;
    }

    public NetworkCapabilitiesEx() {
        this.mNetworkCapabilities = new NetworkCapabilities();
    }

    @HwSystemApi
    public NetworkCapabilities getNetworkCapabilities() {
        return this.mNetworkCapabilities;
    }

    @HwSystemApi
    public NetworkCapabilitiesEx addTransportType(int transportType) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.addTransportType(transportType);
        return this;
    }

    @HwSystemApi
    public NetworkCapabilitiesEx addCapability(int capability) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.addCapability(capability);
        return this;
    }

    @HwSystemApi
    public NetworkCapabilitiesEx setNetworkSpecifier(StringNetworkSpecifierEx specifier) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.setNetworkSpecifier(specifier.getStringNetworkSpecifier());
        return this;
    }

    @HwSystemApi
    public NetworkCapabilitiesEx setNetworkSpecifier(MatchAllNetworkSpecifierEx specifier) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.setNetworkSpecifier(specifier.getMatchAllNetworkSpecifier());
        return this;
    }

    @HwSystemApi
    public NetworkSpecifier getNetworkSpecifier() {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        return networkCapabilities.getNetworkSpecifier();
    }

    public static int[] getTransportTypes(NetworkCapabilities nc) {
        return nc.getTransportTypes();
    }
}
