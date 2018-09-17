package android.icu.util;

import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;

abstract class CECalendar extends Calendar {
    private static final int[][] LIMITS = new int[][]{new int[]{0, 0, 1, 1}, new int[]{1, 1, 5000000, 5000000}, new int[]{0, 0, 12, 12}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 5, 30}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 1, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0]};
    private static final long serialVersionUID = -999547623066414271L;

    protected abstract int getJDEpochOffset();

    protected CECalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    protected CECalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    protected CECalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    protected CECalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    protected CECalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    protected CECalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    protected CECalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(year, month, date);
    }

    protected CECalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        setTime(date);
    }

    protected CECalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(year, month, date, hour, minute, second);
    }

    protected int handleComputeMonthStart(int eyear, int emonth, boolean useMonth) {
        return ceToJD((long) eyear, emonth, 0, getJDEpochOffset());
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        if ((month + 1) % 13 != 0) {
            return 30;
        }
        return ((extendedYear % 4) / 3) + 5;
    }

    public static int ceToJD(long year, int month, int day, int jdEpochOffset) {
        if (month >= 0) {
            year += (long) (month / 13);
            month %= 13;
        } else {
            month++;
            year += (long) ((month / 13) - 1);
            month = (month % 13) + 12;
        }
        return (int) (((((((long) jdEpochOffset) + (365 * year)) + Calendar.floorDivide(year, 4)) + ((long) (month * 30))) + ((long) day)) - 1);
    }

    public static void jdToCE(int julianDay, int jdEpochOffset, int[] fields) {
        int[] r4 = new int[]{(Calendar.floorDivide(julianDay - jdEpochOffset, 1461, r4) * 4) + ((r4[0] / 365) - (r4[0] / 1460))};
        int doy = r4[0] == 1460 ? 365 : r4[0] % 365;
        fields[1] = doy / 30;
        fields[2] = (doy % 30) + 1;
    }
}
