package com.huawei.g11n.tmr.datetime.utils;

import huawei.android.provider.HwSettings.System;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
    private Day date;
    private Time time;

    public static class Day {
        public int day;
        public int month;
        public int year;

        public Day(int i, int i2, int i3) {
            this.year = -1;
            this.month = -1;
            this.day = -1;
            this.year = i;
            this.month = i2;
            this.day = i3;
        }

        public int getYear() {
            return this.year;
        }

        public void setYear(int i) {
            this.year = i;
        }

        public int getMonth() {
            return this.month;
        }

        public void setMonth(int i) {
            this.month = i;
        }

        public int getDay() {
            return this.day;
        }

        public void setDay(int i) {
            this.day = i;
        }

        public String toString() {
            return this.year + "-" + (this.month + 1) + "-" + this.day;
        }

        public boolean isEmpty() {
            if (this.year == -1 && this.month == -1 && this.day == -1) {
                return true;
            }
            return false;
        }
    }

    public static class Time {
        public int clock;
        public boolean isMarkBefore;
        public String mark;
        public int minute;
        public int second;
        public String timezone;

        public Time(int i, int i2, int i3, String str, String str2, boolean z) {
            this.clock = -1;
            this.minute = -1;
            this.second = 0;
            this.clock = i;
            this.minute = i2;
            this.second = i3;
            this.mark = str;
            this.timezone = str2;
            this.isMarkBefore = z;
        }

        public int getClock() {
            return this.clock;
        }

        public void setClock(int i) {
            this.clock = i;
        }

        public int getMinute() {
            return this.minute;
        }

        public void setMinute(int i) {
            this.minute = i;
        }

        public int getSecond() {
            return this.second;
        }

        public void setSecond(int i) {
            this.second = i;
        }

        public String toString() {
            return this.clock + ":" + this.minute;
        }

        public String getMark() {
            return this.mark;
        }

        public void setMark(String str) {
            this.mark = str;
        }

        public String getTimezone() {
            return this.timezone;
        }

        public void setTimezone(String str) {
            this.timezone = str;
        }

        public boolean isMarkBefore() {
            return this.isMarkBefore;
        }

        public void setMarkBefore(boolean z) {
            this.isMarkBefore = z;
        }

        public boolean isEmpty() {
            if (this.clock == -1 && this.second == -1 && this.minute == 0) {
                return true;
            }
            return false;
        }
    }

    public Day getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        if (date != null) {
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            this.date = new Day(instance.get(1), instance.get(2), instance.get(5));
        }
    }

    public void setDay(Day day) {
        this.date = day;
    }

    public Time getTime() {
        return this.time;
    }

    public void setTime(int i, int i2, int i3, String str, String str2, boolean z) {
        this.time = new Time(i, i2, i3, str, str2, z);
    }

    public void setTime(Time time) {
        this.time = new Time(time.getClock(), time.getMinute(), time.getSecond(), time.getMark(), time.getTimezone(), time.isMarkBefore());
    }

    public void setDay(int i, int i2, int i3) {
        if (i <= -1) {
            i = -1;
        } else if (i < 100) {
            i += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
        }
        if (i2 <= -1) {
            i2 = -1;
        }
        if (i3 <= -1) {
            i3 = -1;
        }
        this.date = new Day(i, i2, i3);
    }

    public void setDayByWeekValue(int i, long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(j));
        int i2 = instance.get(7);
        if (i2 != 1) {
            i2 -= 2;
        } else {
            i2 = 6;
        }
        instance.add(5, i - i2);
        setDate(instance.getTime());
    }

    public void setDayByAddDays(int i, long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(j));
        instance.add(5, i);
        setDate(instance.getTime());
    }

    public int getSatuts() {
        int i;
        int i2 = (this.date == null || this.date.isEmpty()) ? 0 : 1;
        int i3 = (this.time == null || this.time.isEmpty()) ? 0 : 1;
        if (i2 != 0) {
            if (i3 != 0) {
                return 0;
            }
        }
        if (i2 == 0) {
            i = -1;
        } else {
            i = 1;
        }
        if (i3 == 0) {
            return i;
        }
        return 2;
    }

    public boolean isEmpty() {
        if (this.date == null && this.time == null) {
            return true;
        }
        return false;
    }
}
