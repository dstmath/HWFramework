package com.android.server.location.ntp;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.InjectTimeRecord;

public class GpsNtpTimeCheck {
    private static boolean DBG = true;
    private static final long INVAILID_TIME = 0;
    private static final long MISSTAKE_TIME = 50000;
    private static final String TAG = "HwGpsNtpTimeCheck";
    private static GpsNtpTimeCheck mGpsNtpTimeCheck;
    private Context mContext;
    private GpsTimeManager mGpsTimeManager = new GpsTimeManager();
    private NitzTimeManager mNitzTimeManager;

    public static synchronized GpsNtpTimeCheck getInstance(Context context) {
        GpsNtpTimeCheck gpsNtpTimeCheck;
        synchronized (GpsNtpTimeCheck.class) {
            if (mGpsNtpTimeCheck == null) {
                mGpsNtpTimeCheck = new GpsNtpTimeCheck(context);
            }
            gpsNtpTimeCheck = mGpsNtpTimeCheck;
        }
        return gpsNtpTimeCheck;
    }

    private GpsNtpTimeCheck(Context context) {
        this.mContext = context;
        this.mNitzTimeManager = new NitzTimeManager(context);
        if (DBG) {
            Log.d(TAG, " created");
        }
    }

    public boolean checkNtpTime(long ntpMsTime, long msTimeSynsBoot) {
        long currentNtpTime = (SystemClock.elapsedRealtime() + ntpMsTime) - msTimeSynsBoot;
        if (this.mGpsTimeManager.getGpsTime() != 0) {
            return compareTime(currentNtpTime, this.mGpsTimeManager.getGpsTime());
        }
        if (this.mNitzTimeManager.getNitzTime() != 0) {
            return compareTime(currentNtpTime, this.mNitzTimeManager.getNitzTime());
        }
        if (DBG) {
            Log.d(TAG, "checkNtpTime return false");
        }
        return false;
    }

    private boolean compareTime(long currentNtpTime, long compareTime) {
        if (DBG) {
            Log.d(TAG, "compareTime currentNtpTime:" + currentNtpTime + " compareTime:" + compareTime);
        }
        long misstake = Math.abs(currentNtpTime - compareTime);
        if (misstake > MISSTAKE_TIME) {
            if (DBG) {
                Log.d(TAG, "find error ntp time:" + misstake);
            }
            HwGpsLogServices.getInstance(this.mContext).reportErrorNtpTime(currentNtpTime, compareTime);
            return false;
        }
        if (DBG) {
            Log.d(TAG, "compareTime return true");
        }
        return true;
    }

    public void setGpsTime(long gpsMsTime, long nanosSynsBoot) {
        this.mGpsTimeManager.setGpsTime(gpsMsTime, nanosSynsBoot);
    }

    public InjectTimeRecord getInjectTime(long ntpTime) {
        return MajorityTimeManager.getInjectTime(this.mContext, this.mGpsTimeManager.getGpsTime(), ntpTime, this.mNitzTimeManager.getNitzTime());
    }
}
