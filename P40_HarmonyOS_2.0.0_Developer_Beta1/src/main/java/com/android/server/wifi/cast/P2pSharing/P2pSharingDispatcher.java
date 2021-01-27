package com.android.server.wifi.cast.P2pSharing;

import android.content.Context;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.cast.CastOptChr;

public class P2pSharingDispatcher implements P2pSharingListener {
    private static final String TAG = "P2pSharing:P2pSharingDispatcher";
    private boolean isInP2pSharing;
    private boolean isSetUpCalled;
    private final Object lock;
    private P2pSharingInterface p2pSharingBase;

    private P2pSharingDispatcher() {
        this.lock = new Object();
        this.isInP2pSharing = false;
        this.isSetUpCalled = false;
    }

    public static P2pSharingDispatcher getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void setUp(Context context, boolean isGroupOwner) {
        HwHiLog.d(TAG, false, "setUp isGroupOwner:" + isGroupOwner, new Object[0]);
        if (!Utils.isWiFiConnected(context)) {
            HwHiLog.w(TAG, false, "wifi isn't connected, don't go p2psharing", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            if (this.isSetUpCalled) {
                HwHiLog.w(TAG, false, "set up has been called before", new Object[0]);
                return;
            }
            this.isSetUpCalled = true;
        }
        if (isGroupOwner) {
            this.p2pSharingBase = new P2pSharingServer();
        } else {
            this.p2pSharingBase = new P2pSharingClient();
            this.p2pSharingBase.setP2pSharingListener(this);
        }
        this.p2pSharingBase.setContext(context);
        this.p2pSharingBase.setUpTempConnection();
    }

    public void start() {
        if (!(this.p2pSharingBase instanceof P2pSharingClient)) {
            HwHiLog.w(TAG, false, "start is only called in gc or is null", new Object[0]);
            return;
        }
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.handleP2pSharingOptStarted();
        }
        ((P2pSharingClient) this.p2pSharingBase).startP2pBorrowing();
    }

    public void stop() {
        P2pSharingInterface p2pSharingInterface = this.p2pSharingBase;
        if (!(p2pSharingInterface instanceof P2pSharingClient)) {
            HwHiLog.d(TAG, false, "stop is only called in gc or is null", new Object[0]);
        } else {
            ((P2pSharingClient) p2pSharingInterface).stopP2pBorrowing();
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingListener
    public void onEvent(int eventId, int reasonId) {
        HwHiLog.d(TAG, false, "eventId:" + eventId + ", reasonId:" + reasonId, new Object[0]);
        if (eventId == 1 || eventId == 3) {
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.handleP2pSharingFail(reasonId);
            }
            release();
        } else if (eventId == 0) {
            this.isInP2pSharing = true;
        } else {
            HwHiLog.d(TAG, false, "default case", new Object[0]);
        }
    }

    private void release() {
        HwHiLog.d(TAG, false, "release", new Object[0]);
        P2pSharingInterface p2pSharingInterface = this.p2pSharingBase;
        if (p2pSharingInterface != null) {
            p2pSharingInterface.release();
            this.p2pSharingBase = null;
        }
        this.isInP2pSharing = false;
        synchronized (this.lock) {
            this.isSetUpCalled = false;
        }
    }

    public void notifyP2pStateChanged(boolean isConnected) {
        P2pSharingInterface p2pSharingInterface = this.p2pSharingBase;
        if (p2pSharingInterface == null) {
            HwHiLog.w(TAG, false, "needn't to process P2P disconnect broadcast", new Object[0]);
            return;
        }
        p2pSharingInterface.onP2pStateChanged(isConnected);
        if (!isConnected) {
            release();
        }
    }

    public boolean isInP2pSharing() {
        return this.isInP2pSharing;
    }

    /* access modifiers changed from: private */
    public static class SingletonHolder {
        private static final P2pSharingDispatcher INSTANCE = new P2pSharingDispatcher();

        private SingletonHolder() {
        }
    }
}
