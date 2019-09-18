package sun.util.calendar;

import java.util.TimeZone;

public abstract class AbstractCalendar extends CalendarSystem {
    static final int DAY_IN_MILLIS = 86400000;
    static final int EPOCH_OFFSET = 719163;
    static final int HOUR_IN_MILLIS = 3600000;
    static final int MINUTE_IN_MILLIS = 60000;
    static final int SECOND_IN_MILLIS = 1000;
    private Era[] eras;

    /* access modifiers changed from: protected */
    public abstract void getCalendarDateFromFixedDate(CalendarDate calendarDate, long j);

    /* access modifiers changed from: protected */
    public abstract long getFixedDate(CalendarDate calendarDate);

    /* access modifiers changed from: protected */
    public abstract boolean isLeapYear(CalendarDate calendarDate);

    protected AbstractCalendar() {
    }

    public Era getEra(String eraName) {
        if (this.eras != null) {
            for (int i = 0; i < this.eras.length; i++) {
                if (this.eras[i].equals(eraName)) {
                    return this.eras[i];
                }
            }
        }
        return null;
    }

    public Era[] getEras() {
        if (this.eras == null) {
            return null;
        }
        Era[] e = new Era[this.eras.length];
        System.arraycopy((Object) this.eras, 0, (Object) e, 0, this.eras.length);
        return e;
    }

    public void setEra(CalendarDate date, String eraName) {
        if (this.eras != null) {
            int i = 0;
            while (i < this.eras.length) {
                Era e = this.eras[i];
                if (e == null || !e.getName().equals(eraName)) {
                    i++;
                } else {
                    date.setEra(e);
                    return;
                }
            }
            throw new IllegalArgumentException("unknown era name: " + eraName);
        }
    }

    /* access modifiers changed from: protected */
    public void setEras(Era[] eras2) {
        this.eras = eras2;
    }

