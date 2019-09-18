package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;
import sun.util.calendar.BaseCalendar;

public final class Era {
    private final String abbr;
    private int hash = 0;
    private final boolean localTime;
    private final String name;
    private final long since;
    private final CalendarDate sinceDate;

    public Era(String name2, String abbr2, long since2, boolean localTime2) {
        this.name = name2;
        this.abbr = abbr2;
        this.since = since2;
        this.localTime = localTime2;
        Gregorian gcal = CalendarSystem.getGregorianCalendar();
        BaseCalendar.Date d = gcal.newCalendarDate((TimeZone) null);
        gcal.getCalendarDate(since2, (CalendarDate) d);
        this.sinceDate = new ImmutableGregorianDate(d);
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName(Locale locale) {
        return this.name;
    }

    public String getAbbreviation() {
        return this.abbr;
    }

    public String getDiaplayAbbreviation(Locale locale) {
        return this.abbr;
    }

    public long getSince(TimeZone zone) {
        if (zone == null || !this.localTime) {
            return this.since;
        }
        return this.since - ((long) zone.getOffset(this.since));
    }

    public CalendarDate getSinceDate() {
        return this.sinceDate;
    }

    public boolean isLocalTime() {
        return this.localTime;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof Era)) {
            return false;
        }
        Era that = (Era) o;
        if (this.name.equals(that.name) && this.abbr.equals(that.abbr) && this.since == that.since && this.localTime == that.localTime) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        if (this.hash == 0) {
            this.hash = (((this.name.hashCode() ^ this.abbr.hashCode()) ^ ((int) this.since)) ^ ((int) (this.since >> 32))) ^ this.localTime ? 1 : 0;
        }
        return this.hash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(getName());
        sb.append(" (");
        sb.append(getAbbreviation());
        sb.append(')');
        sb.append(" since ");
        sb.append((Object) getSinceDate());
        if (this.localTime) {
            sb.setLength(sb.length() - 1);
            sb.append(" local time");
        }
        sb.append(']');
        return sb.toString();
    }
}
