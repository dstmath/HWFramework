package android.app.usage;

import android.content.Context;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.util.IntArray;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class NetworkStats implements AutoCloseable {
    private static final String TAG = "NetworkStats";
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final long mEndTimeStamp;
    private int mEnumerationIndex = 0;
    private NetworkStatsHistory mHistory = null;
    private NetworkStatsHistory.Entry mRecycledHistoryEntry = null;
    private NetworkStats.Entry mRecycledSummaryEntry = null;
    private INetworkStatsSession mSession;
    private final long mStartTimeStamp;
    private int mState = -1;
    private android.net.NetworkStats mSummary = null;
    private int mTag = 0;
    private NetworkTemplate mTemplate;
    private int mUidOrUidIndex;
    private int[] mUids;

    public static class Bucket {
        public static final int DEFAULT_NETWORK_ALL = -1;
        public static final int DEFAULT_NETWORK_NO = 1;
        public static final int DEFAULT_NETWORK_YES = 2;
        public static final int METERED_ALL = -1;
        public static final int METERED_NO = 1;
        public static final int METERED_YES = 2;
        public static final int ROAMING_ALL = -1;
        public static final int ROAMING_NO = 1;
        public static final int ROAMING_YES = 2;
        public static final int STATE_ALL = -1;
        public static final int STATE_DEFAULT = 1;
        public static final int STATE_FOREGROUND = 2;
        public static final int STATE_STATIC = 9;
        public static final int TAG_NONE = 0;
        public static final int UID_ALL = -1;
        public static final int UID_REMOVED = -4;
        public static final int UID_TETHERING = -5;
        /* access modifiers changed from: private */
        public long mBeginTimeStamp;
        /* access modifiers changed from: private */
        public int mDefaultNetworkStatus;
        /* access modifiers changed from: private */
        public long mEndTimeStamp;
        /* access modifiers changed from: private */
        public int mMetered;
        /* access modifiers changed from: private */
        public int mRoaming;
        /* access modifiers changed from: private */
        public long mRxBytes;
        /* access modifiers changed from: private */
        public long mRxPackets;
        /* access modifiers changed from: private */
        public int mState;
        /* access modifiers changed from: private */
        public int mTag;
        /* access modifiers changed from: private */
        public long mTxBytes;
        /* access modifiers changed from: private */
        public long mTxPackets;
        /* access modifiers changed from: private */
        public int mUid;

        @Retention(RetentionPolicy.SOURCE)
        public @interface DefaultNetworkStatus {
        }

        @Retention(RetentionPolicy.SOURCE)
        public @interface Metered {
        }

        @Retention(RetentionPolicy.SOURCE)
        public @interface Roaming {
        }

        @Retention(RetentionPolicy.SOURCE)
        public @interface State {
        }

        /* access modifiers changed from: private */
        public static int convertSet(int state) {
            if (state == -1) {
                return -1;
            }
            switch (state) {
                case 1:
                    return 0;
                case 2:
                    return 1;
                default:
                    return 0;
            }
        }

        /* access modifiers changed from: private */
        public static int convertState(int networkStatsSet) {
            if (networkStatsSet == 9) {
                return 9;
            }
            switch (networkStatsSet) {
                case -1:
                    return -1;
                case 0:
                    return 1;
                case 1:
                    return 2;
                default:
                    return 0;
            }
        }

        /* access modifiers changed from: private */
        public static int convertUid(int uid) {
            switch (uid) {
                case -5:
                    return -5;
                case -4:
                    return -4;
                default:
                    return uid;
            }
        }

        /* access modifiers changed from: private */
        public static int convertTag(int tag) {
            if (tag != 0) {
                return tag;
            }
            return 0;
        }

        /* access modifiers changed from: private */
        public static int convertMetered(int metered) {
            switch (metered) {
                case -1:
                    return -1;
                case 0:
                    return 1;
                case 1:
                    return 2;
                default:
                    return 0;
            }
        }

        /* access modifiers changed from: private */
        public static int convertRoaming(int roaming) {
            switch (roaming) {
                case -1:
                    return -1;
                case 0:
                    return 1;
                case 1:
                    return 2;
                default:
                    return 0;
            }
        }

        /* access modifiers changed from: private */
        public static int convertDefaultNetworkStatus(int defaultNetworkStatus) {
            switch (defaultNetworkStatus) {
                case -1:
                    return -1;
                case 0:
                    return 1;
                case 1:
                    return 2;
                default:
                    return 0;
            }
        }

        public int getUid() {
            return this.mUid;
        }

        public int getTag() {
            return this.mTag;
        }

        public int getState() {
            return this.mState;
        }

        public int getMetered() {
            return this.mMetered;
        }

        public int getRoaming() {
            return this.mRoaming;
        }

        public int getDefaultNetworkStatus() {
            return this.mDefaultNetworkStatus;
        }

        public long getStartTimeStamp() {
            return this.mBeginTimeStamp;
        }

        public long getEndTimeStamp() {
            return this.mEndTimeStamp;
        }

        public long getRxBytes() {
            return this.mRxBytes;
        }

        public long getTxBytes() {
            return this.mTxBytes;
        }

        public long getRxPackets() {
            return this.mRxPackets;
        }

        public long getTxPackets() {
            return this.mTxPackets;
        }
    }

    NetworkStats(Context context, NetworkTemplate template, int flags, long startTimestamp, long endTimestamp, INetworkStatsService statsService) throws RemoteException, SecurityException {
        this.mSession = statsService.openSessionForUsageStats(flags, context.getOpPackageName());
        this.mCloseGuard.open("close");
        this.mTemplate = template;
        this.mStartTimeStamp = startTimestamp;
        this.mEndTimeStamp = endTimestamp;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    public boolean getNextBucket(Bucket bucketOut) {
        if (this.mSummary != null) {
            return getNextSummaryBucket(bucketOut);
        }
        return getNextHistoryBucket(bucketOut);
    }

    public boolean hasNextBucket() {
        boolean z = true;
        if (this.mSummary != null) {
            if (this.mEnumerationIndex >= this.mSummary.size()) {
                z = false;
            }
            return z;
        } else if (this.mHistory == null) {
            return false;
        } else {
            if (this.mEnumerationIndex >= this.mHistory.size() && !hasNextUid()) {
                z = false;
            }
            return z;
        }
    }

    public void close() {
        if (this.mSession != null) {
            try {
                this.mSession.close();
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        this.mSession = null;
        if (this.mCloseGuard != null) {
            this.mCloseGuard.close();
        }
    }

    /* access modifiers changed from: package-private */
    public Bucket getDeviceSummaryForNetwork() throws RemoteException {
        this.mSummary = this.mSession.getDeviceSummaryForNetwork(this.mTemplate, this.mStartTimeStamp, this.mEndTimeStamp);
        this.mEnumerationIndex = this.mSummary.size();
        return getSummaryAggregate();
    }

    /* access modifiers changed from: package-private */
    public void startSummaryEnumeration() throws RemoteException {
        this.mSummary = this.mSession.getSummaryForAllUid(this.mTemplate, this.mStartTimeStamp, this.mEndTimeStamp, false);
        this.mEnumerationIndex = 0;
    }

    /* access modifiers changed from: package-private */
    public void startHistoryEnumeration(int uid, int tag, int state) {
        this.mHistory = null;
        try {
            this.mHistory = this.mSession.getHistoryIntervalForUid(this.mTemplate, uid, Bucket.convertSet(state), tag, -1, this.mStartTimeStamp, this.mEndTimeStamp);
            setSingleUidTagState(uid, tag, state);
        } catch (RemoteException e) {
            Log.w(TAG, e);
        }
        this.mEnumerationIndex = 0;
    }

    /* access modifiers changed from: package-private */
    public void startUserUidEnumeration() throws RemoteException {
        int[] uids = this.mSession.getRelevantUids();
        IntArray filteredUids = new IntArray(uids.length);
        for (int uid : uids) {
            try {
                NetworkStatsHistory history = this.mSession.getHistoryIntervalForUid(this.mTemplate, uid, -1, 0, -1, this.mStartTimeStamp, this.mEndTimeStamp);
                if (history != null && history.size() > 0) {
                    filteredUids.add(uid);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Error while getting history of uid " + uid, e);
            }
        }
        this.mUids = filteredUids.toArray();
        this.mUidOrUidIndex = -1;
        stepHistory();
    }

    private void stepHistory() {
        if (hasNextUid()) {
            stepUid();
            this.mHistory = null;
            try {
                this.mHistory = this.mSession.getHistoryIntervalForUid(this.mTemplate, getUid(), -1, 0, -1, this.mStartTimeStamp, this.mEndTimeStamp);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
            this.mEnumerationIndex = 0;
        }
    }

    private void fillBucketFromSummaryEntry(Bucket bucketOut) {
        int unused = bucketOut.mUid = Bucket.convertUid(this.mRecycledSummaryEntry.uid);
        int unused2 = bucketOut.mTag = Bucket.convertTag(this.mRecycledSummaryEntry.tag);
        int unused3 = bucketOut.mState = Bucket.convertState(this.mRecycledSummaryEntry.set);
        int unused4 = bucketOut.mDefaultNetworkStatus = Bucket.convertDefaultNetworkStatus(this.mRecycledSummaryEntry.defaultNetwork);
        int unused5 = bucketOut.mMetered = Bucket.convertMetered(this.mRecycledSummaryEntry.metered);
        int unused6 = bucketOut.mRoaming = Bucket.convertRoaming(this.mRecycledSummaryEntry.roaming);
        long unused7 = bucketOut.mBeginTimeStamp = this.mStartTimeStamp;
        long unused8 = bucketOut.mEndTimeStamp = this.mEndTimeStamp;
        long unused9 = bucketOut.mRxBytes = this.mRecycledSummaryEntry.rxBytes;
        long unused10 = bucketOut.mRxPackets = this.mRecycledSummaryEntry.rxPackets;
        long unused11 = bucketOut.mTxBytes = this.mRecycledSummaryEntry.txBytes;
        long unused12 = bucketOut.mTxPackets = this.mRecycledSummaryEntry.txPackets;
    }

    private boolean getNextSummaryBucket(Bucket bucketOut) {
        if (bucketOut == null || this.mEnumerationIndex >= this.mSummary.size()) {
            return false;
        }
        android.net.NetworkStats networkStats = this.mSummary;
        int i = this.mEnumerationIndex;
        this.mEnumerationIndex = i + 1;
        this.mRecycledSummaryEntry = networkStats.getValues(i, this.mRecycledSummaryEntry);
        fillBucketFromSummaryEntry(bucketOut);
        return true;
    }

    /* access modifiers changed from: package-private */
    public Bucket getSummaryAggregate() {
        if (this.mSummary == null) {
            return null;
        }
        Bucket bucket = new Bucket();
        if (this.mRecycledSummaryEntry == null) {
            this.mRecycledSummaryEntry = new NetworkStats.Entry();
        }
        this.mSummary.getTotal(this.mRecycledSummaryEntry);
        fillBucketFromSummaryEntry(bucket);
        return bucket;
    }

    private boolean getNextHistoryBucket(Bucket bucketOut) {
        if (!(bucketOut == null || this.mHistory == null)) {
            if (this.mEnumerationIndex < this.mHistory.size()) {
                NetworkStatsHistory networkStatsHistory = this.mHistory;
                int i = this.mEnumerationIndex;
                this.mEnumerationIndex = i + 1;
                this.mRecycledHistoryEntry = networkStatsHistory.getValues(i, this.mRecycledHistoryEntry);
                int unused = bucketOut.mUid = Bucket.convertUid(getUid());
                int unused2 = bucketOut.mTag = Bucket.convertTag(this.mTag);
                int unused3 = bucketOut.mState = this.mState;
                int unused4 = bucketOut.mDefaultNetworkStatus = -1;
                int unused5 = bucketOut.mMetered = -1;
                int unused6 = bucketOut.mRoaming = -1;
                long unused7 = bucketOut.mBeginTimeStamp = this.mRecycledHistoryEntry.bucketStart;
                long unused8 = bucketOut.mEndTimeStamp = this.mRecycledHistoryEntry.bucketStart + this.mRecycledHistoryEntry.bucketDuration;
                long unused9 = bucketOut.mRxBytes = this.mRecycledHistoryEntry.rxBytes;
                long unused10 = bucketOut.mRxPackets = this.mRecycledHistoryEntry.rxPackets;
                long unused11 = bucketOut.mTxBytes = this.mRecycledHistoryEntry.txBytes;
                long unused12 = bucketOut.mTxPackets = this.mRecycledHistoryEntry.txPackets;
                return true;
            } else if (hasNextUid()) {
                stepHistory();
                return getNextHistoryBucket(bucketOut);
            }
        }
        return false;
    }

    private boolean isUidEnumeration() {
        return this.mUids != null;
    }

    private boolean hasNextUid() {
        return isUidEnumeration() && this.mUidOrUidIndex + 1 < this.mUids.length;
    }

    private int getUid() {
        if (!isUidEnumeration()) {
            return this.mUidOrUidIndex;
        }
        if (this.mUidOrUidIndex >= 0 && this.mUidOrUidIndex < this.mUids.length) {
            return this.mUids[this.mUidOrUidIndex];
        }
        throw new IndexOutOfBoundsException("Index=" + this.mUidOrUidIndex + " mUids.length=" + this.mUids.length);
    }

    private void setSingleUidTagState(int uid, int tag, int state) {
        this.mUidOrUidIndex = uid;
        this.mTag = tag;
        this.mState = state;
    }

    private void stepUid() {
        if (this.mUids != null) {
            this.mUidOrUidIndex++;
        }
    }
}
