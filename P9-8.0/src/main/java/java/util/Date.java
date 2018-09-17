package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.time.Instant;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;

public class Date implements Serializable, Cloneable, Comparable<Date> {
    private static int defaultCenturyStart = 0;
    private static final BaseCalendar gcal = CalendarSystem.getGregorianCalendar();
    private static BaseCalendar jcal = null;
    private static final long serialVersionUID = 7523967970034938905L;
    private static final int[] ttb = new int[]{14, 1, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 10000, 10000, 10000, 10300, 10240, 10360, 10300, 10420, 10360, 10480, 10420};
    private static final String[] wtb = new String[]{"am", "pm", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december", "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"};
    private transient sun.util.calendar.BaseCalendar.Date cdate;
    private transient long fastTime;

    public Date() {
        this(System.currentTimeMillis());
    }

    public Date(long date) {
        this.fastTime = date;
    }

    @Deprecated
    public Date(int year, int month, int date) {
        this(year, month, date, 0, 0, 0);
    }

    @Deprecated
    public Date(int year, int month, int date, int hrs, int min) {
        this(year, month, date, hrs, min, 0);
    }

    @Deprecated
    public Date(int year, int month, int date, int hrs, int min, int sec) {
        int y = year + 1900;
        if (month >= 12) {
            y += month / 12;
            month %= 12;
        } else if (month < 0) {
            y += CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        this.cdate = (sun.util.calendar.BaseCalendar.Date) getCalendarSystem(y).newCalendarDate(TimeZone.getDefaultRef());
        this.cdate.setNormalizedDate(y, month + 1, date).setTimeOfDay(hrs, min, sec, 0);
        getTimeImpl();
        this.cdate = null;
    }

    @Deprecated
    public Date(String s) {
        this(parse(s));
    }

    public Object clone() {
        Date d = null;
        try {
            d = (Date) super.clone();
            if (this.cdate != null) {
                d.cdate = (sun.util.calendar.BaseCalendar.Date) this.cdate.clone();
            }
        } catch (CloneNotSupportedException e) {
        }
        return d;
    }

    @Deprecated
    public static long UTC(int year, int month, int date, int hrs, int min, int sec) {
        int y = year + 1900;
        if (month >= 12) {
            y += month / 12;
            month %= 12;
        } else if (month < 0) {
            y += CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        sun.util.calendar.BaseCalendar.Date udate = (sun.util.calendar.BaseCalendar.Date) getCalendarSystem(y).newCalendarDate(null);
        udate.setNormalizedDate(y, month + 1, date).setTimeOfDay(hrs, min, sec, 0);
        Date d = new Date(0);
        d.normalize(udate);
        return d.fastTime;
    }

    @Deprecated
    public static long parse(String s) {
        int year = Integer.MIN_VALUE;
        int mon = -1;
        int mday = -1;
        int hour = -1;
        int min = -1;
        int sec = -1;
        int i = 0;
        int tzoffset = -1;
        int prevc = 0;
        if (s != null) {
            int limit = s.length();
            while (i < limit) {
                int c = s.charAt(i);
                i++;
                if (c > 32 && c != 44) {
                    byte mon2;
                    if (c == 40) {
                        int depth = 1;
                        while (i < limit) {
                            c = s.charAt(i);
                            i++;
                            if (c == 40) {
                                depth++;
                            } else if (c == 41) {
                                depth--;
                                if (depth <= 0) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        }
                    } else if (48 > c || c > 57) {
                        if (c != 47 && c != 58 && c != 43 && c != 45) {
                            int st = i - 1;
                            while (i < limit) {
                                c = s.charAt(i);
                                if ((65 > c || c > 90) && (97 > c || c > 122)) {
                                    break;
                                }
                                i++;
                            }
                            if (i <= st + 1) {
                                break;
                            }
                            int k = wtb.length;
                            do {
                                k--;
                                if (k < 0) {
                                    break;
                                }
                            } while (!wtb[k].regionMatches(true, 0, s, st, i - st));
                            int action = ttb[k];
                            if (action != 0) {
                                if (action == 1) {
                                    if (hour > 12 || hour < 1) {
                                        break;
                                    } else if (hour < 12) {
                                        hour += 12;
                                    }
                                } else if (action == 14) {
                                    if (hour > 12 || hour < 1) {
                                        break;
                                    } else if (hour == 12) {
                                        hour = 0;
                                    }
                                } else if (action <= 13) {
                                    if (mon >= 0) {
                                        break;
                                    }
                                    mon2 = (byte) (action - 2);
                                } else {
                                    tzoffset = action - 10000;
                                }
                            }
                            if (k < 0) {
                                break;
                            }
                            prevc = 0;
                        } else {
                            prevc = c;
                        }
                    } else {
                        int n = c - 48;
                        while (i < limit) {
                            c = s.charAt(i);
                            if (48 > c || c > 57) {
                                break;
                            }
                            n = ((n * 10) + c) - 48;
                            i++;
                        }
                        if (prevc != 43 && (prevc != 45 || year == Integer.MIN_VALUE)) {
                            if (n < 70) {
                                byte min2;
                                if (c != 58) {
                                    byte mday2;
                                    if (c != 47) {
                                        if (i < limit && c != 44 && c > 32 && c != 45) {
                                            break;
                                        } else if (hour >= 0 && min < 0) {
                                            min2 = (byte) n;
                                        } else if (min < 0 || sec >= 0) {
                                            if (mday >= 0) {
                                                if (year != Integer.MIN_VALUE || mon < 0 || mday < 0) {
                                                    break;
                                                }
                                                year = n;
                                            } else {
                                                mday2 = (byte) n;
                                            }
                                        } else {
                                            byte sec2 = (byte) n;
                                        }
                                    } else if (mon >= 0) {
                                        if (mday >= 0) {
                                            break;
                                        }
                                        mday2 = (byte) n;
                                    } else {
                                        mon2 = (byte) (n - 1);
                                    }
                                } else if (hour >= 0) {
                                    if (min >= 0) {
                                        break;
                                    }
                                    min2 = (byte) n;
                                } else {
                                    hour = (byte) n;
                                }
                            } else if (year != Integer.MIN_VALUE || (c > 32 && c != 44 && c != 47 && i < limit)) {
                                break;
                            } else {
                                year = n;
                            }
                        } else if (tzoffset != 0 && tzoffset != -1) {
                            break;
                        } else {
                            if (n < 24) {
                                n *= 60;
                                int minutesPart = 0;
                                if (i < limit && s.charAt(i) == ':') {
                                    while (true) {
                                        i++;
                                        if (i >= limit) {
                                            break;
                                        }
                                        c = s.charAt(i);
                                        if (48 > c || c > 57) {
                                            break;
                                        }
                                        minutesPart = (minutesPart * 10) + (c - 48);
                                    }
                                }
                                n += minutesPart;
                            } else {
                                n = (n % 100) + ((n / 100) * 60);
                            }
                            if (prevc == 43) {
                                n = -n;
                            }
                            tzoffset = n;
                        }
                        prevc = 0;
                    }
                }
            }
            if (year != Integer.MIN_VALUE && mon >= 0 && mday >= 0) {
                if (year < 100) {
                    synchronized (Date.class) {
                        if (defaultCenturyStart == 0) {
                            defaultCenturyStart = gcal.getCalendarDate().getYear() - 80;
                        }
                    }
                    year += (defaultCenturyStart / 100) * 100;
                    if (year < defaultCenturyStart) {
                        year += 100;
                    }
                }
                if (sec < 0) {
                    sec = 0;
                }
                if (min < 0) {
                    min = 0;
                }
                if (hour < 0) {
                    hour = 0;
                }
                BaseCalendar cal = getCalendarSystem(year);
                if (tzoffset == -1) {
                    CalendarDate ldate = (sun.util.calendar.BaseCalendar.Date) cal.newCalendarDate(TimeZone.getDefaultRef());
                    ldate.setDate(year, mon + 1, mday);
                    ldate.setTimeOfDay(hour, min, sec, 0);
                    return cal.getTime(ldate);
                }
                CalendarDate udate = (sun.util.calendar.BaseCalendar.Date) cal.newCalendarDate(null);
                udate.setDate(year, mon + 1, mday);
                udate.setTimeOfDay(hour, min, sec, 0);
                return cal.getTime(udate) + ((long) (60000 * tzoffset));
            }
        }
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getYear() {
        return normalize().getYear() - 1900;
    }

    @Deprecated
    public void setYear(int year) {
        getCalendarDate().setNormalizedYear(year + 1900);
    }

    @Deprecated
    public int getMonth() {
        return normalize().getMonth() - 1;
    }

    @Deprecated
    public void setMonth(int month) {
        int y = 0;
        if (month >= 12) {
            y = month / 12;
            month %= 12;
        } else if (month < 0) {
            y = CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        sun.util.calendar.BaseCalendar.Date d = getCalendarDate();
        if (y != 0) {
            d.setNormalizedYear(d.getNormalizedYear() + y);
        }
        d.setMonth(month + 1);
    }

    @Deprecated
    public int getDate() {
        return normalize().getDayOfMonth();
    }

    @Deprecated
    public void setDate(int date) {
        getCalendarDate().setDayOfMonth(date);
    }

    @Deprecated
    public int getDay() {
        return normalize().getDayOfWeek() - 1;
    }

    @Deprecated
    public int getHours() {
        return normalize().getHours();
    }

    @Deprecated
    public void setHours(int hours) {
        getCalendarDate().setHours(hours);
    }

    @Deprecated
    public int getMinutes() {
        return normalize().getMinutes();
    }

    @Deprecated
    public void setMinutes(int minutes) {
        getCalendarDate().setMinutes(minutes);
    }

    @Deprecated
    public int getSeconds() {
        return normalize().getSeconds();
    }

    @Deprecated
    public void setSeconds(int seconds) {
        getCalendarDate().setSeconds(seconds);
    }

    public long getTime() {
        return getTimeImpl();
    }

    private final long getTimeImpl() {
        if (!(this.cdate == null || (this.cdate.isNormalized() ^ 1) == 0)) {
            normalize();
        }
        return this.fastTime;
    }

    public void setTime(long time) {
        this.fastTime = time;
        this.cdate = null;
    }

    public boolean before(Date when) {
        return getMillisOf(this) < getMillisOf(when);
    }

    public boolean after(Date when) {
        return getMillisOf(this) > getMillisOf(when);
    }

    public boolean equals(Object obj) {
        return (obj instanceof Date) && getTime() == ((Date) obj).getTime();
    }

    static final long getMillisOf(Date date) {
        if (date.cdate == null || date.cdate.isNormalized()) {
            return date.fastTime;
        }
        return gcal.getTime((sun.util.calendar.BaseCalendar.Date) date.cdate.clone());
    }

    public int compareTo(Date anotherDate) {
        long thisTime = getMillisOf(this);
        long anotherTime = getMillisOf(anotherDate);
        if (thisTime < anotherTime) {
            return -1;
        }
        return thisTime == anotherTime ? 0 : 1;
    }

    public int hashCode() {
        long ht = getTime();
        return ((int) ht) ^ ((int) (ht >> 32));
    }

    public String toString() {
        sun.util.calendar.BaseCalendar.Date date = normalize();
        StringBuilder sb = new StringBuilder(28);
        int index = date.getDayOfWeek();
        if (index == 1) {
            index = 8;
        }
        convertToAbbr(sb, wtb[index]).append(' ');
        convertToAbbr(sb, wtb[((date.getMonth() - 1) + 2) + 7]).append(' ');
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 2).append(' ');
        CalendarUtils.sprintf0d(sb, date.getHours(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getMinutes(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getSeconds(), 2).append(' ');
        TimeZone zi = date.getZone();
        if (zi != null) {
            sb.append(zi.getDisplayName(date.isDaylightTime(), 0, Locale.US));
        } else {
            sb.append("GMT");
        }
        sb.append(' ').append(date.getYear());
        return sb.toString();
    }

    private static final StringBuilder convertToAbbr(StringBuilder sb, String name) {
        sb.append(Character.toUpperCase(name.charAt(0)));
        sb.append(name.charAt(1)).append(name.charAt(2));
        return sb;
    }

    @Deprecated
    public String toLocaleString() {
        return DateFormat.getDateTimeInstance().format(this);
    }

    @Deprecated
    public String toGMTString() {
        sun.util.calendar.BaseCalendar.Date date = (sun.util.calendar.BaseCalendar.Date) getCalendarSystem(getTime()).getCalendarDate(getTime(), (TimeZone) null);
        StringBuilder sb = new StringBuilder(32);
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 1).append(' ');
        convertToAbbr(sb, wtb[((date.getMonth() - 1) + 2) + 7]).append(' ');
        sb.append(date.getYear()).append(' ');
        CalendarUtils.sprintf0d(sb, date.getHours(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getMinutes(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getSeconds(), 2);
        sb.append(" GMT");
        return sb.toString();
    }

    @Deprecated
    public int getTimezoneOffset() {
        int zoneOffset;
        if (this.cdate == null) {
            GregorianCalendar cal = new GregorianCalendar(this.fastTime);
            zoneOffset = cal.get(15) + cal.get(16);
        } else {
            normalize();
            zoneOffset = this.cdate.getZoneOffset();
        }
        return (-zoneOffset) / 60000;
    }

    private final sun.util.calendar.BaseCalendar.Date getCalendarDate() {
        if (this.cdate == null) {
            this.cdate = (sun.util.calendar.BaseCalendar.Date) getCalendarSystem(this.fastTime).getCalendarDate(this.fastTime, TimeZone.getDefaultRef());
        }
        return this.cdate;
    }

    private final sun.util.calendar.BaseCalendar.Date normalize() {
        if (this.cdate == null) {
            this.cdate = (sun.util.calendar.BaseCalendar.Date) getCalendarSystem(this.fastTime).getCalendarDate(this.fastTime, TimeZone.getDefaultRef());
            return this.cdate;
        }
        if (!this.cdate.isNormalized()) {
            this.cdate = normalize(this.cdate);
        }
        TimeZone tz = TimeZone.getDefaultRef();
        if (tz != this.cdate.getZone()) {
            this.cdate.setZone(tz);
            getCalendarSystem(this.cdate).getCalendarDate(this.fastTime, this.cdate);
        }
        return this.cdate;
    }

    private final sun.util.calendar.BaseCalendar.Date normalize(sun.util.calendar.BaseCalendar.Date date) {
        int y = date.getNormalizedYear();
        int m = date.getMonth();
        int d = date.getDayOfMonth();
        int hh = date.getHours();
        int mm = date.getMinutes();
        int ss = date.getSeconds();
        int ms = date.getMillis();
        TimeZone tz = date.getZone();
        if (y == 1582 || y > 280000000 || y < -280000000) {
            if (tz == null) {
                tz = TimeZone.getTimeZone("GMT");
            }
            GregorianCalendar gc = new GregorianCalendar(tz);
            gc.clear();
            gc.set(14, ms);
            gc.set(y, m - 1, d, hh, mm, ss);
            this.fastTime = gc.getTimeInMillis();
            return (sun.util.calendar.BaseCalendar.Date) getCalendarSystem(this.fastTime).getCalendarDate(this.fastTime, tz);
        }
        BaseCalendar cal = getCalendarSystem(y);
        if (cal != getCalendarSystem(date)) {
            date = (sun.util.calendar.BaseCalendar.Date) cal.newCalendarDate(tz);
            date.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
        }
        this.fastTime = cal.getTime(date);
        BaseCalendar ncal = getCalendarSystem(this.fastTime);
        if (ncal != cal) {
            date = (sun.util.calendar.BaseCalendar.Date) ncal.newCalendarDate(tz);
            date.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
            this.fastTime = ncal.getTime(date);
        }
        return date;
    }

    private static final BaseCalendar getCalendarSystem(int year) {
        if (year >= 1582) {
            return gcal;
        }
        return getJulianCalendar();
    }

    private static final BaseCalendar getCalendarSystem(long utc) {
        if (utc >= 0 || utc >= -12219292800000L - ((long) TimeZone.getDefaultRef().getOffset(utc))) {
            return gcal;
        }
        return getJulianCalendar();
    }

    private static final BaseCalendar getCalendarSystem(sun.util.calendar.BaseCalendar.Date cdate) {
        if (jcal == null) {
            return gcal;
        }
        if (cdate.getEra() != null) {
            return jcal;
        }
        return gcal;
    }

    private static final synchronized BaseCalendar getJulianCalendar() {
        BaseCalendar baseCalendar;
        synchronized (Date.class) {
            if (jcal == null) {
                jcal = (BaseCalendar) CalendarSystem.forName("julian");
            }
            baseCalendar = jcal;
        }
        return baseCalendar;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.writeLong(getTimeImpl());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        this.fastTime = s.readLong();
    }

    public static Date from(Instant instant) {
        try {
            return new Date(instant.toEpochMilli());
        } catch (Throwable ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public Instant toInstant() {
        return Instant.ofEpochMilli(getTime());
    }
}
