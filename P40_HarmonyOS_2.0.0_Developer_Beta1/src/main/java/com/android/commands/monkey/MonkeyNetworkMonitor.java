package com.android.commands.monkey;

import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;

public class MonkeyNetworkMonitor extends IIntentReceiver.Stub {
    private static final boolean LDEBUG = false;
    private final IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    private long mCollectionStartTime;
    private long mElapsedTime = 0;
    private long mEventTime;
    private int mLastNetworkType = -1;
    private long mMobileElapsedTime = 0;
    private long mWifiElapsedTime = 0;

    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        updateNetworkStats();
        if (NetworkInfo.State.CONNECTED == ni.getState()) {
            this.mLastNetworkType = ni.getType();
        } else if (NetworkInfo.State.DISCONNECTED == ni.getState()) {
            this.mLastNetworkType = -1;
        }
        this.mEventTime = SystemClock.elapsedRealtime();
    }

    private void updateNetworkStats() {
        long timeNow = SystemClock.elapsedRealtime();
        long delta = timeNow - this.mEventTime;
        int i = this.mLastNetworkType;
        if (i == 0) {
            this.mMobileElapsedTime += delta;
        } else if (i == 1) {
            this.mWifiElapsedTime += delta;
        }
        this.mElapsedTime = timeNow - this.mCollectionStartTime;
    }

    public void start() {
        this.mWifiElapsedTime = 0;
        this.mMobileElapsedTime = 0;
        this.mElapsedTime = 0;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mCollectionStartTime = elapsedRealtime;
        this.mEventTime = elapsedRealtime;
    }

    public void register(IActivityManager am) throws RemoteException {
        am.registerReceiver((IApplicationThread) null, (String) null, this, this.filter, (String) null, -1, 0);
    }

    public void unregister(IActivityManager am) throws RemoteException {
        am.unregisterReceiver(this);
    }

    public void stop() {
        updateNetworkStats();
    }

    public void dump() {
        Logger logger = Logger.out;
        logger.println("## Network stats: elapsed time=" + this.mElapsedTime + "ms (" + this.mMobileElapsedTime + "ms mobile, " + this.mWifiElapsedTime + "ms wifi, " + ((this.mElapsedTime - this.mMobileElapsedTime) - this.mWifiElapsedTime) + "ms not connected)");
    }
}
