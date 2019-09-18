package com.huawei.g11n.tmr.datetime.utils;

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

        public void setYear(int year2) {
            this.year = year2;
        }

        public int getMonth() {
            return this.month;
        }

        public void setMonth(int month2) {
            this.month = month2;
        }

        public int getDay() {
            return this.day;
        }

        public void setDay(int day2) {
            this.day = day2;
        }

        public String toString() {
            return String.valueOf(this.year) + "-" + (this.month + 1) + "-" + this.day;
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

        public Time(int c, int m, int s, String mark2, String timezone2, boolean isBefore) {
            this.clock = c;
            this.minute = m;
            this.second = s;
            this.mark = mark2;
            this.timezone = timezone2;
            this.isMarkBefore = isBefore;
        }

        public int getClock() {
            return this.clock;
        }

        public void setClock(int clock2) {
            this.clock = clock2;
        }

        public int getMinute() {
            return this.minute;
        }

        public void setMinute(int minute2) {
            this.minute = minute2;
        }

        public int getSecond() {
            return this.second;
        }

        public void setSecond(int second2) {
            this.second = second2;
        }

        public String toString() {
            return String.valueOf(this.clock) + ":" + this.minute;
        }

        public String getMark() {
            return this.mark;
        }

        public void setMark(String mark2) {
            this.mark = mark2;
        }

        public String getTimezone() {
            return this.timezone;
        }

        public void setTimezone(String timezone2) {
            this.timezone = timezone2;
        }

        public boolean isMarkBefore() {
            return this.isMarkBefore;
        }

        public void setMarkBefore(boolean isMarkBefore2) {
            this.isMarkBefore = isMarkBefore2;
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
            if (h < 4 || h > 11) {
                mk = "pm";
            } else {
                mk = "am";
            }
        }
        Time time2 = new Time(h, m, s, mk, tz, isBefore);
        this.time = time2;
    }

    public void setTime(Time t) {
        Time time2 = new Time(t.getClock(), t.getMinute(), t.getSecond(), t.getMark(), t.getTimezone(), t.isMarkBefore());
        this.time = time2;
    }

    public void setDay(int year, int month, int day) {
        int y = -1;
        int m = -1;
        int d = -1;
        if (year > -1) {
            if (year < 100) {
                year += 2000;
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
        int c;
        int t;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(defaultTime));
        int c2 = calendar.get(7);
        if (c2 == 1) {
            c = 6;
        } else {
            c = c2 - 2;
        }
        if (w - c >= 0) {
            t = w - c;
        } else {
            t = 7 + (w - c);
        }
        calendar.add(5, t);
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
