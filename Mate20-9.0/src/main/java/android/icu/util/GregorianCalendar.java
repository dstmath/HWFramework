package android.icu.util;

import android.icu.impl.Grego;
import android.icu.lang.UCharacter;
import android.icu.util.ULocale;
import java.util.Date;
import java.util.Locale;

public class GregorianCalendar extends Calendar {
    public static final int AD = 1;
    public static final int BC = 0;
    private static final int EPOCH_YEAR = 1970;
    private static final int[][] LIMITS = {new int[]{0, 0, 1, 1}, new int[]{1, 1, 5828963, 5838270}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 28, 31}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 4, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5838270, -5838270, 5828964, 5838271}, new int[0], new int[]{-5838269, -5838269, 5828963, 5838270}, new int[0], new int[0], new int[0]};
    private static final int[][] MONTH_COUNT = {new int[]{31, 31, 0, 0}, new int[]{28, 29, 31, 31}, new int[]{31, 31, 59, 60}, new int[]{30, 30, 90, 91}, new int[]{31, 31, 120, 121}, new int[]{30, 30, 151, 152}, new int[]{31, 31, 181, 182}, new int[]{31, 31, 212, 213}, new int[]{30, 30, 243, 244}, new int[]{31, 31, UCharacter.UnicodeBlock.TANGUT_COMPONENTS_ID, UCharacter.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F_ID}, new int[]{30, 30, 304, 305}, new int[]{31, 31, 334, 335}};
    private static final long serialVersionUID = 9199388694351062137L;
    private transient int cutoverJulianDay;
    private long gregorianCutover;
    private transient int gregorianCutoverYear;
    protected transient boolean invertGregorian;
    protected transient boolean isGregorian;

    /* access modifiers changed from: protected */
    public int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    public GregorianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public GregorianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public GregorianCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, year);
        set(2, month);
        set(5, date);
    }

    public GregorianCalendar(int year, int month, int date, int hour, int minute) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
    }

    public GregorianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
        set(13, second);
    }

    public void setGregorianChange(Date date) {
        this.gregorianCutover = date.getTime();
        if (this.gregorianCutover <= Grego.MIN_MILLIS) {
            this.cutoverJulianDay = Integer.MIN_VALUE;
            this.gregorianCutoverYear = Integer.MIN_VALUE;
        } else if (this.gregorianCutover >= Grego.MAX_MILLIS) {
            this.cutoverJulianDay = Integer.MAX_VALUE;
            this.gregorianCutoverYear = Integer.MAX_VALUE;
        } else {
            this.cutoverJulianDay = (int) floorDivide(this.gregorianCutover, 86400000);
            GregorianCalendar cal = new GregorianCalendar(getTimeZone());
            cal.setTime(date);
            this.gregorianCutoverYear = cal.get(19);
        }
    }

    public final Date getGregorianChange() {
        return new Date(this.gregorianCutover);
    }

    public boolean isLeapYear(int year) {
        if (year >= this.gregorianCutoverYear) {
            if (year % 4 != 0) {
                return false;
            }
            if (year % 100 == 0 && year % 400 != 0) {
                return false;
            }
        } else if (year % 4 != 0) {
            return false;
        }
        return true;
    }

    public boolean isEquivalentTo(Calendar other) {
        return super.isEquivalentTo(other) && this.gregorianCutover == ((GregorianCalendar) other).gregorianCutover;
    }

    public int hashCode() {
        return super.hashCode() ^ ((int) this.gregorianCutover);
    }

    public void roll(int field, int amount) {
        if (field != 3) {
            super.roll(field, amount);
            return;
        }
        int woy = get(3);
        int isoYear = get(17);
        int isoDoy = internalGet(6);
        if (internalGet(2) == 0) {
            if (woy >= 52) {
                isoDoy += handleGetYearLength(isoYear);
            }
        } else if (woy == 1) {
            isoDoy -= handleGetYearLength(isoYear - 1);
        }
        int woy2 = woy + amount;
        if (woy2 < 1 || woy2 > 52) {
            int lastDoy = handleGetYearLength(isoYear);
            int lastRelDow = (((lastDoy - isoDoy) + internalGet(7)) - getFirstDayOfWeek()) % 7;
            if (lastRelDow < 0) {
                lastRelDow += 7;
            }
            if (6 - lastRelDow >= getMinimalDaysInFirstWeek()) {
                lastDoy -= 7;
            }
            int lastWoy = weekNumber(lastDoy, lastRelDow + 1);
            woy2 = (((woy2 + lastWoy) - 1) % lastWoy) + 1;
        }
        set(3, woy2);
        set(1, isoYear);
    }

    public int getActualMinimum(int field) {
        return getMinimum(field);
    }

    public int getActualMaximum(int field) {
        if (field != 1) {
            return super.getActualMaximum(field);
        }
        Calendar cal = (Calendar) clone();
        cal.setLenient(true);
        int era = cal.get(0);
        Date d = cal.getTime();
        int lowGood = LIMITS[1][1];
        int highBad = LIMITS[1][2] + 1;
        while (lowGood + 1 < highBad) {
            int y = (lowGood + highBad) / 2;
            cal.set(1, y);
            if (cal.get(1) == y && cal.get(0) == era) {
                lowGood = y;
            } else {
                highBad = y;
                cal.setTime(d);
            }
        }
        return lowGood;
    }

    /* access modifiers changed from: package-private */
    public boolean inDaylightTime() {
        boolean z = false;
        if (!getTimeZone().useDaylightTime()) {
            return false;
        }
        complete();
        if (internalGet(16) != 0) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public int handleGetMonthLength(int extendedYear, int month) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            extendedYear += floorDivide(month, 12, rem);
            month = rem[0];
        }
        return MONTH_COUNT[month][isLeapYear(extendedYear)];
    }

    /* access modifiers changed from: protected */
    public int handleGetYearLength(int eyear) {
        return isLeapYear(eyear) ? 366 : 365;
    }

    /* access modifiers changed from: protected */
    public void handleComputeFields(int julianDay) {
        int month;
        int dayOfYear;
        int dayOfMonth;
        int month2;
        int i = julianDay;
        if (i >= this.cutoverJulianDay) {
            int month3 = getGregorianMonth();
            dayOfMonth = getGregorianDayOfMonth();
            dayOfYear = getGregorianDayOfYear();
            month = month3;
            month2 = getGregorianYear();
        } else {
            long julianEpochDay = (long) (i - 1721424);
            month2 = (int) floorDivide((4 * julianEpochDay) + 1464, 1461);
            int dayOfYear2 = (int) (julianEpochDay - ((365 * (((long) month2) - 1)) + floorDivide(((long) month2) - 1, 4)));
            boolean isLeap = (month2 & 3) == 0;
            int correction = 0;
            if (dayOfYear2 >= (isLeap ? 60 : 59)) {
                correction = isLeap ? 1 : 2;
            }
            month = ((12 * (dayOfYear2 + correction)) + 6) / 367;
            dayOfYear = dayOfYear2 + 1;
            dayOfMonth = (dayOfYear2 - MONTH_COUNT[month][isLeap ? (char) 3 : 2]) + 1;
        }
        internalSet(2, month);
        internalSet(5, dayOfMonth);
        internalSet(6, dayOfYear);
        internalSet(19, month2);
        int era = 1;
        if (month2 < 1) {
            era = 0;
            month2 = 1 - month2;
        }
        internalSet(0, era);
        internalSet(1, month2);
    }

    /* access modifiers changed from: protected */
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
    public int handleComputeJulianDay(int bestField) {
        boolean z = false;
        this.invertGregorian = false;
        int jd = super.handleComputeJulianDay(bestField);
        boolean z2 = this.isGregorian;
        if (jd >= this.cutoverJulianDay) {
            z = true;
        }
        if (z2 == z) {
            return jd;
        }
        this.invertGregorian = true;
        return super.handleComputeJulianDay(bestField);
    }

    /* access modifiers changed from: protected */
    public int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        boolean z = false;
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += floorDivide(month, 12, rem);
            month = rem[0];
        }
        boolean isLeap = eyear % 4 == 0;
        int y = eyear - 1;
        int julianDay = (365 * y) + floorDivide(y, 4) + 1721423;
        this.isGregorian = eyear >= this.gregorianCutoverYear;
        if (this.invertGregorian) {
            this.isGregorian = !this.isGregorian;
        }
        char c = 2;
        if (this.isGregorian) {
            if (isLeap && (eyear % 100 != 0 || eyear % 400 == 0)) {
                z = true;
            }
            isLeap = z;
            julianDay += (floorDivide(y, 400) - floorDivide(y, 100)) + 2;
        }
        if (month == 0) {
            return julianDay;
        }
        int[] iArr = MONTH_COUNT[month];
        if (isLeap) {
            c = 3;
        }
        return julianDay + iArr[c];
    }

    public String getType() {
        return "gregorian";
    }
}
