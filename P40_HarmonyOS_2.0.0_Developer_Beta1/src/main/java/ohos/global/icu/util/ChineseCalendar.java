package ohos.global.icu.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;
import ohos.global.icu.impl.CalendarAstronomer;
import ohos.global.icu.impl.CalendarCache;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.util.ULocale;
import ohos.workschedulerservice.controller.WorkStatus;

public class ChineseCalendar extends Calendar {
    private static final TimeZone CHINA_ZONE = new SimpleTimeZone(28800000, "CHINA_ZONE").freeze();
    static final int[][][] CHINESE_DATE_PRECEDENCE = {new int[][]{new int[]{5}, new int[]{3, 7}, new int[]{4, 7}, new int[]{8, 7}, new int[]{3, 18}, new int[]{4, 18}, new int[]{8, 18}, new int[]{6}, new int[]{37, 22}}, new int[][]{new int[]{3}, new int[]{4}, new int[]{8}, new int[]{40, 7}, new int[]{40, 18}}};
    private static final int CHINESE_EPOCH_YEAR = -2636;
    private static final int[][] LIMITS = {new int[]{1, 1, 83333, 83333}, new int[]{1, 1, 60, 60}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 50, 55}, new int[0], new int[]{1, 1, 29, 30}, new int[]{1, 1, 353, 385}, new int[0], new int[]{-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0], new int[]{0, 0, 1, 1}};
    private static final int SYNODIC_GAP = 25;
    private static final long serialVersionUID = 7312110751940929420L;
    private transient CalendarAstronomer astro;
    private int epochYear;
    private transient boolean isLeapYear;
    private transient CalendarCache newYearCache;
    private transient CalendarCache winterSolsticeCache;
    private TimeZone zoneAstro;

    @Override // ohos.global.icu.util.Calendar
    public String getType() {
        return "chinese";
    }

    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    public ChineseCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(Date date) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
        setTime(date);
    }

    public ChineseCalendar(int i, int i2, int i3, int i4) {
        this(i, i2, i3, i4, 0, 0, 0);
    }

    public ChineseCalendar(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
        set(14, 0);
        set(1, i);
        set(2, i2);
        set(22, i3);
        set(5, i4);
        set(11, i5);
        set(12, i6);
        set(13, i7);
    }

    public ChineseCalendar(int i, int i2, int i3, int i4, int i5) {
        this(i, i2, i3, i4, i5, 0, 0, 0);
    }

    public ChineseCalendar(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
        set(14, 0);
        set(0, i);
        set(1, i2);
        set(2, i3);
        set(22, i4);
        set(5, i5);
        set(11, i6);
        set(12, i7);
        set(13, i8);
    }

    public ChineseCalendar(Locale locale) {
        this(TimeZone.getDefault(), ULocale.forLocale(locale), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone timeZone) {
        this(timeZone, ULocale.getDefault(ULocale.Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone timeZone, Locale locale) {
        this(timeZone, ULocale.forLocale(locale), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(ULocale uLocale) {
        this(TimeZone.getDefault(), uLocale, (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone timeZone, ULocale uLocale) {
        this(timeZone, uLocale, (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    @Deprecated
    protected ChineseCalendar(TimeZone timeZone, ULocale uLocale, int i, TimeZone timeZone2) {
        super(timeZone, uLocale);
        this.astro = new CalendarAstronomer();
        this.winterSolsticeCache = new CalendarCache();
        this.newYearCache = new CalendarCache();
        this.epochYear = i;
        this.zoneAstro = timeZone2;
        setTimeInMillis(System.currentTimeMillis());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetLimit(int i, int i2) {
        return LIMITS[i][i2];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetExtendedYear() {
        if (newestStamp(0, 1, 0) <= getStamp(19)) {
            return internalGet(19, 1);
        }
        return (((internalGet(0, 1) - 1) * 60) + internalGet(1, 1)) - (this.epochYear + 2636);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetMonthLength(int i, int i2) {
        int handleComputeMonthStart = (handleComputeMonthStart(i, i2, true) - 2440588) + 1;
        return newMoonNear(handleComputeMonthStart + 25, true) - handleComputeMonthStart;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public DateFormat handleGetDateFormat(String str, String str2, ULocale uLocale) {
        return super.handleGetDateFormat(str, str2, uLocale);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int[][][] getFieldResolutionTable() {
        return CHINESE_DATE_PRECEDENCE;
    }

    private void offsetMonth(int i, int i2, int i3) {
        int newMoonNear = ((newMoonNear(i + ((int) ((((double) i3) - 0.5d) * 29.530588853d)), true) + 2440588) - 1) + i2;
        if (i2 > 29) {
            set(20, newMoonNear - 1);
            complete();
            if (getActualMaximum(5) >= i2) {
                set(20, newMoonNear);
                return;
            }
            return;
        }
        set(20, newMoonNear);
    }

    @Override // ohos.global.icu.util.Calendar
    public void add(int i, int i2) {
        if (i != 2) {
            super.add(i, i2);
        } else if (i2 != 0) {
            int i3 = get(5);
            offsetMonth(((get(20) - 2440588) - i3) + 1, i3, i2);
        }
    }

    @Override // ohos.global.icu.util.Calendar
    public void roll(int i, int i2) {
        if (i != 2) {
            super.roll(i, i2);
        } else if (i2 != 0) {
            int i3 = get(5);
            int i4 = ((get(20) - 2440588) - i3) + 1;
            int i5 = get(2);
            if (this.isLeapYear && (get(22) == 1 || isLeapMonthBetween(newMoonNear(i4 - ((int) ((((double) i5) - 0.5d) * 29.530588853d)), true), i4))) {
                i5++;
            }
            int i6 = this.isLeapYear ? 13 : 12;
            int i7 = (i2 + i5) % i6;
            if (i7 < 0) {
                i7 += i6;
            }
            if (i7 != i5) {
                offsetMonth(i4, i3, i7 - i5);
            }
        }
    }

    private final long daysToMillis(int i) {
        long j = ((long) i) * WorkStatus.RARE_DELAY_TIME;
        return j - ((long) this.zoneAstro.getOffset(j));
    }

    private final int millisToDays(long j) {
        return (int) floorDivide(j + ((long) this.zoneAstro.getOffset(j)), (long) WorkStatus.RARE_DELAY_TIME);
    }

    private int winterSolstice(int i) {
        long j = (long) i;
        long j2 = this.winterSolsticeCache.get(j);
        if (j2 == CalendarCache.EMPTY) {
            this.astro.setTime(daysToMillis((computeGregorianMonthStart(i, 11) + 1) - 2440588));
            j2 = (long) millisToDays(this.astro.getSunTime(CalendarAstronomer.WINTER_SOLSTICE, true));
            this.winterSolsticeCache.put(j, j2);
        }
        return (int) j2;
    }

    private int newMoonNear(int i, boolean z) {
        this.astro.setTime(daysToMillis(i));
        return millisToDays(this.astro.getMoonTime(CalendarAstronomer.NEW_MOON, z));
    }

    private int synodicMonthsBetween(int i, int i2) {
        return (int) Math.round(((double) (i2 - i)) / 29.530588853d);
    }

    private int majorSolarTerm(int i) {
        this.astro.setTime(daysToMillis(i));
        int floor = (((int) Math.floor((this.astro.getSunLongitude() * 6.0d) / 3.141592653589793d)) + 2) % 12;
        return floor < 1 ? floor + 12 : floor;
    }

    private boolean hasNoMajorSolarTerm(int i) {
        if (majorSolarTerm(i) == majorSolarTerm(newMoonNear(i + 25, true))) {
            return true;
        }
        return false;
    }

    private boolean isLeapMonthBetween(int i, int i2) {
        if (synodicMonthsBetween(i, i2) >= 50) {
            throw new IllegalArgumentException("isLeapMonthBetween(" + i + ", " + i2 + "): Invalid parameters");
        } else if (i2 < i) {
            return false;
        } else {
            if (isLeapMonthBetween(i, newMoonNear(i2 - 25, false)) || hasNoMajorSolarTerm(i2)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public void handleComputeFields(int i) {
        computeChineseFields(i - 2440588, getGregorianYear(), getGregorianMonth(), true);
    }

    private void computeChineseFields(int i, int i2, int i3, boolean z) {
        int i4;
        int winterSolstice = winterSolstice(i2);
        if (i < winterSolstice) {
            i4 = winterSolstice;
            winterSolstice = winterSolstice(i2 - 1);
        } else {
            i4 = winterSolstice(i2 + 1);
        }
        int newMoonNear = newMoonNear(winterSolstice + 1, true);
        int newMoonNear2 = newMoonNear(i4 + 1, false);
        int newMoonNear3 = newMoonNear(i + 1, false);
        this.isLeapYear = synodicMonthsBetween(newMoonNear, newMoonNear2) == 12;
        int synodicMonthsBetween = synodicMonthsBetween(newMoonNear, newMoonNear3);
        if (this.isLeapYear && isLeapMonthBetween(newMoonNear, newMoonNear3)) {
            synodicMonthsBetween--;
        }
        if (synodicMonthsBetween < 1) {
            synodicMonthsBetween += 12;
        }
        int i5 = (!this.isLeapYear || !hasNoMajorSolarTerm(newMoonNear3) || isLeapMonthBetween(newMoonNear, newMoonNear(newMoonNear3 + -25, false))) ? 0 : 1;
        internalSet(2, synodicMonthsBetween - 1);
        internalSet(22, i5);
        if (z) {
            int i6 = i2 - this.epochYear;
            int i7 = i2 + 2636;
            if (synodicMonthsBetween < 11 || i3 >= 6) {
                i6++;
                i7++;
            }
            internalSet(19, i6);
            int[] iArr = new int[1];
            internalSet(0, floorDivide(i7 - 1, 60, iArr) + 1);
            internalSet(1, iArr[0] + 1);
            internalSet(5, (i - newMoonNear3) + 1);
            int newYear = newYear(i2);
            if (i < newYear) {
                newYear = newYear(i2 - 1);
            }
            internalSet(6, (i - newYear) + 1);
        }
    }

    private int newYear(int i) {
        long j = (long) i;
        long j2 = this.newYearCache.get(j);
        if (j2 == CalendarCache.EMPTY) {
            int winterSolstice = winterSolstice(i - 1);
            int winterSolstice2 = winterSolstice(i);
            int newMoonNear = newMoonNear(winterSolstice + 1, true);
            int newMoonNear2 = newMoonNear(newMoonNear + 25, true);
            j2 = (synodicMonthsBetween(newMoonNear, newMoonNear(winterSolstice2 + 1, false)) != 12 || (!hasNoMajorSolarTerm(newMoonNear) && !hasNoMajorSolarTerm(newMoonNear2))) ? (long) newMoonNear2 : (long) newMoonNear(newMoonNear2 + 25, true);
            this.newYearCache.put(j, j2);
        }
        return (int) j2;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleComputeMonthStart(int i, int i2, boolean z) {
        if (i2 < 0 || i2 > 11) {
            int[] iArr = new int[1];
            i += floorDivide(i2, 12, iArr);
            i2 = iArr[0];
        }
        int newMoonNear = newMoonNear(newYear((i + this.epochYear) - 1) + (i2 * 29), true);
        int i3 = newMoonNear + 2440588;
        int internalGet = internalGet(2);
        int internalGet2 = internalGet(22);
        int i4 = z ? internalGet2 : 0;
        computeGregorianFields(i3);
        computeChineseFields(newMoonNear, getGregorianYear(), getGregorianMonth(), false);
        if (!(i2 == internalGet(2) && i4 == internalGet(22))) {
            i3 = newMoonNear(newMoonNear + 25, true) + 2440588;
        }
        internalSet(2, internalGet);
        internalSet(22, internalGet2);
        return i3 - 1;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        this.epochYear = CHINESE_EPOCH_YEAR;
        this.zoneAstro = CHINA_ZONE;
        objectInputStream.defaultReadObject();
        this.astro = new CalendarAstronomer();
        this.winterSolsticeCache = new CalendarCache();
        this.newYearCache = new CalendarCache();
    }
}
