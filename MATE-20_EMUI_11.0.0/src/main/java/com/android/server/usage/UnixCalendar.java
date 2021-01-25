package com.android.server.usage;

import android.util.Slog;
import java.util.TimeZone;

public class UnixCalendar {
    public static final long DAY_IN_MILLIS = 86400000;
    public static final long MONTH_IN_MILLIS = 2592000000L;
    private static final String TAG = "UsageEvents";
    public static final long WEEK_IN_MILLIS = 604800000;
    public static final long YEAR_IN_MILLIS = 31536000000L;
    private long mTime;

    public UnixCalendar(long time) {
        this.mTime = time;
    }

    public void addDays(int val) {
        this.mTime += ((long) val) * 86400000;
    }

    public void addWeeks(int val) {
        this.mTime += ((long) val) * WEEK_IN_MILLIS;
    }

    public void addMonths(int val) {
        this.mTime += ((long) val) * MONTH_IN_MILLIS;
    }

    public void addYears(int val) {
        this.mTime += ((long) val) * 31536000000L;
    }

    public void setTimeInMillis(long time) {
        this.mTime = time;
    }

    public long getTimeInMillis() {
        return this.mTime;
    }

    public void truncateToDay() {
        this.mTime += (long) TimeZone.getDefault().getRawOffset();
        long j = this.mTime;
        this.mTime = j - (j % 86400000);
        this.mTime -= (long) TimeZone.getDefault().getRawOffset();
    }

    public void truncateToWeek() {
        long j = this.mTime;
        this.mTime = j - (j % WEEK_IN_MILLIS);
    }

    public void truncateToMonth() {
        long j = this.mTime;
        this.mTime = j - (j % MONTH_IN_MILLIS);
    }

    public void truncateToYear() {
        long j = this.mTime;
        this.mTime = j - (j % 31536000000L);
    }

    public static void truncateTo(UnixCalendar calendar, int intervalType) {
        if (intervalType == 0) {
            calendar.truncateToDay();
        } else if (intervalType == 1) {
            calendar.truncateToWeek();
        } else if (intervalType == 2) {
            calendar.truncateToMonth();
        } else if (intervalType != 3) {
            Slog.e(TAG, "Can't truncate date to interval " + intervalType);
        } else {
            calendar.truncateToYear();
        }
    }
}
