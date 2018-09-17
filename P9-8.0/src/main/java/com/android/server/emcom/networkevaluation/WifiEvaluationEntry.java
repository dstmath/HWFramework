package com.android.server.emcom.networkevaluation;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.emcom.EmcomThread;
import java.util.Arrays;

class WifiEvaluationEntry {
    private static final int INVALID_VALUE = -1;
    private static final int METRIC_COUNT = 8;
    private static final int METRIC_INTERVAL = 1000;
    private static final int MSG_TIMER_TRIGGERED = 10001;
    private static final String TAG = "WifiEvaluationEntry";
    private static volatile WifiEvaluationEntry sWifiEvaluationEntry;
    private Context mContext;
    private Handler mOutputHandler;
    private int[] mRssiArray = new int[8];
    private int mRssiWindowIndex;
    private boolean mRunning;
    private Handler mTimeHandler;
    private boolean mWindowFull;

    private class WifiTimeHandler extends Handler {
        WifiTimeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (WifiEvaluationEntry.this.mTimeHandler == null) {
                Log.e(WifiEvaluationEntry.TAG, "null mTimeHandler in triggerDelayed()");
            } else if (WifiEvaluationEntry.this.mRssiArray == null) {
                Log.e(WifiEvaluationEntry.TAG, "null mRssiArray in triggerDelayed()");
            } else {
                switch (msg.what) {
                    case 10001:
                        if (WifiEvaluationEntry.this.mRunning) {
                            int rssi = WifiEvaluationEntry.this.getRssi();
                            if (-1 == rssi) {
                                Log.d(WifiEvaluationEntry.TAG, "got an invalid signal strength");
                            } else {
                                WifiEvaluationEntry.this.mRssiArray[WifiEvaluationEntry.this.mRssiWindowIndex] = rssi;
                                WifiEvaluationEntry wifiEvaluationEntry = WifiEvaluationEntry.this;
                                wifiEvaluationEntry.mRssiWindowIndex = wifiEvaluationEntry.mRssiWindowIndex + 1;
                                if (WifiEvaluationEntry.this.mWindowFull) {
                                    WifiEvaluationEntry.this.executeRankingProcess(8);
                                    if (WifiEvaluationEntry.this.mRssiWindowIndex >= 8) {
                                        WifiEvaluationEntry.this.mRssiWindowIndex = 0;
                                    }
                                } else if (WifiEvaluationEntry.this.mRssiWindowIndex >= 8) {
                                    WifiEvaluationEntry.this.executeRankingProcess(8);
                                    WifiEvaluationEntry.this.mWindowFull = true;
                                    WifiEvaluationEntry.this.mRssiWindowIndex = 0;
                                } else {
                                    WifiEvaluationEntry.this.executeRankingProcess(WifiEvaluationEntry.this.mRssiWindowIndex);
                                }
                            }
                            Message newMsg = Message.obtain();
                            newMsg.what = 10001;
                            newMsg.arg1 = -1;
                            WifiEvaluationEntry.this.mTimeHandler.sendMessageDelayed(newMsg, 1000);
                        }
                        return;
                    default:
                        Log.d(WifiEvaluationEntry.TAG, "received a illegal message");
                        return;
                }
            }
        }
    }

    private WifiEvaluationEntry(Context context, Handler handler) {
        this.mContext = context;
        this.mOutputHandler = handler;
        this.mRssiArray = new int[8];
        this.mTimeHandler = new WifiTimeHandler(EmcomThread.getInstanceLooper());
    }

    public static synchronized WifiEvaluationEntry getInstance(Context context, Handler handler) {
        synchronized (WifiEvaluationEntry.class) {
            WifiEvaluationEntry wifiEvaluationEntry;
            if (sWifiEvaluationEntry != null) {
                wifiEvaluationEntry = sWifiEvaluationEntry;
                return wifiEvaluationEntry;
            } else if (context == null || handler == null) {
                Log.e(TAG, "return null");
                return null;
            } else {
                sWifiEvaluationEntry = new WifiEvaluationEntry(context, handler);
                wifiEvaluationEntry = sWifiEvaluationEntry;
                return wifiEvaluationEntry;
            }
        }
    }

    void startEvaluation() {
        if (this.mRunning) {
            Log.d(TAG, "attempt to start a new WiFi evaluation while a evaluation is already running");
            return;
        }
        Log.d(TAG, "start evaluation");
        this.mRssiWindowIndex = 0;
        Arrays.fill(this.mRssiArray, 0);
        this.mWindowFull = false;
        this.mRunning = true;
        triggerDelayed();
    }

    void stopEvaluation() {
        if (this.mRunning) {
            this.mRssiWindowIndex = 0;
            this.mWindowFull = false;
            this.mRunning = false;
            Log.d(TAG, "stop evaluation");
            return;
        }
        Log.d(TAG, "attempt to stop a stopped WiFi evaluation");
    }

    private void executeRankingProcess(int length) {
        WifiRankingProcess.staticStartRanking(this.mRssiArray, length, this.mOutputHandler);
        Log.d(TAG, "started a ranking without RTT");
    }

    private void triggerDelayed() {
        if (this.mTimeHandler == null) {
            Log.e(TAG, "null mTimeHandler in triggerDelayed()");
            return;
        }
        this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(10001), 1000);
    }

    private int getRssi() {
        try {
            return ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo().getRssi();
        } catch (Exception e) {
            Log.d(TAG, "failed to getRssi()");
            e.printStackTrace();
            return -1;
        }
    }
}
