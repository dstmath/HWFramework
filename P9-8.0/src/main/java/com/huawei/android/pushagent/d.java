package com.huawei.android.pushagent;

import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import com.huawei.android.pushagent.a.a;
import com.huawei.android.pushagent.utils.d.c;

final class d extends NetworkCallback {
    final /* synthetic */ PushService iz;

    d(PushService pushService) {
        this.iz = pushService;
    }

    public void onAvailable(Network network) {
        super.onAvailable(network);
        c.sh(PushService.TAG, "onAvailable");
        a.xv(10);
        this.iz.zd(this.iz.ih, network, true);
    }

    public void onLost(Network network) {
        super.onLost(network);
        c.sh(PushService.TAG, "onLost");
        a.xv(11);
        this.iz.zd(this.iz.ih, network, false);
    }

    public void onNetworkResumed(Network network) {
        super.onNetworkResumed(network);
        c.sh(PushService.TAG, "onNetworkResumed");
        a.xv(12);
        this.iz.zd(this.iz.ih, network, true);
    }
}
