package com.android.internal.telephony;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.Rlog;
import com.android.internal.annotations.VisibleForTesting;

@TargetApi(8)
public class RilWakelockInfo {
    private final String LOG_TAG = RilWakelockInfo.class.getSimpleName();
    private int mConcurrentRequests;
    private long mLastAggregatedTime;
    private long mRequestTime;
    private long mResponseTime;
    private int mRilRequestSent;
    private int mTokenNumber;
    private long mWakelockTimeAttributedSoFar;

    @VisibleForTesting
    public int getConcurrentRequests() {
        return this.mConcurrentRequests;
    }

    RilWakelockInfo(int rilRequest, int tokenNumber, int concurrentRequests, long requestTime) {
        int concurrentRequests2 = validateConcurrentRequests(concurrentRequests);
        this.mRilRequestSent = rilRequest;
        this.mTokenNumber = tokenNumber;
        this.mConcurrentRequests = concurrentRequests2;
        this.mRequestTime = requestTime;
        this.mWakelockTimeAttributedSoFar = 0;
        this.mLastAggregatedTime = requestTime;
    }

    private int validateConcurrentRequests(int concurrentRequests) {
        if (concurrentRequests > 0) {
            return concurrentRequests;
        }
        if (!Build.IS_DEBUGGABLE) {
            return 1;
        }
        IllegalArgumentException e = new IllegalArgumentException("concurrentRequests should always be greater than 0.");
        Rlog.e(this.LOG_TAG, e.toString());
        throw e;
    }

    /* access modifiers changed from: package-private */
    public int getTokenNumber() {
        return this.mTokenNumber;
    }

    /* access modifiers changed from: package-private */
    public int getRilRequestSent() {
        return this.mRilRequestSent;
    }

    /* access modifiers changed from: package-private */
    public void setResponseTime(long responseTime) {
        updateTime(responseTime);
        this.mResponseTime = responseTime;
    }

    /* access modifiers changed from: package-private */
    public void updateConcurrentRequests(int concurrentRequests, long time) {
        int concurrentRequests2 = validateConcurrentRequests(concurrentRequests);
        updateTime(time);
        this.mConcurrentRequests = concurrentRequests2;
    }

    /* access modifiers changed from: package-private */
    public synchronized void updateTime(long time) {
        this.mWakelockTimeAttributedSoFar += (time - this.mLastAggregatedTime) / ((long) this.mConcurrentRequests);
        this.mLastAggregatedTime = time;
    }

    /* access modifiers changed from: package-private */
    public long getWakelockTimeAttributedToClient() {
        return this.mWakelockTimeAttributedSoFar;
    }

    public String toString() {
        return "WakelockInfo{rilRequestSent=" + this.mRilRequestSent + ", tokenNumber=" + this.mTokenNumber + ", requestTime=" + this.mRequestTime + ", responseTime=" + this.mResponseTime + ", mWakelockTimeAttributed=" + this.mWakelockTimeAttributedSoFar + '}';
    }
}
