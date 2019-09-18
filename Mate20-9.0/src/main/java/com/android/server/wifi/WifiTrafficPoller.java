package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.HwLog;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiTrafficPoller {
    private static final int ADD_CLIENT = 3;
    private static final boolean DBG = false;
    private static final int ENABLE_TRAFFIC_STATS_POLL = 1;
    private static final int POLL_TRAFFIC_STATS_INTERVAL_MSECS = 1000;
    private static final int REMOVE_CLIENT = 4;
    private static final String TAG = "WifiTrafficPoller";
    private static final int TRAFFIC_STATS_POLL = 2;
    /* access modifiers changed from: private */
    public final List<Messenger> mClients = new ArrayList();
    private int mDataActivity;
    /* access modifiers changed from: private */
    public boolean mEnableTrafficStatsPoll = false;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    private long mRxPkts;
    /* access modifiers changed from: private */
    public AtomicBoolean mScreenOn = new AtomicBoolean(true);
    private final TrafficHandler mTrafficHandler;
    /* access modifiers changed from: private */
    public int mTrafficStatsPollToken = 0;
    private long mTxPkts;
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    /* access modifiers changed from: private */
    public final WifiNative mWifiNative;

    private class TrafficHandler extends Handler {
        public TrafficHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WifiTrafficPoller wifiTrafficPoller = WifiTrafficPoller.this;
                    boolean z = true;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    boolean unused = wifiTrafficPoller.mEnableTrafficStatsPoll = z;
                    if (WifiTrafficPoller.this.mVerboseLoggingEnabled) {
                        Log.d(WifiTrafficPoller.TAG, "ENABLE_TRAFFIC_STATS_POLL " + WifiTrafficPoller.this.mEnableTrafficStatsPoll + " Token " + Integer.toString(WifiTrafficPoller.this.mTrafficStatsPollToken));
                    }
                    int unused2 = WifiTrafficPoller.this.mTrafficStatsPollToken = WifiTrafficPoller.this.mTrafficStatsPollToken + 1;
                    String ifaceName = WifiTrafficPoller.this.mWifiNative.getClientInterfaceName();
                    if (WifiTrafficPoller.this.mEnableTrafficStatsPoll && !TextUtils.isEmpty(ifaceName)) {
                        WifiTrafficPoller.this.notifyOnDataActivity(ifaceName);
                        sendMessageDelayed(Message.obtain(this, 2, WifiTrafficPoller.this.mTrafficStatsPollToken, 0), 1000);
                        return;
                    }
                    return;
                case 2:
                    if (msg.arg1 == WifiTrafficPoller.this.mTrafficStatsPollToken) {
                        String ifaceName2 = WifiTrafficPoller.this.mWifiNative.getClientInterfaceName();
                        if (!TextUtils.isEmpty(ifaceName2)) {
                            WifiTrafficPoller.this.notifyOnDataActivity(ifaceName2);
                            sendMessageDelayed(Message.obtain(this, 2, WifiTrafficPoller.this.mTrafficStatsPollToken, 0), 1000);
                            return;
                        }
                        return;
                    }
                    return;
                case 3:
                    WifiTrafficPoller.this.mClients.add((Messenger) msg.obj);
                    if (WifiTrafficPoller.this.mVerboseLoggingEnabled) {
                        Log.d(WifiTrafficPoller.TAG, "ADD_CLIENT: " + Integer.toString(WifiTrafficPoller.this.mClients.size()));
                        return;
                    }
                    return;
                case 4:
                    WifiTrafficPoller.this.mClients.remove(msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    WifiTrafficPoller(Context context, Looper looper, WifiNative wifiNative) {
        this.mTrafficHandler = new TrafficHandler(looper);
        this.mWifiNative = wifiNative;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                        NetworkInfo unused = WifiTrafficPoller.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                        WifiTrafficPoller.this.mScreenOn.set(false);
                    } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                        WifiTrafficPoller.this.mScreenOn.set(true);
                    }
                    WifiTrafficPoller.this.evaluateTrafficStatsPolling();
                }
            }
        }, filter);
    }

    public void addClient(Messenger client) {
        Message.obtain(this.mTrafficHandler, 3, client).sendToTarget();
    }

    public void removeClient(Messenger client) {
        Message.obtain(this.mTrafficHandler, 4, client).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    /* access modifiers changed from: private */
    public void evaluateTrafficStatsPolling() {
        Message msg;
        if (this.mNetworkInfo != null) {
            if (this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED || !this.mScreenOn.get()) {
                msg = Message.obtain(this.mTrafficHandler, 1, 0, 0);
            } else {
                msg = Message.obtain(this.mTrafficHandler, 1, 1, 0);
            }
            msg.sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void notifyOnDataActivity(String ifaceName) {
        long preTxPkts = this.mTxPkts;
        long preRxPkts = this.mRxPkts;
        int dataActivity = 0;
        this.mTxPkts = this.mWifiNative.getTxPackets(ifaceName);
        this.mRxPkts = this.mWifiNative.getRxPackets(ifaceName);
        if (preTxPkts > 0 || preRxPkts > 0) {
            long received = this.mRxPkts - preRxPkts;
            if (this.mTxPkts - preTxPkts > 0) {
                dataActivity = 0 | 2;
            }
            if (received > 0) {
                dataActivity |= 1;
            }
            if (dataActivity != this.mDataActivity) {
                HwLog.dubaie("DUBAI_TAG_WIFI_ACTIVITY", "activity=" + dataActivity);
            }
            if (dataActivity != this.mDataActivity && this.mScreenOn.get()) {
                this.mDataActivity = dataActivity;
                if (this.mVerboseLoggingEnabled) {
                    Log.e(TAG, "notifying of data activity " + Integer.toString(this.mDataActivity));
                }
                for (Messenger client : this.mClients) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg1 = this.mDataActivity;
                    try {
                        client.send(msg);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mEnableTrafficStatsPoll " + this.mEnableTrafficStatsPoll);
        pw.println("mTrafficStatsPollToken " + this.mTrafficStatsPollToken);
        pw.println("mTxPkts " + this.mTxPkts);
        pw.println("mRxPkts " + this.mRxPkts);
        pw.println("mDataActivity " + this.mDataActivity);
    }
}
