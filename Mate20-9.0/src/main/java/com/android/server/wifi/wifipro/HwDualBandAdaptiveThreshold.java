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
    private int[] mEntries = new int[this.mEntriesSize];
    private int mEntriesSize = 61;
    private int mRssiBase = BSSID_RSSI_RANGE_LOW_DBM;
    private WifiManager mWifiManager;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);

    public HwDualBandAdaptiveThreshold(Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        Context context3 = this.mContext;
        this.mWifiManager = (WifiManager) context2.getSystemService("wifi");
        for (int i = 0; i < this.mEntriesSize; i++) {
            this.mEntries[i] = BSSID_RSSI_RANGE_LOW_DBM + i;
        }
    }

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
            this.mEntries[i] = BSSID_RSSI_RANGE_LOW_DBM + i;
        }
        if (!this.mWifiProHistoryRecordManager.updateRSSIThreshold(bssid2G, this.mEntries)) {
            Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping, fail to add a new record in database");
        }
        return this.mEntries[index];
    }

    public void updateRSSIThreshold(String bssid2G, String bssid5G, int newRssi2G, int newRssi5G, int scanRssi2G, int targetRssi5G) {
        int i = newRssi2G;
        int i2 = newRssi5G;
        int i3 = scanRssi2G;
        int i4 = targetRssi5G;
        if (i < BSSID_RSSI_RANGE_LOW_DBM) {
            Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping, 2G RSSI from scan results is out of range (<BSSID_RSSI_RANGE_LOW_DBM).");
            return;
        }
        int newScanRssi2G = i3;
        if (i2 < i4) {
            int newScanRssi2G2 = newScanRssi2G + 5;
            int i5 = BSSID_TARGET_RSSI_MAX_DBM;
            if (newScanRssi2G2 <= BSSID_TARGET_RSSI_MAX_DBM) {
                i5 = newScanRssi2G2;
            }
            newScanRssi2G = i5;
            Log.e(HwDualBandMessageUtil.TAG, "updateRSSIThreshold handover failed, newRssi2G = " + i + " newRssi5G = " + i2 + " targetRssi5G = " + i4 + "old scanRssi2G = " + i3 + "new scanRssi2G = " + newScanRssi2G);
        } else if (i2 > i4 + 10 && i < i3 + 10) {
            newScanRssi2G -= 5;
            Log.e(HwDualBandMessageUtil.TAG, "updateRSSIThreshold handover success, newRssi2G = " + i + " newRssi5G = " + i2 + " targetRssi5G = " + i4 + "old scanRssi2G = " + i3 + "new scanRssi2G = " + newScanRssi2G);
        }
        int x2 = i4;
        int y2 = newScanRssi2G;
        if (-45 != x2) {
            for (int x = BSSID_RSSI_RANGE_LOW_DBM; x <= -45; x++) {
                this.mEntries[x - this.mRssiBase] = (((-45 - y2) * (x - x2)) / (-45 - x2)) + y2;
            }
            if (this.mWifiProHistoryRecordManager != null) {
                if (!this.mWifiProHistoryRecordManager.updateRSSIThreshold(bssid2G, this.mEntries)) {
                    Log.e(HwDualBandMessageUtil.TAG, "5G->2G RSSI Mapping, fail to update a record in database");
                }
            } else {
                String str = bssid2G;
            }
        }
    }
}
