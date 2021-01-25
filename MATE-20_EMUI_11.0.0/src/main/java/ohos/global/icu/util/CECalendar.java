package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;
import ohos.global.icu.util.ULocale;

/* access modifiers changed from: package-private */
public abstract class CECalendar extends Calendar {
    private static final int[][] LIMITS = {new int[]{0, 0, 1, 1}, new int[]{1, 1, 5000000, 5000000}, new int[]{0, 0, 12, 12}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 5, 30}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 1, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0]};
    private static final long serialVersionUID = -999547623066414271L;

    /* access modifiers changed from: protected */
    public abstract int getJDEpochOffset();

    protected CECalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    protected CECalendar(TimeZone timeZone) {
        this(timeZone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    protected CECalendar(Locale locale) {
        this(TimeZone.getDefault(), locale);
    }

    protected CECalendar(ULocale uLocale) {
        this(TimeZone.getDefault(), uLocale);
    }

    protected CECalendar(TimeZone timeZone, Locale locale) {
        super(timeZone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    protected CECalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    protected CECalendar(int i, int i2, int i3) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        set(i, i2, i3);
    }

    protected CECalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        setTime(date);
    }

    protected CECalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        set(i, i2, i3, i4, i5, i6);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleComputeMonthStart(int i, int i2, boolean z) {
        return ceToJD((long) i, i2, 0, getJDEpochOffset());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetLimit(int i, int i2) {
        return LIMITS[i][i2];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetMonthLength(int i, int i2) {
        if ((i2 + 1) % 13 != 0) {
            return 30;
        }
        return ((i % 4) / 3) + 5;
    }

    public static int ceToJD(long j, int i, int i2, int i3) {
        int i4;
        long j2;
        if (i >= 0) {
            j2 = j + ((long) (i / 13));
            i4 = i % 13;
        } else {
            int i5 = i + 1;
            j2 = j + ((long) ((i5 / 13) - 1));
            i4 = (i5 % 13) + 12;
        }
        return (int) (((((((long) i3) + (365 * j2)) + floorDivide(j2, 4)) + ((long) (i4 * 30))) + ((long) i2)) - 1);
    }

    public static void jdToCE(int i, int i2, int[] iArr) {
        int[] iArr2 = new int[1];
        int i3 = 365;
        iArr[0] = (floorDivide(i - i2, 1461, iArr2) * 4) + ((iArr2[0] / 365) - (iArr2[0] / 1460));
        if (iArr2[0] != 1460) {
            i3 = iArr2[0] % 365;
        }
        iArr[1] = i3 / 30;
        iArr[2] = (i3 % 30) + 1;
    }
}
