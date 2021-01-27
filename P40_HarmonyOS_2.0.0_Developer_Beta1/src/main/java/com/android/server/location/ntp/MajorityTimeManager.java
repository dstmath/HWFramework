package com.android.server.location.ntp;

import android.content.Context;
import android.provider.Settings;
import com.android.server.location.InjectTimeRecord;
import com.android.server.location.LBSLog;
import java.util.Arrays;

public class MajorityTimeManager {
    private static final int DEFAULT_UNCERTAINTY = 30;
    private static final long GPS_UTC_REFERENCE_TIME = 946656000;
    private static final long INVAILID_TIME = 0;
    private static final int PRIORITY_GPS = 3;
    private static final int PRIORITY_NITZ = 2;
    private static final int PRIORITY_NTP = 1;
    private static final String TAG = "MajorityTimeManager";

    /* access modifiers changed from: package-private */
    public static class TimeRecord implements Comparable<TimeRecord> {
        private int priority;
        private long timeValue;

        public TimeRecord(long timeValue2, int priority2) {
            this.timeValue = timeValue2;
            this.priority = priority2;
        }

        public int compareTo(TimeRecord o) {
            long j = this.timeValue;
            long j2 = o.timeValue;
            if (j < j2) {
                return -1;
            }
            if (j > j2) {
                return 1;
            }
            return 0;
        }

        @Override // java.lang.Object
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            TimeRecord timRecord = null;
            if (object instanceof TimeRecord) {
                timRecord = (TimeRecord) object;
            }
            if (timRecord == null) {
                return false;
            }
            if (this.timeValue == timRecord.timeValue) {
                return true;
            }
            return false;
        }

        @Override // java.lang.Object
        public int hashCode() {
            long j = this.timeValue;
            return (int) (j ^ (j >>> 32));
        }
    }

    public static InjectTimeRecord getInjectTime(Context context, long gpsTime, long ntpTime, long nitzTime) {
        long gpsTime2;
        InjectTimeRecord injectTimeRecord = new InjectTimeRecord();
        if (nitzTime == 0 && Settings.Global.getInt(context.getContentResolver(), "auto_time", 0) != 0) {
            LBSLog.i(TAG, false, "nitzTime is invalid, get systemTime.", new Object[0]);
        }
        if (gpsTime < GPS_UTC_REFERENCE_TIME) {
            gpsTime2 = 0;
        } else {
            gpsTime2 = gpsTime;
        }
        if (gpsTime2 != 0 && ntpTime != 0 && nitzTime != 0) {
            double avg = ((double) ((gpsTime2 + ntpTime) + nitzTime)) / 3.0d;
            TimeRecord[] timeArr = {new TimeRecord(gpsTime2, 3), new TimeRecord(nitzTime, 2), new TimeRecord(ntpTime, 1)};
            Arrays.sort(timeArr);
            if (avg - ((double) timeArr[0].timeValue) < ((double) timeArr[2].timeValue) - avg) {
                choose(injectTimeRecord, timeArr[0], timeArr[1]);
            } else if (avg - ((double) timeArr[0].timeValue) > ((double) timeArr[2].timeValue) - avg) {
                choose(injectTimeRecord, timeArr[1], timeArr[2]);
            } else {
                LBSLog.e(TAG, false, "Grouping failed, inject gps time: %{public}d", Long.valueOf(gpsTime2));
                injectTimeRecord.setInjectTime(gpsTime2);
                injectTimeRecord.setUncertainty(30);
            }
        } else if (gpsTime2 != 0 && nitzTime != 0) {
            LBSLog.i(TAG, false, "Gps time and nitz time is valid, inject gps time: %{public}d", Long.valueOf(gpsTime2));
            injectTimeRecord.setInjectTime(gpsTime2);
            injectTimeRecord.setUncertainty(getUncertainty(gpsTime2, nitzTime));
        } else if (ntpTime != 0 && nitzTime != 0) {
            LBSLog.i(TAG, false, "Ntp time and nitz time is valid, inject ntp time: %{public}d", Long.valueOf(ntpTime));
            injectTimeRecord.setInjectTime(ntpTime);
            injectTimeRecord.setUncertainty(getUncertainty(ntpTime, nitzTime));
        } else if (gpsTime2 != 0 && ntpTime != 0) {
            LBSLog.i(TAG, false, "Gps time and ntp time is valid, inject gps time: %{public}d", Long.valueOf(gpsTime2));
            injectTimeRecord.setInjectTime(gpsTime2);
            injectTimeRecord.setUncertainty(getUncertainty(gpsTime2, ntpTime));
        } else if (gpsTime2 != 0) {
            LBSLog.i(TAG, false, "Gps time is valid, inject gps time: %{public}d", Long.valueOf(gpsTime2));
            injectTimeRecord.setInjectTime(gpsTime2);
            injectTimeRecord.setUncertainty(30);
        } else if (nitzTime != 0) {
            LBSLog.i(TAG, false, "Nitz time is valid, inject nitz time: %{public}d", Long.valueOf(nitzTime));
            injectTimeRecord.setInjectTime(nitzTime);
            injectTimeRecord.setUncertainty(30);
        } else {
            LBSLog.i(TAG, false, "Get time failed, inject invalid time.", new Object[0]);
            injectTimeRecord.setInjectTime(0);
        }
        return injectTimeRecord;
    }

    private static void choose(InjectTimeRecord injectTimeRecord, TimeRecord x, TimeRecord y) {
        TimeRecord timeRecord = x.priority > y.priority ? x : y;
        injectTimeRecord.setInjectTime(timeRecord.timeValue);
        injectTimeRecord.setUncertainty(getUncertainty(x.timeValue, y.timeValue));
        LBSLog.i(TAG, false, "Inject time : %{public}d, priority : %{public}d", Long.valueOf(timeRecord.timeValue), Integer.valueOf(timeRecord.priority));
    }

    private static int getUncertainty(long x, long y) {
        int difference = (int) Math.abs(x - y);
        if (difference > 30) {
            return difference;
        }
        return 30;
    }
}
