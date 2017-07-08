package android.icu.impl;

import java.util.Date;
import java.util.TimeZone;

public class TimeZoneAdapter extends TimeZone {
    static final long serialVersionUID = -2040072218820018557L;
    private android.icu.util.TimeZone zone;

    public static TimeZone wrap(android.icu.util.TimeZone tz) {
        return new TimeZoneAdapter(tz);
    }

    public android.icu.util.TimeZone unwrap() {
        return this.zone;
    }

    public TimeZoneAdapter(android.icu.util.TimeZone zone) {
        this.zone = zone;
        super.setID(zone.getID());
    }

    public void setID(String ID) {
        super.setID(ID);
        this.zone.setID(ID);
    }

    public boolean hasSameRules(TimeZone other) {
        if (other instanceof TimeZoneAdapter) {
            return this.zone.hasSameRules(((TimeZoneAdapter) other).zone);
        }
        return false;
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        return this.zone.getOffset(era, year, month, day, dayOfWeek, millis);
    }

    public int getRawOffset() {
        return this.zone.getRawOffset();
    }

    public void setRawOffset(int offsetMillis) {
        this.zone.setRawOffset(offsetMillis);
    }

    public boolean useDaylightTime() {
        return this.zone.useDaylightTime();
    }

    public boolean inDaylightTime(Date date) {
        return this.zone.inDaylightTime(date);
    }

    public Object clone() {
        return new TimeZoneAdapter((android.icu.util.TimeZone) this.zone.clone());
    }

    public synchronized int hashCode() {
        return this.zone.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof TimeZoneAdapter) {
            obj = ((TimeZoneAdapter) obj).zone;
        }
        return this.zone.equals(obj);
    }

    public String toString() {
        return "TimeZoneAdapter: " + this.zone.toString();
    }
}
