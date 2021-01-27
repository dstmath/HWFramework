package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.os.Message;
import android.os.SystemClock;
import com.android.server.wifi.WifiNative;

public class HiCoexArbitration {
    private static final int CMD_SET_NRCOEX_PRIORITY = 111;
    private static final String IFACE = "wlan0";
    private static final int MIN_CELL_SCORE_TO_DRV = 10;
    public static final int NRCOEX_PRIOR_CELLULAR_CONTEST = 2;
    public static final int NRCOEX_PRIOR_CELLULAR_HIGH = 1;
    public static final int NRCOEX_PRIOR_CELLULAR_LOW = 0;
    private static final String TAG = "HiCoexArbitration";
    private Context mContext;
    private int mCurrentNRPriority = 1;
    private HiCoexCellularScore mHiCoexCellScore;
    private HiCoexChrImpl mHiCoexChrImpl;
    private HiCoexReceiver mHiCoexReceiver;
    private HiCoexWiFiScore mHiCoexWiFiScore;
    private long mLastTimestamp = 0;
    private WifiNative mWifiNative;

    public HiCoexArbitration(Context context, HiCoexReceiver receiver, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mHiCoexReceiver = receiver;
        this.mHiCoexWiFiScore = new HiCoexWiFiScore(this.mContext, this.mHiCoexReceiver);
        this.mHiCoexCellScore = new HiCoexCellularScore(this.mContext, this.mHiCoexReceiver);
        this.mHiCoexChrImpl = HiCoexChrImpl.getInstance();
    }

    public void onReceiveEvent(Message msg) {
        int nrPriority;
        this.mHiCoexWiFiScore.onReceiveEvent(msg);
        this.mHiCoexCellScore.onReceiveEvent(msg);
        int wiFiScore = this.mHiCoexWiFiScore.getCurrentScore();
        int cellScore = this.mHiCoexCellScore.getCurrentScore();
        if (wiFiScore > cellScore) {
            nrPriority = 0;
        } else if (wiFiScore == cellScore) {
            nrPriority = 2;
        } else {
            nrPriority = 1;
        }
        if (cellScore < 10) {
            HiCoexUtils.logD(TAG, "cellScore is below threshold:" + cellScore);
            nrPriority = 2;
        }
        long now = SystemClock.elapsedRealtime();
        long j = this.mLastTimestamp;
        if (j == 0) {
            this.mLastTimestamp = now;
        } else {
            long time = now - j;
            this.mLastTimestamp = now;
            if (this.mHiCoexChrImpl != null) {
                this.mHiCoexChrImpl.updateCoexPriority((int) time, this.mCurrentNRPriority, this.mHiCoexCellScore.getMaxScoreScene(), this.mHiCoexReceiver.isNrNetwork());
            }
        }
        if (this.mCurrentNRPriority != nrPriority) {
            this.mCurrentNRPriority = nrPriority;
            setNRCoexPriority(nrPriority);
            return;
        }
        HiCoexUtils.logD(TAG, "nrPriority is no change:" + nrPriority);
    }

    private void setNRCoexPriority(int priority) {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null && wifiNative.mHwWifiNativeEx != null) {
            HiCoexUtils.logD(TAG, "setNRCoexPriority:" + priority);
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 111, new byte[]{(byte) priority});
        }
    }
}
