package com.android.server.usage;

import java.util.TimeZone;

public class UnixCalendar {
    public static final long DAY_IN_MILLIS = 86400000;
    public static final long MONTH_IN_MILLIS = 2592000000L;
    public static final long WEEK_IN_MILLIS = 604800000;
    public static final long YEAR_IN_MILLIS = 31536000000L;
    private long mTime;

    public UnixCalendar(long time) {
        this.mTime = time;
    }

    public void addDays(int val) {
        this.mTime += ((long) val) * DAY_IN_MILLIS;
    }

    public void addWeeks(int val) {
        this.mTime += ((long) val) * WEEK_IN_MILLIS;
    }

    public void addMonths(int val) {
        this.mTime += ((long) val) * MONTH_IN_MILLIS;
    }

    public void addYears(int val) {
        this.mTime += ((long) val) * YEAR_IN_MILLIS;
    }

    public void setTimeInMillis(long time) {
        this.mTime = time;
    }

    public long getTimeInMillis() {
        return this.mTime;
    }

    public void truncateToDay() {
        this.mTime += (long) TimeZone.getDefault().getRawOffset();
        this.mTime -= this.mTime % DAY_IN_MILLIS;
        this.mTime -= (long) TimeZone.getDefault().getRawOffset();
    }

    public void truncateToWeek() {
        this.mTime -= this.mTime % WEEK_IN_MILLIS;
    }

    public void truncateToMonth() {
        this.mTime -= this.mTime % MONTH_IN_MILLIS;
    }

    public void truncateToYear() {
        this.mTime -= this.mTime % YEAR_IN_MILLIS;
    }

    public static void truncateTo(UnixCalendar calendar, int intervalType) {
        switch (intervalType) {
            case 0:
                calendar.truncateToDay();
                return;
            case 1:
                calendar.truncateToWeek();
                return;
            case 2:
                calendar.truncateToMonth();
                return;
            case 3:
                calendar.truncateToYear();
                return;
            default:
                throw new UnsupportedOperationException("Can't truncate date to interval " + intervalType);
        }
    }
}
