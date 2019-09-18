package sun.util.calendar;

import java.util.TimeZone;
import sun.util.calendar.BaseCalendar;

public class JulianCalendar extends BaseCalendar {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int BCE = 0;
    private static final int CE = 1;
    private static final int JULIAN_EPOCH = -1;
    /* access modifiers changed from: private */
    public static final Era[] eras;

    private static class Date extends BaseCalendar.Date {
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

        /* access modifiers changed from: protected */
        public void setKnownEra(Era era) {
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
            String time2 = time.substring(time.indexOf(84));
            StringBuffer sb = new StringBuffer();
            Era era = getEra();
            if (era != null) {
                String n = era.getAbbreviation();
                if (n != null) {
                    sb.append(n);
                    sb.append(' ');
                }
            }
            sb.append(getYear());
            sb.append('-');
            CalendarUtils.sprintf0d(sb, getMonth(), 2).append('-');
            CalendarUtils.sprintf0d(sb, getDayOfMonth(), 2);
            sb.append(time2);
            return sb.toString();
        }
    }

    static {
        Era era = new Era("BeforeCommonEra", "B.C.E.", Long.MIN_VALUE, $assertionsDisabled);
        Era era2 = new Era("CommonEra", "C.E.", -62135709175808L, true);
        eras = new Era[]{era, era2};
    }

    JulianCalendar() {
        setEras(eras);
    }

    public String getName() {
        return "julian";
    }

    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), (CalendarDate) newCalendarDate());
    }

    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, (CalendarDate) newCalendarDate());
    }

    public Date getCalendarDate(long millis, CalendarDate date) {
        return (Date) super.getCalendarDate(millis, date);
    }

    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, (CalendarDate) newCalendarDate(zone));
    }

    public Date newCalendarDate() {
        return new Date();
    }

    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }

    public long getFixedDate(int jyear, int month, int dayOfMonth, BaseCalendar.Date cache) {
        long days;
        long days2;
        int i = jyear;
        int i2 = month;
        int i3 = dayOfMonth;
        BaseCalendar.Date date = cache;
        boolean isJan1 = true;
        if (!(i2 == 1 && i3 == 1)) {
            isJan1 = $assertionsDisabled;
        }
        if (date == null || !date.hit(i)) {
            long y = (long) i;
            long days3 = -2 + (365 * (y - 1)) + ((long) i3);
            if (y > 0) {
                days = days3 + ((y - 1) / 4);
            } else {
                days = days3 + CalendarUtils.floorDivide(y - 1, 4);
            }
            if (i2 > 0) {
                days2 = days + (((367 * ((long) i2)) - 362) / 12);
            } else {
                days2 = days + CalendarUtils.floorDivide((367 * ((long) i2)) - 362, 12);
            }
            if (i2 > 2) {
                days2 -= CalendarUtils.isJulianLeapYear(jyear) ? 1 : 2;
            }
            if (date != null && isJan1) {
                date.setCache(i, days2, CalendarUtils.isJulianLeapYear(jyear) ? 366 : 365);
            }
            return days2;
        } else if (isJan1) {
            return cache.getCachedJan1();
        } else {
            return (cache.getCachedJan1() + getDayOfYear(jyear, month, dayOfMonth)) - 1;
        }
    }

    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        int year;
        int month;
        Date jdate = (Date) date;
        long fd = (4 * (fixedDate - -1)) + 1464;
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
        int month2 = (12 * priorDays) + 373;
        if (month2 > 0) {
            month = month2 / 367;
        } else {
            month = CalendarUtils.floorDivide(month2, 367);
        }
        int dayOfWeek = getDayOfWeekFromFixedDate(fixedDate);
        jdate.setNormalizedYear(year);
        jdate.setMonth(month);
        jdate.setDayOfMonth(((int) (fixedDate - getFixedDate(year, month, 1, jdate))) + 1);
        jdate.setDayOfWeek(dayOfWeek);
        jdate.setLeapYear(isLeap);
        jdate.setNormalized(true);
    }

    public int getYearFromFixedDate(long fixedDate) {
        return (int) CalendarUtils.floorDivide((4 * (fixedDate - -1)) + 1464, 1461);
    }

    public int getDayOfWeek(CalendarDate date) {
        return getDayOfWeekFromFixedDate(getFixedDate(date));
    }

    /* access modifiers changed from: package-private */
    public boolean isLeapYear(int jyear) {
        return CalendarUtils.isJulianLeapYear(jyear);
    }
}
