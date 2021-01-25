package com.huawei.android.net;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Looper;
import com.huawei.annotation.HwSystemApi;
import java.io.FileDescriptor;
import java.io.PrintWriter;

@HwSystemApi
public class NetworkFactoryBridge extends NetworkFactory {
    private NetworkFactoryEx mNetworkFactoryEx;

    NetworkFactoryBridge(Looper looper, Context context, String logTag, NetworkCapabilities filter) {
        super(looper, context, logTag, filter);
    }

    /* access modifiers changed from: package-private */
    public void setNetworkFactoryEx(NetworkFactoryEx networkFactoryEx) {
        this.mNetworkFactoryEx = networkFactoryEx;
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        NetworkFactoryEx networkFactoryEx = this.mNetworkFactoryEx;
        if (networkFactoryEx != null) {
            networkFactoryEx.needNetworkFor(networkRequest, score);
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        NetworkFactoryEx networkFactoryEx = this.mNetworkFactoryEx;
        if (networkFactoryEx != null) {
            networkFactoryEx.releaseNetworkFor(networkRequest);
        }
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        NetworkFactoryEx networkFactoryEx = this.mNetworkFactoryEx;
        if (networkFactoryEx != null) {
            return networkFactoryEx.acceptRequest(request, score);
        }
        return false;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        NetworkFactoryEx networkFactoryEx = this.mNetworkFactoryEx;
        if (networkFactoryEx != null) {
            networkFactoryEx.dump(fd, writer, args);
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        NetworkFactoryEx networkFactoryEx = this.mNetworkFactoryEx;
        if (networkFactoryEx != null) {
            networkFactoryEx.log(s);
        }
    }
}
