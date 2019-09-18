package com.android.server.location.ntp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

public class GpsTimeManager {
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final long EXPIRT_TIME = 1209600000;
    private static final long INVAILID_TIME = 0;
    private static final long MAX_MISSTAKE_TIME = 10000;
    private static final int NTP_TIME_INJECT_CLOSE = 0;
    private static final String NTP_TIME_INJECT_FLAG = "hw_ntp_time_inject";
    private static final String TAG = "NtpGpsTimeManager";
    private BroadcastReceiver mBootBroadcastReceiver;
    private Context mContext;
    private ElapsedRealTimeCheck mElapsedRealTimeCheck = ElapsedRealTimeCheck.getInstance();
    /* access modifiers changed from: private */
    public long mLastSystemTime;
    /* access modifiers changed from: private */
    public TimeManager mTimeManager = new TimeManager(TAG, EXPIRT_TIME);
    private boolean mValidFlag;
    private int ntpSwitch;

    public GpsTimeManager(Context context) {
        this.mContext = context;
        this.ntpSwitch = Settings.Secure.getInt(this.mContext.getContentResolver(), NTP_TIME_INJECT_FLAG, 0);
        if (0 == this.mTimeManager.getmTimestamp()) {
            Log.i(TAG, "mTimestamp is zero.");
            long msTime = SystemProperties.getLong("persist.sys.hwGpsTimestamp", 0);
            long msLastSystemTime = SystemProperties.getLong("persist.sys.hwLastSystemTime", 0);
            if (!(0 == msTime || 0 == msLastSystemTime)) {
                this.mLastSystemTime = System.currentTimeMillis();
                this.mTimeManager.setCurrentTime((this.mLastSystemTime + msTime) - msLastSystemTime, SystemClock.elapsedRealtime());
                Log.i(TAG, "getCurrentTime mTimestamp:" + msTime + " mTimeReference:" + msLastSystemTime);
            }
        }
        this.mValidFlag = false;
        if (this.ntpSwitch > 0) {
            registerBootBroadcastReceiver();
        }
    }

    private void registerBootBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SHUTDOWN);
        this.mBootBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && intent.getAction().equals(GpsTimeManager.ACTION_SHUTDOWN) && !GpsTimeManager.this.checkValid(GpsTimeManager.this.mTimeManager.getCurrentTime(), GpsTimeManager.this.mLastSystemTime)) {
                    GpsTimeManager.this.setGpsTime(0, 0);
                }
            }
        };
        this.mContext.registerReceiver(this.mBootBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public boolean checkValid(long gpsTime, long lastSystemTime) {
        if (Math.abs((gpsTime - this.mTimeManager.getmTimestamp()) - (System.currentTimeMillis() - lastSystemTime)) > 10000) {
            return false;
        }
        return true;
    }

    public long getGpsTime() {
        long tmpTime = this.mTimeManager.getCurrentTime();
        if (this.ntpSwitch <= 0 || this.mValidFlag || tmpTime <= 0 || checkValid(tmpTime, this.mLastSystemTime)) {
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
