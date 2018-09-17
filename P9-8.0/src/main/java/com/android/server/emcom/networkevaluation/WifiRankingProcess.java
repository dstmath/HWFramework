package com.android.server.emcom.networkevaluation;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.Arrays;

class WifiRankingProcess {
    private static final int DEFAULT_RTT_VALUE = 20;
    private static final int MSG_WIFI_RANKING_COMPLETE = 2001;
    private static final int MSG_WIFI_RANKING_FAIL = 2002;
    private static final String TAG = "WifiRankingProcess";
    private Handler mHandler;
    private int[] mRssiArray;
    private int[] mRttArray;

    public WifiRankingProcess(int[] rssiArray, Handler handler) {
        if (rssiArray == null || handler == null) {
            Log.e(TAG, "null parameter, failed to create instance");
            return;
        }
        this.mRssiArray = rssiArray;
        this.mHandler = handler;
        this.mRttArray = new int[rssiArray.length];
        Arrays.fill(this.mRttArray, 20);
    }

    public WifiRankingProcess(int[] rssiArray, int[] rttArray, Handler handler) {
        if (rssiArray == null && rttArray == null && handler == null) {
            Log.e(TAG, "null parameters, failed to create instance");
        } else if (rttArray == null && rssiArray != null && handler != null) {
            this.mRssiArray = rssiArray;
            this.mHandler = handler;
            this.mRttArray = new int[rssiArray.length];
            Arrays.fill(this.mRttArray, 20);
        } else if (rttArray == null || rssiArray == null || handler == null) {
            Log.e(TAG, "failed to create new WifiRankingProcess, miss either rssi array or handler");
        } else {
            this.mRssiArray = rssiArray;
            this.mRttArray = rttArray;
            this.mHandler = handler;
        }
    }

    public void startRanking() {
        WifiInformationClass info = new WifiInformationClass(this.mRssiArray, this.mRttArray);
        if (info.extractVitalMetrics()) {
            int[] result = info.calComprehensiveQuality();
            Message msg = Message.obtain();
            msg.what = 2001;
            msg.obj = result;
            this.mHandler.sendMessage(msg);
            return;
        }
        Log.d(TAG, "failed to extract metrics");
    }

    static void staticStartRanking(int[] rssiArray, int length, Handler outputHandler) {
        int[] result = WifiInformationClass.calComprehensiveQuality(rssiArray, length);
        if (result.length > 0) {
            Message msg = Message.obtain();
            msg.what = 2001;
            msg.obj = result;
            outputHandler.sendMessage(msg);
        }
    }
}
