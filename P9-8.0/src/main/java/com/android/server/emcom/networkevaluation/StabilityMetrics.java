package com.android.server.emcom.networkevaluation;

/* compiled from: WifiInformationClass */
class StabilityMetrics {
    int mdevRssi;
    int mdevRtt;

    StabilityMetrics(int mdevRtt, int mdevRssi) {
        this.mdevRtt = mdevRtt;
        this.mdevRssi = mdevRssi;
    }

    public String toString() {
        return "mdevRTT : " + String.valueOf(this.mdevRtt) + " mdevRssi :" + String.valueOf(this.mdevRssi) + "\n";
    }

    public void setValues(int mdevRtt, int mdevRssi) {
        this.mdevRtt = mdevRtt;
        this.mdevRssi = mdevRssi;
    }
}
