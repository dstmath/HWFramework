package com.android.server.location.ntp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.server.location.LBSLog;

public class GpsTimeManager {
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final long EXPIRT_TIME = 1209600000;
    private static final long INVAILID_TIME = 0;
    private static final long MAX_MISSTAKE_TIME = 10000;
    private static final String TAG = "NtpGpsTimeManager";
    private static final long TIME = 1000;
    private BroadcastReceiver mBootBroadcastReceiver;
    private Context mContext;
    private ElapsedRealTimeCheck mElapsedRealTimeCheck = ElapsedRealTimeCheck.getInstance();
    private long mLastSystemTime;
    private TimeManager mTimeManager = new TimeManager(TAG, EXPIRT_TIME);
    private boolean mValidFlag;

    public GpsTimeManager(Context context) {
        this.mContext = context;
        this.mValidFlag = false;
        if (this.mTimeManager.getmTimestamp() == 0) {
            LBSLog.i(TAG, false, "mTimestamp is zero.", new Object[0]);
            long msTime = SystemProperties.getLong("persist.sys.hwGpsTimestamp", 0);
            long msLastSystemTime = SystemProperties.getLong("persist.sys.hwLastSystemTime", 0);
            if (!(msTime == 0 || msLastSystemTime == 0)) {
                this.mLastSystemTime = System.currentTimeMillis();
                this.mTimeManager.setCurrentTime((this.mLastSystemTime + msTime) - msLastSystemTime, SystemClock.elapsedRealtime());
                LBSLog.i(TAG, false, "getCurrentTime mTimestamp:%{public}d, mTimeReference:%{public}d", Long.valueOf(msTime), Long.valueOf(msLastSystemTime));
            }
        }
        registerBootBroadcastReceiver();
    }

    private void registerBootBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mBootBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.location.ntp.GpsTimeManager.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && "android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                    GpsTimeManager gpsTimeManager = GpsTimeManager.this;
                    if (!gpsTimeManager.checkValid(gpsTimeManager.mTimeManager.getCurrentTime(), GpsTimeManager.this.mLastSystemTime)) {
                        GpsTimeManager.this.setGpsTime(0, 0);
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBootBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkValid(long gpsTime, long lastSystemTime) {
        if (Math.abs((gpsTime - this.mTimeManager.getmTimestamp()) - (System.currentTimeMillis() - lastSystemTime)) > 10000) {
            return false;
        }
        return true;
    }

    public long getGpsTime() {
        long tmpTime = this.mTimeManager.getCurrentTime();
        if (this.mValidFlag || tmpTime <= 0 || checkValid(tmpTime, this.mLastSystemTime)) {
            return tmpTime;
        }
        return 0;
    }

    public void setGpsTime(long gpsMsTime, long nanosSynsBoot) {
        this.mValidFlag = true;
        this.mLastSystemTime = System.currentTimeMillis();
        this.mTimeManager.setCurrentTime(gpsMsTime, nanosSynsBoot / 1000000);
        SystemProperties.set("persist.sys.hwGpsTimestamp", String.valueOf(gpsMsTime));
        SystemProperties.set("persist.sys.hwLastSystemTime", String.valueOf(this.mLastSystemTime));
        this.mElapsedRealTimeCheck.checkRealTime(gpsMsTime);
    }
}
