package com.android.server.wifi.aware;

import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.Clock;
import com.android.server.wifi.aware.WifiAwareDataPathStateManager;
import com.android.server.wifi.nano.WifiMetricsProto;
import com.android.server.wifi.util.MetricsUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WifiAwareMetrics {
    private static final MetricsUtils.LogHistParms DURATION_LOG_HISTOGRAM = new MetricsUtils.LogHistParms(0, 1, 10, 9, 8);
    private static final int[] RANGING_LIMIT_METERS = {10, 30, 60, 100};
    private static final String TAG = "WifiAwareMetrics";
    private static final boolean VDBG = false;
    private Set<Integer> mAppsWithDiscoverySessionResourceFailure = new HashSet();
    private Map<Integer, AttachData> mAttachDataByUid = new HashMap();
    private SparseIntArray mAttachStatusData = new SparseIntArray();
    private long mAvailableTimeMs = 0;
    private final Clock mClock;
    boolean mDbg = false;
    private long mEnabledTimeMs = 0;
    private SparseIntArray mHistogramAttachDuration = new SparseIntArray();
    private SparseIntArray mHistogramAwareAvailableDurationMs = new SparseIntArray();
    private SparseIntArray mHistogramAwareEnabledDurationMs = new SparseIntArray();
    private SparseIntArray mHistogramNdpDuration = new SparseIntArray();
    private SparseIntArray mHistogramPublishDuration = new SparseIntArray();
    private SparseIntArray mHistogramSubscribeDuration = new SparseIntArray();
    private SparseIntArray mHistogramSubscribeGeofenceMax = new SparseIntArray();
    private SparseIntArray mHistogramSubscribeGeofenceMin = new SparseIntArray();
    private SparseIntArray mInBandNdpStatusData = new SparseIntArray();
    private long mLastEnableAwareInThisSampleWindowMs = 0;
    private long mLastEnableAwareMs = 0;
    private long mLastEnableUsageInThisSampleWindowMs = 0;
    private long mLastEnableUsageMs = 0;
    private final Object mLock = new Object();
    private int mMaxDiscoveryInApp = 0;
    private int mMaxDiscoveryInSystem = 0;
    private int mMaxNdiInApp = 0;
    private int mMaxNdiInSystem = 0;
    private int mMaxNdpInApp = 0;
    private int mMaxNdpInSystem = 0;
    private int mMaxNdpPerNdi = 0;
    private int mMaxPublishInApp = 0;
    private int mMaxPublishInSystem = 0;
    private int mMaxPublishWithRangingInApp = 0;
    private int mMaxPublishWithRangingInSystem = 0;
    private int mMaxSecureNdpInApp = 0;
    private int mMaxSecureNdpInSystem = 0;
    private int mMaxSubscribeInApp = 0;
    private int mMaxSubscribeInSystem = 0;
    private int mMaxSubscribeWithRangingInApp = 0;
    private int mMaxSubscribeWithRangingInSystem = 0;
    private SparseIntArray mNdpCreationTimeDuration = new SparseIntArray();
    private long mNdpCreationTimeMax = 0;
    private long mNdpCreationTimeMin = -1;
    private long mNdpCreationTimeNumSamples = 0;
    private long mNdpCreationTimeSum = 0;
    private long mNdpCreationTimeSumSq = 0;
    private int mNumMatchesWithRanging = 0;
    private int mNumMatchesWithoutRangingForRangingEnabledSubscribes = 0;
    private int mNumSubscribesWithRanging = 0;
    private SparseIntArray mOutOfBandNdpStatusData = new SparseIntArray();
    private SparseIntArray mPublishStatusData = new SparseIntArray();
    private SparseIntArray mSubscribeStatusData = new SparseIntArray();

    /* access modifiers changed from: private */
    public static class AttachData {
        int mMaxConcurrentAttaches;
        boolean mUsesIdentityCallback;

        private AttachData() {
        }
    }

    public WifiAwareMetrics(Clock clock) {
        this.mClock = clock;
    }

    public void recordEnableUsage() {
        synchronized (this.mLock) {
            if (this.mLastEnableUsageMs != 0) {
                Log.w(TAG, "enableUsage: mLastEnableUsage*Ms initialized!?");
            }
            this.mLastEnableUsageMs = this.mClock.getElapsedSinceBootMillis();
            this.mLastEnableUsageInThisSampleWindowMs = this.mLastEnableUsageMs;
        }
    }

    public void recordDisableUsage() {
        synchronized (this.mLock) {
            if (this.mLastEnableUsageMs == 0) {
                Log.e(TAG, "disableUsage: mLastEnableUsage not initialized!?");
                return;
            }
            long now = this.mClock.getElapsedSinceBootMillis();
            MetricsUtils.addValueToLogHistogram(now - this.mLastEnableUsageMs, this.mHistogramAwareAvailableDurationMs, DURATION_LOG_HISTOGRAM);
            this.mAvailableTimeMs += now - this.mLastEnableUsageInThisSampleWindowMs;
            this.mLastEnableUsageMs = 0;
            this.mLastEnableUsageInThisSampleWindowMs = 0;
        }
    }

    public void recordEnableAware() {
        synchronized (this.mLock) {
            if (this.mLastEnableAwareMs == 0) {
                this.mLastEnableAwareMs = this.mClock.getElapsedSinceBootMillis();
                this.mLastEnableAwareInThisSampleWindowMs = this.mLastEnableAwareMs;
            }
        }
    }

    public void recordDisableAware() {
        synchronized (this.mLock) {
            if (this.mLastEnableAwareMs != 0) {
                long now = this.mClock.getElapsedSinceBootMillis();
                MetricsUtils.addValueToLogHistogram(now - this.mLastEnableAwareMs, this.mHistogramAwareEnabledDurationMs, DURATION_LOG_HISTOGRAM);
                this.mEnabledTimeMs += now - this.mLastEnableAwareInThisSampleWindowMs;
                this.mLastEnableAwareMs = 0;
                this.mLastEnableAwareInThisSampleWindowMs = 0;
            }
        }
    }

    public void recordAttachSession(int uid, boolean usesIdentityCallback, SparseArray<WifiAwareClientState> clients) {
        int currentConcurrentCount = 0;
        for (int i = 0; i < clients.size(); i++) {
            if (clients.valueAt(i).getUid() == uid) {
                currentConcurrentCount++;
            }
        }
        synchronized (this.mLock) {
            AttachData data = this.mAttachDataByUid.get(Integer.valueOf(uid));
            if (data == null) {
                data = new AttachData();
                this.mAttachDataByUid.put(Integer.valueOf(uid), data);
            }
            data.mUsesIdentityCallback |= usesIdentityCallback;
            data.mMaxConcurrentAttaches = Math.max(data.mMaxConcurrentAttaches, currentConcurrentCount);
            recordAttachStatus(0);
        }
    }

    public void recordAttachStatus(int status) {
        synchronized (this.mLock) {
            this.mAttachStatusData.put(status, this.mAttachStatusData.get(status) + 1);
        }
    }

    public void recordAttachSessionDuration(long creationTime) {
        synchronized (this.mLock) {
            MetricsUtils.addValueToLogHistogram(this.mClock.getElapsedSinceBootMillis() - creationTime, this.mHistogramAttachDuration, DURATION_LOG_HISTOGRAM);
        }
    }

    public void recordDiscoverySession(int uid, SparseArray<WifiAwareClientState> clients) {
        recordDiscoverySessionInternal(uid, clients, false, -1, -1);
    }

    public void recordDiscoverySessionWithRanging(int uid, boolean isSubscriberWithRanging, int minRange, int maxRange, SparseArray<WifiAwareClientState> clients) {
        recordDiscoverySessionInternal(uid, clients, isSubscriberWithRanging, minRange, maxRange);
    }

    private void recordDiscoverySessionInternal(int uid, SparseArray<WifiAwareClientState> clients, boolean isRangingEnabledSubscriber, int minRange, int maxRange) {
        int numPublishesOnUid = 0;
        int numSubscribesOnUid = 0;
        int numPublishesInSystem = 0;
        int numSubscribesWithRangingInSystem = 0;
        int numPublishesWithRangingInSystem = 0;
        int i = 0;
        int numSubscribesWithRangingOnUid = 0;
        int numSubscribesInSystem = 0;
        int numSubscribesInSystem2 = 0;
        while (i < clients.size()) {
            WifiAwareClientState client = clients.valueAt(i);
            boolean sameUid = client.getUid() == uid;
            SparseArray<WifiAwareDiscoverySessionState> sessions = client.getSessions();
            int numPublishesInSystem2 = numSubscribesInSystem2;
            int j = 0;
            int numPublishesWithRangingOnUid = numSubscribesWithRangingOnUid;
            int numSubscribesWithRangingOnUid2 = numPublishesWithRangingInSystem;
            int numPublishesWithRangingInSystem2 = numPublishesInSystem;
            while (j < sessions.size()) {
                WifiAwareDiscoverySessionState session = sessions.valueAt(j);
                boolean isRangingEnabledForThisSession = session.isRangingEnabled();
                if (session.isPublishSession()) {
                    numPublishesInSystem2++;
                    if (isRangingEnabledForThisSession) {
                        numPublishesWithRangingInSystem2++;
                    }
                    if (sameUid) {
                        numPublishesOnUid++;
                        if (isRangingEnabledForThisSession) {
                            numPublishesWithRangingOnUid++;
                        }
                    }
                } else {
                    numSubscribesInSystem++;
                    if (isRangingEnabledForThisSession) {
                        numSubscribesWithRangingInSystem++;
                    }
                    if (sameUid) {
                        numSubscribesOnUid++;
                        if (isRangingEnabledForThisSession) {
                            numSubscribesWithRangingOnUid2++;
                        }
                    }
                }
                j++;
                client = client;
            }
            i++;
            numSubscribesInSystem2 = numPublishesInSystem2;
            numPublishesInSystem = numPublishesWithRangingInSystem2;
            numPublishesWithRangingInSystem = numSubscribesWithRangingOnUid2;
            numSubscribesWithRangingOnUid = numPublishesWithRangingOnUid;
        }
        synchronized (this.mLock) {
            this.mMaxPublishInApp = Math.max(this.mMaxPublishInApp, numPublishesOnUid);
            this.mMaxSubscribeInApp = Math.max(this.mMaxSubscribeInApp, numSubscribesOnUid);
            this.mMaxDiscoveryInApp = Math.max(this.mMaxDiscoveryInApp, numPublishesOnUid + numSubscribesOnUid);
            this.mMaxPublishInSystem = Math.max(this.mMaxPublishInSystem, numSubscribesInSystem2);
            this.mMaxSubscribeInSystem = Math.max(this.mMaxSubscribeInSystem, numSubscribesInSystem);
            this.mMaxDiscoveryInSystem = Math.max(this.mMaxDiscoveryInSystem, numSubscribesInSystem2 + numSubscribesInSystem);
            this.mMaxPublishWithRangingInApp = Math.max(this.mMaxPublishWithRangingInApp, numSubscribesWithRangingOnUid);
            this.mMaxSubscribeWithRangingInApp = Math.max(this.mMaxSubscribeWithRangingInApp, numPublishesWithRangingInSystem);
            this.mMaxPublishWithRangingInSystem = Math.max(this.mMaxPublishWithRangingInSystem, numPublishesInSystem);
            this.mMaxSubscribeWithRangingInSystem = Math.max(this.mMaxSubscribeWithRangingInSystem, numSubscribesWithRangingInSystem);
            if (isRangingEnabledSubscriber) {
                this.mNumSubscribesWithRanging++;
            }
            if (minRange != -1) {
                MetricsUtils.addValueToLinearHistogram(minRange, this.mHistogramSubscribeGeofenceMin, RANGING_LIMIT_METERS);
            }
            if (maxRange != -1) {
                MetricsUtils.addValueToLinearHistogram(maxRange, this.mHistogramSubscribeGeofenceMax, RANGING_LIMIT_METERS);
            }
        }
    }

    public void recordDiscoveryStatus(int uid, int status, boolean isPublish) {
        synchronized (this.mLock) {
            if (isPublish) {
                this.mPublishStatusData.put(status, this.mPublishStatusData.get(status) + 1);
            } else {
                this.mSubscribeStatusData.put(status, this.mSubscribeStatusData.get(status) + 1);
            }
            if (status == 4) {
                this.mAppsWithDiscoverySessionResourceFailure.add(Integer.valueOf(uid));
            }
        }
    }

    public void recordDiscoverySessionDuration(long creationTime, boolean isPublish) {
        synchronized (this.mLock) {
            MetricsUtils.addValueToLogHistogram(this.mClock.getElapsedSinceBootMillis() - creationTime, isPublish ? this.mHistogramPublishDuration : this.mHistogramSubscribeDuration, DURATION_LOG_HISTOGRAM);
        }
    }

    public void recordMatchIndicationForRangeEnabledSubscribe(boolean rangeProvided) {
        if (rangeProvided) {
            this.mNumMatchesWithRanging++;
        } else {
            this.mNumMatchesWithoutRangingForRangingEnabledSubscribes++;
        }
    }

    public void recordNdpCreation(int uid, Map<WifiAwareNetworkSpecifier, WifiAwareDataPathStateManager.AwareNetworkRequestInformation> networkRequestCache) {
        int numNdpInSystem = 0;
        int numSecureNdpInSystem = 0;
        Map<String, Integer> ndpPerNdiMap = new HashMap<>();
        Set<String> ndiInApp = new HashSet<>();
        Set<String> ndiInSystem = new HashSet<>();
        int numSecureNdpInApp = 0;
        int numNdpInApp = 0;
        for (WifiAwareDataPathStateManager.AwareNetworkRequestInformation anri : networkRequestCache.values()) {
            if (anri.state == 102) {
                boolean isSecure = false;
                boolean sameUid = anri.uid == uid;
                if (!TextUtils.isEmpty(anri.networkSpecifier.passphrase) || !(anri.networkSpecifier.pmk == null || anri.networkSpecifier.pmk.length == 0)) {
                    isSecure = true;
                }
                if (sameUid) {
                    numNdpInApp++;
                    if (isSecure) {
                        numSecureNdpInApp++;
                    }
                    ndiInApp.add(anri.interfaceName);
                }
                numNdpInSystem++;
                if (isSecure) {
                    numSecureNdpInSystem++;
                }
                Integer ndpCount = ndpPerNdiMap.get(anri.interfaceName);
                if (ndpCount == null) {
                    ndpPerNdiMap.put(anri.interfaceName, 1);
                } else {
                    ndpPerNdiMap.put(anri.interfaceName, Integer.valueOf(ndpCount.intValue() + 1));
                }
                ndiInSystem.add(anri.interfaceName);
            }
        }
        synchronized (this.mLock) {
            this.mMaxNdiInApp = Math.max(this.mMaxNdiInApp, ndiInApp.size());
            this.mMaxNdpInApp = Math.max(this.mMaxNdpInApp, numNdpInApp);
            this.mMaxSecureNdpInApp = Math.max(this.mMaxSecureNdpInApp, numSecureNdpInApp);
            this.mMaxNdiInSystem = Math.max(this.mMaxNdiInSystem, ndiInSystem.size());
            this.mMaxNdpInSystem = Math.max(this.mMaxNdpInSystem, numNdpInSystem);
            this.mMaxSecureNdpInSystem = Math.max(this.mMaxSecureNdpInSystem, numSecureNdpInSystem);
            this.mMaxNdpPerNdi = Math.max(this.mMaxNdpPerNdi, ((Integer) Collections.max(ndpPerNdiMap.values())).intValue());
        }
    }

    public void recordNdpStatus(int status, boolean isOutOfBand, long startTimestamp) {
        synchronized (this.mLock) {
            if (isOutOfBand) {
                this.mOutOfBandNdpStatusData.put(status, this.mOutOfBandNdpStatusData.get(status) + 1);
            } else {
                this.mInBandNdpStatusData.put(status, this.mOutOfBandNdpStatusData.get(status) + 1);
            }
            if (status == 0) {
                long creationTime = this.mClock.getElapsedSinceBootMillis() - startTimestamp;
                MetricsUtils.addValueToLogHistogram(creationTime, this.mNdpCreationTimeDuration, DURATION_LOG_HISTOGRAM);
                this.mNdpCreationTimeMin = this.mNdpCreationTimeMin == -1 ? creationTime : Math.min(this.mNdpCreationTimeMin, creationTime);
                this.mNdpCreationTimeMax = Math.max(this.mNdpCreationTimeMax, creationTime);
                this.mNdpCreationTimeSum += creationTime;
                this.mNdpCreationTimeSumSq += creationTime * creationTime;
                this.mNdpCreationTimeNumSamples++;
            }
        }
    }

    public void recordNdpSessionDuration(long creationTime) {
        synchronized (this.mLock) {
            MetricsUtils.addValueToLogHistogram(this.mClock.getElapsedSinceBootMillis() - creationTime, this.mHistogramNdpDuration, DURATION_LOG_HISTOGRAM);
        }
    }

    public WifiMetricsProto.WifiAwareLog consolidateProto() {
        WifiMetricsProto.WifiAwareLog log = new WifiMetricsProto.WifiAwareLog();
        long now = this.mClock.getElapsedSinceBootMillis();
        synchronized (this.mLock) {
            log.histogramAwareAvailableDurationMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mHistogramAwareAvailableDurationMs, DURATION_LOG_HISTOGRAM));
            log.availableTimeMs = this.mAvailableTimeMs;
            if (this.mLastEnableUsageInThisSampleWindowMs != 0) {
                log.availableTimeMs += now - this.mLastEnableUsageInThisSampleWindowMs;
            }
            log.histogramAwareEnabledDurationMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mHistogramAwareEnabledDurationMs, DURATION_LOG_HISTOGRAM));
            log.enabledTimeMs = this.mEnabledTimeMs;
            if (this.mLastEnableAwareInThisSampleWindowMs != 0) {
                log.enabledTimeMs += now - this.mLastEnableAwareInThisSampleWindowMs;
            }
            log.numApps = this.mAttachDataByUid.size();
            log.numAppsUsingIdentityCallback = 0;
            log.maxConcurrentAttachSessionsInApp = 0;
            for (AttachData ad : this.mAttachDataByUid.values()) {
                if (ad.mUsesIdentityCallback) {
                    log.numAppsUsingIdentityCallback++;
                }
                log.maxConcurrentAttachSessionsInApp = Math.max(log.maxConcurrentAttachSessionsInApp, ad.mMaxConcurrentAttaches);
            }
            log.histogramAttachSessionStatus = histogramToProtoArray(this.mAttachStatusData);
            log.histogramAttachDurationMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mHistogramAttachDuration, DURATION_LOG_HISTOGRAM));
            log.maxConcurrentPublishInApp = this.mMaxPublishInApp;
            log.maxConcurrentSubscribeInApp = this.mMaxSubscribeInApp;
            log.maxConcurrentDiscoverySessionsInApp = this.mMaxDiscoveryInApp;
            log.maxConcurrentPublishInSystem = this.mMaxPublishInSystem;
            log.maxConcurrentSubscribeInSystem = this.mMaxSubscribeInSystem;
            log.maxConcurrentDiscoverySessionsInSystem = this.mMaxDiscoveryInSystem;
            log.histogramPublishStatus = histogramToProtoArray(this.mPublishStatusData);
            log.histogramSubscribeStatus = histogramToProtoArray(this.mSubscribeStatusData);
            log.numAppsWithDiscoverySessionFailureOutOfResources = this.mAppsWithDiscoverySessionResourceFailure.size();
            log.histogramPublishSessionDurationMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mHistogramPublishDuration, DURATION_LOG_HISTOGRAM));
            log.histogramSubscribeSessionDurationMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mHistogramSubscribeDuration, DURATION_LOG_HISTOGRAM));
            log.maxConcurrentPublishWithRangingInApp = this.mMaxPublishWithRangingInApp;
            log.maxConcurrentSubscribeWithRangingInApp = this.mMaxSubscribeWithRangingInApp;
            log.maxConcurrentPublishWithRangingInSystem = this.mMaxPublishWithRangingInSystem;
            log.maxConcurrentSubscribeWithRangingInSystem = this.mMaxSubscribeWithRangingInSystem;
            log.histogramSubscribeGeofenceMin = histogramToProtoArray(MetricsUtils.linearHistogramToGenericBuckets(this.mHistogramSubscribeGeofenceMin, RANGING_LIMIT_METERS));
            log.histogramSubscribeGeofenceMax = histogramToProtoArray(MetricsUtils.linearHistogramToGenericBuckets(this.mHistogramSubscribeGeofenceMax, RANGING_LIMIT_METERS));
            log.numSubscribesWithRanging = this.mNumSubscribesWithRanging;
            log.numMatchesWithRanging = this.mNumMatchesWithRanging;
            log.numMatchesWithoutRangingForRangingEnabledSubscribes = this.mNumMatchesWithoutRangingForRangingEnabledSubscribes;
            log.maxConcurrentNdiInApp = this.mMaxNdiInApp;
            log.maxConcurrentNdiInSystem = this.mMaxNdiInSystem;
            log.maxConcurrentNdpInApp = this.mMaxNdpInApp;
            log.maxConcurrentNdpInSystem = this.mMaxNdpInSystem;
            log.maxConcurrentSecureNdpInApp = this.mMaxSecureNdpInApp;
            log.maxConcurrentSecureNdpInSystem = this.mMaxSecureNdpInSystem;
            log.maxConcurrentNdpPerNdi = this.mMaxNdpPerNdi;
            log.histogramRequestNdpStatus = histogramToProtoArray(this.mInBandNdpStatusData);
            log.histogramRequestNdpOobStatus = histogramToProtoArray(this.mOutOfBandNdpStatusData);
            log.histogramNdpCreationTimeMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mNdpCreationTimeDuration, DURATION_LOG_HISTOGRAM));
            log.ndpCreationTimeMsMin = this.mNdpCreationTimeMin;
            log.ndpCreationTimeMsMax = this.mNdpCreationTimeMax;
            log.ndpCreationTimeMsSum = this.mNdpCreationTimeSum;
            log.ndpCreationTimeMsSumOfSq = this.mNdpCreationTimeSumSq;
            log.ndpCreationTimeMsNumSamples = this.mNdpCreationTimeNumSamples;
            log.histogramNdpSessionDurationMs = histogramToProtoArray(MetricsUtils.logHistogramToGenericBuckets(this.mHistogramNdpDuration, DURATION_LOG_HISTOGRAM));
        }
        return log;
    }

    public void clear() {
        long now = this.mClock.getElapsedSinceBootMillis();
        synchronized (this.mLock) {
            this.mHistogramAwareAvailableDurationMs.clear();
            this.mAvailableTimeMs = 0;
            if (this.mLastEnableUsageInThisSampleWindowMs != 0) {
                this.mLastEnableUsageInThisSampleWindowMs = now;
            }
            this.mHistogramAwareEnabledDurationMs.clear();
            this.mEnabledTimeMs = 0;
            if (this.mLastEnableAwareInThisSampleWindowMs != 0) {
                this.mLastEnableAwareInThisSampleWindowMs = now;
            }
            this.mAttachDataByUid.clear();
            this.mAttachStatusData.clear();
            this.mHistogramAttachDuration.clear();
            this.mMaxPublishInApp = 0;
            this.mMaxSubscribeInApp = 0;
            this.mMaxDiscoveryInApp = 0;
            this.mMaxPublishInSystem = 0;
            this.mMaxSubscribeInSystem = 0;
            this.mMaxDiscoveryInSystem = 0;
            this.mPublishStatusData.clear();
            this.mSubscribeStatusData.clear();
            this.mHistogramPublishDuration.clear();
            this.mHistogramSubscribeDuration.clear();
            this.mAppsWithDiscoverySessionResourceFailure.clear();
            this.mMaxPublishWithRangingInApp = 0;
            this.mMaxSubscribeWithRangingInApp = 0;
            this.mMaxPublishWithRangingInSystem = 0;
            this.mMaxSubscribeWithRangingInSystem = 0;
            this.mHistogramSubscribeGeofenceMin.clear();
            this.mHistogramSubscribeGeofenceMax.clear();
            this.mNumSubscribesWithRanging = 0;
            this.mNumMatchesWithRanging = 0;
            this.mNumMatchesWithoutRangingForRangingEnabledSubscribes = 0;
            this.mMaxNdiInApp = 0;
            this.mMaxNdpInApp = 0;
            this.mMaxSecureNdpInApp = 0;
            this.mMaxNdiInSystem = 0;
            this.mMaxNdpInSystem = 0;
            this.mMaxSecureNdpInSystem = 0;
            this.mMaxNdpPerNdi = 0;
            this.mInBandNdpStatusData.clear();
            this.mOutOfBandNdpStatusData.clear();
            this.mNdpCreationTimeDuration.clear();
            this.mNdpCreationTimeMin = -1;
            this.mNdpCreationTimeMax = 0;
            this.mNdpCreationTimeSum = 0;
            this.mNdpCreationTimeSumSq = 0;
            this.mNdpCreationTimeNumSamples = 0;
            this.mHistogramNdpDuration.clear();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            pw.println("mLastEnableUsageMs:" + this.mLastEnableUsageMs);
            pw.println("mLastEnableUsageInThisSampleWindowMs:" + this.mLastEnableUsageInThisSampleWindowMs);
            pw.println("mAvailableTimeMs:" + this.mAvailableTimeMs);
            pw.println("mHistogramAwareAvailableDurationMs:");
            for (int i = 0; i < this.mHistogramAwareAvailableDurationMs.size(); i++) {
                pw.println("  " + this.mHistogramAwareAvailableDurationMs.keyAt(i) + ": " + this.mHistogramAwareAvailableDurationMs.valueAt(i));
            }
            pw.println("mLastEnableAwareMs:" + this.mLastEnableAwareMs);
            pw.println("mLastEnableAwareInThisSampleWindowMs:" + this.mLastEnableAwareInThisSampleWindowMs);
            pw.println("mEnabledTimeMs:" + this.mEnabledTimeMs);
            pw.println("mHistogramAwareEnabledDurationMs:");
            for (int i2 = 0; i2 < this.mHistogramAwareEnabledDurationMs.size(); i2++) {
                pw.println("  " + this.mHistogramAwareEnabledDurationMs.keyAt(i2) + ": " + this.mHistogramAwareEnabledDurationMs.valueAt(i2));
            }
            pw.println("mAttachDataByUid:");
            for (Map.Entry<Integer, AttachData> ade : this.mAttachDataByUid.entrySet()) {
                pw.println("  uid=" + ade.getKey() + ": identity=" + ade.getValue().mUsesIdentityCallback + ", maxConcurrent=" + ade.getValue().mMaxConcurrentAttaches);
            }
            pw.println("mAttachStatusData:");
            for (int i3 = 0; i3 < this.mAttachStatusData.size(); i3++) {
                pw.println("  " + this.mAttachStatusData.keyAt(i3) + ": " + this.mAttachStatusData.valueAt(i3));
            }
            pw.println("mHistogramAttachDuration:");
            for (int i4 = 0; i4 < this.mHistogramAttachDuration.size(); i4++) {
                pw.println("  " + this.mHistogramAttachDuration.keyAt(i4) + ": " + this.mHistogramAttachDuration.valueAt(i4));
            }
            pw.println("mMaxPublishInApp:" + this.mMaxPublishInApp);
            pw.println("mMaxSubscribeInApp:" + this.mMaxSubscribeInApp);
            pw.println("mMaxDiscoveryInApp:" + this.mMaxDiscoveryInApp);
            pw.println("mMaxPublishInSystem:" + this.mMaxPublishInSystem);
            pw.println("mMaxSubscribeInSystem:" + this.mMaxSubscribeInSystem);
            pw.println("mMaxDiscoveryInSystem:" + this.mMaxDiscoveryInSystem);
            pw.println("mPublishStatusData:");
            for (int i5 = 0; i5 < this.mPublishStatusData.size(); i5++) {
                pw.println("  " + this.mPublishStatusData.keyAt(i5) + ": " + this.mPublishStatusData.valueAt(i5));
            }
            pw.println("mSubscribeStatusData:");
            for (int i6 = 0; i6 < this.mSubscribeStatusData.size(); i6++) {
                pw.println("  " + this.mSubscribeStatusData.keyAt(i6) + ": " + this.mSubscribeStatusData.valueAt(i6));
            }
            pw.println("mHistogramPublishDuration:");
            for (int i7 = 0; i7 < this.mHistogramPublishDuration.size(); i7++) {
                pw.println("  " + this.mHistogramPublishDuration.keyAt(i7) + ": " + this.mHistogramPublishDuration.valueAt(i7));
            }
            pw.println("mHistogramSubscribeDuration:");
            for (int i8 = 0; i8 < this.mHistogramSubscribeDuration.size(); i8++) {
                pw.println("  " + this.mHistogramSubscribeDuration.keyAt(i8) + ": " + this.mHistogramSubscribeDuration.valueAt(i8));
            }
            pw.println("mAppsWithDiscoverySessionResourceFailure:");
            Iterator<Integer> it = this.mAppsWithDiscoverySessionResourceFailure.iterator();
            while (it.hasNext()) {
                pw.println("  " + it.next());
            }
            pw.println("mMaxPublishWithRangingInApp:" + this.mMaxPublishWithRangingInApp);
            pw.println("mMaxSubscribeWithRangingInApp:" + this.mMaxSubscribeWithRangingInApp);
            pw.println("mMaxPublishWithRangingInSystem:" + this.mMaxPublishWithRangingInSystem);
            pw.println("mMaxSubscribeWithRangingInSystem:" + this.mMaxSubscribeWithRangingInSystem);
            pw.println("mHistogramSubscribeGeofenceMin:");
            for (int i9 = 0; i9 < this.mHistogramSubscribeGeofenceMin.size(); i9++) {
                pw.println("  " + this.mHistogramSubscribeGeofenceMin.keyAt(i9) + ": " + this.mHistogramSubscribeGeofenceMin.valueAt(i9));
            }
            pw.println("mHistogramSubscribeGeofenceMax:");
            for (int i10 = 0; i10 < this.mHistogramSubscribeGeofenceMax.size(); i10++) {
                pw.println("  " + this.mHistogramSubscribeGeofenceMax.keyAt(i10) + ": " + this.mHistogramSubscribeGeofenceMax.valueAt(i10));
            }
            pw.println("mNumSubscribesWithRanging:" + this.mNumSubscribesWithRanging);
            pw.println("mNumMatchesWithRanging:" + this.mNumMatchesWithRanging);
            pw.println("mNumMatchesWithoutRangingForRangingEnabledSubscribes:" + this.mNumMatchesWithoutRangingForRangingEnabledSubscribes);
            pw.println("mMaxNdiInApp:" + this.mMaxNdiInApp);
            pw.println("mMaxNdpInApp:" + this.mMaxNdpInApp);
            pw.println("mMaxSecureNdpInApp:" + this.mMaxSecureNdpInApp);
            pw.println("mMaxNdiInSystem:" + this.mMaxNdiInSystem);
            pw.println("mMaxNdpInSystem:" + this.mMaxNdpInSystem);
            pw.println("mMaxSecureNdpInSystem:" + this.mMaxSecureNdpInSystem);
            pw.println("mMaxNdpPerNdi:" + this.mMaxNdpPerNdi);
            pw.println("mInBandNdpStatusData:");
            for (int i11 = 0; i11 < this.mInBandNdpStatusData.size(); i11++) {
                pw.println("  " + this.mInBandNdpStatusData.keyAt(i11) + ": " + this.mInBandNdpStatusData.valueAt(i11));
            }
            pw.println("mOutOfBandNdpStatusData:");
            for (int i12 = 0; i12 < this.mOutOfBandNdpStatusData.size(); i12++) {
                pw.println("  " + this.mOutOfBandNdpStatusData.keyAt(i12) + ": " + this.mOutOfBandNdpStatusData.valueAt(i12));
            }
            pw.println("mNdpCreationTimeDuration:");
            for (int i13 = 0; i13 < this.mNdpCreationTimeDuration.size(); i13++) {
                pw.println("  " + this.mNdpCreationTimeDuration.keyAt(i13) + ": " + this.mNdpCreationTimeDuration.valueAt(i13));
            }
            pw.println("mNdpCreationTimeMin:" + this.mNdpCreationTimeMin);
            pw.println("mNdpCreationTimeMax:" + this.mNdpCreationTimeMax);
            pw.println("mNdpCreationTimeSum:" + this.mNdpCreationTimeSum);
            pw.println("mNdpCreationTimeSumSq:" + this.mNdpCreationTimeSumSq);
            pw.println("mNdpCreationTimeNumSamples:" + this.mNdpCreationTimeNumSamples);
            pw.println("mHistogramNdpDuration:");
            for (int i14 = 0; i14 < this.mHistogramNdpDuration.size(); i14++) {
                pw.println("  " + this.mHistogramNdpDuration.keyAt(i14) + ": " + this.mHistogramNdpDuration.valueAt(i14));
            }
        }
    }

    @VisibleForTesting
    public static WifiMetricsProto.WifiAwareLog.HistogramBucket[] histogramToProtoArray(MetricsUtils.GenericBucket[] buckets) {
        WifiMetricsProto.WifiAwareLog.HistogramBucket[] protoArray = new WifiMetricsProto.WifiAwareLog.HistogramBucket[buckets.length];
        for (int i = 0; i < buckets.length; i++) {
            protoArray[i] = new WifiMetricsProto.WifiAwareLog.HistogramBucket();
            protoArray[i].start = buckets[i].start;
            protoArray[i].end = buckets[i].end;
            protoArray[i].count = buckets[i].count;
        }
        return protoArray;
    }

    public static void addNanHalStatusToHistogram(int halStatus, SparseIntArray histogram) {
        int protoStatus = convertNanStatusTypeToProtoEnum(halStatus);
        histogram.put(protoStatus, histogram.get(protoStatus) + 1);
    }

    @VisibleForTesting
    public static WifiMetricsProto.WifiAwareLog.NanStatusHistogramBucket[] histogramToProtoArray(SparseIntArray histogram) {
        WifiMetricsProto.WifiAwareLog.NanStatusHistogramBucket[] protoArray = new WifiMetricsProto.WifiAwareLog.NanStatusHistogramBucket[histogram.size()];
        for (int i = 0; i < histogram.size(); i++) {
            protoArray[i] = new WifiMetricsProto.WifiAwareLog.NanStatusHistogramBucket();
            protoArray[i].nanStatusType = histogram.keyAt(i);
            protoArray[i].count = histogram.valueAt(i);
        }
        return protoArray;
    }

    public static int convertNanStatusTypeToProtoEnum(int nanStatusType) {
        switch (nanStatusType) {
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
            default:
                Log.e(TAG, "Unrecognized NanStatusType: " + nanStatusType);
                return 14;
        }
    }
}
