package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;
import ohos.global.icu.util.ULocale;
import ohos.miscservices.download.DownloadSession;
import ohos.workschedulerservice.controller.WorkStatus;

public class GregorianCalendar extends Calendar {
    public static final int AD = 1;
    public static final int BC = 0;
    private static final int EPOCH_YEAR = 1970;
    private static final int[][] LIMITS = {new int[]{0, 0, 1, 1}, new int[]{1, 1, 5828963, 5838270}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 28, 31}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 4, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5838270, -5838270, 5828964, 5838271}, new int[0], new int[]{-5838269, -5838269, 5828963, 5838270}, new int[0], new int[0], new int[0]};
    private static final int[][] MONTH_COUNT = {new int[]{31, 31, 0, 0}, new int[]{28, 29, 31, 31}, new int[]{31, 31, 59, 60}, new int[]{30, 30, 90, 91}, new int[]{31, 31, 120, 121}, new int[]{30, 30, 151, 152}, new int[]{31, 31, 181, 182}, new int[]{31, 31, 212, 213}, new int[]{30, 30, 243, 244}, new int[]{31, 31, 273, 274}, new int[]{30, 30, DownloadSession.PAUSED_UNKNOWN, 305}, new int[]{31, 31, 334, 335}};
    private static final long serialVersionUID = 9199388694351062137L;
    private transient int cutoverJulianDay;
    private long gregorianCutover;
    private transient int gregorianCutoverYear;
    protected transient boolean invertGregorian;
    protected transient boolean isGregorian;

    @Override // ohos.global.icu.util.Calendar
    public String getType() {
        return "gregorian";
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetLimit(int i, int i2) {
        return LIMITS[i][i2];
    }

    public GregorianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public GregorianCalendar(TimeZone timeZone) {
        this(timeZone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public GregorianCalendar(Locale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public GregorianCalendar(ULocale uLocale) {
        this(TimeZone.getDefault(), uLocale);
    }

    public GregorianCalendar(TimeZone timeZone, Locale locale) {
        super(timeZone, locale);
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(int i, int i2, int i3) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, i);
        set(2, i2);
        set(5, i3);
    }

    public GregorianCalendar(int i, int i2, int i3, int i4, int i5) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, i);
        set(2, i2);
        set(5, i3);
        set(11, i4);
        set(12, i5);
    }

    public GregorianCalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, i);
        set(2, i2);
        set(5, i3);
        set(11, i4);
        set(12, i5);
        set(13, i6);
    }

    public void setGregorianChange(Date date) {
        this.gregorianCutover = date.getTime();
        long j = this.gregorianCutover;
        if (j <= -184303902528000000L) {
            this.cutoverJulianDay = Integer.MIN_VALUE;
            this.gregorianCutoverYear = Integer.MIN_VALUE;
        } else if (j >= 183882168921600000L) {
            this.cutoverJulianDay = Integer.MAX_VALUE;
            this.gregorianCutoverYear = Integer.MAX_VALUE;
        } else {
            this.cutoverJulianDay = (int) floorDivide(j, (long) WorkStatus.RARE_DELAY_TIME);
            GregorianCalendar gregorianCalendar = new GregorianCalendar(getTimeZone());
            gregorianCalendar.setTime(date);
            this.gregorianCutoverYear = gregorianCalendar.get(19);
        }
    }

    public final Date getGregorianChange() {
        return new Date(this.gregorianCutover);
    }

    public boolean isLeapYear(int i) {
        if (i >= this.gregorianCutoverYear) {
            if (i % 4 == 0 && (i % 100 != 0 || i % 400 == 0)) {
                return true;
            }
        } else if (i % 4 == 0) {
            return true;
        }
        return false;
    }

    @Override // ohos.global.icu.util.Calendar
    public boolean isEquivalentTo(Calendar calendar) {
        return super.isEquivalentTo(calendar) && this.gregorianCutover == ((GregorianCalendar) calendar).gregorianCutover;
    }

    @Override // ohos.global.icu.util.Calendar, java.lang.Object
    public int hashCode() {
        return ((int) this.gregorianCutover) ^ super.hashCode();
    }

    @Override // ohos.global.icu.util.Calendar
    public void roll(int i, int i2) {
        if (i != 3) {
            super.roll(i, i2);
            return;
        }
        int i3 = get(3);
        int i4 = get(17);
        int internalGet = internalGet(6);
        if (internalGet(2) == 0) {
            if (i3 >= 52) {
                internalGet += handleGetYearLength(i4);
            }
        } else if (i3 == 1) {
            internalGet -= handleGetYearLength(i4 - 1);
        }
        int i5 = i3 + i2;
        if (i5 < 1 || i5 > 52) {
            int handleGetYearLength = handleGetYearLength(i4);
            int internalGet2 = (((handleGetYearLength - internalGet) + internalGet(7)) - getFirstDayOfWeek()) % 7;
            if (internalGet2 < 0) {
                internalGet2 += 7;
            }
            if (6 - internalGet2 >= getMinimalDaysInFirstWeek()) {
                handleGetYearLength -= 7;
            }
            int weekNumber = weekNumber(handleGetYearLength, internalGet2 + 1);
            i5 = (((i5 + weekNumber) - 1) % weekNumber) + 1;
        }
        set(3, i5);
        set(1, i4);
    }

    @Override // ohos.global.icu.util.Calendar
    public int getActualMinimum(int i) {
        return getMinimum(i);
    }

    @Override // ohos.global.icu.util.Calendar
    public int getActualMaximum(int i) {
        if (i != 1) {
            return super.getActualMaximum(i);
        }
        Calendar calendar = (Calendar) clone();
        calendar.setLenient(true);
        int i2 = calendar.get(0);
        Date time = calendar.getTime();
        int[][] iArr = LIMITS;
        int i3 = iArr[1][1];
        int i4 = iArr[1][2] + 1;
        while (i3 + 1 < i4) {
            int i5 = (i3 + i4) / 2;
            calendar.set(1, i5);
            if (calendar.get(1) == i5 && calendar.get(0) == i2) {
                i3 = i5;
            } else {
                calendar.setTime(time);
                i4 = i5;
            }
        }
        return i3;
    }

    /* access modifiers changed from: package-private */
    public boolean inDaylightTime() {
        if (!getTimeZone().useDaylightTime()) {
            return false;
        }
        complete();
        if (internalGet(16) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetMonthLength(int i, int i2) {
        if (i2 < 0 || i2 > 11) {
            int[] iArr = new int[1];
            i += floorDivide(i2, 12, iArr);
            i2 = iArr[0];
        }
        return MONTH_COUNT[i2][isLeapYear(i) ? 1 : 0];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetYearLength(int i) {
        return isLeapYear(i) ? 366 : 365;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public void handleComputeFields(int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        if (i >= this.cutoverJulianDay) {
            i5 = getGregorianMonth();
            i4 = getGregorianDayOfMonth();
            i3 = getGregorianDayOfYear();
            i2 = getGregorianYear();
        } else {
            long j = (long) (i - 1721424);
            int floorDivide = (int) floorDivide((j * 4) + 1464, 1461);
            long j2 = ((long) floorDivide) - 1;
            int floorDivide2 = (int) (j - ((365 * j2) + floorDivide(j2, 4)));
            boolean z = (floorDivide & 3) == 0;
            int i7 = ((((floorDivide2 >= (z ? 60 : 59) ? z ? 1 : 2 : 0) + floorDivide2) * 12) + 6) / 367;
            i4 = (floorDivide2 - MONTH_COUNT[i7][z ? (char) 3 : 2]) + 1;
            i2 = floorDivide;
            i3 = floorDivide2 + 1;
            i5 = i7;
        }
        internalSet(2, i5);
        internalSet(5, i4);
        internalSet(6, i3);
        internalSet(19, i2);
        if (i2 < 1) {
            i2 = 1 - i2;
            i6 = 0;
        } else {
            i6 = 1;
        }
        internalSet(0, i6);
        internalSet(1, i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, EPOCH_YEAR);
        }
        if (internalGet(0, 1) == 0) {
            return 1 - internalGet(1, 1);
        }
        return internalGet(1, EPOCH_YEAR);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleComputeJulianDay(int i) {
        boolean z = false;
        this.invertGregorian = false;
        int handleComputeJulianDay = super.handleComputeJulianDay(i);
        boolean z2 = this.isGregorian;
        if (handleComputeJulianDay >= this.cutoverJulianDay) {
            z = true;
        }
        if (z2 == z) {
            return handleComputeJulianDay;
        }
        this.invertGregorian = true;
        return super.handleComputeJulianDay(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleComputeMonthStart(int i, int i2, boolean z) {
        if (i2 < 0 || i2 > 11) {
            int[] iArr = new int[1];
            i += floorDivide(i2, 12, iArr);
            i2 = iArr[0];
        }
        boolean z2 = i % 4 == 0;
        int i3 = i - 1;
        int floorDivide = (i3 * 365) + floorDivide(i3, 4) + 1721423;
        this.isGregorian = i >= this.gregorianCutoverYear;
        if (this.invertGregorian) {
            this.isGregorian = !this.isGregorian;
        }
        char c = 2;
        if (this.isGregorian) {
            z2 = z2 && (i % 100 != 0 || i % 400 == 0);
            floorDivide += (floorDivide(i3, 400) - floorDivide(i3, 100)) + 2;
        }
        if (i2 == 0) {
            return floorDivide;
        }
        int[] iArr2 = MONTH_COUNT[i2];
        if (z2) {
            c = 3;
        }
        return floorDivide + iArr2[c];
    }
}
