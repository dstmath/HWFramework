package com.huawei.wifi2;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Looper;
import android.util.Pair;
import android.util.SparseArray;
import android.util.wifi.HwHiLog;

public class HwWifi2NetworkFactory extends NetworkFactory {
    private static final int SCORE_FILTER = 10;
    private static final String TAG = "HwWifi2NetworkFactory";
    private final SparseArray<NetworkRequest> mNetworkRequests = new SparseArray<>();
    private String mRequestPackageName = "";
    private int mRequestUid = -1;
    private final HwWifi2Injector mWifiInjector;

    public HwWifi2NetworkFactory(Looper looper, Context context, NetworkCapabilities nc, HwWifi2Injector wifiInjector) {
        super(looper, context, TAG, nc);
        this.mWifiInjector = wifiInjector;
        setScoreFilter(SCORE_FILTER);
    }

    public boolean acceptRequest(NetworkRequest networkRequest, int score) {
        if (networkRequest.hasCapability(39)) {
            return true;
        }
        HwHiLog.i(TAG, false, "acceptRequest not ask for slave wifi", new Object[0]);
        return false;
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        if (!networkRequest.hasCapability(39)) {
            HwHiLog.i(TAG, false, "needNetworkFor not ask for slave wifi", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "needNetworkFor: %{public}s, score = %{public}d", new Object[]{networkRequest, Integer.valueOf(score)});
        this.mNetworkRequests.put(networkRequest.requestId, networkRequest);
        this.mWifiInjector.getWifiConnectivityManager().requestWifi2Network(1);
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        if (networkRequest.hasCapability(39)) {
            HwHiLog.i(TAG, false, "releaseNetworkFor: %{public}s", new Object[]{networkRequest});
            this.mNetworkRequests.remove(networkRequest.requestId);
            if (this.mNetworkRequests.size() == 0) {
                HwHiLog.i(TAG, false, "release wifi2 network", new Object[0]);
                this.mWifiInjector.getWifiConnectivityManager().releaseWifi2Network(1001);
            }
        }
    }

    public Pair<Integer, String> getSpecificNetworkRequestUidAndPackageName() {
        return Pair.create(Integer.valueOf(this.mRequestUid), this.mRequestPackageName);
    }
}
