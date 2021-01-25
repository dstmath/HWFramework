package com.android.server.wifi;

import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.nano.WifiMetricsProto;
import com.android.server.wifi.util.IntHistogram;
import java.io.PrintWriter;

public class DppMetrics {
    @VisibleForTesting
    public static final int[] DPP_OPERATION_TIME = {1, 10, 25, 39};
    private SparseIntArray mHistogramDppConfiguratorSuccessCode = new SparseIntArray();
    private SparseIntArray mHistogramDppFailureCode = new SparseIntArray();
    private IntHistogram mHistogramDppOperationTime = new IntHistogram(DPP_OPERATION_TIME);
    private final Object mLock = new Object();
    private final WifiMetricsProto.WifiDppLog mWifiDppLogProto = new WifiMetricsProto.WifiDppLog();

    public void updateDppConfiguratorInitiatorRequests() {
        synchronized (this.mLock) {
            this.mWifiDppLogProto.numDppConfiguratorInitiatorRequests++;
        }
    }

    public void updateDppEnrolleeInitiatorRequests() {
        synchronized (this.mLock) {
            this.mWifiDppLogProto.numDppEnrolleeInitiatorRequests++;
        }
    }

    public void updateDppEnrolleeSuccess() {
        synchronized (this.mLock) {
            this.mWifiDppLogProto.numDppEnrolleeSuccess++;
        }
    }

    public void updateDppConfiguratorSuccess(int code) {
        synchronized (this.mLock) {
            if (code == 0) {
                this.mHistogramDppConfiguratorSuccessCode.put(1, this.mHistogramDppConfiguratorSuccessCode.get(1) + 1);
            }
        }
    }

    public void updateDppFailure(int code) {
        synchronized (this.mLock) {
            switch (code) {
                case ScoringParams.Values.MIN_HORIZON /* -9 */:
                    this.mHistogramDppFailureCode.put(9, this.mHistogramDppFailureCode.get(9) + 1);
                    break;
                case -8:
                    this.mHistogramDppFailureCode.put(8, this.mHistogramDppFailureCode.get(8) + 1);
                    break;
                case -7:
                    this.mHistogramDppFailureCode.put(7, this.mHistogramDppFailureCode.get(7) + 1);
                    break;
                case -6:
                    this.mHistogramDppFailureCode.put(6, this.mHistogramDppFailureCode.get(6) + 1);
                    break;
                case -5:
                    this.mHistogramDppFailureCode.put(5, this.mHistogramDppFailureCode.get(5) + 1);
                    break;
                case -4:
                    this.mHistogramDppFailureCode.put(4, this.mHistogramDppFailureCode.get(4) + 1);
                    break;
                case -3:
                    this.mHistogramDppFailureCode.put(3, this.mHistogramDppFailureCode.get(3) + 1);
                    break;
                case SarInfo.INITIAL_SAR_SCENARIO /* -2 */:
                    this.mHistogramDppFailureCode.put(2, this.mHistogramDppFailureCode.get(2) + 1);
                    break;
                case -1:
                    this.mHistogramDppFailureCode.put(1, this.mHistogramDppFailureCode.get(1) + 1);
                    break;
            }
        }
    }

    public void updateDppOperationTime(int timeMs) {
        synchronized (this.mLock) {
            this.mHistogramDppOperationTime.increment(timeMs / 1000);
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("---Easy Connect/DPP metrics---");
            pw.println("mWifiDppLogProto.numDppConfiguratorInitiatorRequests=" + this.mWifiDppLogProto.numDppConfiguratorInitiatorRequests);
            pw.println("mWifiDppLogProto.numDppEnrolleeInitiatorRequests=" + this.mWifiDppLogProto.numDppEnrolleeInitiatorRequests);
            pw.println("mWifiDppLogProto.numDppEnrolleeSuccess=" + this.mWifiDppLogProto.numDppEnrolleeSuccess);
            if (this.mHistogramDppFailureCode.size() > 0) {
                pw.println("mHistogramDppFailureCode=");
                pw.println(this.mHistogramDppFailureCode);
            }
            if (this.mHistogramDppConfiguratorSuccessCode.size() > 0) {
                pw.println("mHistogramDppConfiguratorSuccessCode=");
                pw.println(this.mHistogramDppConfiguratorSuccessCode);
            }
            if (this.mHistogramDppOperationTime.numNonEmptyBuckets() > 0) {
                pw.println("mHistogramDppOperationTime=");
                pw.println(this.mHistogramDppOperationTime);
            }
            pw.println("---End of Easy Connect/DPP metrics---");
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            this.mWifiDppLogProto.numDppConfiguratorInitiatorRequests = 0;
            this.mWifiDppLogProto.numDppEnrolleeInitiatorRequests = 0;
            this.mWifiDppLogProto.numDppEnrolleeSuccess = 0;
            this.mHistogramDppFailureCode.clear();
            this.mHistogramDppOperationTime.clear();
            this.mHistogramDppConfiguratorSuccessCode.clear();
        }
    }

    private WifiMetricsProto.WifiDppLog.DppFailureStatusHistogramBucket[] consolidateDppFailure(SparseIntArray data) {
        WifiMetricsProto.WifiDppLog.DppFailureStatusHistogramBucket[] dppFailureStatusHistogramBuckets = new WifiMetricsProto.WifiDppLog.DppFailureStatusHistogramBucket[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dppFailureStatusHistogramBuckets[i] = new WifiMetricsProto.WifiDppLog.DppFailureStatusHistogramBucket();
            dppFailureStatusHistogramBuckets[i].dppStatusType = data.keyAt(i);
            dppFailureStatusHistogramBuckets[i].count = data.valueAt(i);
        }
        return dppFailureStatusHistogramBuckets;
    }

    private WifiMetricsProto.WifiDppLog.DppConfiguratorSuccessStatusHistogramBucket[] consolidateDppSuccess(SparseIntArray data) {
        WifiMetricsProto.WifiDppLog.DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessStatusHistogramBuckets = new WifiMetricsProto.WifiDppLog.DppConfiguratorSuccessStatusHistogramBucket[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dppConfiguratorSuccessStatusHistogramBuckets[i] = new WifiMetricsProto.WifiDppLog.DppConfiguratorSuccessStatusHistogramBucket();
            dppConfiguratorSuccessStatusHistogramBuckets[i].dppStatusType = data.keyAt(i);
            dppConfiguratorSuccessStatusHistogramBuckets[i].count = data.valueAt(i);
        }
        return dppConfiguratorSuccessStatusHistogramBuckets;
    }

    public WifiMetricsProto.WifiDppLog consolidateProto() {
        WifiMetricsProto.WifiDppLog log = new WifiMetricsProto.WifiDppLog();
        synchronized (this.mLock) {
            log.numDppConfiguratorInitiatorRequests = this.mWifiDppLogProto.numDppConfiguratorInitiatorRequests;
            log.numDppEnrolleeInitiatorRequests = this.mWifiDppLogProto.numDppEnrolleeInitiatorRequests;
            log.numDppEnrolleeSuccess = this.mWifiDppLogProto.numDppEnrolleeSuccess;
            log.dppFailureCode = consolidateDppFailure(this.mHistogramDppFailureCode);
            log.dppConfiguratorSuccessCode = consolidateDppSuccess(this.mHistogramDppConfiguratorSuccessCode);
            log.dppOperationTime = this.mHistogramDppOperationTime.toProto();
        }
        return log;
    }
}
