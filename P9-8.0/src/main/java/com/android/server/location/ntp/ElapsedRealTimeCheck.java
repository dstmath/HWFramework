package com.android.server.location.ntp;

import android.os.SystemClock;
import android.util.Log;

public class ElapsedRealTimeCheck {
    private static boolean DBG = true;
    private static final long MAX_MISS_MS = 24000;
    private static final String TAG = "NtpElapsedRealTimeCheck";
    private static ElapsedRealTimeCheck mElapsedRealTimeCheck;
    private boolean mCanTrustElapsedRealTime = true;
    private long mTimeBegin;
    private long mTimeBeginElapsed;
    private long mTimeCheck;
    private long mTimeCheckElapsed;

    public static synchronized ElapsedRealTimeCheck getInstance() {
        ElapsedRealTimeCheck elapsedRealTimeCheck;
        synchronized (ElapsedRealTimeCheck.class) {
            if (mElapsedRealTimeCheck == null) {
                mElapsedRealTimeCheck = new ElapsedRealTimeCheck();
            }
            elapsedRealTimeCheck = mElapsedRealTimeCheck;
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
        }
        if (this.mTimeBegin != 0 && this.mTimeCheck != 0) {
            long missTime = (this.mTimeCheck - this.mTimeBegin) - (this.mTimeCheckElapsed - this.mTimeBeginElapsed);
            if (DBG) {
                Log.d(TAG, "checkRealTime missTime:" + missTime);
            }
            if (Math.abs(missTime) >= MAX_MISS_MS) {
                this.mCanTrustElapsedRealTime = false;
            } else {
                this.mCanTrustElapsedRealTime = true;
            }
            this.mTimeCheck = 0;
            this.mTimeCheckElapsed = 0;
        }
    }

    public boolean canTrustElapsedRealTime() {
        return this.mCanTrustElapsedRealTime;
    }
}
