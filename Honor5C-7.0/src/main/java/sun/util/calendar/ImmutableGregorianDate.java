package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;
import sun.util.calendar.BaseCalendar.Date;

class ImmutableGregorianDate extends Date {
    private final Date date;

    ImmutableGregorianDate(Date date) {
        if (date == null) {
            throw new NullPointerException();
        }
        this.date = date;
    }

    public Era getEra() {
        return this.date.getEra();
    }

    public CalendarDate setEra(Era era) {
        unsupported();
        return this;
    }

    public int getYear() {
        return this.date.getYear();
    }

    public CalendarDate setYear(int year) {
        unsupported();
        return this;
    }

    public CalendarDate addYear(int n) {
        unsupported();
        return this;
    }

    public boolean isLeapYear() {
        return this.date.isLeapYear();
    }

    void setLeapYear(boolean leapYear) {
        unsupported();
    }

    public int getMonth() {
        return this.date.getMonth();
    }

    public CalendarDate setMonth(int month) {
        unsupported();
        return this;
    }

    public CalendarDate addMonth(int n) {
        unsupported();
        return this;
    }

    public int getDayOfMonth() {
        return this.date.getDayOfMonth();
    }

    public CalendarDate setDayOfMonth(int date) {
        unsupported();
        return this;
    }

    public CalendarDate addDayOfMonth(int n) {
        unsupported();
        return this;
    }

    public int getDayOfWeek() {
        return this.date.getDayOfWeek();
    }

    public int getHours() {
        return this.date.getHours();
    }

    public CalendarDate setHours(int hours) {
        unsupported();
        return this;
    }

    public CalendarDate addHours(int n) {
        unsupported();
        return this;
    }

    public int getMinutes() {
        return this.date.getMinutes();
    }

    public CalendarDate setMinutes(int minutes) {
        unsupported();
        return this;
    }

    public CalendarDate addMinutes(int n) {
        unsupported();
        return this;
    }

    public int getSeconds() {
        return this.date.getSeconds();
    }

    public CalendarDate setSeconds(int seconds) {
        unsupported();
        return this;
    }

    public CalendarDate addSeconds(int n) {
        unsupported();
        return this;
    }

    public int getMillis() {
        return this.date.getMillis();
    }

    public CalendarDate setMillis(int millis) {
        unsupported();
        return this;
    }

    public CalendarDate addMillis(int n) {
        unsupported();
        return this;
    }

    public long getTimeOfDay() {
        return this.date.getTimeOfDay();
    }

    public CalendarDate setDate(int year, int month, int dayOfMonth) {
        unsupported();
        return this;
    }

    public CalendarDate addDate(int year, int month, int dayOfMonth) {
        unsupported();
        return this;
    }

    public CalendarDate setTimeOfDay(int hours, int minutes, int seconds, int millis) {
        unsupported();
        return this;
    }

    public CalendarDate addTimeOfDay(int hours, int minutes, int seconds, int millis) {
        unsupported();
        return this;
    }

    protected void setTimeOfDay(long fraction) {
        unsupported();
    }

    public boolean isNormalized() {
        return this.date.isNormalized();
    }

    public boolean isStandardTime() {
        return this.date.isStandardTime();
    }

    public void setStandardTime(boolean standardTime) {
        unsupported();
    }

    public boolean isDaylightTime() {
        return this.date.isDaylightTime();
    }

    protected void setLocale(Locale loc) {
        unsupported();
    }

    public TimeZone getZone() {
        return this.date.getZone();
    }

    public CalendarDate setZone(TimeZone zoneinfo) {
        unsupported();
        return this;
    }

    public boolean isSameDate(CalendarDate date) {
        return date.isSameDate(date);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ImmutableGregorianDate) {
            return this.date.equals(((ImmutableGregorianDate) obj).date);
        }
        return false;
    }

    public int hashCode() {
        return this.date.hashCode();
    }

    public Object clone() {
        return super.clone();
    }

    public String toString() {
        return this.date.toString();
    }

    protected void setDayOfWeek(int dayOfWeek) {
        unsupported();
    }

    protected void setNormalized(boolean normalized) {
        unsupported();
    }

    public int getZoneOffset() {
        return this.date.getZoneOffset();
    }

    protected void setZoneOffset(int offset) {
        unsupported();
    }

    public int getDaylightSaving() {
        return this.date.getDaylightSaving();
    }

    protected void setDaylightSaving(int daylightSaving) {
        unsupported();
    }

    public int getNormalizedYear() {
        return this.date.getNormalizedYear();
    }

    public void setNormalizedYear(int normalizedYear) {
        unsupported();
    }

    private void unsupported() {
        throw new UnsupportedOperationException();
    }
}
