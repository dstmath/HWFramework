package android.icu.util;

import android.icu.impl.Grego;
import android.icu.lang.UCharacterEnums;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

public class SimpleTimeZone extends BasicTimeZone {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int DOM_MODE = 1;
    private static final int DOW_GE_DOM_MODE = 3;
    private static final int DOW_IN_MONTH_MODE = 2;
    private static final int DOW_LE_DOM_MODE = 4;
    public static final int STANDARD_TIME = 1;
    public static final int UTC_TIME = 2;
    public static final int WALL_TIME = 0;
    private static final long serialVersionUID = -7034676239311322769L;
    private static final byte[] staticMonthLength = {31, 29, 31, UCharacterEnums.ECharacterCategory.CHAR_CATEGORY_COUNT, 31, UCharacterEnums.ECharacterCategory.CHAR_CATEGORY_COUNT, 31, 31, UCharacterEnums.ECharacterCategory.CHAR_CATEGORY_COUNT, 31, UCharacterEnums.ECharacterCategory.CHAR_CATEGORY_COUNT, 31};
    private int dst = 3600000;
    private transient AnnualTimeZoneRule dstRule;
    private int endDay;
    private int endDayOfWeek;
    private int endMode;
    private int endMonth;
    private int endTime;
    private int endTimeMode;
    private transient TimeZoneTransition firstTransition;
    private transient InitialTimeZoneRule initialRule;
    private volatile transient boolean isFrozen = false;
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
    private STZInfo xinfo = null;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleTimeZone(int rawOffset, String ID) {
        super(ID);
        construct(rawOffset, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3600000);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleTimeZone(int rawOffset, String ID, int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, int endMonth2, int endDay2, int endDayOfWeek2, int endTime2) {
        super(ID);
        construct(rawOffset, startMonth2, startDay2, startDayOfWeek2, startTime2, 0, endMonth2, endDay2, endDayOfWeek2, endTime2, 0, 3600000);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleTimeZone(int rawOffset, String ID, int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, int startTimeMode2, int endMonth2, int endDay2, int endDayOfWeek2, int endTime2, int endTimeMode2, int dstSavings) {
        super(ID);
        construct(rawOffset, startMonth2, startDay2, startDayOfWeek2, startTime2, startTimeMode2, endMonth2, endDay2, endDayOfWeek2, endTime2, endTimeMode2, dstSavings);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleTimeZone(int rawOffset, String ID, int startMonth2, int startDay2, int startDayOfWeek2, int startTime2, int endMonth2, int endDay2, int endDayOfWeek2, int endTime2, int dstSavings) {
        super(ID);
        construct(rawOffset, startMonth2, startDay2, startDayOfWeek2, startTime2, 0, endMonth2, endDay2, endDayOfWeek2, endTime2, 0, dstSavings);
    }

    public void setID(String ID) {
        if (!isFrozen()) {
            super.setID(ID);
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setRawOffset(int offsetMillis) {
        if (!isFrozen()) {
            this.raw = offsetMillis;
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public int getRawOffset() {
        return this.raw;
    }

    public void setStartYear(int year) {
        if (!isFrozen()) {
            getSTZInfo().sy = year;
            this.startYear = year;
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setStartRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time) {
        if (!isFrozen()) {
            getSTZInfo().setStart(month, dayOfWeekInMonth, dayOfWeek, time, -1, false);
            setStartRule(month, dayOfWeekInMonth, dayOfWeek, time, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    private void setStartRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time, int mode) {
        this.startMonth = month;
        this.startDay = dayOfWeekInMonth;
        this.startDayOfWeek = dayOfWeek;
        this.startTime = time;
        this.startTimeMode = mode;
        decodeStartRule();
        this.transitionRulesInitialized = false;
    }

    public void setStartRule(int month, int dayOfMonth, int time) {
        if (!isFrozen()) {
            getSTZInfo().setStart(month, -1, -1, time, dayOfMonth, false);
            setStartRule(month, dayOfMonth, 0, time, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setStartRule(int month, int dayOfMonth, int dayOfWeek, int time, boolean after) {
        if (!isFrozen()) {
            getSTZInfo().setStart(month, -1, dayOfWeek, time, dayOfMonth, after);
            setStartRule(month, after ? dayOfMonth : -dayOfMonth, -dayOfWeek, time, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setEndRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time) {
        if (!isFrozen()) {
            getSTZInfo().setEnd(month, dayOfWeekInMonth, dayOfWeek, time, -1, false);
            setEndRule(month, dayOfWeekInMonth, dayOfWeek, time, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setEndRule(int month, int dayOfMonth, int time) {
        if (!isFrozen()) {
            getSTZInfo().setEnd(month, -1, -1, time, dayOfMonth, false);
            setEndRule(month, dayOfMonth, 0, time);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setEndRule(int month, int dayOfMonth, int dayOfWeek, int time, boolean after) {
        if (!isFrozen()) {
            int i = dayOfMonth;
            getSTZInfo().setEnd(month, -1, dayOfWeek, time, i, after);
            setEndRule(month, i, dayOfWeek, time, 0, after);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    private void setEndRule(int month, int dayOfMonth, int dayOfWeek, int time, int mode, boolean after) {
        setEndRule(month, after ? dayOfMonth : -dayOfMonth, -dayOfWeek, time, mode);
    }

    private void setEndRule(int month, int dayOfWeekInMonth, int dayOfWeek, int time, int mode) {
        this.endMonth = month;
        this.endDay = dayOfWeekInMonth;
        this.endDayOfWeek = dayOfWeek;
        this.endTime = time;
        this.endTimeMode = mode;
        decodeEndRule();
        this.transitionRulesInitialized = false;
    }

    public void setDSTSavings(int millisSavedDuringDST) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        } else if (millisSavedDuringDST > 0) {
            this.dst = millisSavedDuringDST;
            this.transitionRulesInitialized = false;
        } else {
            throw new IllegalArgumentException();
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
        int i = month;
        if (i < 0 || i > 11) {
            throw new IllegalArgumentException();
        }
        return getOffset(era, year, i, day, dayOfWeek, millis, Grego.monthLength(year, i), Grego.previousMonthLength(year, i));
    }

    private int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis, int monthLength, int prevMonthLength) {
        int result;
        int result2;
        int i;
        int i2;
        int i3 = era;
        int i4 = month;
        int i5 = day;
        int i6 = dayOfWeek;
        int result3 = millis;
        int i7 = monthLength;
        int i8 = prevMonthLength;
        if ((i3 == 1 || i3 == 0) && i4 >= 0 && i4 <= 11 && i5 >= 1 && i5 <= i7 && i6 >= 1 && i6 <= 7 && result3 >= 0 && result3 < 86400000 && i7 >= 28 && i7 <= 31 && i8 >= 28 && i8 <= 31) {
            int result4 = this.raw;
            if (!this.useDaylight || year < this.startYear) {
                result = result4;
            } else if (i3 != 1) {
                result = result4;
            } else {
                boolean southern = this.startMonth > this.endMonth;
                boolean southern2 = southern;
                int result5 = result4;
                int startCompare = compareToRule(i4, i7, i8, i5, i6, result3, this.startTimeMode == 2 ? -this.raw : 0, this.startMode, this.startMonth, this.startDayOfWeek, this.startDay, this.startTime);
                int endCompare = 0;
                if (southern2 != (startCompare >= 0)) {
                    if (this.endTimeMode == 0) {
                        i2 = this.dst;
                    } else if (this.endTimeMode == 2) {
                        i2 = -this.raw;
                    } else {
                        i = 0;
                        endCompare = compareToRule(month, monthLength, prevMonthLength, day, dayOfWeek, millis, i, this.endMode, this.endMonth, this.endDayOfWeek, this.endDay, this.endTime);
                    }
                    i = i2;
                    endCompare = compareToRule(month, monthLength, prevMonthLength, day, dayOfWeek, millis, i, this.endMode, this.endMonth, this.endDayOfWeek, this.endDay, this.endTime);
                }
                if ((southern2 || startCompare < 0 || endCompare >= 0) && (!southern2 || (startCompare < 0 && endCompare >= 0))) {
                    result2 = result5;
                } else {
                    result2 = result5 + this.dst;
                }
                return result2;
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    @Deprecated
    public void getOffsetFromLocal(long date, int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        long date2 = date;
        offsets[0] = getRawOffset();
        int[] fields = new int[6];
        Grego.timeToFields(date2, fields);
        offsets[1] = getOffset(1, fields[0], fields[1], fields[2], fields[3], fields[5]) - offsets[0];
        boolean recalc = false;
        if (offsets[1] > 0) {
            if ((nonExistingTimeOpt & 3) == 1 || !((nonExistingTimeOpt & 3) == 3 || (nonExistingTimeOpt & 12) == 12)) {
                date2 -= (long) getDSTSavings();
                recalc = true;
            }
        } else if ((duplicatedTimeOpt & 3) == 3 || ((duplicatedTimeOpt & 3) != 1 && (duplicatedTimeOpt & 12) == 4)) {
            date2 -= (long) getDSTSavings();
            recalc = true;
        }
        if (recalc) {
            Grego.timeToFields(date2, fields);
            offsets[1] = getOffset(1, fields[0], fields[1], fields[2], fields[3], fields[5]) - offsets[0];
        }
    }

    private int compareToRule(int month, int monthLen, int prevMonthLen, int dayOfMonth, int dayOfWeek, int millis, int millisDelta, int ruleMode, int ruleMonth, int ruleDayOfWeek, int ruleDay, int ruleMillis) {
        int i = monthLen;
        int i2 = ruleMonth;
        int i3 = ruleMillis;
        int millis2 = millis + millisDelta;
        int month2 = month;
        int dayOfMonth2 = dayOfMonth;
        int dayOfWeek2 = dayOfWeek;
        while (millis2 >= 86400000) {
            millis2 -= Grego.MILLIS_PER_DAY;
            dayOfMonth2++;
            dayOfWeek2 = 1 + (dayOfWeek2 % 7);
            if (dayOfMonth2 > i) {
                dayOfMonth2 = 1;
                month2++;
            }
        }
        while (millis2 < 0) {
            int dayOfMonth3 = dayOfMonth2 - 1;
            dayOfWeek2 = 1 + ((dayOfWeek2 + 5) % 7);
            if (dayOfMonth3 < 1) {
                dayOfMonth3 = prevMonthLen;
                month2--;
            }
            millis2 += Grego.MILLIS_PER_DAY;
        }
        if (month2 < i2) {
            return -1;
        }
        if (month2 > i2) {
            return 1;
        }
        int ruleDayOfMonth = 0;
        int ruleDay2 = ruleDay;
        if (ruleDay2 > i) {
            ruleDay2 = i;
        }
        switch (ruleMode) {
            case 1:
                ruleDayOfMonth = ruleDay2;
                break;
            case 2:
                if (ruleDay2 <= 0) {
                    ruleDayOfMonth = (((ruleDay2 + 1) * 7) + i) - (((((dayOfWeek2 + i) - dayOfMonth2) + 7) - ruleDayOfWeek) % 7);
                    break;
                } else {
                    ruleDayOfMonth = ((ruleDay2 - 1) * 7) + 1 + (((7 + ruleDayOfWeek) - ((dayOfWeek2 - dayOfMonth2) + 1)) % 7);
                    break;
                }
            case 3:
                ruleDayOfMonth = ruleDay2 + (((((49 + ruleDayOfWeek) - ruleDay2) - dayOfWeek2) + dayOfMonth2) % 7);
                break;
            case 4:
                ruleDayOfMonth = ruleDay2 - (((((49 - ruleDayOfWeek) + ruleDay2) + dayOfWeek2) - dayOfMonth2) % 7);
                break;
        }
        if (dayOfMonth2 < ruleDayOfMonth) {
            return -1;
        }
        if (dayOfMonth2 > ruleDayOfMonth) {
            return 1;
        }
        if (millis2 < i3) {
            return -1;
        }
        if (millis2 > i3) {
            return 1;
        }
        return 0;
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
        this.startYear = 0;
        this.startMode = 1;
        this.endMode = 1;
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
        this.useDaylight = (this.startDay == 0 || this.endDay == 0) ? false : true;
        if (this.useDaylight && this.dst == 0) {
            this.dst = Grego.MILLIS_PER_DAY;
        }
        if (this.startDay == 0) {
            return;
        }
        if (this.startMonth < 0 || this.startMonth > 11) {
            throw new IllegalArgumentException();
        } else if (this.startTime < 0 || this.startTime > 86400000 || this.startTimeMode < 0 || this.startTimeMode > 2) {
            throw new IllegalArgumentException();
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
                    throw new IllegalArgumentException();
                }
            }
            if (this.startMode == 2) {
                if (this.startDay < -5 || this.startDay > 5) {
                    throw new IllegalArgumentException();
                }
            } else if (this.startDay < 1 || this.startDay > staticMonthLength[this.startMonth]) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void decodeEndRule() {
        this.useDaylight = (this.startDay == 0 || this.endDay == 0) ? false : true;
        if (this.useDaylight && this.dst == 0) {
            this.dst = Grego.MILLIS_PER_DAY;
        }
        if (this.endDay == 0) {
            return;
        }
        if (this.endMonth < 0 || this.endMonth > 11) {
            throw new IllegalArgumentException();
        } else if (this.endTime < 0 || this.endTime > 86400000 || this.endTimeMode < 0 || this.endTimeMode > 2) {
            throw new IllegalArgumentException();
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
                    throw new IllegalArgumentException();
                }
            }
            if (this.endMode == 2) {
                if (this.endDay < -5 || this.endDay > 5) {
                    throw new IllegalArgumentException();
                }
            } else if (this.endDay < 1 || this.endDay > staticMonthLength[this.endMonth]) {
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
            return false;
        }
        SimpleTimeZone that = (SimpleTimeZone) obj;
        if (this.raw != that.raw || this.useDaylight != that.useDaylight || !idEquals(getID(), that.getID()) || (this.useDaylight && !(this.dst == that.dst && this.startMode == that.startMode && this.startMonth == that.startMonth && this.startDay == that.startDay && this.startDayOfWeek == that.startDayOfWeek && this.startTime == that.startTime && this.startTimeMode == that.startTimeMode && this.endMode == that.endMode && this.endMonth == that.endMonth && this.endDay == that.endDay && this.endDayOfWeek == that.endDayOfWeek && this.endTime == that.endTime && this.endTimeMode == that.endTimeMode && this.startYear == that.startYear))) {
            z = false;
        }
        return z;
    }

    private boolean idEquals(String id1, String id2) {
        if (id1 == null && id2 == null) {
            return true;
        }
        if (id1 == null || id2 == null) {
            return false;
        }
        return id1.equals(id2);
    }

    public int hashCode() {
        int ret = (super.hashCode() + this.raw) ^ ((this.raw >>> 8) + (this.useDaylight ^ true ? 1 : 0));
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
            return false;
        }
        SimpleTimeZone other = (SimpleTimeZone) othr;
        if (other == null || this.raw != other.raw || this.useDaylight != other.useDaylight || (this.useDaylight && !(this.dst == other.dst && this.startMode == other.startMode && this.startMonth == other.startMonth && this.startDay == other.startDay && this.startDayOfWeek == other.startDayOfWeek && this.startTime == other.startTime && this.startTimeMode == other.startTimeMode && this.endMode == other.endMode && this.endMonth == other.endMonth && this.endDay == other.endDay && this.endDayOfWeek == other.endDayOfWeek && this.endTime == other.endTime && this.endTimeMode == other.endTimeMode && this.startYear == other.startYear))) {
            z = false;
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
        boolean z = inclusive;
        Date stdDate = this.stdRule.getNextStart(base, this.dstRule.getRawOffset(), this.dstRule.getDSTSavings(), z);
        Date dstDate = this.dstRule.getNextStart(base, this.stdRule.getRawOffset(), this.stdRule.getDSTSavings(), z);
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
        boolean z = inclusive;
        Date stdDate = this.stdRule.getPreviousStart(base, this.dstRule.getRawOffset(), this.dstRule.getDSTSavings(), z);
        Date dstDate = this.dstRule.getPreviousStart(base, this.stdRule.getRawOffset(), this.stdRule.getDSTSavings(), z);
        if (stdDate != null && (dstDate == null || stdDate.after(dstDate))) {
            return new TimeZoneTransition(stdDate.getTime(), this.dstRule, this.stdRule);
        }
        if (dstDate == null || (stdDate != null && !dstDate.after(stdDate))) {
            return null;
        }
        return new TimeZoneTransition(dstDate.getTime(), this.stdRule, this.dstRule);
    }

    public TimeZoneRule[] getTimeZoneRules() {
        initTransitionRules();
        TimeZoneRule[] rules = new TimeZoneRule[(this.useDaylight ? 3 : 1)];
        rules[0] = this.initialRule;
        if (this.useDaylight) {
            rules[1] = this.stdRule;
            rules[2] = this.dstRule;
        }
        return rules;
    }

    private synchronized void initTransitionRules() {
        if (!this.transitionRulesInitialized) {
            if (this.useDaylight) {
                DateTimeRule dtRule = null;
                int i = 2;
                int timeRuleType = this.startTimeMode == 1 ? 1 : this.startTimeMode == 2 ? 2 : 0;
                switch (this.startMode) {
                    case 1:
                        dtRule = new DateTimeRule(this.startMonth, this.startDay, this.startTime, timeRuleType);
                        break;
                    case 2:
                        DateTimeRule dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, this.startTime, timeRuleType);
                        dtRule = dateTimeRule;
                        break;
                    case 3:
                        DateTimeRule dateTimeRule2 = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, true, this.startTime, timeRuleType);
                        dtRule = dateTimeRule2;
                        break;
                    case 4:
                        DateTimeRule dateTimeRule3 = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, false, this.startTime, timeRuleType);
                        dtRule = dateTimeRule3;
                        break;
                }
                AnnualTimeZoneRule annualTimeZoneRule = new AnnualTimeZoneRule(getID() + "(DST)", getRawOffset(), getDSTSavings(), dtRule, this.startYear, Integer.MAX_VALUE);
                this.dstRule = annualTimeZoneRule;
                long firstDstStart = this.dstRule.getFirstStart(getRawOffset(), 0).getTime();
                if (this.endTimeMode == 1) {
                    i = 1;
                } else if (this.endTimeMode != 2) {
                    i = 0;
                }
                int timeRuleType2 = i;
                switch (this.endMode) {
                    case 1:
                        dtRule = new DateTimeRule(this.endMonth, this.endDay, this.endTime, timeRuleType2);
                        break;
                    case 2:
                        DateTimeRule dateTimeRule4 = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, this.endTime, timeRuleType2);
                        dtRule = dateTimeRule4;
                        break;
                    case 3:
                        DateTimeRule dateTimeRule5 = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, true, this.endTime, timeRuleType2);
                        dtRule = dateTimeRule5;
                        break;
                    case 4:
                        DateTimeRule dateTimeRule6 = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, false, this.endTime, timeRuleType2);
                        dtRule = dateTimeRule6;
                        break;
                }
                AnnualTimeZoneRule annualTimeZoneRule2 = new AnnualTimeZoneRule(getID() + "(STD)", getRawOffset(), 0, dtRule, this.startYear, Integer.MAX_VALUE);
                this.stdRule = annualTimeZoneRule2;
                long firstStdStart = this.stdRule.getFirstStart(getRawOffset(), this.dstRule.getDSTSavings()).getTime();
                if (firstStdStart < firstDstStart) {
                    this.initialRule = new InitialTimeZoneRule(getID() + "(DST)", getRawOffset(), this.dstRule.getDSTSavings());
                    this.firstTransition = new TimeZoneTransition(firstStdStart, this.initialRule, this.stdRule);
                } else {
                    this.initialRule = new InitialTimeZoneRule(getID() + "(STD)", getRawOffset(), 0);
                    this.firstTransition = new TimeZoneTransition(firstDstStart, this.initialRule, this.dstRule);
                }
            } else {
                this.initialRule = new InitialTimeZoneRule(getID(), getRawOffset(), 0);
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
        tz.isFrozen = false;
        return tz;
    }
}