    public CalendarDate getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }

    public CalendarDate getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }

    public CalendarDate getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, newCalendarDate(zone));
    }

    public CalendarDate getCalendarDate(long millis, CalendarDate date) {
        int ms = 0;
        int zoneOffset = 0;
        int saving = 0;
        long days = 0;
        TimeZone zi = date.getZone();
        if (zi != null) {
            int[] offsets = new int[2];
            zoneOffset = zi.getOffset(millis);
            offsets[0] = zi.getRawOffset();
            offsets[1] = zoneOffset - offsets[0];
            days = (long) (zoneOffset / DAY_IN_MILLIS);
            ms = zoneOffset % DAY_IN_MILLIS;
            saving = offsets[1];
        }
        date.setZoneOffset(zoneOffset);
        date.setDaylightSaving(saving);
        long days2 = days + (millis / 86400000);
        int ms2 = ms + ((int) (millis % 86400000));
        if (ms2 >= DAY_IN_MILLIS) {
            ms2 -= DAY_IN_MILLIS;
            days2++;
        } else {
            while (ms2 < 0) {
                ms2 += DAY_IN_MILLIS;
                days2--;
            }
        }
        getCalendarDateFromFixedDate(date, days2 + 719163);
        setTimeOfDay(date, ms2);
        date.setLeapYear(isLeapYear(date));
        date.setNormalized(true);
        return date;
    }

    public long getTime(CalendarDate date) {
        long ms = ((getFixedDate(date) - 719163) * 86400000) + getTimeOfDay(date);
        int zoneOffset = 0;
        TimeZone zi = date.getZone();
        if (zi != null) {
            if (date.isNormalized()) {
                return ms - ((long) date.getZoneOffset());
            }
            int[] iArr = new int[2];
            zoneOffset = date.isStandardTime() ? zi.getOffset(ms - ((long) zi.getRawOffset())) : zi.getOffset(ms - ((long) zi.getRawOffset()));
        }
        long ms2 = ms - ((long) zoneOffset);
        getCalendarDate(ms2, date);
        return ms2;
    }

    /* access modifiers changed from: protected */
    public long getTimeOfDay(CalendarDate date) {
        long fraction = date.getTimeOfDay();
        if (fraction != Long.MIN_VALUE) {
            return fraction;
        }
        long fraction2 = getTimeOfDayValue(date);
        date.setTimeOfDay(fraction2);
        return fraction2;
    }

    public long getTimeOfDayValue(CalendarDate date) {
        return (((((((long) date.getHours()) * 60) + ((long) date.getMinutes())) * 60) + ((long) date.getSeconds())) * 1000) + ((long) date.getMillis());
    }

    public CalendarDate setTimeOfDay(CalendarDate cdate, int fraction) {
        if (fraction >= 0) {
            boolean normalizedState = cdate.isNormalized();
            int time = fraction;
            int hours = time / HOUR_IN_MILLIS;
            int time2 = time % HOUR_IN_MILLIS;
            int minutes = time2 / MINUTE_IN_MILLIS;
            int time3 = time2 % MINUTE_IN_MILLIS;
            cdate.setHours(hours);
            cdate.setMinutes(minutes);
            cdate.setSeconds(time3 / 1000);
            cdate.setMillis(time3 % 1000);
            cdate.setTimeOfDay((long) fraction);
            if (hours < 24 && normalizedState) {
                cdate.setNormalized(normalizedState);
            }
            return cdate;
        }
        throw new IllegalArgumentException();
    }

    public int getWeekLength() {
        return 7;
    }

    public CalendarDate getNthDayOfWeek(int nth, int dayOfWeek, CalendarDate date) {
        long nfd;
        CalendarDate ndate = (CalendarDate) date.clone();
        normalize(ndate);
        long fd = getFixedDate(ndate);
        if (nth > 0) {
            nfd = ((long) (7 * nth)) + getDayOfWeekDateBefore(fd, dayOfWeek);
        } else {
            nfd = ((long) (7 * nth)) + getDayOfWeekDateAfter(fd, dayOfWeek);
        }
        getCalendarDateFromFixedDate(ndate, nfd);
        return ndate;
    }

    static long getDayOfWeekDateBefore(long fixedDate, int dayOfWeek) {
        return getDayOfWeekDateOnOrBefore(fixedDate - 1, dayOfWeek);
    }

    static long getDayOfWeekDateAfter(long fixedDate, int dayOfWeek) {
        return getDayOfWeekDateOnOrBefore(7 + fixedDate, dayOfWeek);
    }

    public static long getDayOfWeekDateOnOrBefore(long fixedDate, int dayOfWeek) {
        long fd = fixedDate - ((long) (dayOfWeek - 1));
        if (fd >= 0) {
            return fixedDate - (fd % 7);
        }
        return fixedDate - CalendarUtils.mod(fd, 7);
    }

    public boolean validateTime(CalendarDate date) {
        int t = date.getHours();
        if (t < 0 || t >= 24) {
            return false;
        }
        int t2 = date.getMinutes();
        if (t2 < 0 || t2 >= 60) {
            return false;
        }
        int t3 = date.getSeconds();
        if (t3 < 0 || t3 >= 60) {
            return false;
        }
        int t4 = date.getMillis();
        if (t4 < 0 || t4 >= 1000) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int normalizeTime(CalendarDate date) {
        long fraction = getTimeOfDay(date);
        long days = 0;
        if (fraction >= 86400000) {
            days = fraction / 86400000;
            fraction %= 86400000;
        } else if (fraction < 0) {
            days = CalendarUtils.floorDivide(fraction, 86400000);
            if (days != 0) {
                fraction -= 86400000 * days;
            }
        }
        if (days != 0) {
            date.setTimeOfDay(fraction);
        }
        date.setMillis((int) (fraction % 1000));
        long fraction2 = fraction / 1000;
        date.setSeconds((int) (fraction2 % 60));
        long fraction3 = fraction2 / 60;
        date.setMinutes((int) (fraction3 % 60));
        date.setHours((int) (fraction3 / 60));
        return (int) days;
    }
}
