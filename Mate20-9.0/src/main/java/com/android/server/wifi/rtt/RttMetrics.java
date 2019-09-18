package com.android.server.wifi.rtt;

import android.hardware.wifi.V1_0.RttResult;
import android.net.MacAddress;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.ResponderConfig;
import android.os.WorkSource;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.server.wifi.Clock;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.nano.WifiMetricsProto;
import com.android.server.wifi.util.MetricsUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RttMetrics {
    private static final MetricsUtils.LogHistParms COUNT_LOG_HISTOGRAM;
    private static final int[] DISTANCE_MM_HISTOGRAM = {0, ScoringParams.BAND5, 15000, WifiStateMachine.LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS, 60000, 100000};
    private static final int PEER_AP = 0;
    private static final int PEER_AWARE = 1;
    private static final String TAG = "RttMetrics";
    private static final boolean VDBG = false;
    private final Clock mClock;
    boolean mDbg = false;
    private final Object mLock = new Object();
    private int mNumStartRangingCalls = 0;
    private SparseIntArray mOverallStatusHistogram = new SparseIntArray();
    private PerPeerTypeInfo[] mPerPeerTypeInfo;

    private class PerPeerTypeInfo {
        public SparseIntArray measuredDistanceHistogram;
        public int numCalls;
        public int numIndividualCalls;
        public SparseIntArray numRequestsHistogram;
        public SparseArray<PerUidInfo> perUidInfo;
        public SparseIntArray requestGapHistogram;
        public SparseIntArray statusHistogram;

        private PerPeerTypeInfo() {
            this.perUidInfo = new SparseArray<>();
            this.numRequestsHistogram = new SparseIntArray();
            this.requestGapHistogram = new SparseIntArray();
            this.statusHistogram = new SparseIntArray();
            this.measuredDistanceHistogram = new SparseIntArray();
        }

        public String toString() {
            return "numCalls=" + this.numCalls + ", numIndividualCalls=" + this.numIndividualCalls + ", perUidInfo=" + this.perUidInfo + ", numRequestsHistogram=" + this.numRequestsHistogram + ", requestGapHistogram=" + this.requestGapHistogram + ", measuredDistanceHistogram=" + this.measuredDistanceHistogram;
        }
    }

    private class PerUidInfo {
        public long lastRequestMs;
        public int numRequests;

        private PerUidInfo() {
        }

        public String toString() {
            return "numRequests=" + this.numRequests + ", lastRequestMs=" + this.lastRequestMs;
        }
    }

    static {
        MetricsUtils.LogHistParms logHistParms = new MetricsUtils.LogHistParms(0, 1, 10, 1, 7);
        COUNT_LOG_HISTOGRAM = logHistParms;
    }

    public RttMetrics(Clock clock) {
        this.mClock = clock;
        this.mPerPeerTypeInfo = new PerPeerTypeInfo[2];
        this.mPerPeerTypeInfo[0] = new PerPeerTypeInfo();
        this.mPerPeerTypeInfo[1] = new PerPeerTypeInfo();
    }

    public void recordRequest(WorkSource ws, RangingRequest requests) {
        this.mNumStartRangingCalls++;
        int numApRequests = 0;
        int numAwareRequests = 0;
        for (ResponderConfig request : requests.mRttPeers) {
            if (request != null) {
                if (request.responderType == 4) {
                    numAwareRequests++;
                } else if (request.responderType == 0) {
                    numApRequests++;
                } else if (this.mDbg) {
                    Log.d(TAG, "Unexpected Responder type: " + request.responderType);
                }
            }
        }
        updatePeerInfoWithRequestInfo(this.mPerPeerTypeInfo[0], ws, numApRequests);
        updatePeerInfoWithRequestInfo(this.mPerPeerTypeInfo[1], ws, numAwareRequests);
    }

    public void recordResult(RangingRequest requests, List<RttResult> results) {
        PerPeerTypeInfo peerInfo;
        Map<MacAddress, ResponderConfig> requestEntries = new HashMap<>();
        for (ResponderConfig responder : requests.mRttPeers) {
            requestEntries.put(responder.macAddress, responder);
        }
        if (results != null) {
            for (RttResult result : results) {
                if (result != null) {
                    ResponderConfig responder2 = requestEntries.remove(MacAddress.fromBytes(result.addr));
                    if (responder2 == null) {
                        Log.e(TAG, "recordResult: found a result which doesn't match any requests: " + result);
                    } else if (responder2.responderType == 0) {
                        updatePeerInfoWithResultInfo(this.mPerPeerTypeInfo[0], result);
                    } else if (responder2.responderType == 4) {
                        updatePeerInfoWithResultInfo(this.mPerPeerTypeInfo[1], result);
                    } else {
                        Log.e(TAG, "recordResult: unexpected peer type in responder: " + responder2);
                    }
                }
            }
        }
        for (ResponderConfig responder3 : requestEntries.values()) {
            if (responder3.responderType == 0) {
                peerInfo = this.mPerPeerTypeInfo[0];
            } else if (responder3.responderType == 4) {
                peerInfo = this.mPerPeerTypeInfo[1];
            } else {
                Log.e(TAG, "recordResult: unexpected peer type in responder: " + responder3);
            }
            peerInfo.statusHistogram.put(17, peerInfo.statusHistogram.get(17) + 1);
        }
    }

    public void recordOverallStatus(int status) {
        this.mOverallStatusHistogram.put(status, this.mOverallStatusHistogram.get(status) + 1);
    }

    private void updatePeerInfoWithRequestInfo(PerPeerTypeInfo peerInfo, WorkSource ws, int numIndividualCalls) {
        if (numIndividualCalls != 0) {
            long nowMs = this.mClock.getElapsedSinceBootMillis();
            peerInfo.numCalls++;
            peerInfo.numIndividualCalls += numIndividualCalls;
            peerInfo.numRequestsHistogram.put(numIndividualCalls, peerInfo.numRequestsHistogram.get(numIndividualCalls) + 1);
            boolean recordedIntervals = false;
            for (int i = 0; i < ws.size(); i++) {
                int uid = ws.get(i);
                PerUidInfo perUidInfo = peerInfo.perUidInfo.get(uid);
                if (perUidInfo == null) {
                    perUidInfo = new PerUidInfo();
                }
                perUidInfo.numRequests++;
                if (!recordedIntervals && perUidInfo.lastRequestMs != 0) {
                    recordedIntervals = true;
                    MetricsUtils.addValueToLogHistogram(nowMs - perUidInfo.lastRequestMs, peerInfo.requestGapHistogram, COUNT_LOG_HISTOGRAM);
                }
                perUidInfo.lastRequestMs = nowMs;
                peerInfo.perUidInfo.put(uid, perUidInfo);
            }
        }
    }

    private void updatePeerInfoWithResultInfo(PerPeerTypeInfo peerInfo, RttResult result) {
        int protoStatus = convertRttStatusTypeToProtoEnum(result.status);
        peerInfo.statusHistogram.put(protoStatus, peerInfo.statusHistogram.get(protoStatus) + 1);
        MetricsUtils.addValueToLinearHistogram(result.distanceInMm, peerInfo.measuredDistanceHistogram, DISTANCE_MM_HISTOGRAM);
    }

    public WifiMetricsProto.WifiRttLog consolidateProto() {
        WifiMetricsProto.WifiRttLog log = new WifiMetricsProto.WifiRttLog();
        log.rttToAp = new WifiMetricsProto.WifiRttLog.RttToPeerLog();
        log.rttToAware = new WifiMetricsProto.WifiRttLog.RttToPeerLog();
        synchronized (this.mLock) {
            log.numRequests = this.mNumStartRangingCalls;
            log.histogramOverallStatus = consolidateOverallStatus(this.mOverallStatusHistogram);
            consolidatePeerType(log.rttToAp, this.mPerPeerTypeInfo[0]);
            consolidatePeerType(log.rttToAware, this.mPerPeerTypeInfo[1]);
        }
        return log;
    }

    private WifiMetricsProto.WifiRttLog.RttOverallStatusHistogramBucket[] consolidateOverallStatus(SparseIntArray histogram) {
        WifiMetricsProto.WifiRttLog.RttOverallStatusHistogramBucket[] h = new WifiMetricsProto.WifiRttLog.RttOverallStatusHistogramBucket[histogram.size()];
        for (int i = 0; i < histogram.size(); i++) {
            h[i] = new WifiMetricsProto.WifiRttLog.RttOverallStatusHistogramBucket();
            h[i].statusType = histogram.keyAt(i);
            h[i].count = histogram.valueAt(i);
        }
        return h;
    }

    private void consolidatePeerType(WifiMetricsProto.WifiRttLog.RttToPeerLog peerLog, PerPeerTypeInfo peerInfo) {
        peerLog.numRequests = peerInfo.numCalls;
        peerLog.numIndividualRequests = peerInfo.numIndividualCalls;
        peerLog.numApps = peerInfo.perUidInfo.size();
        peerLog.histogramNumPeersPerRequest = consolidateNumPeersPerRequest(peerInfo.numRequestsHistogram);
        peerLog.histogramNumRequestsPerApp = consolidateNumRequestsPerApp(peerInfo.perUidInfo);
        peerLog.histogramRequestIntervalMs = genericBucketsToRttBuckets(MetricsUtils.logHistogramToGenericBuckets(peerInfo.requestGapHistogram, COUNT_LOG_HISTOGRAM));
        peerLog.histogramIndividualStatus = consolidateIndividualStatus(peerInfo.statusHistogram);
        peerLog.histogramDistance = genericBucketsToRttBuckets(MetricsUtils.linearHistogramToGenericBuckets(peerInfo.measuredDistanceHistogram, DISTANCE_MM_HISTOGRAM));
    }

    private WifiMetricsProto.WifiRttLog.RttIndividualStatusHistogramBucket[] consolidateIndividualStatus(SparseIntArray histogram) {
        WifiMetricsProto.WifiRttLog.RttIndividualStatusHistogramBucket[] h = new WifiMetricsProto.WifiRttLog.RttIndividualStatusHistogramBucket[histogram.size()];
        for (int i = 0; i < histogram.size(); i++) {
            h[i] = new WifiMetricsProto.WifiRttLog.RttIndividualStatusHistogramBucket();
            h[i].statusType = histogram.keyAt(i);
            h[i].count = histogram.valueAt(i);
        }
        return h;
    }

    private WifiMetricsProto.WifiRttLog.HistogramBucket[] consolidateNumPeersPerRequest(SparseIntArray data) {
        WifiMetricsProto.WifiRttLog.HistogramBucket[] protoArray = new WifiMetricsProto.WifiRttLog.HistogramBucket[data.size()];
        for (int i = 0; i < data.size(); i++) {
            protoArray[i] = new WifiMetricsProto.WifiRttLog.HistogramBucket();
            protoArray[i].start = (long) data.keyAt(i);
            protoArray[i].end = (long) data.keyAt(i);
            protoArray[i].count = data.valueAt(i);
        }
        return protoArray;
    }

    private WifiMetricsProto.WifiRttLog.HistogramBucket[] consolidateNumRequestsPerApp(SparseArray<PerUidInfo> perUidInfos) {
        SparseIntArray histogramNumRequestsPerUid = new SparseIntArray();
        for (int i = 0; i < perUidInfos.size(); i++) {
            MetricsUtils.addValueToLogHistogram((long) perUidInfos.valueAt(i).numRequests, histogramNumRequestsPerUid, COUNT_LOG_HISTOGRAM);
        }
        return genericBucketsToRttBuckets(MetricsUtils.logHistogramToGenericBuckets(histogramNumRequestsPerUid, COUNT_LOG_HISTOGRAM));
    }

    private WifiMetricsProto.WifiRttLog.HistogramBucket[] genericBucketsToRttBuckets(MetricsUtils.GenericBucket[] genericHistogram) {
        WifiMetricsProto.WifiRttLog.HistogramBucket[] histogram = new WifiMetricsProto.WifiRttLog.HistogramBucket[genericHistogram.length];
        for (int i = 0; i < genericHistogram.length; i++) {
            histogram[i] = new WifiMetricsProto.WifiRttLog.HistogramBucket();
            histogram[i].start = genericHistogram[i].start;
            histogram[i].end = genericHistogram[i].end;
            histogram[i].count = genericHistogram[i].count;
        }
        return histogram;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            pw.println("RTT Metrics:");
            pw.println("mNumStartRangingCalls:" + this.mNumStartRangingCalls);
            pw.println("mOverallStatusHistogram:" + this.mOverallStatusHistogram);
            pw.println("AP:" + this.mPerPeerTypeInfo[0]);
            pw.println("AWARE:" + this.mPerPeerTypeInfo[1]);
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            this.mNumStartRangingCalls = 0;
            this.mOverallStatusHistogram.clear();
            this.mPerPeerTypeInfo[0] = new PerPeerTypeInfo();
            this.mPerPeerTypeInfo[1] = new PerPeerTypeInfo();
        }
    }

    public static int convertRttStatusTypeToProtoEnum(int rttStatusType) {
        switch (rttStatusType) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            case 7:
                return 8;
            case 8:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 11:
                return 12;
            case 12:
                return 13;
            case 13:
                return 14;
            case 14:
                return 15;
            case 15:
                return 16;
            default:
                Log.e(TAG, "Unrecognized RttStatus: " + rttStatusType);
                return 0;
        }
    }
}
