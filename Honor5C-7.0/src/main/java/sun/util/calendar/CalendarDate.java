package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;

public abstract class CalendarDate implements Cloneable {
    public static final int FIELD_UNDEFINED = Integer.MIN_VALUE;
    public static final long TIME_UNDEFINED = Long.MIN_VALUE;
    private int dayOfMonth;
    private int dayOfWeek;
    private int daylightSaving;
    private Era era;
    private boolean forceStandardTime;
    private long fraction;
    private int hours;
    private boolean leapYear;
    private Locale locale;
    private int millis;
    private int minutes;
    private int month;
    private boolean normalized;
    private int seconds;
    private int year;
    private int zoneOffset;
    private TimeZone zoneinfo;

    protected CalendarDate() {
        this(TimeZone.getDefault());
    }

    protected CalendarDate(TimeZone zone) {
        this.dayOfWeek = FIELD_UNDEFINED;
        this.zoneinfo = zone;
    }

    public Era getEra() {
        return this.era;
    }

    public CalendarDate setEra(Era era) {
        if (this.era == era) {
            return this;
        }
        this.era = era;
        this.normalized = false;
        return this;
    }

    public int getYear() {
        return this.year;
    }

    public CalendarDate setYear(int year) {
        if (this.year != year) {
            this.year = year;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addYear(int n) {
        if (n != 0) {
            this.year += n;
            this.normalized = false;
        }
        return this;
    }

    public boolean isLeapYear() {
        return this.leapYear;
    }

    void setLeapYear(boolean leapYear) {
        this.leapYear = leapYear;
    }

    public int getMonth() {
        return this.month;
    }

    public CalendarDate setMonth(int month) {
        if (this.month != month) {
            this.month = month;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addMonth(int n) {
        if (n != 0) {
            this.month += n;
            this.normalized = false;
        }
        return this;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    public CalendarDate setDayOfMonth(int date) {
        if (this.dayOfMonth != date) {
            this.dayOfMonth = date;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addDayOfMonth(int n) {
        if (n != 0) {
            this.dayOfMonth += n;
            this.normalized = false;
        }
        return this;
    }

    public int getDayOfWeek() {
        if (!isNormalized()) {
            this.dayOfWeek = FIELD_UNDEFINED;
        }
        return this.dayOfWeek;
    }

    public int getHours() {
        return this.hours;
    }

    public CalendarDate setHours(int hours) {
        if (this.hours != hours) {
            this.hours = hours;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addHours(int n) {
        if (n != 0) {
            this.hours += n;
            this.normalized = false;
        }
        return this;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public CalendarDate setMinutes(int minutes) {
        if (this.minutes != minutes) {
            this.minutes = minutes;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addMinutes(int n) {
        if (n != 0) {
            this.minutes += n;
            this.normalized = false;
        }
        return this;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public CalendarDate setSeconds(int seconds) {
        if (this.seconds != seconds) {
            this.seconds = seconds;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addSeconds(int n) {
        if (n != 0) {
            this.seconds += n;
            this.normalized = false;
        }
        return this;
    }

    public int getMillis() {
        return this.millis;
    }

    public CalendarDate setMillis(int millis) {
        if (this.millis != millis) {
            this.millis = millis;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addMillis(int n) {
        if (n != 0) {
            this.millis += n;
            this.normalized = false;
        }
        return this;
    }

    public long getTimeOfDay() {
        if (isNormalized()) {
            return this.fraction;
        }
        this.fraction = TIME_UNDEFINED;
        return TIME_UNDEFINED;
    }

    public CalendarDate setDate(int year, int month, int dayOfMonth) {
        setYear(year);
        setMonth(month);
        setDayOfMonth(dayOfMonth);
        return this;
    }

    public CalendarDate addDate(int year, int month, int dayOfMonth) {
        addYear(year);
        addMonth(month);
        addDayOfMonth(dayOfMonth);
        return this;
    }

    public CalendarDate setTimeOfDay(int hours, int minutes, int seconds, int millis) {
        setHours(hours);
        setMinutes(minutes);
        setSeconds(seconds);
        setMillis(millis);
        return this;
    }

    public CalendarDate addTimeOfDay(int hours, int minutes, int seconds, int millis) {
        addHours(hours);
        addMinutes(minutes);
        addSeconds(seconds);
        addMillis(millis);
        return this;
    }

    protected void setTimeOfDay(long fraction) {
        this.fraction = fraction;
    }

    public boolean isNormalized() {
        return this.normalized;
    }

    public boolean isStandardTime() {
        return this.forceStandardTime;
    }

    public void setStandardTime(boolean standardTime) {
        this.forceStandardTime = standardTime;
    }

    public boolean isDaylightTime() {
        boolean z = false;
        if (isStandardTime()) {
            return false;
        }
        if (this.daylightSaving != 0) {
            z = true;
        }
        return z;
    }

    protected void setLocale(Locale loc) {
        this.locale = loc;
    }

    public TimeZone getZone() {
        return this.zoneinfo;
    }

    public CalendarDate setZone(TimeZone zoneinfo) {
        this.zoneinfo = zoneinfo;
        return this;
    }

    public boolean isSameDate(CalendarDate date) {
        if (getDayOfWeek() == date.getDayOfWeek() && getMonth() == date.getMonth() && getYear() == date.getYear() && getEra() == date.getEra()) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (!(obj instanceof CalendarDate)) {
            return false;
        }
        CalendarDate that = (CalendarDate) obj;
        if (isNormalized() != that.isNormalized()) {
            return false;
        }
        boolean thatHasZone;
        boolean hasZone = this.zoneinfo != null;
        if (that.zoneinfo != null) {
            thatHasZone = true;
        } else {
            thatHasZone = false;
        }
        if (hasZone != thatHasZone) {
            return false;
        }
        if (hasZone && !this.zoneinfo.equals(that.zoneinfo)) {
            return false;
        }
        if (getEra() != that.getEra() || this.year != that.year || this.month != that.month || this.dayOfMonth != that.dayOfMonth || this.hours != that.hours || this.minutes != that.minutes || this.seconds != that.seconds || this.millis != that.millis) {
            z = false;
        } else if (this.zoneOffset != that.zoneOffset) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        long hash = (((((((((long) this.hours) + ((((((((long) this.year) - 1970) * 12) + ((long) (this.month - 1))) * 30) + ((long) this.dayOfMonth)) * 24)) * 60) + ((long) this.minutes)) * 60) + ((long) this.seconds)) * 1000) + ((long) this.millis)) - ((long) this.zoneOffset);
        int normalized = isNormalized() ? 1 : 0;
        int era = 0;
        Era e = getEra();
        if (e != null) {
            era = e.hashCode();
        }
        return (((((int) hash) * ((int) (hash >> 32))) ^ era) ^ normalized) ^ (this.zoneinfo != null ? this.zoneinfo.hashCode() : 0);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        CalendarUtils.sprintf0d(sb, this.year, 4).append('-');
        CalendarUtils.sprintf0d(sb, this.month, 2).append('-');
        CalendarUtils.sprintf0d(sb, this.dayOfMonth, 2).append('T');
        CalendarUtils.sprintf0d(sb, this.hours, 2).append(':');
        CalendarUtils.sprintf0d(sb, this.minutes, 2).append(':');
        CalendarUtils.sprintf0d(sb, this.seconds, 2).append('.');
        CalendarUtils.sprintf0d(sb, this.millis, 3);
        if (this.zoneOffset == 0) {
            sb.append('Z');
        } else if (this.zoneOffset != FIELD_UNDEFINED) {
            int offset;
            char sign;
            if (this.zoneOffset > 0) {
                offset = this.zoneOffset;
                sign = '+';
            } else {
                offset = -this.zoneOffset;
                sign = '-';
            }
            offset /= 60000;
            sb.append(sign);
            CalendarUtils.sprintf0d(sb, offset / 60, 2);
            CalendarUtils.sprintf0d(sb, offset % 60, 2);
        } else {
            sb.append(" local time");
        }
        return sb.toString();
    }

    protected void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    protected void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public int getZoneOffset() {
        return this.zoneOffset;
    }

    protected void setZoneOffset(int offset) {
        this.zoneOffset = offset;
    }

    public int getDaylightSaving() {
        return this.daylightSaving;
    }

    protected void setDaylightSaving(int daylightSaving) {
        this.daylightSaving = daylightSaving;
    }
}
