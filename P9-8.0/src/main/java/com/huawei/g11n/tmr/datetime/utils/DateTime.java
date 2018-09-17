package com.huawei.g11n.tmr.datetime.utils;

import huawei.android.provider.HwSettings.System;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
    private Day date;
    private Time time;

    public static class Day {
        public int day = -1;
        public int month = -1;
        public int year = -1;

        public Day(int y, int m, int d) {
            this.year = y;
            this.month = m;
            this.day = d;
        }

        public int getYear() {
            return this.year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return this.month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return this.day;
        }

        public void setDay(int day) {
            this.day = day;
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
        public int clock = -1;
        public boolean isMarkBefore;
        public String mark;
        public int minute = -1;
        public int second = 0;
        public String timezone;

        public Time(int c, int m, int s, String mark, String timezone, boolean isBefore) {
            this.clock = c;
            this.minute = m;
            this.second = s;
            this.mark = mark;
            this.timezone = timezone;
            this.isMarkBefore = isBefore;
        }

        public int getClock() {
            return this.clock;
        }

        public void setClock(int clock) {
            this.clock = clock;
        }

        public int getMinute() {
            return this.minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public int getSecond() {
            return this.second;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public String toString() {
            return this.clock + ":" + this.minute;
        }

        public String getMark() {
            return this.mark;
        }

        public void setMark(String mark) {
            this.mark = mark;
        }

        public String getTimezone() {
            return this.timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public boolean isMarkBefore() {
            return this.isMarkBefore;
        }

        public void setMarkBefore(boolean isMarkBefore) {
            this.isMarkBefore = isMarkBefore;
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

    public void setDate(Date d) {
        if (d != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            this.date = new Day(c.get(1), c.get(2), c.get(5));
        }
    }

    public void setDay(Day day) {
        this.date = day;
    }

    public Time getTime() {
        return this.time;
    }

    public void setTime(int h, int m, int s, String mk, String tz, boolean isBefore) {
        if (mk.equals("mm")) {
            if (h >= 4 && h <= 11) {
                mk = "am";
            } else {
                mk = "pm";
            }
        }
        this.time = new Time(h, m, s, mk, tz, isBefore);
    }

    public void setTime(Time t) {
        this.time = new Time(t.getClock(), t.getMinute(), t.getSecond(), t.getMark(), t.getTimezone(), t.isMarkBefore());
    }

    public void setDay(int year, int month, int day) {
        int y = -1;
        int m = -1;
        int d = -1;
        if (year > -1) {
            if (year < 100) {
                year += System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT;
            }
            y = year;
        }
        if (month > -1) {
            m = month;
        }
        if (day > -1) {
            d = day;
        }
        this.date = new Day(y, m, d);
    }

    public void setDayByWeekValue(int w, long defaultTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(defaultTime));
        int c = calendar.get(7);
        if (c != 1) {
            c -= 2;
        } else {
            c = 6;
        }
        calendar.add(5, w - c);
        setDate(calendar.getTime());
    }

    public void setDayByAddDays(int d, long defaultTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(defaultTime));
        calendar.add(5, d);
        setDate(calendar.getTime());
    }

    public int getSatuts() {
        int result = -1;
        boolean dflag = true;
        boolean tflag = true;
        if (this.date == null || this.date.isEmpty()) {
            dflag = false;
        }
        if (this.time == null || this.time.isEmpty()) {
            tflag = false;
        }
        if (dflag && tflag) {
            return 0;
        }
        if (dflag) {
            result = 1;
        }
        if (tflag) {
            return 2;
        }
        return result;
    }

    public boolean isEmpty() {
        if (this.date == null && this.time == null) {
            return true;
        }
        return false;
    }
}
