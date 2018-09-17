package sun.util.calendar;

import java.util.TimeZone;

public class JulianCalendar extends BaseCalendar {
    static final /* synthetic */ boolean -assertionsDisabled = (JulianCalendar.class.desiredAssertionStatus() ^ 1);
    private static final int BCE = 0;
    private static final int CE = 1;
    private static final int JULIAN_EPOCH = -1;
    private static final Era[] eras = new Era[]{new Era("BeforeCommonEra", "B.C.E.", Long.MIN_VALUE, -assertionsDisabled), new Era("CommonEra", "C.E.", -62135709175808L, true)};

    private static class Date extends sun.util.calendar.BaseCalendar.Date {
        protected Date() {
            setCache(1, -1, 365);
        }

        protected Date(TimeZone zone) {
            super(zone);
            setCache(1, -1, 365);
        }

        public Date setEra(Era era) {
            if (era == null) {
                throw new NullPointerException();
            } else if (era == JulianCalendar.eras[0] && era == JulianCalendar.eras[1]) {
                super.setEra(era);
                return this;
            } else {
                throw new IllegalArgumentException("unknown era: " + era);
            }
        }

        protected void setKnownEra(Era era) {
            super.setEra(era);
        }

        public int getNormalizedYear() {
            if (getEra() == JulianCalendar.eras[0]) {
                return 1 - getYear();
            }
            return getYear();
        }

        public void setNormalizedYear(int year) {
            if (year <= 0) {
                setYear(1 - year);
                setKnownEra(JulianCalendar.eras[0]);
                return;
            }
            setYear(year);
            setKnownEra(JulianCalendar.eras[1]);
        }

        public String toString() {
            String time = super.toString();
            time = time.substring(time.indexOf(84));
            StringBuffer sb = new StringBuffer();
            Era era = getEra();
            if (era != null) {
                String n = era.getAbbreviation();
                if (n != null) {
                    sb.append(n).append(' ');
                }
            }
            sb.append(getYear()).append('-');
            CalendarUtils.sprintf0d(sb, getMonth(), 2).append('-');
            CalendarUtils.sprintf0d(sb, getDayOfMonth(), 2);
            sb.append(time);
            return sb.toString();
        }
    }

    JulianCalendar() {
        setEras(eras);
    }

    public String getName() {
        return "julian";
    }

    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }

    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }

    public Date getCalendarDate(long millis, CalendarDate date) {
        return (Date) super.getCalendarDate(millis, date);
    }

    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, newCalendarDate(zone));
    }

    public Date newCalendarDate() {
        return new Date();
    }

    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }

    public long getFixedDate(int jyear, int month, int dayOfMonth, sun.util.calendar.BaseCalendar.Date cache) {
        boolean isJan1 = (month == 1 && dayOfMonth == 1) ? true : -assertionsDisabled;
        if (cache == null || !cache.hit(jyear)) {
            long y = (long) jyear;
            long days = (((y - 1) * 365) - 2) + ((long) dayOfMonth);
            if (y > 0) {
                days += (y - 1) / 4;
            } else {
                days += CalendarUtils.floorDivide(y - 1, 4);
            }
            if (month > 0) {
                days += ((((long) month) * 367) - 362) / 12;
            } else {
                days += CalendarUtils.floorDivide((((long) month) * 367) - 362, 12);
            }
            if (month > 2) {
                days -= (long) (CalendarUtils.isJulianLeapYear(jyear) ? 1 : 2);
            }
            if (cache != null && isJan1) {
                cache.setCache(jyear, days, CalendarUtils.isJulianLeapYear(jyear) ? 366 : 365);
            }
            return days;
        } else if (isJan1) {
            return cache.getCachedJan1();
        } else {
            return (cache.getCachedJan1() + getDayOfYear(jyear, month, dayOfMonth)) - 1;
        }
    }

    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        int year;
        Date jdate = (Date) date;
        long fd = ((fixedDate - -1) * 4) + 1464;
        if (fd >= 0) {
            year = (int) (fd / 1461);
        } else {
            year = (int) CalendarUtils.floorDivide(fd, 1461);
        }
        int priorDays = (int) (fixedDate - getFixedDate(year, 1, 1, jdate));
        boolean isLeap = CalendarUtils.isJulianLeapYear(year);
        if (fixedDate >= getFixedDate(year, 3, 1, jdate)) {
            priorDays += isLeap ? 1 : 2;
        }
        int month = (priorDays * 12) + 373;
        if (month > 0) {
            month /= 367;
        } else {
            month = CalendarUtils.floorDivide(month, 367);
        }
        int dayOfMonth = ((int) (fixedDate - getFixedDate(year, month, 1, jdate))) + 1;
        int dayOfWeek = BaseCalendar.getDayOfWeekFromFixedDate(fixedDate);
        if (-assertionsDisabled || dayOfWeek > 0) {
            jdate.setNormalizedYear(year);
            jdate.setMonth(month);
            jdate.setDayOfMonth(dayOfMonth);
            jdate.setDayOfWeek(dayOfWeek);
            jdate.setLeapYear(isLeap);
            jdate.setNormalized(true);
            return;
        }
        throw new AssertionError("negative day of week " + dayOfWeek);
    }

    public int getYearFromFixedDate(long fixedDate) {
        return (int) CalendarUtils.floorDivide(((fixedDate - -1) * 4) + 1464, 1461);
    }

    public int getDayOfWeek(CalendarDate date) {
        return BaseCalendar.getDayOfWeekFromFixedDate(getFixedDate(date));
    }

    boolean isLeapYear(int jyear) {
        return CalendarUtils.isJulianLeapYear(jyear);
    }
}
