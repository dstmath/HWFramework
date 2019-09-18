package com.android.server.location.ntp;

import android.os.SystemClock;
import android.util.Log;

public class TimeManager {
    private static boolean DBG = true;
    private static final long GPS_UTC_REFERENCE_TIME = 946656000;
    private static final long INVAILID_TIME = 0;
    private long mExpireTime;
    private String mTag;
    private long mTimeSynsBoot;
    private long mTimestamp;

    public TimeManager(String tag, long expireTime) {
        this.mExpireTime = expireTime;
        this.mTag = tag;
    }

    public long getCurrentTime() {
        if (DBG) {
            String str = this.mTag;
            Log.d(str, "beginning mTimestamp is " + this.mTimestamp);
        }
        if (this.mTimestamp < GPS_UTC_REFERENCE_TIME) {
            return 0;
        }
        if (!ElapsedRealTimeCheck.getInstance().canTrustElapsedRealTime()) {
            if (DBG) {
                Log.d(this.mTag, "getCurrentTime ElapsedRealTime INVAILID_TIME");
            }
            return 0;
        }
        long timeTillNow = SystemClock.elapsedRealtime() - this.mTimeSynsBoot;
        if (timeTillNow >= this.mExpireTime) {
            if (DBG) {
                Log.d(this.mTag, "getCurrentTime INVAILID_TIME");
            }
            return 0;
        }
        if (DBG) {
            String str2 = this.mTag;
            Log.d(str2, "end mTimestamp is " + this.mTimestamp);
        }
        if (DBG) {
            String str3 = this.mTag;
            Log.d(str3, "getCurrentTime:" + (this.mTimestamp + timeTillNow));
        }
        return this.mTimestamp + timeTillNow;
    }

    public void setCurrentTime(long msTime, long msTimeSynsBoot) {
        this.mTimestamp = msTime;
        this.mTimeSynsBoot = msTimeSynsBoot;
        if (DBG) {
            String str = this.mTag;
            Log.d(str, "setCurrentTime mTimestamp:" + this.mTimestamp + " mTimeReference:" + this.mTimeSynsBoot);
        }
    }

    public long getmTimestamp() {
        return this.mTimestamp;
    }
}
