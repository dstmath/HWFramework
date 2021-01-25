package com.android.server.wifi;

public class NetworkUpdateResult {
    boolean credentialChanged;
    boolean ipChanged;
    boolean isNewNetwork;
    int netId;
    boolean proxyChanged;

    public NetworkUpdateResult(int id) {
        this.isNewNetwork = false;
        this.netId = id;
        this.ipChanged = false;
        this.proxyChanged = false;
        this.credentialChanged = false;
    }

    public NetworkUpdateResult(boolean ip, boolean proxy, boolean credential) {
        this.isNewNetwork = false;
        this.netId = -1;
        this.ipChanged = ip;
        this.proxyChanged = proxy;
        this.credentialChanged = credential;
    }

    public void setNetworkId(int id) {
        this.netId = id;
    }

    public int getNetworkId() {
        return this.netId;
    }

    public boolean hasIpChanged() {
        return this.ipChanged;
    }

    public boolean hasProxyChanged() {
        return this.proxyChanged;
    }

    public boolean hasCredentialChanged() {
        return this.credentialChanged;
    }

    public boolean isNewNetwork() {
        return this.isNewNetwork;
    }

    public void setIsNewNetwork(boolean isNew) {
        this.isNewNetwork = isNew;
    }

    public boolean isSuccess() {
        return this.netId != -1;
    }
}
