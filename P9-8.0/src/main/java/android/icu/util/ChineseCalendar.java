package android.icu.util;

import android.icu.impl.CalendarAstronomer;
import android.icu.impl.CalendarCache;
import android.icu.text.DateFormat;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;

public class ChineseCalendar extends Calendar {
    private static final TimeZone CHINA_ZONE = new SimpleTimeZone(28800000, "CHINA_ZONE").freeze();
    static final int[][][] CHINESE_DATE_PRECEDENCE;
    private static final int CHINESE_EPOCH_YEAR = -2636;
    private static final int[][] LIMITS = new int[][]{new int[]{1, 1, 83333, 83333}, new int[]{1, 1, 60, 60}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 50, 55}, new int[0], new int[]{1, 1, 29, 30}, new int[]{1, 1, 353, 385}, new int[0], new int[]{-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[]{-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0], new int[]{0, 0, 1, 1}};
    private static final int SYNODIC_GAP = 25;
    private static final long serialVersionUID = 7312110751940929420L;
    private transient CalendarAstronomer astro;
    private int epochYear;
    private transient boolean isLeapYear;
    private transient CalendarCache newYearCache;
    private transient CalendarCache winterSolsticeCache;
    private TimeZone zoneAstro;

    public ChineseCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(Date date) {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
        setTime(date);
    }

    public ChineseCalendar(int year, int month, int isLeapMonth, int date) {
        this(year, month, isLeapMonth, date, 0, 0, 0);
    }

    public ChineseCalendar(int year, int month, int isLeapMonth, int date, int hour, int minute, int second) {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
        set(14, 0);
        set(1, year);
        set(2, month);
        set(22, isLeapMonth);
        set(5, date);
        set(11, hour);
        set(12, minute);
        set(13, second);
    }

    public ChineseCalendar(int era, int year, int month, int isLeapMonth, int date) {
        this(era, year, month, isLeapMonth, date, 0, 0, 0);
    }

    public ChineseCalendar(int era, int year, int month, int isLeapMonth, int date, int hour, int minute, int second) {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
        set(14, 0);
        set(0, era);
        set(1, year);
        set(2, month);
        set(22, isLeapMonth);
        set(5, date);
        set(11, hour);
        set(12, minute);
        set(13, second);
    }

