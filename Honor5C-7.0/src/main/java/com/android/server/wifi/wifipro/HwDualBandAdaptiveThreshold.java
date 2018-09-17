package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class HwDualBandAdaptiveThreshold {
    private static final int BSSID_RSSI_OVERFLOW_DBM = 10;
    private static final int BSSID_RSSI_RANGE_HIGH_DBM = -45;
    private static final int BSSID_RSSI_RANGE_LOW_DBM = -105;
    private static final int BSSID_RSSI_STEP_DBM = 5;
    private static final int BSSID_TARGET_RSSI_MAX_DBM = -50;
    private Context mContext;
    private int[] mEntries;
    private int mEntriesSize;
    private int mRssiBase;
    private WifiManager mWifiManager;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;

    public HwDualBandAdaptiveThreshold(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);
        this.mRssiBase = BSSID_RSSI_RANGE_LOW_DBM;
        this.mEntriesSize = 61;
        this.mEntries = new int[this.mEntriesSize];
        for (int i = 0; i < this.mEntriesSize; i++) {
            this.mEntries[i] = i + BSSID_RSSI_RANGE_LOW_DBM;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getScanRSSIThreshold(String bssid2G, String bssid5G, int targetRssi5G) {
        int index = targetRssi5G - this.mRssiBase;
        if (index < 0 || index >= this.mEntriesSize || this.mWifiProHistoryRecordManager == null) {
            return 0;
        }
        int[] mMapping = this.mWifiProHistoryRecordManager.getRSSIThreshold(bssid2G);
        if (mMapping.length != 0) {
            return mMapping[index];
        }
        Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping is null.");
        for (int i = 0; i < this.mEntriesSize; i++) {
            this.mEntries[i] = i + BSSID_RSSI_RANGE_LOW_DBM;
        }
        if (!this.mWifiProHistoryRecordManager.updateRSSIThreshold(bssid2G, this.mEntries)) {
            Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping, fail to add a new record in database with bssid " + bssid2G);
        }
        return this.mEntries[index];
    }

    public void updateRSSIThreshold(String bssid2G, String bssid5G, int newRssi2G, int newRssi5G, int scanRssi2G, int targetRssi5G) {
        if (newRssi2G < BSSID_RSSI_RANGE_LOW_DBM) {
            Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping, 2G RSSI from scan results is out of range (<BSSID_RSSI_RANGE_LOW_DBM).");
            return;
        }
        int newScanRssi2G = scanRssi2G;
        if (newRssi5G < targetRssi5G) {
            newScanRssi2G = scanRssi2G + BSSID_RSSI_STEP_DBM;
            if (newScanRssi2G > BSSID_TARGET_RSSI_MAX_DBM) {
                newScanRssi2G = BSSID_TARGET_RSSI_MAX_DBM;
            }
            Log.e(HwDualBandMessageUtil.TAG, "updateRSSIThreshold handover failed, bssid2G = " + bssid2G + " bssid5G = " + bssid5G + " newRssi2G = " + newRssi2G + " newRssi5G = " + newRssi5G + " targetRssi5G = " + targetRssi5G + "old scanRssi2G = " + scanRssi2G + "new scanRssi2G = " + newScanRssi2G);
        } else if (newRssi5G > targetRssi5G + BSSID_RSSI_OVERFLOW_DBM && newRssi2G < scanRssi2G + BSSID_RSSI_OVERFLOW_DBM) {
            newScanRssi2G = scanRssi2G - 5;
            Log.e(HwDualBandMessageUtil.TAG, "updateRSSIThreshold handover success, bssid2G = " + bssid2G + " bssid5G = " + bssid5G + " newRssi2G = " + newRssi2G + " newRssi5G = " + newRssi5G + " targetRssi5G = " + targetRssi5G + "old scanRssi2G = " + scanRssi2G + "new scanRssi2G = " + newScanRssi2G);
        }
        int x2 = targetRssi5G;
        int y2 = newScanRssi2G;
        if (BSSID_RSSI_RANGE_HIGH_DBM != targetRssi5G) {
            for (int x = BSSID_RSSI_RANGE_LOW_DBM; x <= BSSID_RSSI_RANGE_HIGH_DBM; x++) {
                this.mEntries[x - this.mRssiBase] = (((-45 - y2) * (x - targetRssi5G)) / (-45 - targetRssi5G)) + y2;
            }
            if (!(this.mWifiProHistoryRecordManager == null || this.mWifiProHistoryRecordManager.updateRSSIThreshold(bssid2G, this.mEntries))) {
                Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping, fail to update a record in database with bssid " + bssid2G);
            }
        }
    }
}
