package com.huawei.i18n.tmr.datetime.utils;

import com.huawei.i18n.tmr.datetime.utils.DatePeriod;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
    private Day date;
    private Time time;

    public static class Day {
        int day = -1;
        int month = -1;
        int year = -1;

        public Day(int year2, int month2, int day2) {
            this.year = year2;
            this.month = month2;
            this.day = day2;
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
        int clock = -1;
        boolean isMarkBefore;
        String mark;
        int minute = -1;
        int second = 0;
        String timezone;

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
            return this.clock + ":" + this.minute;
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

    public void setDate(Date date2) {
        if (date2 != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date2);
            this.date = new Day(calendar.get(1), calendar.get(2), calendar.get(5));
        }
    }

    public void setDay(Day day) {
        this.date = day;
    }

    public void setDay(int year, int month, int day) {
        int tempYear = year;
        int finYear = -1;
        int finMonth = -1;
        int finDay = -1;
        if (tempYear > -1) {
            if (tempYear < 100) {
                tempYear += 2000;
            }
            finYear = tempYear;
        }
        if (month > -1) {
            finMonth = month;
        }
        if (day > -1) {
            finDay = day;
        }
        this.date = new Day(finYear, finMonth, finDay);
    }

    public Time getTime() {
        return this.time;
    }

    public void setTime(Time time2) {
        setTime(time2, false);
    }

    public void setTime(Time time2, boolean isChangeMark) {
        String mark = time2.getMark();
        if (isChangeMark && "mm".equals(mark)) {
            mark = (time2.getClock() < 4 || time2.getClock() > 11) ? "pm" : "am";
        }
        time2.setMark(mark);
        this.time = time2;
    }

    public void setDayByWeekValue(int week, long defaultTime) {
        int weekend;
        int amount;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(defaultTime));
        int weekend2 = calendar.get(7);
        if (weekend2 == 1) {
            weekend = 6;
        } else {
            weekend = weekend2 - 2;
        }
        if (week - weekend >= 0) {
            amount = week - weekend;
        } else {
            amount = 7 + (week - weekend);
        }
        calendar.add(5, amount);
        setDate(calendar.getTime());
    }

    public void setDayByAddDays(int amount, long defaultTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(defaultTime));
        calendar.add(5, amount);
        setDate(calendar.getTime());
    }

    public DatePeriod.DatePeriodType getType() {
        DatePeriod.DatePeriodType type = DatePeriod.DatePeriodType.TYPE_NULL;
        boolean isDateExit = true;
        boolean isTimeExit = true;
        Day day = this.date;
        if (day == null || day.isEmpty()) {
            isDateExit = false;
        }
        Time time2 = this.time;
        if (time2 == null || time2.isEmpty()) {
            isTimeExit = false;
        }
        if (isDateExit && isTimeExit) {
            return DatePeriod.DatePeriodType.TYPE_DATETIME;
        }
        if (isDateExit) {
            type = DatePeriod.DatePeriodType.TYPE_DATE;
        }
        if (isTimeExit) {
            return DatePeriod.DatePeriodType.TYPE_TIME;
        }
        return type;
    }

    public boolean isEmpty() {
        return this.date == null && this.time == null;
    }
}