    public ChineseCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), ULocale.forLocale(aLocale), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale), (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale, (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone zone, ULocale locale) {
        this(zone, locale, (int) CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    @Deprecated
    protected ChineseCalendar(TimeZone zone, ULocale locale, int epochYear, TimeZone zoneAstroCalc) {
        super(zone, locale);
        this.astro = new CalendarAstronomer();
        this.winterSolsticeCache = new CalendarCache();
        this.newYearCache = new CalendarCache();
        this.epochYear = epochYear;
        this.zoneAstro = zoneAstroCalc;
        setTimeInMillis(System.currentTimeMillis());
    }

    static {
        int[][][] iArr = new int[2][][];
        r1 = new int[9][];
        r1[0] = new int[]{5};
        r1[1] = new int[]{3, 7};
        r1[2] = new int[]{4, 7};
        r1[3] = new int[]{8, 7};
        r1[4] = new int[]{3, 18};
        r1[5] = new int[]{4, 18};
        r1[6] = new int[]{8, 18};
        r1[7] = new int[]{6};
        r1[8] = new int[]{37, 22};
        iArr[0] = r1;
        r1 = new int[5][];
        r1[0] = new int[]{3};
        r1[1] = new int[]{4};
        r1[2] = new int[]{8};
        r1[3] = new int[]{40, 7};
        r1[4] = new int[]{40, 18};
        iArr[1] = r1;
        CHINESE_DATE_PRECEDENCE = iArr;
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    protected int handleGetExtendedYear() {
        if (newestStamp(0, 1, 0) <= getStamp(19)) {
            return internalGet(19, 1);
        }
        return (((internalGet(0, 1) - 1) * 60) + internalGet(1, 1)) - (this.epochYear + 2636);
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        int thisStart = (handleComputeMonthStart(extendedYear, month, true) - 2440588) + 1;
        return newMoonNear(thisStart + 25, true) - thisStart;
    }

    protected DateFormat handleGetDateFormat(String pattern, String override, ULocale locale) {
        return super.handleGetDateFormat(pattern, override, locale);
    }

    protected int[][][] getFieldResolutionTable() {
        return CHINESE_DATE_PRECEDENCE;
    }

    private void offsetMonth(int newMoon, int dom, int delta) {
        int jd = ((2440588 + newMoonNear(newMoon + ((int) ((((double) delta) - 0.5d) * 29.530588853d)), true)) - 1) + dom;
        if (dom > 29) {
            set(20, jd - 1);
            complete();
            if (getActualMaximum(5) >= dom) {
                set(20, jd);
                return;
            }
            return;
        }
        set(20, jd);
    }

    public void add(int field, int amount) {
        switch (field) {
            case 2:
                if (amount != 0) {
                    int dom = get(5);
                    offsetMonth(((get(20) - 2440588) - dom) + 1, dom, amount);
                    return;
                }
                return;
            default:
                super.add(field, amount);
                return;
        }
    }

    public void roll(int field, int amount) {
        switch (field) {
            case 2:
                if (amount != 0) {
                    int dom = get(5);
                    int moon = ((get(20) - 2440588) - dom) + 1;
                    int m = get(2);
                    if (this.isLeapYear) {
                        if (get(22) == 1) {
                            m++;
                        } else if (isLeapMonthBetween(newMoonNear(moon - ((int) ((((double) m) - 0.5d) * 29.530588853d)), true), moon)) {
                            m++;
                        }
                    }
                    int n = this.isLeapYear ? 13 : 12;
                    int newM = (m + amount) % n;
                    if (newM < 0) {
                        newM += n;
                    }
                    if (newM != m) {
                        offsetMonth(moon, dom, newM - m);
                        return;
                    }
                    return;
                }
                return;
            default:
                super.roll(field, amount);
                return;
        }
    }

    private final long daysToMillis(int days) {
        long millis = ((long) days) * 86400000;
        return millis - ((long) this.zoneAstro.getOffset(millis));
    }

    private final int millisToDays(long millis) {
        return (int) Calendar.floorDivide(((long) this.zoneAstro.getOffset(millis)) + millis, 86400000);
    }

    private int winterSolstice(int gyear) {
        long cacheValue = this.winterSolsticeCache.get((long) gyear);
        if (cacheValue == CalendarCache.EMPTY) {
            this.astro.setTime(daysToMillis((computeGregorianMonthStart(gyear, 11) + 1) - 2440588));
            cacheValue = (long) millisToDays(this.astro.getSunTime(CalendarAstronomer.WINTER_SOLSTICE, true));
            this.winterSolsticeCache.put((long) gyear, cacheValue);
        }
        return (int) cacheValue;
    }

    private int newMoonNear(int days, boolean after) {
        this.astro.setTime(daysToMillis(days));
        return millisToDays(this.astro.getMoonTime(CalendarAstronomer.NEW_MOON, after));
    }

    private int synodicMonthsBetween(int day1, int day2) {
        return (int) Math.round(((double) (day2 - day1)) / 29.530588853d);
    }

    private int majorSolarTerm(int days) {
        this.astro.setTime(daysToMillis(days));
        int term = (((int) Math.floor((this.astro.getSunLongitude() * 6.0d) / 3.141592653589793d)) + 2) % 12;
        if (term < 1) {
            return term + 12;
        }
        return term;
    }

    private boolean hasNoMajorSolarTerm(int newMoon) {
        if (majorSolarTerm(newMoon) == majorSolarTerm(newMoonNear(newMoon + 25, true))) {
            return true;
        }
        return false;
    }

    private boolean isLeapMonthBetween(int newMoon1, int newMoon2) {
        if (synodicMonthsBetween(newMoon1, newMoon2) >= 50) {
            throw new IllegalArgumentException("isLeapMonthBetween(" + newMoon1 + ", " + newMoon2 + "): Invalid parameters");
        } else if (newMoon2 < newMoon1) {
            return false;
        } else {
            if (isLeapMonthBetween(newMoon1, newMoonNear(newMoon2 - 25, false))) {
                return true;
            }
            return hasNoMajorSolarTerm(newMoon2);
        }
    }

    protected void handleComputeFields(int julianDay) {
        computeChineseFields(julianDay - 2440588, getGregorianYear(), getGregorianMonth(), true);
    }

    private void computeChineseFields(int days, int gyear, int gmonth, boolean setAllFields) {
        int solsticeBefore;
        int isLeapMonth;
        int solsticeAfter = winterSolstice(gyear);
        if (days < solsticeAfter) {
            solsticeBefore = winterSolstice(gyear - 1);
        } else {
            solsticeBefore = solsticeAfter;
            solsticeAfter = winterSolstice(gyear + 1);
        }
        int firstMoon = newMoonNear(solsticeBefore + 1, true);
        int lastMoon = newMoonNear(solsticeAfter + 1, false);
        int thisMoon = newMoonNear(days + 1, false);
        this.isLeapYear = synodicMonthsBetween(firstMoon, lastMoon) == 12;
        int month = synodicMonthsBetween(firstMoon, thisMoon);
        if (this.isLeapYear && isLeapMonthBetween(firstMoon, thisMoon)) {
            month--;
        }
        if (month < 1) {
            month += 12;
        }
        if (this.isLeapYear && hasNoMajorSolarTerm(thisMoon)) {
            isLeapMonth = isLeapMonthBetween(firstMoon, newMoonNear(thisMoon - 25, false)) ^ 1;
        } else {
            isLeapMonth = 0;
        }
        internalSet(2, month - 1);
        internalSet(22, isLeapMonth != 0 ? 1 : 0);
        if (setAllFields) {
            int extended_year = gyear - this.epochYear;
            int cycle_year = gyear + 2636;
            if (month < 11 || gmonth >= 6) {
                extended_year++;
                cycle_year++;
            }
            int dayOfMonth = (days - thisMoon) + 1;
            internalSet(19, extended_year);
            int[] yearOfCycle = new int[1];
            internalSet(0, Calendar.floorDivide(cycle_year - 1, 60, yearOfCycle) + 1);
            internalSet(1, yearOfCycle[0] + 1);
            internalSet(5, dayOfMonth);
            int newYear = newYear(gyear);
            if (days < newYear) {
                newYear = newYear(gyear - 1);
            }
            internalSet(6, (days - newYear) + 1);
        }
    }

    private int newYear(int gyear) {
        long cacheValue = this.newYearCache.get((long) gyear);
        if (cacheValue == CalendarCache.EMPTY) {
            int solsticeBefore = winterSolstice(gyear - 1);
            int solsticeAfter = winterSolstice(gyear);
            int newMoon1 = newMoonNear(solsticeBefore + 1, true);
            int newMoon2 = newMoonNear(newMoon1 + 25, true);
            if (synodicMonthsBetween(newMoon1, newMoonNear(solsticeAfter + 1, false)) == 12 && (hasNoMajorSolarTerm(newMoon1) || hasNoMajorSolarTerm(newMoon2))) {
                cacheValue = (long) newMoonNear(newMoon2 + 25, true);
            } else {
                cacheValue = (long) newMoon2;
            }
            this.newYearCache.put((long) gyear, cacheValue);
        }
        return (int) cacheValue;
    }

    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += Calendar.floorDivide(month, 12, rem);
            month = rem[0];
        }
        int newMoon = newMoonNear((month * 29) + newYear((this.epochYear + eyear) - 1), true);
        int julianDay = newMoon + 2440588;
        int saveMonth = internalGet(2);
        int saveIsLeapMonth = internalGet(22);
        int isLeapMonth = useMonth ? saveIsLeapMonth : 0;
        computeGregorianFields(julianDay);
        computeChineseFields(newMoon, getGregorianYear(), getGregorianMonth(), false);
        if (!(month == internalGet(2) && isLeapMonth == internalGet(22))) {
            julianDay = newMoonNear(newMoon + 25, true) + 2440588;
        }
        internalSet(2, saveMonth);
        internalSet(22, saveIsLeapMonth);
        return julianDay - 1;
    }

    public String getType() {
        return "chinese";
    }

    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.epochYear = CHINESE_EPOCH_YEAR;
        this.zoneAstro = CHINA_ZONE;
        stream.defaultReadObject();
        this.astro = new CalendarAstronomer();
        this.winterSolsticeCache = new CalendarCache();
        this.newYearCache = new CalendarCache();
    }
}
