package ohos.global.icu.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import ohos.global.icu.impl.Grego;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

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
    private static final byte[] staticMonthLength = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
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

    public SimpleTimeZone(int i, String str) {
        super(str);
        construct(i, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3600000);
    }

    public SimpleTimeZone(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        super(str);
        construct(i, i2, i3, i4, i5, 0, i6, i7, i8, i9, 0, 3600000);
    }

    public SimpleTimeZone(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12) {
        super(str);
        construct(i, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12);
    }

    public SimpleTimeZone(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
        super(str);
        construct(i, i2, i3, i4, i5, 0, i6, i7, i8, i9, 0, i10);
    }

    @Override // ohos.global.icu.util.TimeZone
    public void setID(String str) {
        if (!isFrozen()) {
            super.setID(str);
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    @Override // ohos.global.icu.util.TimeZone
    public void setRawOffset(int i) {
        if (!isFrozen()) {
            this.raw = i;
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    @Override // ohos.global.icu.util.TimeZone
    public int getRawOffset() {
        return this.raw;
    }

    public void setStartYear(int i) {
        if (!isFrozen()) {
            getSTZInfo().sy = i;
            this.startYear = i;
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setStartRule(int i, int i2, int i3, int i4) {
        if (!isFrozen()) {
            getSTZInfo().setStart(i, i2, i3, i4, -1, false);
            setStartRule(i, i2, i3, i4, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    private void setStartRule(int i, int i2, int i3, int i4, int i5) {
        this.startMonth = i;
        this.startDay = i2;
        this.startDayOfWeek = i3;
        this.startTime = i4;
        this.startTimeMode = i5;
        decodeStartRule();
        this.transitionRulesInitialized = false;
    }

    public void setStartRule(int i, int i2, int i3) {
        if (!isFrozen()) {
            getSTZInfo().setStart(i, -1, -1, i3, i2, false);
            setStartRule(i, i2, 0, i3, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setStartRule(int i, int i2, int i3, int i4, boolean z) {
        if (!isFrozen()) {
            getSTZInfo().setStart(i, -1, i3, i4, i2, z);
            if (!z) {
                i2 = -i2;
            }
            setStartRule(i, i2, -i3, i4, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setEndRule(int i, int i2, int i3, int i4) {
        if (!isFrozen()) {
            getSTZInfo().setEnd(i, i2, i3, i4, -1, false);
            setEndRule(i, i2, i3, i4, 0);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setEndRule(int i, int i2, int i3) {
        if (!isFrozen()) {
            getSTZInfo().setEnd(i, -1, -1, i3, i2, false);
            setEndRule(i, i2, 0, i3);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    public void setEndRule(int i, int i2, int i3, int i4, boolean z) {
        if (!isFrozen()) {
            getSTZInfo().setEnd(i, -1, i3, i4, i2, z);
            setEndRule(i, i2, i3, i4, 0, z);
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
    }

    private void setEndRule(int i, int i2, int i3, int i4, int i5, boolean z) {
        if (!z) {
            i2 = -i2;
        }
        setEndRule(i, i2, -i3, i4, i5);
    }

    private void setEndRule(int i, int i2, int i3, int i4, int i5) {
        this.endMonth = i;
        this.endDay = i2;
        this.endDayOfWeek = i3;
        this.endTime = i4;
        this.endTimeMode = i5;
        decodeEndRule();
        this.transitionRulesInitialized = false;
    }

    public void setDSTSavings(int i) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen SimpleTimeZone instance.");
        } else if (i != 0) {
            this.dst = i;
            this.transitionRulesInitialized = false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // ohos.global.icu.util.TimeZone
    public int getDSTSavings() {
        return this.dst;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        STZInfo sTZInfo = this.xinfo;
        if (sTZInfo != null) {
            sTZInfo.applyTo(this);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "SimpleTimeZone: " + getID();
    }

    private STZInfo getSTZInfo() {
        if (this.xinfo == null) {
            this.xinfo = new STZInfo();
        }
        return this.xinfo;
    }

    @Override // ohos.global.icu.util.TimeZone
    public int getOffset(int i, int i2, int i3, int i4, int i5, int i6) {
        if (i3 >= 0 && i3 <= 11) {
            return getOffset(i, i2, i3, i4, i5, i6, Grego.monthLength(i2, i3));
        }
        throw new IllegalArgumentException();
    }

    @Deprecated
    public int getOffset(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (i3 >= 0 && i3 <= 11) {
            return getOffset(i, i2, i3, i4, i5, i6, Grego.monthLength(i2, i3), Grego.previousMonthLength(i2, i3));
        }
        throw new IllegalArgumentException();
    }

    private int getOffset(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9;
        int i10;
        if ((i == 1 || i == 0) && i3 >= 0 && i3 <= 11 && i4 >= 1 && i4 <= i7 && i5 >= 1 && i5 <= 7 && i6 >= 0 && i6 < 86400000 && i7 >= 28 && i7 <= 31 && i8 >= 28 && i8 <= 31) {
            int i11 = this.raw;
            if (!this.useDaylight || i2 < this.startYear || i != 1) {
                return i11;
            }
            int i12 = 0;
            boolean z = this.startMonth > this.endMonth;
            int compareToRule = compareToRule(i3, i7, i8, i4, i5, i6, this.startTimeMode == 2 ? -this.raw : 0, this.startMode, this.startMonth, this.startDayOfWeek, this.startDay, this.startTime);
            if (z != (compareToRule >= 0)) {
                int i13 = this.endTimeMode;
                if (i13 == 0) {
                    i10 = this.dst;
                } else if (i13 == 2) {
                    i10 = -this.raw;
                } else {
                    i9 = 0;
                    i12 = compareToRule(i3, i7, i8, i4, i5, i6, i9, this.endMode, this.endMonth, this.endDayOfWeek, this.endDay, this.endTime);
                }
                i9 = i10;
                i12 = compareToRule(i3, i7, i8, i4, i5, i6, i9, this.endMode, this.endMonth, this.endDayOfWeek, this.endDay, this.endTime);
            }
            if (z || compareToRule < 0 || i12 >= 0) {
                if (!z) {
                    return i11;
                }
                if (compareToRule < 0 && i12 >= 0) {
                    return i11;
                }
            }
            return i11 + this.dst;
        }
        throw new IllegalArgumentException();
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:19:? A[RETURN, SYNTHETIC] */
    @Override // ohos.global.icu.util.BasicTimeZone
    @Deprecated
    public void getOffsetFromLocal(long j, int i, int i2, int[] iArr) {
        long j2;
        boolean z;
        int i3;
        iArr[0] = getRawOffset();
        int[] iArr2 = new int[6];
        Grego.timeToFields(j, iArr2);
        iArr[1] = getOffset(1, iArr2[0], iArr2[1], iArr2[2], iArr2[3], iArr2[5]) - iArr[0];
        if (iArr[1] > 0) {
            int i4 = i & 3;
            if (i4 == 1 || !(i4 == 3 || (i & 12) == 12)) {
                i3 = getDSTSavings();
                j2 = j - ((long) i3);
                z = true;
                if (z) {
                    Grego.timeToFields(j2, iArr2);
                    iArr[1] = getOffset(1, iArr2[0], iArr2[1], iArr2[2], iArr2[3], iArr2[5]) - iArr[0];
                    return;
                }
                return;
            }
        } else {
            int i5 = i2 & 3;
            if (i5 == 3 || (i5 != 1 && (i2 & 12) == 4)) {
                i3 = getDSTSavings();
                j2 = j - ((long) i3);
                z = true;
                if (z) {
                }
            }
        }
        j2 = j;
        z = false;
        if (z) {
        }
    }

    private int compareToRule(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12) {
        int i13;
        int i14 = i6 + i7;
        while (i14 >= 86400000) {
            i14 -= ConstantValue.DAY_MILLSECONDS;
            i4++;
            i5 = (i5 % 7) + 1;
            if (i4 > i2) {
                i++;
                i4 = 1;
            }
        }
        while (i14 < 0) {
            i4--;
            i5 = ((i5 + 5) % 7) + 1;
            if (i4 < 1) {
                i--;
                i4 = i3;
            }
            i14 += ConstantValue.DAY_MILLSECONDS;
        }
        if (i < i9) {
            return -1;
        }
        if (i > i9) {
            return 1;
        }
        if (i11 > i2) {
            i11 = i2;
        }
        if (i8 != 1) {
            if (i8 != 2) {
                if (i8 != 3) {
                    i11 = i8 != 4 ? 0 : i11 - (((((49 - i10) + i11) + i5) - i4) % 7);
                } else {
                    i13 = ((((i10 + 49) - i11) - i5) + i4) % 7;
                }
            } else if (i11 > 0) {
                i11 = ((i11 - 1) * 7) + 1;
                i13 = ((i10 + 7) - ((i5 - i4) + 1)) % 7;
            } else {
                i11 = (((i11 + 1) * 7) + i2) - (((((i5 + i2) - i4) + 7) - i10) % 7);
            }
            i11 += i13;
        }
        if (i4 < i11) {
            return -1;
        }
        if (i4 > i11) {
            return 1;
        }
        if (i14 < i12) {
            return -1;
        }
        if (i14 > i12) {
            return 1;
        }
        return 0;
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean useDaylightTime() {
        return this.useDaylight;
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean observesDaylightTime() {
        return this.useDaylight;
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean inDaylightTime(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(this);
        gregorianCalendar.setTime(date);
        return gregorianCalendar.inDaylightTime();
    }

    private void construct(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12) {
        this.raw = i;
        this.startMonth = i2;
        this.startDay = i3;
        this.startDayOfWeek = i4;
        this.startTime = i5;
        this.startTimeMode = i6;
        this.endMonth = i7;
        this.endDay = i8;
        this.endDayOfWeek = i9;
        this.endTime = i10;
        this.endTimeMode = i11;
        this.dst = i12;
        this.startYear = 0;
        this.startMode = 1;
        this.endMode = 1;
        decodeRules();
        if (i12 == 0) {
            throw new IllegalArgumentException();
        }
    }

    private void decodeRules() {
        decodeStartRule();
        decodeEndRule();
    }

    private void decodeStartRule() {
        int i;
        this.useDaylight = (this.startDay == 0 || this.endDay == 0) ? false : true;
        if (this.useDaylight && this.dst == 0) {
            this.dst = ConstantValue.DAY_MILLSECONDS;
        }
        int i2 = this.startDay;
        if (i2 != 0) {
            int i3 = this.startMonth;
            if (i3 < 0 || i3 > 11) {
                throw new IllegalArgumentException();
            }
            int i4 = this.startTime;
            if (i4 < 0 || i4 > 86400000 || (i = this.startTimeMode) < 0 || i > 2) {
                throw new IllegalArgumentException();
            }
            int i5 = this.startDayOfWeek;
            if (i5 == 0) {
                this.startMode = 1;
            } else {
                if (i5 > 0) {
                    this.startMode = 2;
                } else {
                    this.startDayOfWeek = -i5;
                    if (i2 > 0) {
                        this.startMode = 3;
                    } else {
                        this.startDay = -i2;
                        this.startMode = 4;
                    }
                }
                if (this.startDayOfWeek > 7) {
                    throw new IllegalArgumentException();
                }
            }
            if (this.startMode == 2) {
                int i6 = this.startDay;
                if (i6 < -5 || i6 > 5) {
                    throw new IllegalArgumentException();
                }
                return;
            }
            int i7 = this.startDay;
            if (i7 < 1 || i7 > staticMonthLength[this.startMonth]) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void decodeEndRule() {
        int i;
        this.useDaylight = (this.startDay == 0 || this.endDay == 0) ? false : true;
        if (this.useDaylight && this.dst == 0) {
            this.dst = ConstantValue.DAY_MILLSECONDS;
        }
        int i2 = this.endDay;
        if (i2 != 0) {
            int i3 = this.endMonth;
            if (i3 < 0 || i3 > 11) {
                throw new IllegalArgumentException();
            }
            int i4 = this.endTime;
            if (i4 < 0 || i4 > 86400000 || (i = this.endTimeMode) < 0 || i > 2) {
                throw new IllegalArgumentException();
            }
            int i5 = this.endDayOfWeek;
            if (i5 == 0) {
                this.endMode = 1;
            } else {
                if (i5 > 0) {
                    this.endMode = 2;
                } else {
                    this.endDayOfWeek = -i5;
                    if (i2 > 0) {
                        this.endMode = 3;
                    } else {
                        this.endDay = -i2;
                        this.endMode = 4;
                    }
                }
                if (this.endDayOfWeek > 7) {
                    throw new IllegalArgumentException();
                }
            }
            if (this.endMode == 2) {
                int i6 = this.endDay;
                if (i6 < -5 || i6 > 5) {
                    throw new IllegalArgumentException();
                }
                return;
            }
            int i7 = this.endDay;
            if (i7 < 1 || i7 > staticMonthLength[this.endMonth]) {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override // ohos.global.icu.util.TimeZone, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SimpleTimeZone simpleTimeZone = (SimpleTimeZone) obj;
        if (this.raw == simpleTimeZone.raw && this.useDaylight == simpleTimeZone.useDaylight && idEquals(getID(), simpleTimeZone.getID())) {
            if (!this.useDaylight) {
                return true;
            }
            if (this.dst == simpleTimeZone.dst && this.startMode == simpleTimeZone.startMode && this.startMonth == simpleTimeZone.startMonth && this.startDay == simpleTimeZone.startDay && this.startDayOfWeek == simpleTimeZone.startDayOfWeek && this.startTime == simpleTimeZone.startTime && this.startTimeMode == simpleTimeZone.startTimeMode && this.endMode == simpleTimeZone.endMode && this.endMonth == simpleTimeZone.endMonth && this.endDay == simpleTimeZone.endDay && this.endDayOfWeek == simpleTimeZone.endDayOfWeek && this.endTime == simpleTimeZone.endTime && this.endTimeMode == simpleTimeZone.endTimeMode && this.startYear == simpleTimeZone.startYear) {
                return true;
            }
        }
        return false;
    }

    private boolean idEquals(String str, String str2) {
        if (str == null && str2 == null) {
            return true;
        }
        if (str == null || str2 == null) {
            return false;
        }
        return str.equals(str2);
    }

    @Override // ohos.global.icu.util.TimeZone, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode();
        int i = this.raw;
        boolean z = this.useDaylight;
        int i2 = (hashCode + i) ^ ((i >>> 8) + (!z ? 1 : 0));
        if (z) {
            return i2;
        }
        int i3 = this.dst;
        int i4 = this.startMode;
        int i5 = i3 ^ ((i3 >>> 10) + i4);
        int i6 = i4 >>> 11;
        int i7 = this.startMonth;
        int i8 = i5 ^ (i6 + i7);
        int i9 = i7 >>> 12;
        int i10 = this.startDay;
        int i11 = i8 ^ (i9 + i10);
        int i12 = i10 >>> 13;
        int i13 = this.startDayOfWeek;
        int i14 = i11 ^ (i12 + i13);
        int i15 = i13 >>> 14;
        int i16 = this.startTime;
        int i17 = i14 ^ (i15 + i16);
        int i18 = i16 >>> 15;
        int i19 = this.startTimeMode;
        int i20 = i17 ^ (i18 + i19);
        int i21 = i19 >>> 16;
        int i22 = this.endMode;
        int i23 = i20 ^ (i21 + i22);
        int i24 = i22 >>> 17;
        int i25 = this.endMonth;
        int i26 = i23 ^ (i24 + i25);
        int i27 = i25 >>> 18;
        int i28 = this.endDay;
        int i29 = i26 ^ (i27 + i28);
        int i30 = i28 >>> 19;
        int i31 = this.endDayOfWeek;
        int i32 = i29 ^ (i30 + i31);
        int i33 = i31 >>> 20;
        int i34 = this.endTime;
        int i35 = i32 ^ (i33 + i34);
        int i36 = i34 >>> 21;
        int i37 = this.endTimeMode;
        int i38 = this.startYear;
        return i2 + ((i38 >>> 23) ^ ((i35 ^ (i36 + i37)) ^ ((i37 >>> 22) + i38)));
    }

    @Override // ohos.global.icu.util.TimeZone, java.lang.Object
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    @Override // ohos.global.icu.util.TimeZone
    public boolean hasSameRules(TimeZone timeZone) {
        boolean z;
        if (this == timeZone) {
            return true;
        }
        if (!(timeZone instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone simpleTimeZone = (SimpleTimeZone) timeZone;
        if (simpleTimeZone != null && this.raw == simpleTimeZone.raw && (z = this.useDaylight) == simpleTimeZone.useDaylight) {
            if (!z) {
                return true;
            }
            if (this.dst == simpleTimeZone.dst && this.startMode == simpleTimeZone.startMode && this.startMonth == simpleTimeZone.startMonth && this.startDay == simpleTimeZone.startDay && this.startDayOfWeek == simpleTimeZone.startDayOfWeek && this.startTime == simpleTimeZone.startTime && this.startTimeMode == simpleTimeZone.startTimeMode && this.endMode == simpleTimeZone.endMode && this.endMonth == simpleTimeZone.endMonth && this.endDay == simpleTimeZone.endDay && this.endDayOfWeek == simpleTimeZone.endDayOfWeek && this.endTime == simpleTimeZone.endTime && this.endTimeMode == simpleTimeZone.endTimeMode && this.startYear == simpleTimeZone.startYear) {
                return true;
            }
        }
        return false;
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneTransition getNextTransition(long j, boolean z) {
        if (!this.useDaylight) {
            return null;
        }
        initTransitionRules();
        int i = (j > this.firstTransition.getTime() ? 1 : (j == this.firstTransition.getTime() ? 0 : -1));
        if (i < 0 || (z && i == 0)) {
            return this.firstTransition;
        }
        Date nextStart = this.stdRule.getNextStart(j, this.dstRule.getRawOffset(), this.dstRule.getDSTSavings(), z);
        Date nextStart2 = this.dstRule.getNextStart(j, this.stdRule.getRawOffset(), this.stdRule.getDSTSavings(), z);
        if (nextStart != null && (nextStart2 == null || nextStart.before(nextStart2))) {
            return new TimeZoneTransition(nextStart.getTime(), this.dstRule, this.stdRule);
        }
        if (nextStart2 == null || (nextStart != null && !nextStart2.before(nextStart))) {
            return null;
        }
        return new TimeZoneTransition(nextStart2.getTime(), this.stdRule, this.dstRule);
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneTransition getPreviousTransition(long j, boolean z) {
        if (!this.useDaylight) {
            return null;
        }
        initTransitionRules();
        int i = (j > this.firstTransition.getTime() ? 1 : (j == this.firstTransition.getTime() ? 0 : -1));
        if (i >= 0 && (z || i != 0)) {
            Date previousStart = this.stdRule.getPreviousStart(j, this.dstRule.getRawOffset(), this.dstRule.getDSTSavings(), z);
            Date previousStart2 = this.dstRule.getPreviousStart(j, this.stdRule.getRawOffset(), this.stdRule.getDSTSavings(), z);
            if (previousStart != null && (previousStart2 == null || previousStart.after(previousStart2))) {
                return new TimeZoneTransition(previousStart.getTime(), this.dstRule, this.stdRule);
            }
            if (previousStart2 != null && (previousStart == null || previousStart2.after(previousStart))) {
                return new TimeZoneTransition(previousStart2.getTime(), this.stdRule, this.dstRule);
            }
        }
        return null;
    }

    @Override // ohos.global.icu.util.BasicTimeZone
    public TimeZoneRule[] getTimeZoneRules() {
        initTransitionRules();
        TimeZoneRule[] timeZoneRuleArr = new TimeZoneRule[(this.useDaylight ? 3 : 1)];
        timeZoneRuleArr[0] = this.initialRule;
        if (this.useDaylight) {
            timeZoneRuleArr[1] = this.stdRule;
            timeZoneRuleArr[2] = this.dstRule;
        }
        return timeZoneRuleArr;
    }

    private synchronized void initTransitionRules() {
        int i;
        int i2;
        if (!this.transitionRulesInitialized) {
            if (this.useDaylight) {
                DateTimeRule dateTimeRule = null;
                if (this.startTimeMode == 1) {
                    i = 1;
                } else {
                    i = this.startTimeMode == 2 ? 2 : 0;
                }
                int i3 = this.startMode;
                if (i3 == 1) {
                    dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startTime, i);
                } else if (i3 == 2) {
                    dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, this.startTime, i);
                } else if (i3 == 3) {
                    dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, true, this.startTime, i);
                } else if (i3 == 4) {
                    dateTimeRule = new DateTimeRule(this.startMonth, this.startDay, this.startDayOfWeek, false, this.startTime, i);
                }
                this.dstRule = new AnnualTimeZoneRule(getID() + "(DST)", getRawOffset(), getDSTSavings(), dateTimeRule, this.startYear, Integer.MAX_VALUE);
                long time = this.dstRule.getFirstStart(getRawOffset(), 0).getTime();
                if (this.endTimeMode == 1) {
                    i2 = 1;
                } else {
                    i2 = this.endTimeMode == 2 ? 2 : 0;
                }
                int i4 = this.endMode;
                if (i4 == 1) {
                    dateTimeRule = new DateTimeRule(this.endMonth, this.endDay, this.endTime, i2);
                } else if (i4 == 2) {
                    dateTimeRule = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, this.endTime, i2);
                } else if (i4 == 3) {
                    dateTimeRule = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, true, this.endTime, i2);
                } else if (i4 == 4) {
                    dateTimeRule = new DateTimeRule(this.endMonth, this.endDay, this.endDayOfWeek, false, this.endTime, i2);
                }
                this.stdRule = new AnnualTimeZoneRule(getID() + "(STD)", getRawOffset(), 0, dateTimeRule, this.startYear, Integer.MAX_VALUE);
                long time2 = this.stdRule.getFirstStart(getRawOffset(), this.dstRule.getDSTSavings()).getTime();
                if (time2 < time) {
                    this.initialRule = new InitialTimeZoneRule(getID() + "(DST)", getRawOffset(), this.dstRule.getDSTSavings());
                    this.firstTransition = new TimeZoneTransition(time2, this.initialRule, this.stdRule);
                } else {
                    this.initialRule = new InitialTimeZoneRule(getID() + "(STD)", getRawOffset(), 0);
                    this.firstTransition = new TimeZoneTransition(time, this.initialRule, this.dstRule);
                }
            } else {
                this.initialRule = new InitialTimeZoneRule(getID(), getRawOffset(), 0);
            }
            this.transitionRulesInitialized = true;
        }
    }

    @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
    public boolean isFrozen() {
        return this.isFrozen;
    }

    @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
    public TimeZone freeze() {
        this.isFrozen = true;
        return this;
    }

    @Override // ohos.global.icu.util.TimeZone, ohos.global.icu.util.Freezable
    public TimeZone cloneAsThawed() {
        SimpleTimeZone simpleTimeZone = (SimpleTimeZone) super.cloneAsThawed();
        simpleTimeZone.isFrozen = false;
        return simpleTimeZone;
    }
}
