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
    private static final int[] ttb = {14, 1, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 10000, 10000, 10000, 10300, 10240, 10360, 10300, 10420, 10360, 10480, 10420};
    private static final String[] wtb = {"am", "pm", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december", "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"};
    private transient BaseCalendar.Date cdate;
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
        this.cdate = (BaseCalendar.Date) getCalendarSystem(y).newCalendarDate(TimeZone.getDefaultRef());
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
                d.cdate = (BaseCalendar.Date) this.cdate.clone();
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
        BaseCalendar.Date udate = (BaseCalendar.Date) getCalendarSystem(y).newCalendarDate(null);
        udate.setNormalizedDate(y, month + 1, date).setTimeOfDay(hrs, min, sec, 0);
        Date d = new Date(0);
        d.normalize(udate);
        return d.fastTime;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:160:0x01ea, code lost:
        r3 = r7;
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x01ec, code lost:
        r6 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x007b, code lost:
        if (r10 != Integer.MIN_VALUE) goto L_0x00ea;
     */
    @Deprecated
    public static long parse(String s) {
        int hour;
        int millis;
        int prevc;
        int millis2;
        int hour2;
        int millis3;
        int millis4;
        int hour3;
        int millis5;
        int hour4;
        int tzoffset;
        int k;
        int n;
        int year;
        int mday;
        byte b;
        String str = s;
        int hour5 = -1;
        int i = 65535;
        int i2 = 0;
        if (str != null) {
            int limit = s.length();
            byte b2 = -1;
            byte b3 = -1;
            int tzoffset2 = -1;
            int prevc2 = 0;
            int year2 = Integer.MIN_VALUE;
            int mon = -1;
            int mday2 = -1;
            while (true) {
                if (i2 < limit) {
                    int c = str.charAt(i2);
                    i2++;
                    if (c > 32) {
                        if (c != 44) {
                            if (c != 40) {
                                if (48 <= c && c <= 57) {
                                    int n2 = c - 48;
                                    while (i2 < limit) {
                                        int charAt = str.charAt(i2);
                                        c = charAt;
                                        if (48 > charAt || c > 57) {
                                            break;
                                        }
                                        n2 = ((n2 * 10) + c) - 48;
                                        i2++;
                                    }
                                    if (prevc2 != 43) {
                                        int i3 = prevc2 == 45 ? Integer.MIN_VALUE : Integer.MIN_VALUE;
                                        if (n2 >= 70) {
                                            if (year2 != i3 || (c > 32 && c != 44 && c != 47 && i2 < limit)) {
                                                break;
                                            }
                                            year = n2;
                                        } else {
                                            if (c == 58) {
                                                if (hour5 >= 0) {
                                                    if (b2 >= 0) {
                                                        break;
                                                    }
                                                    b = (byte) n2;
                                                } else {
                                                    hour5 = (byte) n2;
                                                    prevc2 = 0;
                                                }
                                            } else {
                                                if (c != 47) {
                                                    if (i2 < limit && c != 44 && c > 32 && c != 45) {
                                                        break;
                                                    } else if (hour5 >= 0 && b2 < 0) {
                                                        b = (byte) n2;
                                                    } else if (b2 < 0 || b3 >= 0) {
                                                        if (mday2 >= 0) {
                                                            if (year2 != Integer.MIN_VALUE || mon < 0 || mday2 < 0) {
                                                                break;
                                                            }
                                                            year = n2;
                                                        } else {
                                                            mday = (byte) n2;
                                                        }
                                                    } else {
                                                        b3 = (byte) n2;
                                                        prevc2 = 0;
                                                    }
                                                } else if (mon >= 0) {
                                                    if (mday2 >= 0) {
                                                        break;
                                                    }
                                                    mday = (byte) n2;
                                                } else {
                                                    mon = (byte) (n2 - 1);
                                                    prevc2 = 0;
                                                }
                                                mday2 = mday;
                                                prevc2 = 0;
                                            }
                                            b2 = b;
                                            prevc2 = 0;
                                        }
                                        year2 = year;
                                        prevc2 = 0;
                                    }
                                    if (tzoffset2 != 0 && tzoffset2 != -1) {
                                        break;
                                    }
                                    if (n2 < 24) {
                                        int n3 = n2 * 60;
                                        int minutesPart = 0;
                                        if (i2 < limit && str.charAt(i2) == ':') {
                                            i2++;
                                            while (i2 < limit) {
                                                int charAt2 = str.charAt(i2);
                                                int c2 = charAt2;
                                                if (48 > charAt2 || c2 > 57) {
                                                    break;
                                                }
                                                minutesPart = (minutesPart * 10) + (c2 - 48);
                                                i2++;
                                            }
                                        }
                                        n = n3 + minutesPart;
                                    } else {
                                        n = (n2 % 100) + ((n2 / 100) * 60);
                                    }
                                    if (prevc2 == 43) {
                                        n = -n;
                                    }
                                    tzoffset2 = n;
                                    prevc2 = 0;
                                } else {
                                    if (c != 47 && c != 58 && c != 43) {
                                        if (c != 45) {
                                            int st = i2 - 1;
                                            while (i2 < limit) {
                                                c = str.charAt(i2);
                                                if ((65 > c || c > 'Z') && (97 > c || c > 'z')) {
                                                    break;
                                                }
                                                i2++;
                                            }
                                            int c3 = c;
                                            if (i2 <= st + 1) {
                                                hour4 = hour5;
                                                int i4 = prevc2;
                                                int i5 = i;
                                                tzoffset = tzoffset2;
                                                break;
                                            }
                                            int k2 = wtb.length;
                                            while (true) {
                                                k = k2 - 1;
                                                if (k < 0) {
                                                    hour4 = hour5;
                                                    char c4 = prevc2;
                                                    millis = i;
                                                    tzoffset = tzoffset2;
                                                    break;
                                                }
                                                millis = i;
                                                tzoffset = tzoffset2;
                                                char c5 = prevc2;
                                                hour4 = hour5;
                                                if (wtb[k].regionMatches(true, 0, str, st, i2 - st)) {
                                                    int action = ttb[k];
                                                    if (action != 0) {
                                                        if (action == 1) {
                                                            if (hour4 > 12 || hour4 < 1) {
                                                                break;
                                                            } else if (hour4 < 12) {
                                                                hour5 = hour4 + 12;
                                                            }
                                                        } else if (action == 14) {
                                                            if (hour4 > 12 || hour4 < 1) {
                                                                break;
                                                            } else if (hour4 == 12) {
                                                                hour5 = 0;
                                                            }
                                                        } else if (action <= 13) {
                                                            if (mon >= 0) {
                                                                break;
                                                            }
                                                            mon = (byte) (action - 2);
                                                        } else {
                                                            tzoffset2 = action - 10000;
                                                            hour5 = hour4;
                                                        }
                                                    }
                                                } else {
                                                    hour5 = hour4;
                                                    tzoffset2 = tzoffset;
                                                    k2 = k;
                                                    prevc2 = c5;
                                                    i = millis;
                                                    str = s;
                                                }
                                            }
                                            hour5 = hour4;
                                            tzoffset2 = tzoffset;
                                            if (k < 0) {
                                                break;
                                            }
                                            prevc2 = 0;
                                            int i6 = c3;
                                            i = millis;
                                            str = s;
                                        } else {
                                            hour3 = hour5;
                                            int i7 = prevc2;
                                            millis5 = i;
                                            millis4 = tzoffset2;
                                        }
                                    } else {
                                        hour3 = hour5;
                                        int i8 = prevc2;
                                        millis5 = i;
                                        millis4 = tzoffset2;
                                    }
                                    prevc2 = c;
                                    hour5 = hour3;
                                    tzoffset2 = millis4;
                                    i = millis;
                                    str = s;
                                }
                            } else {
                                int depth = 1;
                                while (i2 < limit) {
                                    int c6 = str.charAt(i2);
                                    i2++;
                                    if (c6 == 40) {
                                        depth++;
                                    } else if (c6 == 41) {
                                        depth--;
                                        if (depth <= 0) {
                                            break;
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        } else {
                            hour2 = hour5;
                            prevc = prevc2;
                            millis3 = i;
                            millis2 = tzoffset2;
                        }
                    } else {
                        hour2 = hour5;
                        prevc = prevc2;
                        millis3 = i;
                        millis2 = tzoffset2;
                    }
                    hour5 = hour2;
                    tzoffset2 = millis2;
                    prevc2 = prevc;
                    i = millis;
                    str = s;
                } else {
                    int hour6 = hour5;
                    int i9 = prevc2;
                    int i10 = i;
                    int tzoffset3 = tzoffset2;
                    if (year2 == Integer.MIN_VALUE || mon < 0 || mday2 < 0) {
                        int i11 = hour6;
                        int i12 = tzoffset3;
                    } else {
                        if (year2 < 100) {
                            synchronized (Date.class) {
                                if (defaultCenturyStart == 0) {
                                    defaultCenturyStart = gcal.getCalendarDate().getYear() - 80;
                                }
                            }
                            year2 += (defaultCenturyStart / 100) * 100;
                            if (year2 < defaultCenturyStart) {
                                year2 += 100;
                            }
                        }
                        if (b3 < 0) {
                            b3 = 0;
                        }
                        byte b4 = b3;
                        if (b2 < 0) {
                            b2 = 0;
                        }
                        byte b5 = b2;
                        if (hour6 < 0) {
                            hour = 0;
                        } else {
                            hour = hour6;
                        }
                        BaseCalendar cal = getCalendarSystem(year2);
                        if (tzoffset3 == -1) {
                            BaseCalendar.Date ldate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.getDefaultRef());
                            ldate.setDate(year2, mon + 1, mday2);
                            ldate.setTimeOfDay(hour, b5, b4, 0);
                            return cal.getTime(ldate);
                        }
                        BaseCalendar.Date udate = (BaseCalendar.Date) cal.newCalendarDate(null);
                        udate.setDate(year2, mon + 1, mday2);
                        udate.setTimeOfDay(hour, b5, b4, 0);
                        byte b6 = b4;
                        byte b7 = b5;
                        return cal.getTime(udate) + ((long) (60000 * tzoffset3));
                    }
                }
            }
            int i13 = prevc2;
            int i14 = i;
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
        BaseCalendar.Date d = getCalendarDate();
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
        if (this.cdate != null && !this.cdate.isNormalized()) {
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
        return gcal.getTime((BaseCalendar.Date) date.cdate.clone());
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
        BaseCalendar.Date date = normalize();
        StringBuilder sb = new StringBuilder(28);
        int index = date.getDayOfWeek();
        if (index == 1) {
            index = 8;
        }
        convertToAbbr(sb, wtb[index]).append(' ');
        convertToAbbr(sb, wtb[(date.getMonth() - 1) + 2 + 7]).append(' ');
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
        sb.append(' ');
        sb.append(date.getYear());
        return sb.toString();
    }

    private static final StringBuilder convertToAbbr(StringBuilder sb, String name) {
        sb.append(Character.toUpperCase(name.charAt(0)));
        sb.append(name.charAt(1));
        sb.append(name.charAt(2));
        return sb;
    }

    @Deprecated
    public String toLocaleString() {
        return DateFormat.getDateTimeInstance().format(this);
    }

    @Deprecated
    public String toGMTString() {
        BaseCalendar.Date date = (BaseCalendar.Date) getCalendarSystem(getTime()).getCalendarDate(getTime(), (TimeZone) null);
        StringBuilder sb = new StringBuilder(32);
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 1).append(' ');
        convertToAbbr(sb, wtb[(date.getMonth() - 1) + 2 + 7]).append(' ');
        sb.append(date.getYear());
        sb.append(' ');
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

    private final BaseCalendar.Date getCalendarDate() {
        if (this.cdate == null) {
            this.cdate = (BaseCalendar.Date) getCalendarSystem(this.fastTime).getCalendarDate(this.fastTime, TimeZone.getDefaultRef());
        }
        return this.cdate;
    }

    private final BaseCalendar.Date normalize() {
        if (this.cdate == null) {
            this.cdate = (BaseCalendar.Date) getCalendarSystem(this.fastTime).getCalendarDate(this.fastTime, TimeZone.getDefaultRef());
            return this.cdate;
        }
        if (!this.cdate.isNormalized()) {
            this.cdate = normalize(this.cdate);
        }
        TimeZone tz = TimeZone.getDefaultRef();
        if (tz != this.cdate.getZone()) {
            this.cdate.setZone(tz);
            getCalendarSystem(this.cdate).getCalendarDate(this.fastTime, (CalendarDate) this.cdate);
        }
        return this.cdate;
    }

    /* JADX WARNING: type inference failed for: r5v1, types: [sun.util.calendar.CalendarDate] */
    /* JADX WARNING: Multi-variable type inference failed */
    private final BaseCalendar.Date normalize(BaseCalendar.Date date) {
        BaseCalendar.Date date2;
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
            TimeZone tz2 = tz;
            GregorianCalendar gc = new GregorianCalendar(tz2);
            gc.clear();
            gc.set(14, ms);
            int i = y;
            int i2 = y;
            gc.set(i, m - 1, d, hh, mm, ss);
            this.fastTime = gc.getTimeInMillis();
            return (BaseCalendar.Date) getCalendarSystem(this.fastTime).getCalendarDate(this.fastTime, tz2);
        }
        BaseCalendar cal = getCalendarSystem(y);
        if (cal != getCalendarSystem(date)) {
            date2 = (BaseCalendar.Date) cal.newCalendarDate(tz);
            date2.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
        } else {
            date2 = date;
        }
        this.fastTime = cal.getTime(date2);
        BaseCalendar ncal = getCalendarSystem(this.fastTime);
        if (ncal != cal) {
            date2 = ncal.newCalendarDate(tz);
            date2.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
            this.fastTime = ncal.getTime(date2);
        }
        return date2;
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

    private static final BaseCalendar getCalendarSystem(BaseCalendar.Date cdate2) {
        if (jcal == null) {
            return gcal;
        }
        if (cdate2.getEra() != null) {
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
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException((Throwable) ex);
        }
    }

    public Instant toInstant() {
        return Instant.ofEpochMilli(getTime());
    }
}
