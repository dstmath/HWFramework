package com.android.server.emcom.networkevaluation;

import android.util.Log;

/* compiled from: WifiInformationClass */
class SnrMetrics {
    int avgRssi;
    private int[] rssiArray;

    SnrMetrics(int[] rssiArray) {
        if (rssiArray == null) {
            Log.d("snr metrics", "null rssiArray");
            this.avgRssi = 0;
            return;
        }
        this.rssiArray = rssiArray;
        this.avgRssi = (int) WifiInformationClass.calculateAverageValue(rssiArray, rssiArray.length);
    }

    public String toString() {
        return "RSSI array length : " + this.rssiArray.length + "avgRssi : " + String.valueOf(this.avgRssi) + "\n";
    }
}
