package android.icu.util;

import android.icu.util.ULocale;
import java.util.Date;
import java.util.Locale;

public class IndianCalendar extends Calendar {
    public static final int AGRAHAYANA = 8;
    public static final int ASADHA = 3;
    public static final int ASVINA = 6;
    public static final int BHADRA = 5;
    public static final int CHAITRA = 0;
    public static final int IE = 0;
    private static final int INDIAN_ERA_START = 78;
    private static final int INDIAN_YEAR_START = 80;
    public static final int JYAISTHA = 2;
    public static final int KARTIKA = 7;
    private static final int[][] LIMITS = {new int[]{0, 0, 0, 0}, new int[]{-5000000, -5000000, 5000000, 5000000}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 30, 31}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0]};
    public static final int MAGHA = 10;
    public static final int PAUSA = 9;
    public static final int PHALGUNA = 11;
    public static final int SRAVANA = 4;
    public static final int VAISAKHA = 1;
    private static final long serialVersionUID = 3617859668165014834L;

    public IndianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public IndianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public IndianCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public IndianCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public IndianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public IndianCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public IndianCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        setTime(date);
    }

    public IndianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        set(1, year);
        set(2, month);
        set(5, date);
    }

    public IndianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
        set(13, second);
    }

    /* access modifiers changed from: protected */
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, 1);
        }
        return internalGet(1, 1);
    }

    /* access modifiers changed from: protected */
    public int handleGetYearLength(int extendedYear) {
        return super.handleGetYearLength(extendedYear);
    }

    /* access modifiers changed from: protected */
    public int handleGetMonthLength(int extendedYear, int month) {
        if (month < 0 || month > 11) {
            int[] remainder = new int[1];
            extendedYear += floorDivide(month, 12, remainder);
            month = remainder[0];
        }
        if (isGregorianLeap(extendedYear + 78) && month == 0) {
            return 31;
        }
        if (month < 1 || month > 5) {
            return 30;
        }
        return 31;
    }

    /* access modifiers changed from: protected */
    public void handleComputeFields(int julianDay) {
        int yday;
        int IndianDayOfMonth;
        int IndianMonth;
        int[] gregorianDay = jdToGregorian((double) julianDay);
        int IndianYear = gregorianDay[0] - 78;
        int yday2 = (int) (((double) julianDay) - gregorianToJD(gregorianDay[0], 1, 1));
        int leapMonth = 30;
        if (yday2 < 80) {
            IndianYear--;
            if (isGregorianLeap(gregorianDay[0] - 1)) {
                leapMonth = 31;
            }
            yday = yday2 + leapMonth + 155 + 90 + 10;
        } else {
            if (isGregorianLeap(gregorianDay[0])) {
                leapMonth = 31;
            }
            yday = yday2 - 80;
        }
        if (yday < leapMonth) {
            IndianDayOfMonth = 0;
            IndianMonth = yday + 1;
        } else {
            int mday = yday - leapMonth;
            if (mday < 155) {
                IndianDayOfMonth = (mday / 31) + 1;
                IndianMonth = (mday % 31) + 1;
            } else {
                int mday2 = mday - 155;
                IndianDayOfMonth = (mday2 / 30) + 6;
                IndianMonth = (mday2 % 30) + 1;
            }
        }
        internalSet(0, 0);
        internalSet(19, IndianYear);
        internalSet(1, IndianYear);
        internalSet(2, IndianDayOfMonth);
        internalSet(5, IndianMonth);
        internalSet(6, yday + 1);
    }

    /* access modifiers changed from: protected */
    public int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    /* access modifiers changed from: protected */
    public int handleComputeMonthStart(int year, int month, boolean useMonth) {
        if (month < 0 || month > 11) {
            year += month / 12;
            month %= 12;
        }
        return (int) IndianToJD(year, month + 1, 1);
    }

    private static double IndianToJD(int year, int month, int date) {
        double start;
        int leapMonth;
        int gyear = year + 78;
        if (isGregorianLeap(gyear)) {
            leapMonth = 31;
            start = gregorianToJD(gyear, 3, 21);
        } else {
            leapMonth = 30;
            start = gregorianToJD(gyear, 3, 22);
        }
        if (month == 1) {
            return ((double) (date - 1)) + start;
        }
        double jd = ((double) leapMonth) + start + ((double) (Math.min(month - 2, 5) * 31));
        if (month >= 8) {
            jd += (double) ((month - 7) * 30);
        }
        return jd + ((double) (date - 1));
    }

    private static double gregorianToJD(int year, int month, int date) {
        int y = year - 1;
        return ((double) ((((((((365 * y) + (y / 4)) - (y / 100)) + (y / 400)) + (((367 * month) - 362) / 12)) + (month <= 2 ? 0 : isGregorianLeap(year) ? -1 : -2)) + date) - 1)) + 1721425.5d;
    }

    private static int[] jdToGregorian(double jd) {
        double wjd = Math.floor(jd - 0.5d) + 0.5d;
        double depoch = wjd - 1721425.5d;
        double quadricent = Math.floor(depoch / 146097.0d);
        double dqc = depoch % 146097.0d;
        double cent = Math.floor(dqc / 36524.0d);
        double dcent = dqc % 36524.0d;
        double JULIAN_EPOCH = Math.floor(dcent / 1461.0d);
        double d = depoch;
        double yindex = Math.floor((dcent % 1461.0d) / 365.0d);
        double d2 = JULIAN_EPOCH;
        int year = (int) ((400.0d * quadricent) + (100.0d * cent) + (JULIAN_EPOCH * 4.0d) + yindex);
        if (!(cent == 4.0d || yindex == 4.0d)) {
            year++;
        }
        double yearday = wjd - gregorianToJD(year, 1, 1);
        double d3 = yindex;
        double leapadj = (double) (wjd < gregorianToJD(year, 3, 1) ? 0 : isGregorianLeap(year) ? 1 : 2);
        double d4 = leapadj;
        int month = (int) Math.floor((((yearday + leapadj) * 12.0d) + 373.0d) / 367.0d);
        return new int[]{year, month, ((int) (wjd - gregorianToJD(year, month, 1))) + 1};
    }

    private static boolean isGregorianLeap(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    public String getType() {
        return "indian";
    }
}
