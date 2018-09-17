package android.icu.util;

import android.icu.impl.CalendarCache;
import android.icu.util.ULocale.Category;
import dalvik.bytecode.Opcodes;
import java.util.Date;
import java.util.Locale;

public class HebrewCalendar extends Calendar {
    public static final int ADAR = 6;
    public static final int ADAR_1 = 5;
    public static final int AV = 11;
    private static final long BAHARAD = 12084;
    private static final long DAY_PARTS = 25920;
    public static final int ELUL = 12;
    public static final int HESHVAN = 1;
    private static final long HOUR_PARTS = 1080;
    public static final int IYAR = 8;
    public static final int KISLEV = 2;
    private static final int[][] LEAP_MONTH_START = null;
    private static final int[][] LIMITS = null;
    private static final int MONTH_DAYS = 29;
    private static final long MONTH_FRACT = 13753;
    private static final int[][] MONTH_LENGTH = null;
    private static final long MONTH_PARTS = 765433;
    private static final int[][] MONTH_START = null;
    public static final int NISAN = 7;
    public static final int SHEVAT = 4;
    public static final int SIVAN = 9;
    public static final int TAMUZ = 10;
    public static final int TEVET = 3;
    public static final int TISHRI = 0;
    private static CalendarCache cache = null;
    private static final long serialVersionUID = -1952524560588825816L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.HebrewCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.HebrewCalendar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.HebrewCalendar.<clinit>():void");
    }

    public HebrewCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public HebrewCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    public HebrewCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public HebrewCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public HebrewCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public HebrewCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public HebrewCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(HESHVAN, year);
        set(KISLEV, month);
        set(ADAR_1, date);
    }

    public HebrewCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        setTime(date);
    }

    public HebrewCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(HESHVAN, year);
        set(KISLEV, month);
        set(ADAR_1, date);
        set(AV, hour);
        set(ELUL, minute);
        set(13, second);
    }

    public void add(int field, int amount) {
        switch (field) {
            case KISLEV /*2*/:
                int month = get(KISLEV);
                int year = get(HESHVAN);
                boolean acrossAdar1;
                if (amount > 0) {
                    acrossAdar1 = month < ADAR_1;
                    month += amount;
                    while (true) {
                        if (acrossAdar1 && month >= ADAR_1 && !isLeapYear(year)) {
                            month += HESHVAN;
                        }
                        if (month > ELUL) {
                            month -= 13;
                            year += HESHVAN;
                            acrossAdar1 = true;
                        }
                    }
                } else {
                    acrossAdar1 = month > ADAR_1;
                    month += amount;
                    while (true) {
                        if (acrossAdar1 && month <= ADAR_1 && !isLeapYear(year)) {
                            month--;
                        }
                        if (month < 0) {
                            month += 13;
                            year--;
                            acrossAdar1 = true;
                        }
                    }
                }
                set(KISLEV, month);
                set(HESHVAN, year);
                pinField(ADAR_1);
            default:
                super.add(field, amount);
        }
    }

    public void roll(int field, int amount) {
        switch (field) {
            case KISLEV /*2*/:
                int month = get(KISLEV);
                int year = get(HESHVAN);
                int newMonth = month + (amount % monthsInYear(year));
                if (!isLeapYear(year)) {
                    if (amount > 0 && month < ADAR_1 && newMonth >= ADAR_1) {
                        newMonth += HESHVAN;
                    } else if (amount < 0 && month > ADAR_1 && newMonth <= ADAR_1) {
                        newMonth--;
                    }
                }
                set(KISLEV, (newMonth + 13) % 13);
                pinField(ADAR_1);
            default:
                super.roll(field, amount);
        }
    }

    private static long startOfYear(int year) {
        long day = cache.get((long) year);
        if (day == CalendarCache.EMPTY) {
            int months = ((year * Opcodes.OP_SPUT_WIDE_VOLATILE) - 234) / 19;
            long frac = (((long) months) * MONTH_FRACT) + BAHARAD;
            day = ((long) (months * MONTH_DAYS)) + (frac / DAY_PARTS);
            frac %= DAY_PARTS;
            int wd = (int) (day % 7);
            if (!(wd == KISLEV || wd == SHEVAT)) {
                if (wd == ADAR) {
                }
                if (wd != HESHVAN && frac > 16404 && !isLeapYear(year)) {
                    day += 2;
                } else if (wd == 0 && frac > 23269 && isLeapYear(year - 1)) {
                    day++;
                }
                cache.put((long) year, day);
            }
            day++;
            wd = (int) (day % 7);
            if (wd != HESHVAN) {
            }
            day++;
            cache.put((long) year, day);
        }
        return day;
    }

    private final int yearType(int year) {
        int yearLength = handleGetYearLength(year);
        if (yearLength > 380) {
            yearLength -= 30;
        }
        switch (yearLength) {
            case 353:
                return TISHRI;
            case 354:
                return HESHVAN;
            case 355:
                return KISLEV;
            default:
                throw new IllegalArgumentException("Illegal year length " + yearLength + " in year " + year);
        }
    }

    @Deprecated
    public static boolean isLeapYear(int year) {
        int x = ((year * ELUL) + 17) % 19;
        if (x >= (x < 0 ? -7 : ELUL)) {
            return true;
        }
        return false;
    }

    private static int monthsInYear(int year) {
        return isLeapYear(year) ? 13 : ELUL;
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        while (month < 0) {
            extendedYear--;
            month += monthsInYear(extendedYear);
        }
        int extendedYear2 = extendedYear;
        while (month > ELUL) {
            month -= monthsInYear(extendedYear2);
            extendedYear2 += HESHVAN;
        }
        switch (month) {
            case HESHVAN /*1*/:
            case KISLEV /*2*/:
                return MONTH_LENGTH[month][yearType(extendedYear2)];
            default:
                return MONTH_LENGTH[month][TISHRI];
        }
    }

    protected int handleGetYearLength(int eyear) {
        return (int) (startOfYear(eyear + HESHVAN) - startOfYear(eyear));
    }

    @Deprecated
    protected void validateField(int field) {
        if (field == KISLEV && !isLeapYear(handleGetExtendedYear()) && internalGet(KISLEV) == ADAR_1) {
            throw new IllegalArgumentException("MONTH cannot be ADAR_1(5) except leap years");
        }
        super.validateField(field);
    }

    protected void handleComputeFields(int julianDay) {
        long d = (long) (julianDay - 347997);
        int year = ((int) (((19 * ((DAY_PARTS * d) / MONTH_PARTS)) + 234) / 235)) + HESHVAN;
        int dayOfYear = (int) (d - startOfYear(year));
        while (dayOfYear < HESHVAN) {
            year--;
            dayOfYear = (int) (d - startOfYear(year));
        }
        int yearType = yearType(year);
        int[][] monthStart = isLeapYear(year) ? LEAP_MONTH_START : MONTH_START;
        int month = TISHRI;
        while (dayOfYear > monthStart[month][yearType]) {
            month += HESHVAN;
        }
        month--;
        int dayOfMonth = dayOfYear - monthStart[month][yearType];
        internalSet(TISHRI, TISHRI);
        internalSet(HESHVAN, year);
        internalSet(19, year);
        internalSet(KISLEV, month);
        internalSet(ADAR_1, dayOfMonth);
        internalSet(ADAR, dayOfYear);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, HESHVAN) == 19) {
            return internalGet(19, HESHVAN);
        }
        return internalGet(HESHVAN, HESHVAN);
    }

    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        while (month < 0) {
            eyear--;
            month += monthsInYear(eyear);
        }
        int eyear2 = eyear;
        while (month > ELUL) {
            month -= monthsInYear(eyear2);
            eyear2 += HESHVAN;
        }
        long day = startOfYear(eyear2);
        if (month != 0) {
            if (isLeapYear(eyear2)) {
                day += (long) LEAP_MONTH_START[month][yearType(eyear2)];
            } else {
                day += (long) MONTH_START[month][yearType(eyear2)];
            }
        }
        return (int) (347997 + day);
    }

    public String getType() {
        return "hebrew";
    }
}
