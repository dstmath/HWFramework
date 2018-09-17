package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.BaseCalendar.Date;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Gregorian;

public class SimpleTimeZone extends TimeZone {
    private static final int DOM_MODE = 1;
    private static final int DOW_GE_DOM_MODE = 3;
    private static final int DOW_IN_MONTH_MODE = 2;
    private static final int DOW_LE_DOM_MODE = 4;
    public static final int STANDARD_TIME = 1;
    public static final int UTC_TIME = 2;
    public static final int WALL_TIME = 0;
    static final int currentSerialVersion = 2;
    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();
    private static final int millisPerDay = 86400000;
    private static final int millisPerHour = 3600000;
    static final long serialVersionUID = -403250971215465050L;
    private static final byte[] staticLeapMonthLength = new byte[]{(byte) 31, Character.INITIAL_QUOTE_PUNCTUATION, (byte) 31, (byte) 30, (byte) 31, (byte) 30, (byte) 31, (byte) 31, (byte) 30, (byte) 31, (byte) 30, (byte) 31};
    private static final byte[] staticMonthLength = new byte[]{(byte) 31, (byte) 28, (byte) 31, (byte) 30, (byte) 31, (byte) 30, (byte) 31, (byte) 31, (byte) 30, (byte) 31, (byte) 30, (byte) 31};
    private transient long cacheEnd;
    private transient long cacheStart;
    private transient long cacheYear;
    private int dstSavings;
    private int endDay;
    private int endDayOfWeek;
    private int endMode;
    private int endMonth;
    private int endTime;
    private int endTimeMode;
    private final byte[] monthLength;
    private int rawOffset;
    private int serialVersionOnStream;
    private int startDay;
    private int startDayOfWeek;
    private int startMode;
    private int startMonth;
    private int startTime;
    private int startTimeMode;
    private int startYear;
    private boolean useDaylight;

    public SimpleTimeZone(int rawOffset, String ID) {
        this.useDaylight = false;
        this.monthLength = staticMonthLength;
        this.serialVersionOnStream = 2;
        this.rawOffset = rawOffset;
        setID(ID);
        this.dstSavings = millisPerHour;
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime) {
        this(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, 0, endMonth, endDay, endDayOfWeek, endTime, 0, millisPerHour);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime, int dstSavings) {
        this(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, 0, endMonth, endDay, endDayOfWeek, endTime, 0, dstSavings);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int startTimeMode, int endMonth, int endDay, int endDayOfWeek, int endTime, int endTimeMode, int dstSavings) {
        this.useDaylight = false;
        this.monthLength = staticMonthLength;
        this.serialVersionOnStream = 2;
        setID(ID);
        this.rawOffset = rawOffset;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime = startTime;
        this.startTimeMode = startTimeMode;
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endDayOfWeek = endDayOfWeek;
        this.endTime = endTime;
        this.endTimeMode = endTimeMode;
        this.dstSavings = dstSavings;
        decodeRules();
        if (dstSavings <= 0) {
            throw new IllegalArgumentException("Illegal daylight saving value: " + dstSavings);
        }
    }

    public void setStartYear(int year) {
        this.startYear = year;
        invalidateCache();
    }

    public void setStartRule(int startMonth, int startDay, int startDayOfWeek, int startTime) {
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime = startTime;
        this.startTimeMode = 0;
        decodeStartRule();
        invalidateCache();
    }

    public void setStartRule(int startMonth, int startDay, int startTime) {
        setStartRule(startMonth, startDay, 0, startTime);
    }

    public void setStartRule(int startMonth, int startDay, int startDayOfWeek, int startTime, boolean after) {
        if (after) {
            setStartRule(startMonth, startDay, -startDayOfWeek, startTime);
        } else {
            setStartRule(startMonth, -startDay, -startDayOfWeek, startTime);
        }
    }

    public void setEndRule(int endMonth, int endDay, int endDayOfWeek, int endTime) {
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endDayOfWeek = endDayOfWeek;
        this.endTime = endTime;
        this.endTimeMode = 0;
        decodeEndRule();
        invalidateCache();
    }

