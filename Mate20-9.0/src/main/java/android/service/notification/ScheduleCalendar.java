package android.service.notification;

import android.service.notification.ZenModeConfig;
import android.util.ArraySet;
import android.util.Log;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class ScheduleCalendar {
    public static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    public static final String TAG = "ScheduleCalendar";
    private final Calendar mCalendar = Calendar.getInstance();
    private final ArraySet<Integer> mDays = new ArraySet<>();
    private ZenModeConfig.ScheduleInfo mSchedule;

    public String toString() {
        return "ScheduleCalendar[mDays=" + this.mDays + ", mSchedule=" + this.mSchedule + "]";
    }

    public boolean exitAtAlarm() {
        return this.mSchedule.exitAtAlarm;
    }

    public void setSchedule(ZenModeConfig.ScheduleInfo schedule) {
        if (!Objects.equals(this.mSchedule, schedule)) {
            this.mSchedule = schedule;
            updateDays();
        }
    }

    public void maybeSetNextAlarm(long now, long nextAlarm) {
        if (this.mSchedule != null && this.mSchedule.exitAtAlarm) {
            if (nextAlarm == 0) {
                this.mSchedule.nextAlarm = 0;
            }
            if (nextAlarm > now) {
                if (this.mSchedule.nextAlarm == 0) {
                    this.mSchedule.nextAlarm = nextAlarm;
                    return;
                }
                this.mSchedule.nextAlarm = Math.min(this.mSchedule.nextAlarm, nextAlarm);
            } else if (this.mSchedule.nextAlarm < now) {
                if (DEBUG) {
                    Log.d(TAG, "All alarms are in the past " + this.mSchedule.nextAlarm);
                }
                this.mSchedule.nextAlarm = 0;
            }
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
        long j = time;
        boolean z = false;
        if (this.mSchedule == null || this.mDays.size() == 0) {
            return false;
        }
        long start = getTime(j, this.mSchedule.startHour, this.mSchedule.startMinute);
        long end = getTime(j, this.mSchedule.endHour, this.mSchedule.endMinute);
        if (end <= start) {
            end = addDays(end, 1);
        }
        long end2 = end;
        if (isInSchedule(-1, j, start, end2) || isInSchedule(0, j, start, end2)) {
            z = true;
        }
        return z;
    }

    public boolean isAlarmInSchedule(long alarm, long now) {
        long j = alarm;
        boolean z = false;
        if (this.mSchedule == null || this.mDays.size() == 0) {
            return false;
        }
        long start = getTime(j, this.mSchedule.startHour, this.mSchedule.startMinute);
        long end = getTime(j, this.mSchedule.endHour, this.mSchedule.endMinute);
        if (end <= start) {
            end = addDays(end, 1);
        }
        long end2 = end;
        if ((isInSchedule(-1, j, start, end2) && isInSchedule(-1, now, start, end2)) || (isInSchedule(0, j, start, end2) && isInSchedule(0, now, start, end2))) {
            z = true;
        }
        return z;
    }

    public boolean shouldExitForAlarm(long time) {
        boolean z = false;
        if (this.mSchedule == null) {
            return false;
        }
        if (this.mSchedule.exitAtAlarm && this.mSchedule.nextAlarm != 0 && time >= this.mSchedule.nextAlarm && isAlarmInSchedule(this.mSchedule.nextAlarm, time)) {
            z = true;
        }
        return z;
    }

    private boolean isInSchedule(int daysOffset, long time, long start, long end) {
        long start2 = addDays(start, daysOffset);
        long end2 = addDays(end, daysOffset);
        if (!this.mDays.contains(Integer.valueOf(((((getDayOfWeek(time) - 1) + (daysOffset % 7)) + 7) % 7) + 1)) || time < start2 || time >= end2) {
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
