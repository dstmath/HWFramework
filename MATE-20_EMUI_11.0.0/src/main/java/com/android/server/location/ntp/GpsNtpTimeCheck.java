package com.android.server.location.ntp;

import android.content.Context;
import android.os.SystemClock;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.InjectTimeRecord;
import com.android.server.location.LBSLog;

public class GpsNtpTimeCheck {
    private static final long INVAILID_TIME = 0;
    private static final long MISSTAKE_TIME = 50000;
    private static final String TAG = "HwGpsNtpTimeCheck";
    private static GpsNtpTimeCheck sGpsNtpTimeCheck;
    private Context mContext;
    private GpsTimeManager mGpsTimeManager;
    private NitzTimeManager mNitzTimeManager;

    public static synchronized GpsNtpTimeCheck getInstance(Context context) {
        GpsNtpTimeCheck gpsNtpTimeCheck;
        synchronized (GpsNtpTimeCheck.class) {
            if (sGpsNtpTimeCheck == null) {
                sGpsNtpTimeCheck = new GpsNtpTimeCheck(context);
            }
            gpsNtpTimeCheck = sGpsNtpTimeCheck;
        }
        return gpsNtpTimeCheck;
    }

    private GpsNtpTimeCheck(Context context) {
        this.mContext = context;
        this.mGpsTimeManager = new GpsTimeManager(context);
        this.mNitzTimeManager = new NitzTimeManager(context);
        LBSLog.i(TAG, false, " created", new Object[0]);
    }

    public boolean checkNtpTime(long ntpMsTime, long msTimeSynsBoot) {
        long currentNtpTime = (SystemClock.elapsedRealtime() + ntpMsTime) - msTimeSynsBoot;
        if (this.mGpsTimeManager.getGpsTime() != 0) {
            return compareTime(currentNtpTime, this.mGpsTimeManager.getGpsTime());
        }
        if (this.mNitzTimeManager.getNitzTime() != 0) {
            return compareTime(currentNtpTime, this.mNitzTimeManager.getNitzTime());
        }
        LBSLog.i(TAG, false, "checkNtpTime return false", new Object[0]);
        return false;
    }

    private boolean compareTime(long currentNtpTime, long compareTime) {
        LBSLog.i(TAG, false, "compareTime currentNtpTime:%{public}d, compareTime:%{public}d", Long.valueOf(currentNtpTime), Long.valueOf(compareTime));
        long misstake = Math.abs(currentNtpTime - compareTime);
        if (misstake > MISSTAKE_TIME) {
            LBSLog.i(TAG, false, "find error ntp time:%{public}d", Long.valueOf(misstake));
            HwGpsLogServices.getInstance(this.mContext).reportErrorNtpTime(currentNtpTime, compareTime);
            return false;
        }
        LBSLog.i(TAG, false, "compareTime return true", new Object[0]);
        return true;
    }

    public void setGpsTime(long gpsMsTime, long nanosSynsBoot) {
        this.mGpsTimeManager.setGpsTime(gpsMsTime, nanosSynsBoot);
    }

    public InjectTimeRecord getInjectTime(long ntpTime) {
        return MajorityTimeManager.getInjectTime(this.mContext, this.mGpsTimeManager.getGpsTime(), ntpTime, this.mNitzTimeManager.getNitzTime());
    }
}
