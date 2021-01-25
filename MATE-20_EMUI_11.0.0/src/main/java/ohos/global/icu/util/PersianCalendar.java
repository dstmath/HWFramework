package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;
import ohos.global.icu.util.ULocale;

@Deprecated
public class PersianCalendar extends Calendar {
    private static final int[][] LIMITS = {new int[]{0, 0, 0, 0}, new int[]{-5000000, -5000000, 5000000, 5000000}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 29, 31}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0]};
    private static final int[][] MONTH_COUNT = {new int[]{31, 31, 0}, new int[]{31, 31, 31}, new int[]{31, 31, 62}, new int[]{31, 31, 93}, new int[]{31, 31, 124}, new int[]{31, 31, 155}, new int[]{30, 30, 186}, new int[]{30, 30, 216}, new int[]{30, 30, 246}, new int[]{30, 30, 276}, new int[]{30, 30, 306}, new int[]{29, 30, 336}};
    private static final int PERSIAN_EPOCH = 1948320;
    private static final long serialVersionUID = -6727306982975111643L;

    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public String getType() {
        return "persian";
    }

    @Deprecated
    public PersianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    @Deprecated
    public PersianCalendar(TimeZone timeZone) {
        this(timeZone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    @Deprecated
    public PersianCalendar(Locale locale) {
        this(TimeZone.getDefault(), locale);
    }

    @Deprecated
    public PersianCalendar(ULocale uLocale) {
        this(TimeZone.getDefault(), uLocale);
    }

    @Deprecated
    public PersianCalendar(TimeZone timeZone, Locale locale) {
        super(timeZone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    @Deprecated
    public PersianCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    @Deprecated
    public PersianCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        setTime(date);
    }

    @Deprecated
    public PersianCalendar(int i, int i2, int i3) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        set(1, i);
        set(2, i2);
        set(5, i3);
    }

    @Deprecated
    public PersianCalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        set(1, i);
        set(2, i2);
        set(5, i3);
        set(11, i4);
        set(12, i5);
        set(13, i6);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public int handleGetLimit(int i, int i2) {
        return LIMITS[i][i2];
    }

    private static final boolean isLeapYear(int i) {
        int[] iArr = new int[1];
        floorDivide((i * 25) + 11, 33, iArr);
        return iArr[0] < 8;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
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
    @Deprecated
    public int handleGetYearLength(int i) {
        return isLeapYear(i) ? 366 : 365;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public int handleComputeMonthStart(int i, int i2, boolean z) {
        if (i2 < 0 || i2 > 11) {
            int[] iArr = new int[1];
            i += floorDivide(i2, 12, iArr);
            i2 = iArr[0];
        }
        int floorDivide = ((i - 1) * 365) + 1948319 + floorDivide((i * 8) + 21, 33);
        return i2 != 0 ? floorDivide + MONTH_COUNT[i2][2] : floorDivide;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, 1);
        }
        return internalGet(1, 1);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public void handleComputeFields(int i) {
        int i2;
        long j = (long) (i - PERSIAN_EPOCH);
        int floorDivide = ((int) floorDivide((j * 33) + 3, 12053)) + 1;
        long j2 = (long) floorDivide;
        int floorDivide2 = (int) (j - (((j2 - 1) * 365) + floorDivide((j2 * 8) + 21, 33)));
        if (floorDivide2 < 216) {
            i2 = floorDivide2 / 31;
        } else {
            i2 = (floorDivide2 - 6) / 30;
        }
        internalSet(0, 0);
        internalSet(1, floorDivide);
        internalSet(19, floorDivide);
        internalSet(2, i2);
        internalSet(5, (floorDivide2 - MONTH_COUNT[i2][2]) + 1);
        internalSet(6, floorDivide2 + 1);
    }
}
