package com.android.server.wifi;

class NetworkUpdateResult {
    boolean ipChanged;
    boolean isNewNetwork;
    int netId;
    boolean proxyChanged;

    public NetworkUpdateResult(int id) {
        this.isNewNetwork = false;
        this.netId = id;
        this.ipChanged = false;
        this.proxyChanged = false;
    }

    public NetworkUpdateResult(boolean ip, boolean proxy) {
        this.isNewNetwork = false;
        this.netId = -1;
        this.ipChanged = ip;
        this.proxyChanged = proxy;
    }

    public void setNetworkId(int id) {
        this.netId = id;
    }

    public int getNetworkId() {
        return this.netId;
    }

    public void setIpChanged(boolean ip) {
        this.ipChanged = ip;
    }

    public boolean hasIpChanged() {
        return this.ipChanged;
    }

    public void setProxyChanged(boolean proxy) {
        this.proxyChanged = proxy;
    }

    public boolean hasProxyChanged() {
        return this.proxyChanged;
    }

    public boolean isNewNetwork() {
        return this.isNewNetwork;
    }

    public void setIsNewNetwork(boolean isNew) {
        this.isNewNetwork = isNew;
    }
}
