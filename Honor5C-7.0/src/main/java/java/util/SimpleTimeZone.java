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
    private static final Gregorian gcal = null;
    private static final int millisPerDay = 86400000;
    private static final int millisPerHour = 3600000;
    static final long serialVersionUID = -403250971215465050L;
    private static final byte[] staticLeapMonthLength = null;
    private static final byte[] staticMonthLength = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.SimpleTimeZone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.SimpleTimeZone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.SimpleTimeZone.<clinit>():void");
    }

    public SimpleTimeZone(int rawOffset, String ID) {
        this.useDaylight = false;
        this.monthLength = staticMonthLength;
        this.serialVersionOnStream = currentSerialVersion;
        this.rawOffset = rawOffset;
        setID(ID);
        this.dstSavings = millisPerHour;
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime) {
        this(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, WALL_TIME, endMonth, endDay, endDayOfWeek, endTime, WALL_TIME, millisPerHour);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime, int dstSavings) {
        this(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, WALL_TIME, endMonth, endDay, endDayOfWeek, endTime, WALL_TIME, dstSavings);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int startTimeMode, int endMonth, int endDay, int endDayOfWeek, int endTime, int endTimeMode, int dstSavings) {
        this.useDaylight = false;
        this.monthLength = staticMonthLength;
        this.serialVersionOnStream = currentSerialVersion;
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
        this.startTimeMode = WALL_TIME;
        decodeStartRule();
        invalidateCache();
    }

    public void setStartRule(int startMonth, int startDay, int startTime) {
        setStartRule(startMonth, startDay, WALL_TIME, startTime);
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
        this.endTimeMode = WALL_TIME;
        decodeEndRule();
        invalidateCache();
    }

    public void setEndRule(int endMonth, int endDay, int endTime) {
        setEndRule(endMonth, endDay, WALL_TIME, endTime);
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
                        cdate.setTimeOfDay(WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME);
                        offset = getOffset(cal, cdate, year, date);
                    }
                } else {
                    offset += this.dstSavings;
                }
            }
        }
        if (offsets != null) {
            offsets[WALL_TIME] = this.rawOffset;
            offsets[STANDARD_TIME] = offset - this.rawOffset;
        }
        return offset;
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        if (era == STANDARD_TIME || era == 0) {
            int y = year;
            if (era == 0) {
                y = 1 - year;
            }
            if (y >= 292278994) {
                y = (y % 2800) + 2800;
            } else if (y <= -292269054) {
                y = (int) CalendarUtils.mod((long) y, 28);
            }
            int m = month + STANDARD_TIME;
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
            if (cdate.getNormalizedYear() != y || cdate.getMonth() != m || cdate.getDayOfMonth() != day || dayOfWeek < STANDARD_TIME || dayOfWeek > 7 || millis < 0 || millis >= millisPerDay) {
                throw new IllegalArgumentException();
            } else if (this.useDaylight && year >= this.startYear && era == STANDARD_TIME) {
                return getOffset(cal, cdate, y, time);
            } else {
                return this.rawOffset;
            }
        }
        throw new IllegalArgumentException("Illegal era " + era);
    }

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
            long start = getStart(cal, cdate, year);
            long end = getEnd(cal, cdate, year);
            int offset = this.rawOffset;
            if (start <= end) {
                if (time >= start && time < end) {
                    offset += this.dstSavings;
                }
                synchronized (this) {
                    this.cacheYear = (long) year;
                    this.cacheStart = start;
                    this.cacheEnd = end;
                }
                return offset;
            }
            if (time < end) {
                start = getStart(cal, cdate, year - 1);
                if (time >= start) {
                    offset += this.dstSavings;
                }
            } else if (time >= start) {
                end = getEnd(cal, cdate, year + STANDARD_TIME);
                if (time < end) {
                    offset += this.dstSavings;
                }
            }
            if (start <= end) {
                synchronized (this) {
                    this.cacheYear = ((long) this.startYear) - 1;
                    this.cacheStart = start;
                    this.cacheEnd = end;
                }
            }
            return offset;
            return offset;
        }
    }

    private long getStart(BaseCalendar cal, Date cdate, int year) {
        int time = this.startTime;
        if (this.startTimeMode != currentSerialVersion) {
            time -= this.rawOffset;
        }
        return getTransition(cal, cdate, this.startMode, year, this.startMonth, this.startDay, this.startDayOfWeek, time);
    }

    private long getEnd(BaseCalendar cal, Date cdate, int year) {
        int time = this.endTime;
        if (this.endTimeMode != currentSerialVersion) {
            time -= this.rawOffset;
        }
        if (this.endTimeMode == 0) {
            time -= this.dstSavings;
        }
        return getTransition(cal, cdate, this.endMode, year, this.endMonth, this.endDay, this.endDayOfWeek, time);
    }

    private long getTransition(BaseCalendar cal, Date cdate, int mode, int year, int month, int dayOfMonth, int dayOfWeek, int timeOfDay) {
        cdate.setNormalizedYear(year);
        cdate.setMonth(month + STANDARD_TIME);
        switch (mode) {
            case STANDARD_TIME /*1*/:
                cdate.setDayOfMonth(dayOfMonth);
                break;
            case currentSerialVersion /*2*/:
                cdate.setDayOfMonth(STANDARD_TIME);
                if (dayOfMonth < 0) {
                    cdate.setDayOfMonth(cal.getMonthLength(cdate));
                }
                cdate = (Date) cal.getNthDayOfWeek(dayOfMonth, dayOfWeek, cdate);
                break;
            case DOW_GE_DOM_MODE /*3*/:
                cdate.setDayOfMonth(dayOfMonth);
                cdate = (Date) cal.getNthDayOfWeek(STANDARD_TIME, dayOfWeek, cdate);
                break;
            case DOW_LE_DOM_MODE /*4*/:
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
        return this.useDaylight ? this.dstSavings : WALL_TIME;
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
                this.startMode = STANDARD_TIME;
            } else {
                if (this.startDayOfWeek > 0) {
                    this.startMode = currentSerialVersion;
                } else {
                    this.startDayOfWeek = -this.startDayOfWeek;
                    if (this.startDay > 0) {
                        this.startMode = DOW_GE_DOM_MODE;
                    } else {
                        this.startDay = -this.startDay;
                        this.startMode = DOW_LE_DOM_MODE;
                    }
                }
                if (this.startDayOfWeek > 7) {
                    throw new IllegalArgumentException("Illegal start day of week " + this.startDayOfWeek);
                }
            }
            if (this.startMode == currentSerialVersion) {
                if (this.startDay < -5 || this.startDay > 5) {
                    throw new IllegalArgumentException("Illegal start day of week in month " + this.startDay);
                }
            } else if (this.startDay < STANDARD_TIME || this.startDay > staticMonthLength[this.startMonth]) {
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
                this.endMode = STANDARD_TIME;
            } else {
                if (this.endDayOfWeek > 0) {
                    this.endMode = currentSerialVersion;
                } else {
                    this.endDayOfWeek = -this.endDayOfWeek;
                    if (this.endDay > 0) {
                        this.endMode = DOW_GE_DOM_MODE;
                    } else {
                        this.endDay = -this.endDay;
                        this.endMode = DOW_LE_DOM_MODE;
                    }
                }
                if (this.endDayOfWeek > 7) {
                    throw new IllegalArgumentException("Illegal end day of week " + this.endDayOfWeek);
                }
            }
            if (this.endMode == currentSerialVersion) {
                if (this.endDay < -5 || this.endDay > 5) {
                    throw new IllegalArgumentException("Illegal end day of week in month " + this.endDay);
                }
            } else if (this.endDay < STANDARD_TIME || this.endDay > staticMonthLength[this.endMonth]) {
                throw new IllegalArgumentException("Illegal end day " + this.endDay);
            }
        }
    }

    private void makeRulesCompatible() {
        switch (this.startMode) {
            case STANDARD_TIME /*1*/:
                this.startDay = (this.startDay / 7) + STANDARD_TIME;
                this.startDayOfWeek = STANDARD_TIME;
                break;
            case DOW_GE_DOM_MODE /*3*/:
                if (this.startDay != STANDARD_TIME) {
                    this.startDay = (this.startDay / 7) + STANDARD_TIME;
                    break;
                }
                break;
            case DOW_LE_DOM_MODE /*4*/:
                if (this.startDay < 30) {
                    this.startDay = (this.startDay / 7) + STANDARD_TIME;
                    break;
                } else {
                    this.startDay = -1;
                    break;
                }
        }
        switch (this.endMode) {
            case STANDARD_TIME /*1*/:
                this.endDay = (this.endDay / 7) + STANDARD_TIME;
                this.endDayOfWeek = STANDARD_TIME;
                break;
            case DOW_GE_DOM_MODE /*3*/:
                if (this.endDay != STANDARD_TIME) {
                    this.endDay = (this.endDay / 7) + STANDARD_TIME;
                    break;
                }
                break;
            case DOW_LE_DOM_MODE /*4*/:
                if (this.endDay < 30) {
                    this.endDay = (this.endDay / 7) + STANDARD_TIME;
                    break;
                } else {
                    this.endDay = -1;
                    break;
                }
        }
        switch (this.startTimeMode) {
            case currentSerialVersion /*2*/:
                this.startTime += this.rawOffset;
                break;
        }
        while (this.startTime < 0) {
            this.startTime += millisPerDay;
            this.startDayOfWeek = ((this.startDayOfWeek + 5) % 7) + STANDARD_TIME;
        }
        while (this.startTime >= millisPerDay) {
            this.startTime -= millisPerDay;
            this.startDayOfWeek = (this.startDayOfWeek % 7) + STANDARD_TIME;
        }
        switch (this.endTimeMode) {
            case STANDARD_TIME /*1*/:
                this.endTime += this.dstSavings;
                break;
            case currentSerialVersion /*2*/:
                this.endTime += this.rawOffset + this.dstSavings;
                break;
        }
        while (this.endTime < 0) {
            this.endTime += millisPerDay;
            this.endDayOfWeek = ((this.endDayOfWeek + 5) % 7) + STANDARD_TIME;
        }
        while (this.endTime >= millisPerDay) {
            this.endTime -= millisPerDay;
            this.endDayOfWeek = (this.endDayOfWeek % 7) + STANDARD_TIME;
        }
    }

    private byte[] packRules() {
        return new byte[]{(byte) this.startDay, (byte) this.startDayOfWeek, (byte) this.endDay, (byte) this.endDayOfWeek, (byte) this.startTimeMode, (byte) this.endTimeMode};
    }

    private void unpackRules(byte[] rules) {
        this.startDay = rules[WALL_TIME];
        this.startDayOfWeek = rules[STANDARD_TIME];
        this.endDay = rules[currentSerialVersion];
        this.endDayOfWeek = rules[DOW_GE_DOM_MODE];
        if (rules.length >= 6) {
            this.startTimeMode = rules[DOW_LE_DOM_MODE];
            this.endTimeMode = rules[5];
        }
    }

    private int[] packTimes() {
        int[] times = new int[currentSerialVersion];
        times[WALL_TIME] = this.startTime;
        times[STANDARD_TIME] = this.endTime;
        return times;
    }

    private void unpackTimes(int[] times) {
        this.startTime = times[WALL_TIME];
        this.endTime = times[STANDARD_TIME];
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
        if (this.serialVersionOnStream < STANDARD_TIME) {
            if (this.startDayOfWeek == 0) {
                this.startDayOfWeek = STANDARD_TIME;
            }
            if (this.endDayOfWeek == 0) {
                this.endDayOfWeek = STANDARD_TIME;
            }
            this.endMode = currentSerialVersion;
            this.startMode = currentSerialVersion;
            this.dstSavings = millisPerHour;
        } else {
            byte[] rules = new byte[stream.readInt()];
            stream.readFully(rules);
            unpackRules(rules);
        }
        if (this.serialVersionOnStream >= currentSerialVersion) {
            unpackTimes((int[]) stream.readObject());
        }
        this.serialVersionOnStream = currentSerialVersion;
    }
}
