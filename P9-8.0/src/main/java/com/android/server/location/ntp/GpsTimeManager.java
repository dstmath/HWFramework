package com.android.server.location.ntp;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;

public class GpsTimeManager {
    private static final long EXPIRT_TIME = 1209600000;
    private static final String TAG = "NtpGpsTimeManager";
    private ElapsedRealTimeCheck mElapsedRealTimeCheck = ElapsedRealTimeCheck.getInstance();
    private TimeManager mTimeManager = new TimeManager(TAG, EXPIRT_TIME);

    public GpsTimeManager() {
        if (this.mTimeManager.getmTimestamp() == 0) {
            Log.i(TAG, "mTimestamp is zero.");
            long msTime = SystemProperties.getLong("persist.sys.hwGpsTimestamp", 0);
            long msLastSystemTime = SystemProperties.getLong("persist.sys.hwLastSystemTime", 0);
            if (0 != msTime && 0 != msLastSystemTime) {
                this.mTimeManager.setCurrentTime((System.currentTimeMillis() + msTime) - msLastSystemTime, SystemClock.elapsedRealtime());
                Log.i(TAG, "getCurrentTime mTimestamp:" + msTime + " mTimeReference:" + msLastSystemTime);
            }
        }
    }

    public long getGpsTime() {
        return this.mTimeManager.getCurrentTime();
    }

    public void setGpsTime(long gpsMsTime, long nanosSynsBoot) {
        this.mTimeManager.setCurrentTime(gpsMsTime, nanosSynsBoot / 1000000);
        SystemProperties.set("persist.sys.hwGpsTimestamp", String.valueOf(gpsMsTime));
        SystemProperties.set("persist.sys.hwLastSystemTime", String.valueOf(System.currentTimeMillis()));
        this.mElapsedRealTimeCheck.checkRealTime(gpsMsTime);
    }
}
