package com.android.server.connectivity.tethering;

import android.net.LinkProperties;
import android.net.util.SharedLog;
import android.os.Handler;

public class OffloadController {
    private static final String TAG = OffloadController.class.getSimpleName();
    private final Handler mHandler;
    private final SharedLog mLog;
    private LinkProperties mUpstreamLinkProperties;

    public OffloadController(Handler h, SharedLog log) {
        this.mHandler = h;
        this.mLog = log.forSubComponent(TAG);
    }

    public void start() {
        this.mLog.i("tethering offload not supported");
    }

    public void stop() {
        this.mUpstreamLinkProperties = null;
    }

    public void setUpstreamLinkProperties(LinkProperties lp) {
        this.mUpstreamLinkProperties = lp;
    }
}
