package com.android.server.notification;

import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.util.ArraySet;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class ScheduleCalendar {
    private final Calendar mCalendar = Calendar.getInstance();
    private final ArraySet<Integer> mDays = new ArraySet();
    private ScheduleInfo mSchedule;

    public String toString() {
        return "ScheduleCalendar[mDays=" + this.mDays + ", mSchedule=" + this.mSchedule + "]";
    }

    public void setSchedule(ScheduleInfo schedule) {
        if (!Objects.equals(this.mSchedule, schedule)) {
            this.mSchedule = schedule;
            updateDays();
        }
    }

    public void maybeSetNextAlarm(long now, long nextAlarm) {
        if (this.mSchedule != null && this.mSchedule.exitAtAlarm && now > this.mSchedule.nextAlarm) {
            this.mSchedule.nextAlarm = nextAlarm;
        }
    }

    public void setTimeZone(TimeZone tz) {
        this.mCalendar.setTimeZone(tz);
    }

    public long getNextChangeTime(long now) {
        if (this.mSchedule == null) {
            return 0;
        }
        return Math.min(getNextTime(now, this.mSchedule.startHour, this.mSchedule.startMinute), getNextTime(now, this.mSchedule.endHour, this.mSchedule.endMinute));
    }

    private long getNextTime(long now, int hr, int min) {
        long time = getTime(now, hr, min);
        return time <= now ? addDays(time, 1) : time;
    }

    private long getTime(long millis, int hour, int min) {
        this.mCalendar.setTimeInMillis(millis);
        this.mCalendar.set(11, hour);
        this.mCalendar.set(12, min);
        this.mCalendar.set(13, 0);
        this.mCalendar.set(14, 0);
        return this.mCalendar.getTimeInMillis();
    }

    public boolean isInSchedule(long time) {
        if (this.mSchedule == null || this.mDays.size() == 0) {
            return false;
        }
        long start = getTime(time, this.mSchedule.startHour, this.mSchedule.startMinute);
        long end = getTime(time, this.mSchedule.endHour, this.mSchedule.endMinute);
        if (end <= start) {
            end = addDays(end, 1);
        }
        return !isInSchedule(-1, time, start, end) ? isInSchedule(0, time, start, end) : true;
    }

    public boolean shouldExitForAlarm(long time) {
        if (!this.mSchedule.exitAtAlarm || this.mSchedule.nextAlarm == 0 || time < this.mSchedule.nextAlarm) {
            return false;
        }
        return true;
    }

    private boolean isInSchedule(int daysOffset, long time, long start, long end) {
        int day = ((((getDayOfWeek(time) - 1) + (daysOffset % 7)) + 7) % 7) + 1;
        start = addDays(start, daysOffset);
        end = addDays(end, daysOffset);
        if (!this.mDays.contains(Integer.valueOf(day)) || time < start || time >= end) {
            return false;
        }
        return true;
    }

    private int getDayOfWeek(long time) {
        this.mCalendar.setTimeInMillis(time);
        return this.mCalendar.get(7);
    }

    private void updateDays() {
        this.mDays.clear();
        if (this.mSchedule != null && this.mSchedule.days != null) {
            for (int valueOf : this.mSchedule.days) {
                this.mDays.add(Integer.valueOf(valueOf));
            }
        }
    }

    private long addDays(long time, int days) {
        this.mCalendar.setTimeInMillis(time);
        this.mCalendar.add(5, days);
        return this.mCalendar.getTimeInMillis();
    }
}
