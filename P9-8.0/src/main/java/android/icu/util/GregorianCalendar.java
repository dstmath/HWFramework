package android.icu.util;

import android.icu.impl.Grego;
import android.icu.lang.UCharacter.UnicodeBlock;
import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;

public class GregorianCalendar extends Calendar {
    public static final int AD = 1;
    public static final int BC = 0;
    private static final int EPOCH_YEAR = 1970;
    private static final int[][] LIMITS = new int[][]{new int[]{0, 0, 1, 1}, new int[]{1, 1, 5828963, 5838270}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 52, 53}, new int[0], new int[]{1, 1, 28, 31}, new int[]{1, 1, 365, 366}, new int[0], new int[]{-1, -1, 4, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5838270, -5838270, 5828964, 5838271}, new int[0], new int[]{-5838269, -5838269, 5828963, 5838270}, new int[0], new int[0], new int[0]};
    private static final int[][] MONTH_COUNT = new int[][]{new int[]{31, 31, 0, 0}, new int[]{28, 29, 31, 31}, new int[]{31, 31, 59, 60}, new int[]{30, 30, 90, 91}, new int[]{31, 31, 120, 121}, new int[]{30, 30, 151, 152}, new int[]{31, 31, 181, 182}, new int[]{31, 31, 212, 213}, new int[]{30, 30, 243, 244}, new int[]{31, 31, UnicodeBlock.TANGUT_COMPONENTS_ID, UnicodeBlock.COUNT}, new int[]{30, 30, 304, 305}, new int[]{31, 31, 334, 335}};
    private static final long serialVersionUID = 9199388694351062137L;
    private transient int cutoverJulianDay;
    private long gregorianCutover;
    private transient int gregorianCutoverYear;
    protected transient boolean invertGregorian;
    protected transient boolean isGregorian;

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    public GregorianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public GregorianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
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
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(0, 1);
        set(1, year);
        set(2, month);
        set(5, date);
    }

    public GregorianCalendar(int year, int month, int date, int hour, int minute) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
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
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
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
            this.cutoverJulianDay = (int) Calendar.floorDivide(this.gregorianCutover, 86400000);
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
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        } else {
            if (year % 4 != 0) {
                return false;
            }
            return true;
        }
    }

    public boolean isEquivalentTo(Calendar other) {
        if (super.isEquivalentTo(other) && this.gregorianCutover == ((GregorianCalendar) other).gregorianCutover) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ ((int) this.gregorianCutover);
    }

    public void roll(int field, int amount) {
        switch (field) {
            case 3:
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
                woy += amount;
                if (woy < 1 || woy > 52) {
                    int lastDoy = handleGetYearLength(isoYear);
                    int lastRelDow = (((lastDoy - isoDoy) + internalGet(7)) - getFirstDayOfWeek()) % 7;
                    if (lastRelDow < 0) {
                        lastRelDow += 7;
                    }
                    if (6 - lastRelDow >= getMinimalDaysInFirstWeek()) {
                        lastDoy -= 7;
                    }
                    int lastWoy = weekNumber(lastDoy, lastRelDow + 1);
                    woy = (((woy + lastWoy) - 1) % lastWoy) + 1;
                }
                set(3, woy);
                set(1, isoYear);
                return;
            default:
                super.roll(field, amount);
                return;
        }
    }

    public int getActualMinimum(int field) {
        return getMinimum(field);
    }

    public int getActualMaximum(int field) {
        switch (field) {
            case 1:
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
            default:
                return super.getActualMaximum(field);
        }
    }

    boolean inDaylightTime() {
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

    protected int handleGetMonthLength(int extendedYear, int month) {
        int i = 1;
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            extendedYear += Calendar.floorDivide(month, 12, rem);
            month = rem[0];
        }
        int[] iArr = MONTH_COUNT[month];
        if (!isLeapYear(extendedYear)) {
            i = 0;
        }
        return iArr[i];
    }

    protected int handleGetYearLength(int eyear) {
        return isLeapYear(eyear) ? 366 : 365;
    }

    protected void handleComputeFields(int julianDay) {
        int month;
        int dayOfMonth;
        int dayOfYear;
        int eyear;
        if (julianDay >= this.cutoverJulianDay) {
            month = getGregorianMonth();
            dayOfMonth = getGregorianDayOfMonth();
            dayOfYear = getGregorianDayOfYear();
            eyear = getGregorianYear();
        } else {
            long julianEpochDay = (long) (julianDay - 1721424);
            eyear = (int) Calendar.floorDivide((4 * julianEpochDay) + 1464, 1461);
            dayOfYear = (int) (julianEpochDay - (((((long) eyear) - 1) * 365) + Calendar.floorDivide(((long) eyear) - 1, 4)));
            boolean isLeap = (eyear & 3) == 0;
            int correction = 0;
            if (dayOfYear >= (isLeap ? 60 : 59)) {
                correction = isLeap ? 1 : 2;
            }
            month = (((dayOfYear + correction) * 12) + 6) / 367;
            dayOfMonth = (dayOfYear - MONTH_COUNT[month][isLeap ? 3 : 2]) + 1;
            dayOfYear++;
        }
        internalSet(2, month);
        internalSet(5, dayOfMonth);
        internalSet(6, dayOfYear);
        internalSet(19, eyear);
        int era = 1;
        if (eyear < 1) {
            era = 0;
            eyear = 1 - eyear;
        }
        internalSet(0, era);
        internalSet(1, eyear);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, EPOCH_YEAR);
        }
        if (internalGet(0, 1) == 0) {
            return 1 - internalGet(1, 1);
        }
        return internalGet(1, EPOCH_YEAR);
    }

    protected int handleComputeJulianDay(int bestField) {
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

    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        boolean z = true;
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += Calendar.floorDivide(month, 12, rem);
            month = rem[0];
        }
        boolean isLeap = eyear % 4 == 0;
        int y = eyear - 1;
        int julianDay = ((y * 365) + Calendar.floorDivide(y, 4)) + 1721423;
        if (eyear < this.gregorianCutoverYear) {
            z = false;
        }
        this.isGregorian = z;
        if (this.invertGregorian) {
            this.isGregorian ^= 1;
        }
        if (this.isGregorian) {
            isLeap = isLeap && (eyear % 100 != 0 || eyear % 400 == 0);
            julianDay += (Calendar.floorDivide(y, 400) - Calendar.floorDivide(y, 100)) + 2;
        }
        if (month == 0) {
            return julianDay;
        }
        return julianDay + MONTH_COUNT[month][isLeap ? 3 : 2];
    }

    public String getType() {
        return "gregorian";
    }
}
