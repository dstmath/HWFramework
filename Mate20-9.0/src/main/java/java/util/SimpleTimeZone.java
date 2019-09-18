package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import sun.util.calendar.BaseCalendar;
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
    private static final byte[] staticLeapMonthLength = {31, Character.INITIAL_QUOTE_PUNCTUATION, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final byte[] staticMonthLength = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
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

    public SimpleTimeZone(int rawOffset2, String ID) {
        this.useDaylight = false;
        this.monthLength = staticMonthLength;
        this.serialVersionOnStream = 2;
        this.rawOffset = rawOffset2;
        setID(ID);
        this.dstSavings = millisPerHour;
    }

    public SimpleTimeZone(int rawOffset2, String ID, int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, int endMonth2, int endDay2, int endDayOfWeek2, int endTime2) {
        this(rawOffset2, ID, startMonth2, startDay2, startDayOfWeek2, startTime2, 0, endMonth2, endDay2, endDayOfWeek2, endTime2, 0, millisPerHour);
    }

    public SimpleTimeZone(int rawOffset2, String ID, int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, int endMonth2, int endDay2, int endDayOfWeek2, int endTime2, int dstSavings2) {
        this(rawOffset2, ID, startMonth2, startDay2, startDayOfWeek2, startTime2, 0, endMonth2, endDay2, endDayOfWeek2, endTime2, 0, dstSavings2);
    }

    public SimpleTimeZone(int rawOffset2, String ID, int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, int startTimeMode2, int endMonth2, int endDay2, int endDayOfWeek2, int endTime2, int endTimeMode2, int dstSavings2) {
        int i = dstSavings2;
        this.useDaylight = false;
        this.monthLength = staticMonthLength;
        this.serialVersionOnStream = 2;
        setID(ID);
        this.rawOffset = rawOffset2;
        this.startMonth = startMonth2;
        this.startDay = startDay2;
        this.startDayOfWeek = startDayOfWeek2;
        this.startTime = startTime2;
        this.startTimeMode = startTimeMode2;
        this.endMonth = endMonth2;
        this.endDay = endDay2;
        this.endDayOfWeek = endDayOfWeek2;
        this.endTime = endTime2;
        this.endTimeMode = endTimeMode2;
        this.dstSavings = i;
        decodeRules();
        if (i <= 0) {
            throw new IllegalArgumentException("Illegal daylight saving value: " + i);
        }
    }

    public void setStartYear(int year) {
        this.startYear = year;
        invalidateCache();
    }

    public void setStartRule(int startMonth2, int startDay2, int startDayOfWeek2, int startTime2) {
        this.startMonth = startMonth2;
        this.startDay = startDay2;
        this.startDayOfWeek = startDayOfWeek2;
        this.startTime = startTime2;
        this.startTimeMode = 0;
        decodeStartRule();
        invalidateCache();
    }

    public void setStartRule(int startMonth2, int startDay2, int startTime2) {
        setStartRule(startMonth2, startDay2, 0, startTime2);
    }

    public void setStartRule(int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, boolean after) {
        if (after) {
            setStartRule(startMonth2, startDay2, -startDayOfWeek2, startTime2);
        } else {
            setStartRule(startMonth2, -startDay2, -startDayOfWeek2, startTime2);
        }
    }

    public void setEndRule(int endMonth2, int endDay2, int endDayOfWeek2, int endTime2) {
        this.endMonth = endMonth2;
        this.endDay = endDay2;
        this.endDayOfWeek = endDayOfWeek2;
        this.endTime = endTime2;
        this.endTimeMode = 0;
        decodeEndRule();
        invalidateCache();
    }

    public void setEndRule(int endMonth2, int endDay2, int endTime2) {
        setEndRule(endMonth2, endDay2, 0, endTime2);
    }

    public void setEndRule(int endMonth2, int endDay2, int endDayOfWeek2, int endTime2, boolean after) {
        if (after) {
            setEndRule(endMonth2, endDay2, -endDayOfWeek2, endTime2);
        } else {
            setEndRule(endMonth2, -endDay2, -endDayOfWeek2, endTime2);
        }
    }

    public int getOffset(long date) {
        return getOffsets(date, null);
    }

    /* access modifiers changed from: package-private */
    public int getOffsets(long date, int[] offsets) {
        int offset = this.rawOffset;
        if (this.useDaylight) {
            synchronized (this) {
                if (this.cacheStart == 0 || date < this.cacheStart || date >= this.cacheEnd) {
                    BaseCalendar cal = date >= -12219292800000L ? gcal : (BaseCalendar) CalendarSystem.forName("julian");
                    BaseCalendar.Date cdate = (BaseCalendar.Date) cal.newCalendarDate(TimeZone.NO_TIMEZONE);
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

    /* JADX WARNING: type inference failed for: r5v6, types: [sun.util.calendar.CalendarDate] */
    /* JADX WARNING: Multi-variable type inference failed */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        int y;
        long time;
        BaseCalendar cal;
        BaseCalendar.Date cdate;
        int i = era;
        int i2 = day;
        int i3 = dayOfWeek;
        int i4 = millis;
        if (i == 1 || i == 0) {
            int y2 = year;
            if (i == 0) {
                y2 = 1 - y2;
            }
            if (y2 >= 292278994) {
                y = 2800 + (y2 % 2800);
            } else {
                if (y2 <= -292269054) {
                    y2 = (int) CalendarUtils.mod((long) y2, 28);
                }
                y = y2;
            }
            int m = month + 1;
            BaseCalendar cal2 = gcal;
            BaseCalendar.Date cdate2 = (BaseCalendar.Date) cal2.newCalendarDate(TimeZone.NO_TIMEZONE);
            cdate2.setDate(y, m, i2);
            long time2 = cal2.getTime(cdate2) + ((long) (i4 - this.rawOffset));
            if (time2 < -12219292800000L) {
                BaseCalendar cal3 = (BaseCalendar) CalendarSystem.forName("julian");
                BaseCalendar.Date cdate3 = cal3.newCalendarDate(TimeZone.NO_TIMEZONE);
                cdate3.setNormalizedDate(y, m, i2);
                cdate = cdate3;
                cal = cal3;
                time = (cal3.getTime(cdate3) + ((long) i4)) - ((long) this.rawOffset);
            } else {
                cal = cal2;
                cdate = cdate2;
                time = time2;
            }
            if (cdate.getNormalizedYear() != y || cdate.getMonth() != m || cdate.getDayOfMonth() != i2 || i3 < 1 || i3 > 7 || i4 < 0 || i4 >= millisPerDay) {
                throw new IllegalArgumentException();
            } else if (!this.useDaylight || year < this.startYear || i != 1) {
                return this.rawOffset;
            } else {
                return getOffset(cal, cdate, y, time);
            }
        } else {
            throw new IllegalArgumentException("Illegal era " + i);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0028, code lost:
        r0 = getStart(r11, r12, r13);
        r2 = getEnd(r11, r12, r13);
        r4 = r10.rawOffset;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        if (r0 > r2) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        if (r14 < r0) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        if (r14 >= r2) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003e, code lost:
        r4 = r4 + r10.dstSavings;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0041, code lost:
        r5 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0042, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r10.cacheYear = (long) r13;
        r10.cacheStart = r0;
        r10.cacheEnd = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004a, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0051, code lost:
        if (r14 >= r2) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0053, code lost:
        r0 = getStart(r11, r12, r13 - 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005b, code lost:
        if (r14 < r0) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x005d, code lost:
        r4 = r4 + r10.dstSavings;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0060, code lost:
        r5 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0064, code lost:
        if (r14 < r0) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0066, code lost:
        r2 = getEnd(r11, r12, r13 + 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x006e, code lost:
        if (r14 >= r2) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0070, code lost:
        r4 = r4 + r10.dstSavings;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0076, code lost:
        if (r0 > r2) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0078, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r10.cacheYear = ((long) r10.startYear) - 1;
        r10.cacheStart = r0;
        r10.cacheEnd = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0085, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x008a, code lost:
        return r5;
     */
    private int getOffset(BaseCalendar cal, BaseCalendar.Date cdate, int year, long time) {
        synchronized (this) {
            if (this.cacheStart != 0) {
                if (time >= this.cacheStart && time < this.cacheEnd) {
                    int i = this.rawOffset + this.dstSavings;
                    return i;
                } else if (((long) year) == this.cacheYear) {
                    int i2 = this.rawOffset;
                    return i2;
                }
            }
        }
    }

    private long getStart(BaseCalendar cal, BaseCalendar.Date cdate, int year) {
        int time = this.startTime;
        if (this.startTimeMode != 2) {
            time -= this.rawOffset;
        }
        return getTransition(cal, cdate, this.startMode, year, this.startMonth, this.startDay, this.startDayOfWeek, time);
    }

    private long getEnd(BaseCalendar cal, BaseCalendar.Date cdate, int year) {
        int time = this.endTime;
        if (this.endTimeMode != 2) {
            time -= this.rawOffset;
        }
        if (this.endTimeMode == 0) {
            time -= this.dstSavings;
        }
        return getTransition(cal, cdate, this.endMode, year, this.endMonth, this.endDay, this.endDayOfWeek, time);
    }

    /* JADX WARNING: type inference failed for: r0v4, types: [sun.util.calendar.CalendarDate] */
    /* JADX WARNING: type inference failed for: r0v6, types: [sun.util.calendar.CalendarDate] */
    /* JADX WARNING: type inference failed for: r0v8, types: [sun.util.calendar.CalendarDate] */
    /* JADX WARNING: Multi-variable type inference failed */
    private long getTransition(BaseCalendar cal, BaseCalendar.Date cdate, int mode, int year, int month, int dayOfMonth, int dayOfWeek, int timeOfDay) {
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
                cdate = cal.getNthDayOfWeek(dayOfMonth, dayOfWeek, cdate);
                break;
            case 3:
                cdate.setDayOfMonth(dayOfMonth);
                cdate = cal.getNthDayOfWeek(1, dayOfWeek, cdate);
                break;
            case 4:
                cdate.setDayOfMonth(dayOfMonth);
                cdate = cal.getNthDayOfWeek(-1, dayOfWeek, cdate);
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
        if (millisSavedDuringDST > 0) {
            this.dstSavings = millisSavedDuringDST;
            return;
        }
        throw new IllegalArgumentException("Illegal daylight saving value: " + millisSavedDuringDST);
    }

    public int getDSTSavings() {
        if (this.useDaylight) {
            return this.dstSavings;
        }
        return 0;
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
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone that = (SimpleTimeZone) obj;
        if (!getID().equals(that.getID()) || !hasSameRules(that)) {
            z = false;
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
        if (!(this.rawOffset == that.rawOffset && this.useDaylight == that.useDaylight && (!this.useDaylight || (this.dstSavings == that.dstSavings && this.startMode == that.startMode && this.startMonth == that.startMonth && this.startDay == that.startDay && this.startDayOfWeek == that.startDayOfWeek && this.startTime == that.startTime && this.startTimeMode == that.startTimeMode && this.endMode == that.endMode && this.endMonth == that.endMonth && this.endDay == that.endDay && this.endDayOfWeek == that.endDayOfWeek && this.endTime == that.endTime && this.endTimeMode == that.endTimeMode && this.startYear == that.startYear)))) {
            z = false;
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
        this.useDaylight = (this.startDay == 0 || this.endDay == 0) ? false : true;
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
        this.useDaylight = (this.startDay == 0 || this.endDay == 0) ? false : true;
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
        int i = this.startMode;
        if (i != 1) {
            switch (i) {
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
        } else {
            this.startDay = (this.startDay / 7) + 1;
            this.startDayOfWeek = 1;
        }
        int i2 = this.endMode;
        if (i2 != 1) {
            switch (i2) {
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
        } else {
            this.endDay = (this.endDay / 7) + 1;
            this.endDayOfWeek = 1;
        }
        if (this.startTimeMode == 2) {
            this.startTime += this.rawOffset;
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
