package com.huawei.android.net;

import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import com.huawei.annotation.HwSystemApi;

public class NetworkCapabilitiesEx {
    @HwSystemApi
    public static final int INVALID_TYPE_5G = -1;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP0 = 25;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP1 = 26;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP2 = 27;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP3 = 28;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP4 = 29;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP5 = 30;
    @HwSystemApi
    public static final int NET_CAPABILITY_BIP6 = 31;
    @HwSystemApi
    public static final int NET_CAPABILITY_INTERNAL_DEFAULT = 32;
    @HwSystemApi
    public static final int NET_CAPABILITY_MMS = 0;
    @HwSystemApi
    public static final int NET_CAPABILITY_NOT_RESTRICTED = 13;
    @HwSystemApi
    public static final int NET_CAPABILITY_SLAVE_WIFI = 39;
    @HwSystemApi
    public static final int NET_CAPABILITY_SNSSAI1 = 33;
    @HwSystemApi
    public static final int NET_CAPABILITY_SNSSAI2 = 34;
    @HwSystemApi
    public static final int NET_CAPABILITY_SNSSAI3 = 35;
    @HwSystemApi
    public static final int NET_CAPABILITY_SNSSAI4 = 36;
    @HwSystemApi
    public static final int NET_CAPABILITY_SNSSAI5 = 37;
    @HwSystemApi
    public static final int NET_CAPABILITY_SNSSAI6 = 38;
    @HwSystemApi
    public static final int NET_CAPABILITY_XCAP = 9;
    @HwSystemApi
    public static final int TRANSPORT_CELLULAR = 0;
    @HwSystemApi
    public static final int TRANSPORT_WIFI = 1;
    private NetworkCapabilities mNetworkCapabilities;

    @HwSystemApi
    public NetworkCapabilitiesEx(NetworkCapabilities networkCapabilities) {
        this.mNetworkCapabilities = networkCapabilities;
    }

    public NetworkCapabilitiesEx() {
        this.mNetworkCapabilities = new NetworkCapabilities();
    }

    public static int[] getTransportTypes(NetworkCapabilities nc) {
        return nc.getTransportTypes();
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
    public NetworkCapabilitiesEx removeCapability(int capability) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.removeCapability(capability);
        return this;
    }

    @HwSystemApi
    public NetworkCapabilitiesEx setLinkUpstreamBandwidthKbps(int upKbps) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.setLinkUpstreamBandwidthKbps(upKbps);
        return this;
    }

    @HwSystemApi
    public NetworkCapabilitiesEx setLinkDownstreamBandwidthKbps(int downKbps) {
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities == null) {
            return null;
        }
        networkCapabilities.setLinkDownstreamBandwidthKbps(downKbps);
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
    public static boolean hasSnssaiCapability(NetworkCapabilities nc) {
        if (nc == null) {
            return false;
        }
        return NetworkCapabilities.hasSNSSAICapability(nc);
    }
}
