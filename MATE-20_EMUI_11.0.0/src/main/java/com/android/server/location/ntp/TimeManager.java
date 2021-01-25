package com.android.server.location.ntp;

import android.os.SystemClock;
import com.android.server.location.LBSLog;

public class TimeManager {
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
        LBSLog.i(this.mTag, false, "beginning mTimestamp is %{public}d", Long.valueOf(this.mTimestamp));
        if (this.mTimestamp < GPS_UTC_REFERENCE_TIME) {
            return 0;
        }
        if (!ElapsedRealTimeCheck.getInstance().canTrustElapsedRealTime()) {
            LBSLog.i(this.mTag, false, "getCurrentTime ElapsedRealTime INVAILID_TIME", new Object[0]);
            return 0;
        }
        long timeTillNow = SystemClock.elapsedRealtime() - this.mTimeSynsBoot;
        if (timeTillNow >= this.mExpireTime) {
            LBSLog.i(this.mTag, false, "getCurrentTime INVAILID_TIME", new Object[0]);
            return 0;
        }
        LBSLog.i(this.mTag, false, "end mTimestamp is %{public}d", Long.valueOf(this.mTimestamp));
        LBSLog.i(this.mTag, false, "getCurrentTime:%{public}d", Long.valueOf(this.mTimestamp + timeTillNow));
        return this.mTimestamp + timeTillNow;
    }

    public void setCurrentTime(long msTime, long msTimeSynsBoot) {
        this.mTimestamp = msTime;
        this.mTimeSynsBoot = msTimeSynsBoot;
        LBSLog.i(this.mTag, false, "setCurrentTime mTimestamp:%{public}d,  mTimeReference:%{public}d", Long.valueOf(this.mTimestamp), Long.valueOf(this.mTimeSynsBoot));
    }

    public long getmTimestamp() {
        return this.mTimestamp;
    }
}
