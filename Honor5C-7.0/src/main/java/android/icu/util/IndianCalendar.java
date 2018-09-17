package android.icu.util;

import android.icu.text.BreakIterator;
import android.icu.util.ULocale.Category;
import dalvik.bytecode.Opcodes;
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
    private static final int[][] LIMITS = null;
    public static final int MAGHA = 10;
    public static final int PAUSA = 9;
    public static final int PHALGUNA = 11;
    public static final int SRAVANA = 4;
    public static final int VAISAKHA = 1;
    private static final long serialVersionUID = 3617859668165014834L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.IndianCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.IndianCalendar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.IndianCalendar.<clinit>():void");
    }

    public IndianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public IndianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
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
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        setTime(date);
    }

    public IndianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(VAISAKHA, year);
        set(JYAISTHA, month);
        set(BHADRA, date);
    }

    public IndianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(VAISAKHA, year);
        set(JYAISTHA, month);
        set(BHADRA, date);
        set(PHALGUNA, hour);
        set(12, minute);
        set(13, second);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, VAISAKHA) == 19) {
            return internalGet(19, VAISAKHA);
        }
        return internalGet(VAISAKHA, VAISAKHA);
    }

    protected int handleGetYearLength(int extendedYear) {
        return super.handleGetYearLength(extendedYear);
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        if (month < 0 || month > PHALGUNA) {
            int[] remainder = new int[VAISAKHA];
            extendedYear += Calendar.floorDivide(month, 12, remainder);
            month = remainder[IE];
        }
        if (isGregorianLeap(extendedYear + INDIAN_ERA_START) && month == 0) {
            return 31;
        }
        if (month < VAISAKHA || month > BHADRA) {
            return 30;
        }
        return 31;
    }

    protected void handleComputeFields(int julianDay) {
        int leapMonth;
        int IndianMonth;
        int IndianDayOfMonth;
        int[] gregorianDay = jdToGregorian((double) julianDay);
        int IndianYear = gregorianDay[IE] - 78;
        int yday = (int) (((double) julianDay) - gregorianToJD(gregorianDay[IE], VAISAKHA, VAISAKHA));
        if (yday < INDIAN_YEAR_START) {
            IndianYear--;
            leapMonth = isGregorianLeap(gregorianDay[IE] + -1) ? 31 : 30;
            yday += ((leapMonth + Opcodes.OP_ADD_LONG) + 90) + MAGHA;
        } else {
            leapMonth = isGregorianLeap(gregorianDay[IE]) ? 31 : 30;
            yday -= 80;
        }
        if (yday < leapMonth) {
            IndianMonth = IE;
            IndianDayOfMonth = yday + VAISAKHA;
        } else {
            int mday = yday - leapMonth;
            if (mday < Opcodes.OP_ADD_LONG) {
                IndianMonth = (mday / 31) + VAISAKHA;
                IndianDayOfMonth = (mday % 31) + VAISAKHA;
            } else {
                mday -= 155;
                IndianMonth = (mday / 30) + ASVINA;
                IndianDayOfMonth = (mday % 30) + VAISAKHA;
            }
        }
        internalSet(IE, IE);
        internalSet(19, IndianYear);
        internalSet(VAISAKHA, IndianYear);
        internalSet(JYAISTHA, IndianMonth);
        internalSet(BHADRA, IndianDayOfMonth);
        internalSet(ASVINA, yday + VAISAKHA);
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    protected int handleComputeMonthStart(int year, int month, boolean useMonth) {
        int imonth;
        if (month < 0 || month > PHALGUNA) {
            year += month / 12;
            month %= 12;
        }
        if (month == 12) {
            imonth = VAISAKHA;
        } else {
            imonth = month + VAISAKHA;
        }
        return (int) IndianToJD(year, imonth, VAISAKHA);
    }

    private static double IndianToJD(int year, int month, int date) {
        int leapMonth;
        double start;
        int gyear = year + INDIAN_ERA_START;
        if (isGregorianLeap(gyear)) {
            leapMonth = 31;
            start = gregorianToJD(gyear, ASADHA, 21);
        } else {
            leapMonth = 30;
            start = gregorianToJD(gyear, ASADHA, 22);
        }
        if (month == VAISAKHA) {
            return start + ((double) (date - 1));
        }
        double jd = (start + ((double) leapMonth)) + ((double) (Math.min(month - 2, BHADRA) * 31));
        if (month >= AGRAHAYANA) {
            jd += (double) ((month - 7) * 30);
        }
        return jd + ((double) (date - 1));
    }

    private static double gregorianToJD(int year, int month, int date) {
        int y = year - 1;
        int i = (((month * 367) - 362) / 12) + ((((y * 365) + (y / SRAVANA)) - (y / 100)) + (y / BreakIterator.WORD_KANA_LIMIT));
        int i2 = month <= JYAISTHA ? IE : isGregorianLeap(year) ? -1 : -2;
        return ((double) (((i2 + i) + date) - 1)) + 1721425.5d;
    }

    private static int[] jdToGregorian(double jd) {
        double wjd = Math.floor(jd - 0.5d) + 0.5d;
        double depoch = wjd - 1721425.5d;
        double quadricent = Math.floor(depoch / 146097.0d);
        double dqc = depoch % 146097.0d;
        double cent = Math.floor(dqc / 36524.0d);
        double dcent = dqc % 36524.0d;
        double quad = Math.floor(dcent / 1461.0d);
        double yindex = Math.floor((dcent % 1461.0d) / 365.0d);
        int year = (int) ((((400.0d * quadricent) + (100.0d * cent)) + (4.0d * quad)) + yindex);
        Object obj = (cent == 4.0d || yindex == 4.0d) ? VAISAKHA : null;
        if (obj == null) {
            year += VAISAKHA;
        }
        double yearday = wjd - gregorianToJD(year, VAISAKHA, VAISAKHA);
        int i = wjd < gregorianToJD(year, ASADHA, VAISAKHA) ? IE : isGregorianLeap(year) ? VAISAKHA : JYAISTHA;
        int month = (int) Math.floor((((yearday + ((double) i)) * 12.0d) + 373.0d) / 367.0d);
        int day = ((int) (wjd - gregorianToJD(year, month, VAISAKHA))) + VAISAKHA;
        int[] julianDate = new int[ASADHA];
        julianDate[IE] = year;
        julianDate[VAISAKHA] = month;
        julianDate[JYAISTHA] = day;
        return julianDate;
    }

    private static boolean isGregorianLeap(int year) {
        if (year % SRAVANA == 0) {
            return year % 100 != 0 || year % BreakIterator.WORD_KANA_LIMIT == 0;
        } else {
            return false;
        }
    }

    public String getType() {
        return "indian";
    }
}
