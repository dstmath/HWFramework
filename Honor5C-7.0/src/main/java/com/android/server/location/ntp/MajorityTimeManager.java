package com.android.server.location.ntp;

import android.content.Context;
import android.provider.Settings.Global;
import android.util.Log;
import com.android.server.location.InjectTimeRecord;
import java.util.Arrays;

public class MajorityTimeManager {
    private static final int DEFAULT_UNCERTAINTY = 30;
    private static final long INVAILID_TIME = 0;
    private static final int PRIORITY_GPS = 3;
    private static final int PRIORITY_NITZ = 2;
    private static final int PRIORITY_NTP = 1;
    private static final String TAG = "MajorityTimeManager";

    static class TimeRecord implements Comparable<TimeRecord> {
        private int priority;
        private long timeValue;

        public TimeRecord(long timeValue, int priority) {
            this.timeValue = timeValue;
            this.priority = priority;
        }

        public int compareTo(TimeRecord o) {
            if (this.timeValue < o.timeValue) {
                return -1;
            }
            if (this.timeValue > o.timeValue) {
                return MajorityTimeManager.PRIORITY_NTP;
            }
            return 0;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (this.timeValue != ((TimeRecord) o).timeValue) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (int) (this.timeValue ^ (this.timeValue >>> 32));
        }
    }

    public static InjectTimeRecord getInjectTime(Context context, long gpsTime, long ntpTime, long nitzTime) {
        InjectTimeRecord injectTimeRecord = new InjectTimeRecord();
        if (INVAILID_TIME == nitzTime && Global.getInt(context.getContentResolver(), "auto_time", 0) != 0) {
            Log.i(TAG, "nitzTime is invalid, get systemTime.");
            nitzTime = System.currentTimeMillis();
        }
        if (INVAILID_TIME != gpsTime && INVAILID_TIME != ntpTime && INVAILID_TIME != nitzTime) {
            double avg = ((double) ((gpsTime + ntpTime) + nitzTime)) / 3.0d;
            TimeRecord[] timeArr = new TimeRecord[PRIORITY_GPS];
            timeArr[0] = new TimeRecord(gpsTime, PRIORITY_GPS);
            timeArr[PRIORITY_NTP] = new TimeRecord(nitzTime, PRIORITY_NITZ);
            timeArr[PRIORITY_NITZ] = new TimeRecord(ntpTime, PRIORITY_NTP);
            Arrays.sort(timeArr);
            if (avg - ((double) timeArr[0].timeValue) < ((double) timeArr[PRIORITY_NITZ].timeValue) - avg) {
                choose(injectTimeRecord, timeArr[0], timeArr[PRIORITY_NTP]);
            } else if (avg - ((double) timeArr[0].timeValue) > ((double) timeArr[PRIORITY_NITZ].timeValue) - avg) {
                choose(injectTimeRecord, timeArr[PRIORITY_NTP], timeArr[PRIORITY_NITZ]);
            } else {
                Log.e(TAG, "Grouping failed, inject gps time: " + gpsTime);
                injectTimeRecord.setInjectTime(gpsTime);
                injectTimeRecord.setUncertainty(DEFAULT_UNCERTAINTY);
            }
        } else if (INVAILID_TIME != gpsTime && INVAILID_TIME != nitzTime) {
            Log.i(TAG, "Gps time and nitz time is valid, inject gps time: " + gpsTime);
            injectTimeRecord.setInjectTime(gpsTime);
            injectTimeRecord.setUncertainty(getUncertainty(gpsTime, nitzTime));
        } else if (INVAILID_TIME != ntpTime && INVAILID_TIME != nitzTime) {
            Log.i(TAG, "Ntp time and nitz time is valid, inject ntp time: " + ntpTime);
            injectTimeRecord.setInjectTime(ntpTime);
            injectTimeRecord.setUncertainty(getUncertainty(ntpTime, nitzTime));
        } else if (INVAILID_TIME != gpsTime && INVAILID_TIME != ntpTime) {
            Log.i(TAG, "Gps time and ntp time is valid, inject gps time: " + gpsTime);
            injectTimeRecord.setInjectTime(gpsTime);
            injectTimeRecord.setUncertainty(getUncertainty(gpsTime, ntpTime));
        } else if (INVAILID_TIME != gpsTime) {
            Log.i(TAG, "Gps time is valid, inject gps time: " + gpsTime);
            injectTimeRecord.setInjectTime(gpsTime);
            injectTimeRecord.setUncertainty(DEFAULT_UNCERTAINTY);
        } else if (INVAILID_TIME != nitzTime) {
            Log.i(TAG, "Nitz time is valid, inject nitz time: " + nitzTime);
            injectTimeRecord.setInjectTime(nitzTime);
            injectTimeRecord.setUncertainty(DEFAULT_UNCERTAINTY);
        } else {
            Log.i(TAG, "Get time failed, inject invalid time.");
            injectTimeRecord.setInjectTime(INVAILID_TIME);
        }
        return injectTimeRecord;
    }

    private static void choose(InjectTimeRecord injectTimeRecord, TimeRecord x, TimeRecord y) {
        TimeRecord timeRecord;
        if (x.priority > y.priority) {
            timeRecord = x;
        } else {
            timeRecord = y;
        }
        injectTimeRecord.setInjectTime(timeRecord.timeValue);
        injectTimeRecord.setUncertainty(getUncertainty(x.timeValue, y.timeValue));
        Log.i(TAG, "Inject time : " + timeRecord.timeValue + ", priority : " + timeRecord.priority);
    }

    private static int getUncertainty(long x, long y) {
        int difference = (int) Math.abs(x - y);
        return difference > DEFAULT_UNCERTAINTY ? difference : DEFAULT_UNCERTAINTY;
    }
}
