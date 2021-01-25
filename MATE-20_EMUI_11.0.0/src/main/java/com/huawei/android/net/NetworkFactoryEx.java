package com.huawei.android.net;

import android.content.Context;
import android.net.NetworkRequest;
import android.os.Looper;
import com.huawei.annotation.HwSystemApi;
import java.io.FileDescriptor;
import java.io.PrintWriter;

@HwSystemApi
public class NetworkFactoryEx {
    private NetworkFactoryBridge mNetworkFactory;

    public NetworkFactoryEx(Looper looper, Context context, String logTag, NetworkCapabilitiesEx filter) {
        this.mNetworkFactory = new NetworkFactoryBridge(looper, context, logTag, filter != null ? filter.getNetworkCapabilities() : null);
        this.mNetworkFactory.setNetworkFactoryEx(this);
    }

    public void setCapabilityFilter(NetworkCapabilitiesEx netCap) {
        NetworkFactoryBridge networkFactoryBridge = this.mNetworkFactory;
        if (networkFactoryBridge != null && netCap != null) {
            networkFactoryBridge.setCapabilityFilter(netCap.getNetworkCapabilities());
        }
    }

    public void setScoreFilter(int score) {
        NetworkFactoryBridge networkFactoryBridge = this.mNetworkFactory;
        if (networkFactoryBridge != null) {
            networkFactoryBridge.setScoreFilter(score);
        }
    }

    public void register() {
        NetworkFactoryBridge networkFactoryBridge = this.mNetworkFactory;
        if (networkFactoryBridge != null) {
            networkFactoryBridge.register();
        }
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }
}