    public void setEndRule(int endMonth, int endDay, int endTime) {
        setEndRule(endMonth, endDay, 0, endTime);
    }

    public void setEndRule(int endMonth, int endDay, int endDayOfWeek, int endTime, boolean after) {
        if (after) {
            setEndRule(endMonth, endDay, -endDayOfWeek, endTime);
        } else {
            setEndRule(endMonth, -endDay, -endDayOfWeek, endTime);
        }
    }

    public int getOffset(long date) {
        return getOffsets(date, null);
    }

    int getOffsets(long date, int[] offsets) {
        int offset = this.rawOffset;
        if (this.useDaylight) {
            synchronized (this) {
                if (this.cacheStart == 0 || date < this.cacheStart || date >= this.cacheEnd) {
                    BaseCalendar cal = date >= -12219292800000L ? gcal : (BaseCalendar) CalendarSystem.forName("julian");
                    Date cdate = (Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    cal.getCalendarDate(((long) this.rawOffset) + date, (CalendarDate) cdate);
                    int year = cdate.getNormalizedYear();
                    if (year >= this.startYear) {
                        cdate.setTimeOfDay(0, 0, 0, 0);
                        offset = getOffset(cal, cdate, year, date);
                    }
                } else {
                    offset += this.dstSavings;
                }
            }
        }
        if (offsets != null) {
            offsets[0] = this.rawOffset;
            offsets[1] = offset - this.rawOffset;
        }
        return offset;
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        if (era == 1 || era == 0) {
            int y = year;
            if (era == 0) {
                y = 1 - year;
            }
            if (y >= 292278994) {
                y = (y % 2800) + 2800;
            } else if (y <= -292269054) {
                y = (int) CalendarUtils.mod((long) y, 28);
            }
            int m = month + 1;
            BaseCalendar cal = gcal;
            Date cdate = (Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
            cdate.setDate(y, m, day);
            long time = cal.getTime(cdate) + ((long) (millis - this.rawOffset));
            if (time < -12219292800000L) {
                cal = (BaseCalendar) CalendarSystem.forName("julian");
                cdate = (Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                cdate.setNormalizedDate(y, m, day);
                time = (cal.getTime(cdate) + ((long) millis)) - ((long) this.rawOffset);
            }
            if (cdate.getNormalizedYear() != y || cdate.getMonth() != m || cdate.getDayOfMonth() != day || dayOfWeek < 1 || dayOfWeek > 7 || millis < 0 || millis >= millisPerDay) {
                throw new IllegalArgumentException();
            } else if (this.useDaylight && year >= this.startYear && era == 1) {
                return getOffset(cal, cdate, y, time);
            } else {
                return this.rawOffset;
            }
        }
        throw new IllegalArgumentException("Illegal era " + era);
    }

    /* JADX WARNING: Missing block: B:20:0x0028, code:
            r4 = getStart(r11, r12, r13);
            r0 = getEnd(r11, r12, r13);
            r2 = r10.rawOffset;
     */
    /* JADX WARNING: Missing block: B:21:0x0034, code:
            if (r4 > r0) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:23:0x0038, code:
            if (r14 < r4) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:25:0x003c, code:
            if (r14 >= r0) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:26:0x003e, code:
            r2 = r2 + r10.dstSavings;
     */
    /* JADX WARNING: Missing block: B:27:0x0041, code:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r10.cacheYear = (long) r13;
            r10.cacheStart = r4;
            r10.cacheEnd = r0;
     */
    /* JADX WARNING: Missing block: B:31:0x0049, code:
            monitor-exit(r10);
     */
    /* JADX WARNING: Missing block: B:32:0x004a, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:40:0x0053, code:
            if (r14 >= r0) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:41:0x0055, code:
            r4 = getStart(r11, r12, r13 - 1);
     */
    /* JADX WARNING: Missing block: B:42:0x005d, code:
            if (r14 < r4) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:43:0x005f, code:
            r2 = r2 + r10.dstSavings;
     */
    /* JADX WARNING: Missing block: B:45:0x0064, code:
            if (r4 > r0) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:46:0x0066, code:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            r10.cacheYear = ((long) r10.startYear) - 1;
            r10.cacheStart = r4;
            r10.cacheEnd = r0;
     */
    /* JADX WARNING: Missing block: B:53:0x0079, code:
            if (r14 < r4) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:54:0x007b, code:
            r0 = getEnd(r11, r12, r13 + 1);
     */
    /* JADX WARNING: Missing block: B:55:0x0083, code:
            if (r14 >= r0) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:56:0x0085, code:
            r2 = r2 + r10.dstSavings;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getOffset(BaseCalendar cal, Date cdate, int year, long time) {
        synchronized (this) {
            if (this.cacheStart != 0) {
                int i;
                if (time < this.cacheStart || time >= this.cacheEnd) {
                    if (((long) year) == this.cacheYear) {
                        i = this.rawOffset;
                        return i;
                    }
                }
                i = this.rawOffset + this.dstSavings;
                return i;
            }
        }
    }

    private long getStart(BaseCalendar cal, Date cdate, int year) {
        int time = this.startTime;
        if (this.startTimeMode != 2) {
            time -= this.rawOffset;
        }
        return getTransition(cal, cdate, this.startMode, year, this.startMonth, this.startDay, this.startDayOfWeek, time);
    }

    private long getEnd(BaseCalendar cal, Date cdate, int year) {
        int time = this.endTime;
        if (this.endTimeMode != 2) {
            time -= this.rawOffset;
        }
        if (this.endTimeMode == 0) {
            time -= this.dstSavings;
        }
        return getTransition(cal, cdate, this.endMode, year, this.endMonth, this.endDay, this.endDayOfWeek, time);
    }

    private long getTransition(BaseCalendar cal, Date cdate, int mode, int year, int month, int dayOfMonth, int dayOfWeek, int timeOfDay) {
        cdate.setNormalizedYear(year);
        cdate.setMonth(month + 1);
        switch (mode) {
            case 1:
                cdate.setDayOfMonth(dayOfMonth);
                break;
            case 2:
                cdate.setDayOfMonth(1);
                if (dayOfMonth < 0) {
                    cdate.setDayOfMonth(cal.getMonthLength(cdate));
                }
                cdate = (Date) cal.getNthDayOfWeek(dayOfMonth, dayOfWeek, cdate);
                break;
            case 3:
                cdate.setDayOfMonth(dayOfMonth);
                cdate = (Date) cal.getNthDayOfWeek(1, dayOfWeek, cdate);
                break;
            case 4:
                cdate.setDayOfMonth(dayOfMonth);
                cdate = (Date) cal.getNthDayOfWeek(-1, dayOfWeek, cdate);
                break;
        }
        return cal.getTime(cdate) + ((long) timeOfDay);
    }

    public int getRawOffset() {
        return this.rawOffset;
    }

    public void setRawOffset(int offsetMillis) {
        this.rawOffset = offsetMillis;
    }

    public void setDSTSavings(int millisSavedDuringDST) {
        if (millisSavedDuringDST <= 0) {
            throw new IllegalArgumentException("Illegal daylight saving value: " + millisSavedDuringDST);
        }
        this.dstSavings = millisSavedDuringDST;
    }

    public int getDSTSavings() {
        return this.useDaylight ? this.dstSavings : 0;
    }

    public boolean useDaylightTime() {
        return this.useDaylight;
    }

    public boolean observesDaylightTime() {
        return useDaylightTime();
    }

    public boolean inDaylightTime(Date date) {
        return getOffset(date.getTime()) != this.rawOffset;
    }

    public Object clone() {
        return super.clone();
    }

    public synchronized int hashCode() {
        return (((((((this.startMonth ^ this.startDay) ^ this.startDayOfWeek) ^ this.startTime) ^ this.endMonth) ^ this.endDay) ^ this.endDayOfWeek) ^ this.endTime) ^ this.rawOffset;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone that = (SimpleTimeZone) obj;
        if (getID().equals(that.getID())) {
            z = hasSameRules(that);
        }
        return z;
    }

    public boolean hasSameRules(TimeZone other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone that = (SimpleTimeZone) other;
        if (this.rawOffset != that.rawOffset || this.useDaylight != that.useDaylight) {
            z = false;
        } else if (this.useDaylight) {
            if (this.dstSavings != that.dstSavings || this.startMode != that.startMode || this.startMonth != that.startMonth || this.startDay != that.startDay || this.startDayOfWeek != that.startDayOfWeek || this.startTime != that.startTime || this.startTimeMode != that.startTimeMode || this.endMode != that.endMode || this.endMonth != that.endMonth || this.endDay != that.endDay || this.endDayOfWeek != that.endDayOfWeek || this.endTime != that.endTime || this.endTimeMode != that.endTimeMode) {
                z = false;
            } else if (this.startYear != that.startYear) {
                z = false;
            }
        }
        return z;
    }

    public String toString() {
        return getClass().getName() + "[id=" + getID() + ",offset=" + this.rawOffset + ",dstSavings=" + this.dstSavings + ",useDaylight=" + this.useDaylight + ",startYear=" + this.startYear + ",startMode=" + this.startMode + ",startMonth=" + this.startMonth + ",startDay=" + this.startDay + ",startDayOfWeek=" + this.startDayOfWeek + ",startTime=" + this.startTime + ",startTimeMode=" + this.startTimeMode + ",endMode=" + this.endMode + ",endMonth=" + this.endMonth + ",endDay=" + this.endDay + ",endDayOfWeek=" + this.endDayOfWeek + ",endTime=" + this.endTime + ",endTimeMode=" + this.endTimeMode + ']';
    }

    private synchronized void invalidateCache() {
        this.cacheYear = (long) (this.startYear - 1);
        this.cacheEnd = 0;
        this.cacheStart = 0;
    }

    private void decodeRules() {
        decodeStartRule();
        decodeEndRule();
    }

    private void decodeStartRule() {
        boolean z = false;
        if (!(this.startDay == 0 || this.endDay == 0)) {
            z = true;
        }
        this.useDaylight = z;
        if (this.startDay == 0) {
            return;
        }
        if (this.startMonth < 0 || this.startMonth > 11) {
            throw new IllegalArgumentException("Illegal start month " + this.startMonth);
        } else if (this.startTime < 0 || this.startTime > millisPerDay) {
            throw new IllegalArgumentException("Illegal start time " + this.startTime);
        } else {
            if (this.startDayOfWeek == 0) {
                this.startMode = 1;
            } else {
                if (this.startDayOfWeek > 0) {
                    this.startMode = 2;
                } else {
                    this.startDayOfWeek = -this.startDayOfWeek;
                    if (this.startDay > 0) {
                        this.startMode = 3;
                    } else {
                        this.startDay = -this.startDay;
                        this.startMode = 4;
                    }
                }
                if (this.startDayOfWeek > 7) {
                    throw new IllegalArgumentException("Illegal start day of week " + this.startDayOfWeek);
                }
            }
            if (this.startMode == 2) {
                if (this.startDay < -5 || this.startDay > 5) {
                    throw new IllegalArgumentException("Illegal start day of week in month " + this.startDay);
                }
            } else if (this.startDay < 1 || this.startDay > staticMonthLength[this.startMonth]) {
                throw new IllegalArgumentException("Illegal start day " + this.startDay);
            }
        }
    }

    private void decodeEndRule() {
        boolean z = false;
        if (!(this.startDay == 0 || this.endDay == 0)) {
            z = true;
        }
        this.useDaylight = z;
        if (this.endDay == 0) {
            return;
        }
        if (this.endMonth < 0 || this.endMonth > 11) {
            throw new IllegalArgumentException("Illegal end month " + this.endMonth);
        } else if (this.endTime < 0 || this.endTime > millisPerDay) {
            throw new IllegalArgumentException("Illegal end time " + this.endTime);
        } else {
            if (this.endDayOfWeek == 0) {
                this.endMode = 1;
            } else {
                if (this.endDayOfWeek > 0) {
                    this.endMode = 2;
                } else {
                    this.endDayOfWeek = -this.endDayOfWeek;
                    if (this.endDay > 0) {
                        this.endMode = 3;
                    } else {
                        this.endDay = -this.endDay;
                        this.endMode = 4;
                    }
                }
                if (this.endDayOfWeek > 7) {
                    throw new IllegalArgumentException("Illegal end day of week " + this.endDayOfWeek);
                }
            }
            if (this.endMode == 2) {
                if (this.endDay < -5 || this.endDay > 5) {
                    throw new IllegalArgumentException("Illegal end day of week in month " + this.endDay);
                }
            } else if (this.endDay < 1 || this.endDay > staticMonthLength[this.endMonth]) {
                throw new IllegalArgumentException("Illegal end day " + this.endDay);
            }
        }
    }

    private void makeRulesCompatible() {
        switch (this.startMode) {
            case 1:
                this.startDay = (this.startDay / 7) + 1;
                this.startDayOfWeek = 1;
                break;
            case 3:
                if (this.startDay != 1) {
                    this.startDay = (this.startDay / 7) + 1;
                    break;
                }
                break;
            case 4:
                if (this.startDay < 30) {
                    this.startDay = (this.startDay / 7) + 1;
                    break;
                } else {
                    this.startDay = -1;
                    break;
                }
        }
        switch (this.endMode) {
            case 1:
                this.endDay = (this.endDay / 7) + 1;
                this.endDayOfWeek = 1;
                break;
            case 3:
                if (this.endDay != 1) {
                    this.endDay = (this.endDay / 7) + 1;
                    break;
                }
                break;
            case 4:
                if (this.endDay < 30) {
                    this.endDay = (this.endDay / 7) + 1;
                    break;
                } else {
                    this.endDay = -1;
                    break;
                }
        }
        switch (this.startTimeMode) {
            case 2:
                this.startTime += this.rawOffset;
                break;
        }
        while (this.startTime < 0) {
            this.startTime += millisPerDay;
            this.startDayOfWeek = ((this.startDayOfWeek + 5) % 7) + 1;
        }
        while (this.startTime >= millisPerDay) {
            this.startTime -= millisPerDay;
            this.startDayOfWeek = (this.startDayOfWeek % 7) + 1;
        }
        switch (this.endTimeMode) {
            case 1:
                this.endTime += this.dstSavings;
                break;
            case 2:
                this.endTime += this.rawOffset + this.dstSavings;
                break;
        }
        while (this.endTime < 0) {
            this.endTime += millisPerDay;
            this.endDayOfWeek = ((this.endDayOfWeek + 5) % 7) + 1;
        }
        while (this.endTime >= millisPerDay) {
            this.endTime -= millisPerDay;
            this.endDayOfWeek = (this.endDayOfWeek % 7) + 1;
        }
    }

    private byte[] packRules() {
        return new byte[]{(byte) this.startDay, (byte) this.startDayOfWeek, (byte) this.endDay, (byte) this.endDayOfWeek, (byte) this.startTimeMode, (byte) this.endTimeMode};
    }

    private void unpackRules(byte[] rules) {
        this.startDay = rules[0];
        this.startDayOfWeek = rules[1];
        this.endDay = rules[2];
        this.endDayOfWeek = rules[3];
        if (rules.length >= 6) {
            this.startTimeMode = rules[4];
            this.endTimeMode = rules[5];
        }
    }

    private int[] packTimes() {
        return new int[]{this.startTime, this.endTime};
    }

    private void unpackTimes(int[] times) {
        this.startTime = times[0];
        this.endTime = times[1];
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        byte[] rules = packRules();
        int[] times = packTimes();
        makeRulesCompatible();
        stream.defaultWriteObject();
        stream.writeInt(rules.length);
        stream.write(rules);
        stream.writeObject(times);
        unpackRules(rules);
        unpackTimes(times);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            if (this.startDayOfWeek == 0) {
                this.startDayOfWeek = 1;
            }
            if (this.endDayOfWeek == 0) {
                this.endDayOfWeek = 1;
            }
            this.endMode = 2;
            this.startMode = 2;
            this.dstSavings = millisPerHour;
        } else {
            byte[] rules = new byte[stream.readInt()];
            stream.readFully(rules);
            unpackRules(rules);
        }
        if (this.serialVersionOnStream >= 2) {
            unpackTimes((int[]) stream.readObject());
        }
        this.serialVersionOnStream = 2;
    }
}
