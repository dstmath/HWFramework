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
        this.dayOfWeek = Integer.MIN_VALUE;
        this.zoneinfo = zone;
    }

    public Era getEra() {
        return this.era;
    }

    public CalendarDate setEra(Era era2) {
        if (this.era == era2) {
            return this;
        }
        this.era = era2;
        this.normalized = false;
        return this;
    }

    public int getYear() {
        return this.year;
    }

    public CalendarDate setYear(int year2) {
        if (this.year != year2) {
            this.year = year2;
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

    /* access modifiers changed from: package-private */
    public void setLeapYear(boolean leapYear2) {
        this.leapYear = leapYear2;
    }

    public int getMonth() {
        return this.month;
    }

    public CalendarDate setMonth(int month2) {
        if (this.month != month2) {
            this.month = month2;
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
            this.dayOfWeek = Integer.MIN_VALUE;
        }
        return this.dayOfWeek;
    }

    public int getHours() {
        return this.hours;
    }

    public CalendarDate setHours(int hours2) {
        if (this.hours != hours2) {
            this.hours = hours2;
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

    public CalendarDate setMinutes(int minutes2) {
        if (this.minutes != minutes2) {
            this.minutes = minutes2;
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

    public CalendarDate setSeconds(int seconds2) {
        if (this.seconds != seconds2) {
            this.seconds = seconds2;
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

    public CalendarDate setMillis(int millis2) {
        if (this.millis != millis2) {
            this.millis = millis2;
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
        this.fraction = Long.MIN_VALUE;
        return Long.MIN_VALUE;
    }

    public CalendarDate setDate(int year2, int month2, int dayOfMonth2) {
        setYear(year2);
        setMonth(month2);
        setDayOfMonth(dayOfMonth2);
        return this;
    }

    public CalendarDate addDate(int year2, int month2, int dayOfMonth2) {
        addYear(year2);
        addMonth(month2);
        addDayOfMonth(dayOfMonth2);
        return this;
    }

    public CalendarDate setTimeOfDay(int hours2, int minutes2, int seconds2, int millis2) {
        setHours(hours2);
        setMinutes(minutes2);
        setSeconds(seconds2);
        setMillis(millis2);
        return this;
    }

    public CalendarDate addTimeOfDay(int hours2, int minutes2, int seconds2, int millis2) {
        addHours(hours2);
        addMinutes(minutes2);
        addSeconds(seconds2);
        addMillis(millis2);
        return this;
    }

    /* access modifiers changed from: protected */
    public void setTimeOfDay(long fraction2) {
        this.fraction = fraction2;
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

    /* access modifiers changed from: protected */
    public void setLocale(Locale loc) {
        this.locale = loc;
    }

    public TimeZone getZone() {
        return this.zoneinfo;
    }

    public CalendarDate setZone(TimeZone zoneinfo2) {
        this.zoneinfo = zoneinfo2;
        return this;
    }

    public boolean isSameDate(CalendarDate date) {
        return getDayOfWeek() == date.getDayOfWeek() && getMonth() == date.getMonth() && getYear() == date.getYear() && getEra() == date.getEra();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof CalendarDate)) {
            return false;
        }
        CalendarDate that = (CalendarDate) obj;
        if (isNormalized() != that.isNormalized()) {
            return false;
        }
        boolean hasZone = this.zoneinfo != null;
        if (hasZone != (that.zoneinfo != null)) {
            return false;
        }
        if (hasZone && !this.zoneinfo.equals(that.zoneinfo)) {
            return false;
        }
        if (getEra() == that.getEra() && this.year == that.year && this.month == that.month && this.dayOfMonth == that.dayOfMonth && this.hours == that.hours && this.minutes == that.minutes && this.seconds == that.seconds && this.millis == that.millis && this.zoneOffset == that.zoneOffset) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        long hash = (((((((((long) this.hours) + ((((((((long) this.year) - 1970) * 12) + ((long) (this.month - 1))) * 30) + ((long) this.dayOfMonth)) * 24)) * 60) + ((long) this.minutes)) * 60) + ((long) this.seconds)) * 1000) + ((long) this.millis)) - ((long) this.zoneOffset);
        int normalized2 = isNormalized();
        int era2 = 0;
        Era e = getEra();
        if (e != null) {
            era2 = e.hashCode();
        }
        return (int) ((((((int) hash) * ((int) (hash >> 32))) ^ era2) ^ normalized2) ^ (this.zoneinfo != null ? this.zoneinfo.hashCode() : 0));
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    public String toString() {
        int offset;
        StringBuilder sb = new StringBuilder();
        char sign = '-';
        CalendarUtils.sprintf0d(sb, this.year, 4).append('-');
        CalendarUtils.sprintf0d(sb, this.month, 2).append('-');
        CalendarUtils.sprintf0d(sb, this.dayOfMonth, 2).append('T');
        CalendarUtils.sprintf0d(sb, this.hours, 2).append(':');
        CalendarUtils.sprintf0d(sb, this.minutes, 2).append(':');
        CalendarUtils.sprintf0d(sb, this.seconds, 2).append('.');
        CalendarUtils.sprintf0d(sb, this.millis, 3);
        if (this.zoneOffset == 0) {
            sb.append('Z');
        } else if (this.zoneOffset != Integer.MIN_VALUE) {
            if (this.zoneOffset > 0) {
                offset = this.zoneOffset;
                sign = '+';
            } else {
                offset = -this.zoneOffset;
            }
            int offset2 = offset / 60000;
            sb.append(sign);
            CalendarUtils.sprintf0d(sb, offset2 / 60, 2);
            CalendarUtils.sprintf0d(sb, offset2 % 60, 2);
        } else {
            sb.append(" local time");
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public void setDayOfWeek(int dayOfWeek2) {
        this.dayOfWeek = dayOfWeek2;
    }

    /* access modifiers changed from: protected */
    public void setNormalized(boolean normalized2) {
        this.normalized = normalized2;
    }

    public int getZoneOffset() {
        return this.zoneOffset;
    }

    /* access modifiers changed from: protected */
    public void setZoneOffset(int offset) {
        this.zoneOffset = offset;
    }

    public int getDaylightSaving() {
        return this.daylightSaving;
    }

    /* access modifiers changed from: protected */
    public void setDaylightSaving(int daylightSaving2) {
        this.daylightSaving = daylightSaving2;
    }
}
