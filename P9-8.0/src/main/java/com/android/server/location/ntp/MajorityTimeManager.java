package com.android.server.location.ntp;

import android.content.Context;
import android.provider.Settings.Global;
import android.util.Log;
import com.android.server.location.InjectTimeRecord;
import java.util.Arrays;

public class MajorityTimeManager {
    private static final int DEFAULT_UNCERTAINTY = 30;
    private static final long GPS_UTC_REFERENCE_TIME = 946656000;
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
                return 1;
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
        if (0 == nitzTime && Global.getInt(context.getContentResolver(), "auto_time", 0) != 0) {
            Log.i(TAG, "nitzTime is invalid, get systemTime.");
            nitzTime = System.currentTimeMillis();
        }
        if (gpsTime < GPS_UTC_REFERENCE_TIME) {
            gpsTime = 0;
        }
        if (0 != gpsTime && 0 != ntpTime && 0 != nitzTime) {
            double avg = ((double) ((gpsTime + ntpTime) + nitzTime)) / 3.0d;
            TimeRecord[] timeArr = new TimeRecord[]{new TimeRecord(gpsTime, 3), new TimeRecord(nitzTime, 2), new TimeRecord(ntpTime, 1)};
            Arrays.sort(timeArr);
            if (avg - ((double) timeArr[0].timeValue) < ((double) timeArr[2].timeValue) - avg) {
                choose(injectTimeRecord, timeArr[0], timeArr[1]);
            } else if (avg - ((double) timeArr[0].timeValue) > ((double) timeArr[2].timeValue) - avg) {
                choose(injectTimeRecord, timeArr[1], timeArr[2]);
            } else {
                Log.e(TAG, "Grouping failed, inject gps time: " + gpsTime);
                injectTimeRecord.setInjectTime(gpsTime);
                injectTimeRecord.setUncertainty(30);
            }
        } else if (0 != gpsTime && 0 != nitzTime) {
            Log.i(TAG, "Gps time and nitz time is valid, inject gps time: " + gpsTime);
            injectTimeRecord.setInjectTime(gpsTime);
            injectTimeRecord.setUncertainty(getUncertainty(gpsTime, nitzTime));
        } else if (0 != ntpTime && 0 != nitzTime) {
            Log.i(TAG, "Ntp time and nitz time is valid, inject ntp time: " + ntpTime);
            injectTimeRecord.setInjectTime(ntpTime);
            injectTimeRecord.setUncertainty(getUncertainty(ntpTime, nitzTime));
        } else if (0 != gpsTime && 0 != ntpTime) {
            Log.i(TAG, "Gps time and ntp time is valid, inject gps time: " + gpsTime);
            injectTimeRecord.setInjectTime(gpsTime);
            injectTimeRecord.setUncertainty(getUncertainty(gpsTime, ntpTime));
        } else if (0 != gpsTime) {
            Log.i(TAG, "Gps time is valid, inject gps time: " + gpsTime);
            injectTimeRecord.setInjectTime(gpsTime);
            injectTimeRecord.setUncertainty(30);
        } else if (0 != nitzTime) {
            Log.i(TAG, "Nitz time is valid, inject nitz time: " + nitzTime);
            injectTimeRecord.setInjectTime(nitzTime);
            injectTimeRecord.setUncertainty(30);
        } else {
            Log.i(TAG, "Get time failed, inject invalid time.");
            injectTimeRecord.setInjectTime(0);
        }
        return injectTimeRecord;
    }

    private static void choose(InjectTimeRecord injectTimeRecord, TimeRecord x, TimeRecord y) {
        TimeRecord timeRecord = x.priority > y.priority ? x : y;
        injectTimeRecord.setInjectTime(timeRecord.timeValue);
        injectTimeRecord.setUncertainty(getUncertainty(x.timeValue, y.timeValue));
        Log.i(TAG, "Inject time : " + timeRecord.timeValue + ", priority : " + timeRecord.priority);
    }

    private static int getUncertainty(long x, long y) {
        int difference = (int) Math.abs(x - y);
        return difference > 30 ? difference : 30;
    }
}
