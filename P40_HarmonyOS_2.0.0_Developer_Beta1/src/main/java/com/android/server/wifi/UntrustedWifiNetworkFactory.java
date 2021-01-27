package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Looper;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UntrustedWifiNetworkFactory extends NetworkFactory {
    private static final int SCORE_FILTER = Integer.MAX_VALUE;
    private static final String TAG = "UntrustedWifiNetworkFactory";
    private int mConnectionReqCount = 0;
    private final WifiConnectivityManager mWifiConnectivityManager;

    public UntrustedWifiNetworkFactory(Looper l, Context c, NetworkCapabilities f, WifiConnectivityManager connectivityManager) {
        super(l, c, TAG, f);
        this.mWifiConnectivityManager = connectivityManager;
        setScoreFilter(Integer.MAX_VALUE);
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        if (!networkRequest.networkCapabilities.hasCapability(14)) {
            int i = this.mConnectionReqCount + 1;
            this.mConnectionReqCount = i;
            if (i == 1) {
                this.mWifiConnectivityManager.setUntrustedConnectionAllowed(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        if (!networkRequest.networkCapabilities.hasCapability(14)) {
            int i = this.mConnectionReqCount;
            if (i == 0) {
                Log.e(TAG, "No valid network request to release");
                return;
            }
            int i2 = i - 1;
            this.mConnectionReqCount = i2;
            if (i2 == 0) {
                this.mWifiConnectivityManager.setUntrustedConnectionAllowed(false);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        UntrustedWifiNetworkFactory.super.dump(fd, pw, args);
        pw.println("UntrustedWifiNetworkFactory: mConnectionReqCount " + this.mConnectionReqCount);
    }

    public boolean hasConnectionRequests() {
        return this.mConnectionReqCount > 0;
    }
}
