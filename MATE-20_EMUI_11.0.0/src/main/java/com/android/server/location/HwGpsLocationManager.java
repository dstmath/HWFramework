package com.android.server.location;

import android.content.Context;
import com.android.server.location.ntp.GpsNtpTimeCheck;

public class HwGpsLocationManager implements IHwGpsLocationManager {
    private static final String TAG = "HwGpsLocationProviderEx";
    private static HwGpsLocationManager sHwGpsLocationManager;
    private GpsNtpTimeCheck mGpsNtpTimeCheck;

    public static synchronized IHwGpsLocationManager getInstance(Context context) {
        HwGpsLocationManager hwGpsLocationManager;
        synchronized (HwGpsLocationManager.class) {
            if (sHwGpsLocationManager == null) {
                sHwGpsLocationManager = new HwGpsLocationManager(context);
            }
            hwGpsLocationManager = sHwGpsLocationManager;
        }
        return hwGpsLocationManager;
    }

    private HwGpsLocationManager(Context context) {
        this.mGpsNtpTimeCheck = GpsNtpTimeCheck.getInstance(context);
    }

    public boolean checkNtpTime(long ntpMsTime, long msTimeSynsBoot) {
        return this.mGpsNtpTimeCheck.checkNtpTime(ntpMsTime, msTimeSynsBoot);
    }

    public void setGpsTime(long gpsMsTime, long nanosSynsBoot) {
        this.mGpsNtpTimeCheck.setGpsTime(gpsMsTime, nanosSynsBoot);
    }

    public InjectTimeRecord getInjectTime(long ntpTime) {
        return this.mGpsNtpTimeCheck.getInjectTime(ntpTime);
    }
}
