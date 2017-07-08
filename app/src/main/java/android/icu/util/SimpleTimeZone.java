package android.icu.util;

import android.icu.impl.Grego;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

public class SimpleTimeZone extends BasicTimeZone {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int DOM_MODE = 1;
    private static final int DOW_GE_DOM_MODE = 3;
    private static final int DOW_IN_MONTH_MODE = 2;
    private static final int DOW_LE_DOM_MODE = 4;
    public static final int STANDARD_TIME = 1;
    public static final int UTC_TIME = 2;
    public static final int WALL_TIME = 0;
    private static final long serialVersionUID = -7034676239311322769L;
    private static final byte[] staticMonthLength = null;
    private int dst;
    private transient AnnualTimeZoneRule dstRule;
    private int endDay;
    private int endDayOfWeek;
    private int endMode;
    private int endMonth;
    private int endTime;
    private int endTimeMode;
    private transient TimeZoneTransition firstTransition;
    private transient InitialTimeZoneRule initialRule;
    private volatile transient boolean isFrozen;
    private int raw;
    private int startDay;
    private int startDayOfWeek;
    private int startMode;
    private int startMonth;
    private int startTime;
    private int startTimeMode;
    private int startYear;
    private transient AnnualTimeZoneRule stdRule;
    private transient boolean transitionRulesInitialized;
    private boolean useDaylight;
    private STZInfo xinfo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.SimpleTimeZone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.SimpleTimeZone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.SimpleTimeZone.<clinit>():void");
    }

    public SimpleTimeZone(int rawOffset, String ID) {
        super(ID);
        this.dst = Grego.MILLIS_PER_HOUR;
        this.xinfo = null;
        this.isFrozen = -assertionsDisabled;
        construct(rawOffset, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, WALL_TIME, Grego.MILLIS_PER_HOUR);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime) {
        super(ID);
        this.dst = Grego.MILLIS_PER_HOUR;
        this.xinfo = null;
        this.isFrozen = -assertionsDisabled;
        construct(rawOffset, startMonth, startDay, startDayOfWeek, startTime, WALL_TIME, endMonth, endDay, endDayOfWeek, endTime, WALL_TIME, Grego.MILLIS_PER_HOUR);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int startTimeMode, int endMonth, int endDay, int endDayOfWeek, int endTime, int endTimeMode, int dstSavings) {
        super(ID);
        this.dst = Grego.MILLIS_PER_HOUR;
        this.xinfo = null;
        this.isFrozen = -assertionsDisabled;
        construct(rawOffset, startMonth, startDay, startDayOfWeek, startTime, startTimeMode, endMonth, endDay, endDayOfWeek, endTime, endTimeMode, dstSavings);
    }

    public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime, int dstSavings) {
        super(ID);
        this.dst = Grego.MILLIS_PER_HOUR;
        this.xinfo = null;
        this.isFrozen = -assertionsDisabled;
        construct(rawOffset, startMonth, startDay, startDayOfWeek, startTime, WALL_TIME, endMonth, endDay, endDayOfWeek, endTime, WALL_TIME, dstSavings);
    }

    public void setID(String ID) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        super.setID(ID);
        this.transitionRulesInitialized = -assertionsDisabled;
    }

    public void setRawOffset(int offsetMillis) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        this.raw = offsetMillis;
        this.transitionRulesInitialized = -assertionsDisabled;
    }

    public int getRawOffset() {
        return this.raw;
    }

    public void setStartYear(int year) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().sy = year;
        this.startYear = year;
        this.transitionRulesInitialized = -assertionsDisabled;
    }

    public void setStartRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().setStart(month, dayOfWeekInMonth, dayOfWeek, time, -1, -assertionsDisabled);
        setStartRule(month, dayOfWeekInMonth, dayOfWeek, time, (int) WALL_TIME);
    }

    private void setStartRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time, int mode) {
        if (!-assertionsDisabled) {
            if (!(isFrozen() ? -assertionsDisabled : true)) {
                throw new AssertionError();
            }
        }
        this.startMonth = month;
        this.startDay = dayOfWeekInMonth;
        this.startDayOfWeek = dayOfWeek;
        this.startTime = time;
        this.startTimeMode = mode;
        decodeStartRule();
        this.transitionRulesInitialized = -assertionsDisabled;
    }

    public void setStartRule(int month, int dayOfMonth, int time) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().setStart(month, -1, -1, time, dayOfMonth, -assertionsDisabled);
        setStartRule(month, dayOfMonth, (int) WALL_TIME, time, WALL_TIME);
    }

    public void setStartRule(int month, int dayOfMonth, int dayOfWeek, int time, boolean after) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().setStart(month, -1, dayOfWeek, time, dayOfMonth, after);
        setStartRule(month, after ? dayOfMonth : -dayOfMonth, -dayOfWeek, time, (int) WALL_TIME);
    }

    public void setEndRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().setEnd(month, dayOfWeekInMonth, dayOfWeek, time, -1, -assertionsDisabled);
        setEndRule(month, dayOfWeekInMonth, dayOfWeek, time, (int) WALL_TIME);
    }

    public void setEndRule(int month, int dayOfMonth, int time) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().setEnd(month, -1, -1, time, dayOfMonth, -assertionsDisabled);
        setEndRule(month, dayOfMonth, WALL_TIME, time);
    }

    public void setEndRule(int month, int dayOfMonth, int dayOfWeek, int time, boolean after) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        }
        getSTZInfo().setEnd(month, -1, dayOfWeek, time, dayOfMonth, after);
        setEndRule(month, dayOfMonth, dayOfWeek, time, WALL_TIME, after);
    }

    private void setEndRule(int month, int dayOfMonth, int dayOfWeek, int time, int mode, boolean after) {
        if (!-assertionsDisabled) {
            if ((isFrozen() ? null : STANDARD_TIME) == null) {
                throw new AssertionError();
            }
        }
        setEndRule(month, after ? dayOfMonth : -dayOfMonth, -dayOfWeek, time, mode);
    }

    private void setEndRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time, int mode) {
        if (!-assertionsDisabled) {
            if (!(isFrozen() ? -assertionsDisabled : true)) {
                throw new AssertionError();
            }
        }
        this.endMonth = month;
        this.endDay = dayOfWeekInMonth;
        this.endDayOfWeek = dayOfWeek;
        this.endTime = time;
        this.endTimeMode = mode;
        decodeEndRule();
        this.transitionRulesInitialized = -assertionsDisabled;
    }

    public void setDSTSavings(int millisSavedDuringDST) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        } else if (millisSavedDuringDST <= 0) {
            throw new IllegalArgumentException();
        } else {
            this.dst = millisSavedDuringDST;
            this.transitionRulesInitialized = -assertionsDisabled;
        }
    }

    public int getDSTSavings() {
        return this.dst;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.xinfo != null) {
            this.xinfo.applyTo(this);
        }
    }

    public String toString() {
        return "SimpleTimeZone: " + getID();
    }

    private STZInfo getSTZInfo() {
        if (this.xinfo == null) {
            this.xinfo = new STZInfo();
        }
        return this.xinfo;
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException();
        }
        return getOffset(era, year, month, day, dayOfWeek, millis, Grego.monthLength(year, month));
    }

    @Deprecated
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis, int monthLength) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException();
        }
        return getOffset(era, year, month, day, dayOfWeek, millis, Grego.monthLength(year, month), Grego.previousMonthLength(year, month));
    }

    private int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis, int monthLength, int prevMonthLength) {
        if ((era == STANDARD_TIME || era == 0) && month >= 0 && month <= 11 && day >= STANDARD_TIME && day <= monthLength && dayOfWeek >= STANDARD_TIME && dayOfWeek <= 7 && millis >= 0 && millis < 86400000 && monthLength >= 28 && monthLength <= 31 && prevMonthLength >= 28 && prevMonthLength <= 31) {
            int result = this.raw;
            if (!this.useDaylight || year < this.startYear || era != STANDARD_TIME) {
                return result;
            }
            boolean southern = this.startMonth > this.endMonth ? true : -assertionsDisabled;
            int startCompare = compareToRule(month, monthLength, prevMonthLength, day, dayOfWeek, millis, this.startTimeMode == UTC_TIME ? -this.raw : WALL_TIME, this.startMode, this.startMonth, this.startDayOfWeek, this.startDay, this.startTime);
            int endCompare = WALL_TIME;
            if (southern != (startCompare >= 0 ? true : -assertionsDisabled)) {
                int i = this.endTimeMode == 0 ? this.dst : this.endTimeMode == UTC_TIME ? -this.raw : WALL_TIME;
                endCompare = compareToRule(month, monthLength, prevMonthLength, day, dayOfWeek, millis, i, this.endMode, this.endMonth, this.endDayOfWeek, this.endDay, this.endTime);
            }
            if (southern || startCompare < 0 || endCompare >= 0) {
                if (southern) {
                    if (startCompare < 0) {
                        if (endCompare < 0) {
                        }
                    }
                }
                return result;
            }
            result += this.dst;
            return result;
        }
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void getOffsetFromLocal(long date, int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        offsets[WALL_TIME] = getRawOffset();
        int[] fields = new int[6];
        Grego.timeToFields(date, fields);
        offsets[STANDARD_TIME] = getOffset(STANDARD_TIME, fields[WALL_TIME], fields[STANDARD_TIME], fields[UTC_TIME], fields[DOW_GE_DOM_MODE], fields[5]) - offsets[WALL_TIME];
        boolean recalc = -assertionsDisabled;
        if (offsets[STANDARD_TIME] > 0) {
            if ((nonExistingTimeOpt & DOW_GE_DOM_MODE) == STANDARD_TIME || !((nonExistingTimeOpt & DOW_GE_DOM_MODE) == DOW_GE_DOM_MODE || (nonExistingTimeOpt & 12) == 12)) {
                date -= (long) getDSTSavings();
                recalc = true;
            }
        } else if ((duplicatedTimeOpt & DOW_GE_DOM_MODE) == DOW_GE_DOM_MODE || ((duplicatedTimeOpt & DOW_GE_DOM_MODE) != STANDARD_TIME && (duplicatedTimeOpt & 12) == DOW_LE_DOM_MODE)) {
            date -= (long) getDSTSavings();
            recalc = true;
        }
        if (recalc) {
            Grego.timeToFields(date, fields);
            offsets[STANDARD_TIME] = getOffset(STANDARD_TIME, fields[WALL_TIME], fields[STANDARD_TIME], fields[UTC_TIME], fields[DOW_GE_DOM_MODE], fields[5]) - offsets[WALL_TIME];
        }
    }

    private int compareToRule(int month, int monthLen, int prevMonthLen, int dayOfMonth, int dayOfWeek, int millis, int millisDelta, int ruleMode, int ruleMonth, int ruleDayOfWeek, int ruleDay, int ruleMillis) {
        millis += millisDelta;
        while (millis >= Grego.MILLIS_PER_DAY) {
            millis -= Grego.MILLIS_PER_DAY;
            dayOfMonth += STANDARD_TIME;
            dayOfWeek = (dayOfWeek % 7) + STANDARD_TIME;
            if (dayOfMonth > monthLen) {
                dayOfMonth = STANDARD_TIME;
                month += STANDARD_TIME;
            }
        }
        while (millis < 0) {
            dayOfMonth--;
            dayOfWeek = ((dayOfWeek + 5) % 7) + STANDARD_TIME;
            if (dayOfMonth < STANDARD_TIME) {
                dayOfMonth = prevMonthLen;
                month--;
            }
            millis += Grego.MILLIS_PER_DAY;
        }
        if (month < ruleMonth) {
            return -1;
        }
        if (month > ruleMonth) {
            return STANDARD_TIME;
        }
        int ruleDayOfMonth = WALL_TIME;
        if (ruleDay > monthLen) {
            ruleDay = monthLen;
        }
        switch (ruleMode) {
            case STANDARD_TIME /*1*/:
                ruleDayOfMonth = ruleDay;
                break;
            case UTC_TIME /*2*/:
                if (ruleDay <= 0) {
                    ruleDayOfMonth = (((ruleDay + STANDARD_TIME) * 7) + monthLen) - (((((dayOfWeek + monthLen) - dayOfMonth) + 7) - ruleDayOfWeek) % 7);
                    break;
                }
                ruleDayOfMonth = (((ruleDay - 1) * 7) + STANDARD_TIME) + (((ruleDayOfWeek + 7) - ((dayOfWeek - dayOfMonth) + STANDARD_TIME)) % 7);
                break;
            case DOW_GE_DOM_MODE /*3*/:
                ruleDayOfMonth = ruleDay + (((((ruleDayOfWeek + 49) - ruleDay) - dayOfWeek) + dayOfMonth) % 7);
                break;
            case DOW_LE_DOM_MODE /*4*/:
                ruleDayOfMonth = ruleDay - (((((49 - ruleDayOfWeek) + ruleDay) + dayOfWeek) - dayOfMonth) % 7);
                break;
        }
        if (dayOfMonth < ruleDayOfMonth) {
            return -1;
        }
        if (dayOfMonth > ruleDayOfMonth) {
            return STANDARD_TIME;
        }
        if (millis < ruleMillis) {
            return -1;
        }
        if (millis > ruleMillis) {
            return STANDARD_TIME;
        }
        return WALL_TIME;
    }

    public boolean useDaylightTime() {
        return this.useDaylight;
    }

    public boolean observesDaylightTime() {
        return this.useDaylight;
    }

    public boolean inDaylightTime(Date date) {
        GregorianCalendar gc = new GregorianCalendar((TimeZone) this);
        gc.setTime(date);
        return gc.inDaylightTime();
    }

    private void construct(int _raw, int _startMonth, int _startDay, int _startDayOfWeek, int _startTime, int _startTimeMode, int _endMonth, int _endDay, int _endDayOfWeek, int _endTime, int _endTimeMode, int _dst) {
        this.raw = _raw;
        this.startMonth = _startMonth;
        this.startDay = _startDay;
        this.startDayOfWeek = _startDayOfWeek;
        this.startTime = _startTime;
        this.startTimeMode = _startTimeMode;
        this.endMonth = _endMonth;
        this.endDay = _endDay;
        this.endDayOfWeek = _endDayOfWeek;
        this.endTime = _endTime;
        this.endTimeMode = _endTimeMode;
        this.dst = _dst;
        this.startYear = WALL_TIME;
        this.startMode = STANDARD_TIME;
        this.endMode = STANDARD_TIME;
        decodeRules();
        if (_dst <= 0) {
            throw new IllegalArgumentException();
        }
    }

    private void decodeRules() {
        decodeStartRule();
        decodeEndRule();
    }

    private void decodeStartRule() {
        boolean z = -assertionsDisabled;
        if (!(this.startDay == 0 || this.endDay == 0)) {
            z = true;
        }
        this.useDaylight = z;
        if (this.useDaylight && this.dst == 0) {
            this.dst = Grego.MILLIS_PER_DAY;
        }
        if (this.startDay == 0) {
            return;
        }
        if (this.startMonth < 0 || this.startMonth > 11) {
            throw new IllegalArgumentException();
        } else if (this.startTime < 0 || this.startTime > Grego.MILLIS_PER_DAY || this.startTimeMode < 0 || this.startTimeMode > UTC_TIME) {
            throw new IllegalArgumentException();
        } else {
            if (this.startDayOfWeek == 0) {
                this.startMode = STANDARD_TIME;
            } else {
                if (this.startDayOfWeek > 0) {
                    this.startMode = UTC_TIME;
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
                    throw new IllegalArgumentException();
                }
            }
            if (this.startMode == UTC_TIME) {
                if (this.startDay < -5 || this.startDay > 5) {
                    throw new IllegalArgumentException();
                }
            } else if (this.startDay < STANDARD_TIME || this.startDay > staticMonthLength[this.startMonth]) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void decodeEndRule() {
        boolean z = -assertionsDisabled;
        if (!(this.startDay == 0 || this.endDay == 0)) {
            z = true;
        }
        this.useDaylight = z;
        if (this.useDaylight && this.dst == 0) {
            this.dst = Grego.MILLIS_PER_DAY;
        }
        if (this.endDay == 0) {
            return;
        }
        if (this.endMonth < 0 || this.endMonth > 11) {
            throw new IllegalArgumentException();
        } else if (this.endTime < 0 || this.endTime > Grego.MILLIS_PER_DAY || this.endTimeMode < 0 || this.endTimeMode > UTC_TIME) {
            throw new IllegalArgumentException();
        } else {
            if (this.endDayOfWeek == 0) {
                this.endMode = STANDARD_TIME;
            } else {
                if (this.endDayOfWeek > 0) {
                    this.endMode = UTC_TIME;
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
                    throw new IllegalArgumentException();
                }
            }
            if (this.endMode == UTC_TIME) {
                if (this.endDay < -5 || this.endDay > 5) {
                    throw new IllegalArgumentException();
                }
            } else if (this.endDay < STANDARD_TIME || this.endDay > staticMonthLength[this.endMonth]) {
                throw new IllegalArgumentException();
            }
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return -assertionsDisabled;
        }
        SimpleTimeZone that = (SimpleTimeZone) obj;
        if (this.raw != that.raw || this.useDaylight != that.useDaylight || !idEquals(getID(), that.getID())) {
            z = -assertionsDisabled;
        } else if (this.useDaylight) {
            if (this.dst != that.dst || this.startMode != that.startMode || this.startMonth != that.startMonth || this.startDay != that.startDay || this.startDayOfWeek != that.startDayOfWeek || this.startTime != that.startTime || this.startTimeMode != that.startTimeMode || this.endMode != that.endMode || this.endMonth != that.endMonth || this.endDay != that.endDay || this.endDayOfWeek != that.endDayOfWeek || this.endTime != that.endTime || this.endTimeMode != that.endTimeMode) {
                z = -assertionsDisabled;
            } else if (this.startYear != that.startYear) {
                z = -assertionsDisabled;
            }
        }
        return z;
    }

    private boolean idEquals(String id1, String id2) {
        if (id1 == null && id2 == null) {
            return true;
        }
        if (id1 == null || id2 == null) {
            return -assertionsDisabled;
        }
        return id1.equals(id2);
    }

    public int hashCode() {
        int ret = (this.raw + super.hashCode()) ^ ((this.useDaylight ? WALL_TIME : STANDARD_TIME) + (this.raw >>> 8));
        return !this.useDaylight ? ret + ((((((((((((((this.dst ^ ((this.dst >>> 10) + this.startMode)) ^ ((this.startMode >>> 11) + this.startMonth)) ^ ((this.startMonth >>> 12) + this.startDay)) ^ ((this.startDay >>> 13) + this.startDayOfWeek)) ^ ((this.startDayOfWeek >>> 14) + this.startTime)) ^ ((this.startTime >>> 15) + this.startTimeMode)) ^ ((this.startTimeMode >>> 16) + this.endMode)) ^ ((this.endMode >>> 17) + this.endMonth)) ^ ((this.endMonth >>> 18) + this.endDay)) ^ ((this.endDay >>> 19) + this.endDayOfWeek)) ^ ((this.endDayOfWeek >>> 20) + this.endTime)) ^ ((this.endTime >>> 21) + this.endTimeMode)) ^ ((this.endTimeMode >>> 22) + this.startYear)) ^ (this.startYear >>> 23)) : ret;
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    public boolean hasSameRules(TimeZone othr) {
        boolean z = true;
        if (this == othr) {
            return true;
        }
        if (!(othr instanceof SimpleTimeZone)) {
            return -assertionsDisabled;
        }
        SimpleTimeZone other = (SimpleTimeZone) othr;
        if (other == null || this.raw != other.raw || this.useDaylight != other.useDaylight) {
            z = -assertionsDisabled;
        } else if (this.useDaylight) {
            if (this.dst != other.dst || this.startMode != other.startMode || this.startMonth != other.startMonth || this.startDay != other.startDay || this.startDayOfWeek != other.startDayOfWeek || this.startTime != other.startTime || this.startTimeMode != other.startTimeMode || this.endMode != other.endMode || this.endMonth != other.endMonth || this.endDay != other.endDay || this.endDayOfWeek != other.endDayOfWeek || this.endTime != other.endTime || this.endTimeMode != other.endTimeMode) {
                z = -assertionsDisabled;
            } else if (this.startYear != other.startYear) {
                z = -assertionsDisabled;
            }
        }
        return z;
    }

    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        if (!this.useDaylight) {
            return null;
        }
        initTransitionRules();
        long firstTransitionTime = this.firstTransition.getTime();
        if (base < firstTransitionTime || (inclusive && base == firstTransitionTime)) {
            return this.firstTransition;
        }
        Date stdDate = this.stdRule.getNextStart(base, this.dstRule.getRawOffset(), this.dstRule.getDSTSavings(), inclusive);
        Date dstDate = this.dstRule.getNextStart(base, this.stdRule.getRawOffset(), this.stdRule.getDSTSavings(), inclusive);
        if (stdDate != null && (dstDate == null || stdDate.before(dstDate))) {
            return new TimeZoneTransition(stdDate.getTime(), this.dstRule, this.stdRule);
        }
        if (dstDate == null || (stdDate != null && !dstDate.before(stdDate))) {
            return null;
        }
        return new TimeZoneTransition(dstDate.getTime(), this.stdRule, this.dstRule);
    }

    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        if (!this.useDaylight) {
            return null;
        }
        initTransitionRules();
        long firstTransitionTime = this.firstTransition.getTime();
        if (base < firstTransitionTime || (!inclusive && base == firstTransitionTime)) {
            return null;
        }
        Date stdDate = this.stdRule.getPreviousStart(base, this.dstRule.getRawOffset(), this.dstRule.getDSTSavings(), inclusive);
        Date dstDate = this.dstRule.getPreviousStart(base, this.stdRule.getRawOffset(), this.stdRule.getDSTSavings(), inclusive);
        if (stdDate != null && (dstDate == null || stdDate.after(dstDate))) {
            return new TimeZoneTransition(stdDate.getTime(), this.dstRule, this.stdRule);
        }
        if (dstDate == null || (stdDate != null && !dstDate.after(stdDate))) {
            return null;
        }
        return new TimeZoneTransition(dstDate.getTime(), this.stdRule, this.dstRule);
    }

    public TimeZoneRule[] getTimeZoneRules() {
        int size;
        initTransitionRules();
        if (this.useDaylight) {
            size = DOW_GE_DOM_MODE;
        } else {
            size = STANDARD_TIME;
        }
        TimeZoneRule[] rules = new TimeZoneRule[size];
        rules[WALL_TIME] = this.initialRule;
        if (this.useDaylight) {
            rules[STANDARD_TIME] = this.stdRule;
            rules[UTC_TIME] = this.dstRule;
        }
        return rules;
    }

    private synchronized void initTransitionRules() {
        if (!this.transitionRulesInitialized) {
            if (this.useDaylight) {
                DateTimeRule dateTimeRule;
                DateTimeRule dtRule = null;
                int timeRuleType = this.startTimeMode == STANDARD_TIME ? STANDARD_TIME : this.startTimeMode == UTC_TIME ? UTC_TIME : WALL_TIME;
                switch (this.startMode) {
                    case STANDARD_TIME /*1*/:
                        dtRule = new DateTimeRule(this.startMonth, this.startDay, this.startTime, timeRuleType);
                        break;
                    case UTC_TIME /*2*/:
                        dtRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, this.startTime, timeRuleType);
                        break;
                    case DOW_GE_DOM_MODE /*3*/:
                        dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, true, this.startTime, timeRuleType);
                        break;
                    case DOW_LE_DOM_MODE /*4*/:
                        dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, -assertionsDisabled, this.startTime, timeRuleType);
                        break;
                }
                this.dstRule = new AnnualTimeZoneRule(getID() + "(DST)", getRawOffset(), getDSTSavings(), dtRule, this.startYear, AnnualTimeZoneRule.MAX_YEAR);
                long firstDstStart = this.dstRule.getFirstStart(getRawOffset(), WALL_TIME).getTime();
                timeRuleType = this.endTimeMode == STANDARD_TIME ? STANDARD_TIME : this.endTimeMode == UTC_TIME ? UTC_TIME : WALL_TIME;
                switch (this.endMode) {
                    case STANDARD_TIME /*1*/:
                        dtRule = new DateTimeRule(this.endMonth, this.endDay, this.endTime, timeRuleType);
                        break;
                    case UTC_TIME /*2*/:
                        dtRule = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, this.endTime, timeRuleType);
                        break;
                    case DOW_GE_DOM_MODE /*3*/:
                        dateTimeRule = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, true, this.endTime, timeRuleType);
                        break;
                    case DOW_LE_DOM_MODE /*4*/:
                        dateTimeRule = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, -assertionsDisabled, this.endTime, timeRuleType);
                        break;
                }
                this.stdRule = new AnnualTimeZoneRule(getID() + "(STD)", getRawOffset(), WALL_TIME, dtRule, this.startYear, AnnualTimeZoneRule.MAX_YEAR);
                long firstStdStart = this.stdRule.getFirstStart(getRawOffset(), this.dstRule.getDSTSavings()).getTime();
                if (firstStdStart < firstDstStart) {
                    this.initialRule = new InitialTimeZoneRule(getID() + "(DST)", getRawOffset(), this.dstRule.getDSTSavings());
                    this.firstTransition = new TimeZoneTransition(firstStdStart, this.initialRule, this.stdRule);
                } else {
                    this.initialRule = new InitialTimeZoneRule(getID() + "(STD)", getRawOffset(), WALL_TIME);
                    this.firstTransition = new TimeZoneTransition(firstDstStart, this.initialRule, this.dstRule);
                }
            } else {
                this.initialRule = new InitialTimeZoneRule(getID(), getRawOffset(), WALL_TIME);
            }
            this.transitionRulesInitialized = true;
        }
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public TimeZone freeze() {
        this.isFrozen = true;
        return this;
    }

    public TimeZone cloneAsThawed() {
        SimpleTimeZone tz = (SimpleTimeZone) super.cloneAsThawed();
        tz.isFrozen = -assertionsDisabled;
        return tz;
    }
}
