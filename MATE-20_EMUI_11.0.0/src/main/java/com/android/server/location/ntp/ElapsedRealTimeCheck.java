package com.android.server.location.ntp;

import android.os.SystemClock;
import com.android.server.location.LBSLog;

public class ElapsedRealTimeCheck {
    private static final boolean DBG = true;
    private static final long MAX_MISS_MS = 24000;
    private static final String TAG = "NtpElapsedRealTimeCheck";
    private static ElapsedRealTimeCheck sElapsedRealTimeCheck;
    private boolean mCanTrustElapsedRealTime = true;
    private long mTimeBegin;
    private long mTimeBeginElapsed;
    private long mTimeCheck;
    private long mTimeCheckElapsed;

    public static synchronized ElapsedRealTimeCheck getInstance() {
        ElapsedRealTimeCheck elapsedRealTimeCheck;
        synchronized (ElapsedRealTimeCheck.class) {
            if (sElapsedRealTimeCheck == null) {
                sElapsedRealTimeCheck = new ElapsedRealTimeCheck();
            }
            elapsedRealTimeCheck = sElapsedRealTimeCheck;
        }
        return elapsedRealTimeCheck;
    }

    private ElapsedRealTimeCheck() {
    }

    public void checkRealTime(long time) {
        if (this.mTimeBegin == 0) {
            this.mTimeBegin = time;
            this.mTimeBeginElapsed = SystemClock.elapsedRealtime();
        } else if (this.mTimeCheck == 0) {
            this.mTimeCheck = time;
            this.mTimeCheckElapsed = SystemClock.elapsedRealtime();
        } else {
            LBSLog.d(TAG, false, "checkRealTime", new Object[0]);
        }
        long j = this.mTimeBegin;
        if (j != 0) {
            long j2 = this.mTimeCheck;
            if (j2 != 0) {
                long missTime = (j2 - j) - (this.mTimeCheckElapsed - this.mTimeBeginElapsed);
                LBSLog.i(TAG, false, "checkRealTime missTime:%{public}d", Long.valueOf(missTime));
                if (Math.abs(missTime) >= MAX_MISS_MS) {
                    this.mCanTrustElapsedRealTime = false;
                } else {
                    this.mCanTrustElapsedRealTime = true;
                }
                this.mTimeCheck = 0;
                this.mTimeCheckElapsed = 0;
            }
        }
    }

    public boolean canTrustElapsedRealTime() {
        return this.mCanTrustElapsedRealTime;
    }
}
