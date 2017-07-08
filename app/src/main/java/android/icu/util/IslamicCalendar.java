package android.icu.util;

import android.icu.impl.CalendarAstronomer;
import android.icu.impl.CalendarCache;
import android.icu.impl.CalendarUtil;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;
import libcore.icu.RelativeDateTimeFormatter;

public class IslamicCalendar extends Calendar {
    private static final long ASTRONOMICAL_EPOC = 1948439;
    private static final long CIVIL_EPOC = 1948440;
    public static final int DHU_AL_HIJJAH = 11;
    public static final int DHU_AL_QIDAH = 10;
    private static final long HIJRA_MILLIS = -42521587200000L;
    public static final int JUMADA_1 = 4;
    public static final int JUMADA_2 = 5;
    private static final int[][] LIMITS = null;
    public static final int MUHARRAM = 0;
    public static final int RABI_1 = 2;
    public static final int RABI_2 = 3;
    public static final int RAJAB = 6;
    public static final int RAMADAN = 8;
    public static final int SAFAR = 1;
    public static final int SHABAN = 7;
    public static final int SHAWWAL = 9;
    private static final int[] UMALQURA_MONTHLENGTH = null;
    private static final int UMALQURA_YEAR_END = 1600;
    private static final int UMALQURA_YEAR_START = 1300;
    private static final byte[] UMALQURA_YEAR_START_ESTIMATE_FIX = null;
    private static CalendarAstronomer astro = null;
    private static CalendarCache cache = null;
    private static final long serialVersionUID = -6253365474073869325L;
    private CalculationType cType;
    private boolean civil;

    public enum CalculationType {
        ;
        
        private String bcpType;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.IslamicCalendar.CalculationType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.IslamicCalendar.CalculationType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.IslamicCalendar.CalculationType.<clinit>():void");
        }

        private CalculationType(String bcpType) {
            this.bcpType = bcpType;
        }

        String bcpType() {
            return this.bcpType;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.IslamicCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.IslamicCalendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.IslamicCalendar.<clinit>():void");
    }

    public IslamicCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public IslamicCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    public IslamicCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public IslamicCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public IslamicCalendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale));
    }

    public IslamicCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        setCalcTypeForLocale(locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public IslamicCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        setTime(date);
    }

    public IslamicCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        set(SAFAR, year);
        set(RABI_1, month);
        set(JUMADA_2, date);
    }

    public IslamicCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        set(SAFAR, year);
        set(RABI_1, month);
        set(JUMADA_2, date);
        set(DHU_AL_HIJJAH, hour);
        set(12, minute);
        set(13, second);
    }

    public void setCivil(boolean beCivil) {
        this.civil = beCivil;
        long m;
        if (beCivil && this.cType != CalculationType.ISLAMIC_CIVIL) {
            m = getTimeInMillis();
            this.cType = CalculationType.ISLAMIC_CIVIL;
            clear();
            setTimeInMillis(m);
        } else if (!beCivil && this.cType != CalculationType.ISLAMIC) {
            m = getTimeInMillis();
            this.cType = CalculationType.ISLAMIC;
            clear();
            setTimeInMillis(m);
        }
    }

    public boolean isCivil() {
        if (this.cType == CalculationType.ISLAMIC_CIVIL) {
            return true;
        }
        return false;
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    private static final boolean civilLeapYear(int year) {
        return ((year * DHU_AL_HIJJAH) + 14) % 30 < DHU_AL_HIJJAH;
    }

    private long yearStart(int year) {
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && (year < UMALQURA_YEAR_START || year > UMALQURA_YEAR_END))) {
            return ((long) ((year - 1) * 354)) + ((long) Math.floor(((double) ((year * DHU_AL_HIJJAH) + RABI_2)) / 30.0d));
        }
        if (this.cType == CalculationType.ISLAMIC) {
            return trueMonthStart((long) ((year - 1) * 12));
        }
        if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            return 0;
        }
        year -= 1300;
        return (long) (UMALQURA_YEAR_START_ESTIMATE_FIX[year] + ((int) (((((double) year) * 354.3672d) + 460322.05d) + 0.5d)));
    }

    private long monthStart(int year, int month) {
        int realYear = year + (month / 12);
        int realMonth = month % 12;
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && year < UMALQURA_YEAR_START)) {
            return (((long) Math.ceil(((double) realMonth) * 29.5d)) + ((long) ((realYear - 1) * 354))) + ((long) Math.floor(((double) ((realYear * DHU_AL_HIJJAH) + RABI_2)) / 30.0d));
        }
        if (this.cType == CalculationType.ISLAMIC) {
            return trueMonthStart((long) (((realYear - 1) * 12) + realMonth));
        }
        if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            return 0;
        }
        long ms = yearStart(year);
        for (int i = MUHARRAM; i < month; i += SAFAR) {
            ms += (long) handleGetMonthLength(year, i);
        }
        return ms;
    }

    private static final long trueMonthStart(long month) {
        long start = cache.get(month);
        if (start != CalendarCache.EMPTY) {
            return start;
        }
        long origin = HIJRA_MILLIS + (((long) Math.floor(((double) month) * CalendarAstronomer.SYNODIC_MONTH)) * RelativeDateTimeFormatter.DAY_IN_MILLIS);
        double age = moonAge(origin);
        if (moonAge(origin) < 0.0d) {
            while (true) {
                origin += RelativeDateTimeFormatter.DAY_IN_MILLIS;
                if (moonAge(origin) >= 0.0d) {
                    break;
                }
            }
        } else {
            do {
                origin -= RelativeDateTimeFormatter.DAY_IN_MILLIS;
            } while (moonAge(origin) >= 0.0d);
        }
        start = ((origin - HIJRA_MILLIS) / RelativeDateTimeFormatter.DAY_IN_MILLIS) + 1;
        cache.put(month, start);
        return start;
    }

    static final double moonAge(long time) {
        double age;
        synchronized (astro) {
            astro.setTime(time);
            age = astro.getMoonAge();
        }
        age = (age * 180.0d) / 3.141592653589793d;
        if (age > 180.0d) {
            return age - 360.0d;
        }
        return age;
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && (extendedYear < UMALQURA_YEAR_START || extendedYear > UMALQURA_YEAR_END))) {
            int length = ((month + SAFAR) % RABI_1) + 29;
            if (month == DHU_AL_HIJJAH && civilLeapYear(extendedYear)) {
                return length + SAFAR;
            }
            return length;
        } else if (this.cType == CalculationType.ISLAMIC) {
            month += (extendedYear - 1) * 12;
            return (int) (trueMonthStart((long) (month + SAFAR)) - trueMonthStart((long) month));
        } else if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            return MUHARRAM;
        } else {
            if ((UMALQURA_MONTHLENGTH[extendedYear - 1300] & (SAFAR << (11 - month))) == 0) {
                return 29;
            }
            return 30;
        }
    }

    protected int handleGetYearLength(int extendedYear) {
        int length = MUHARRAM;
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && (extendedYear < UMALQURA_YEAR_START || extendedYear > UMALQURA_YEAR_END))) {
            return (civilLeapYear(extendedYear) ? SAFAR : MUHARRAM) + 354;
        } else if (this.cType == CalculationType.ISLAMIC) {
            int month = (extendedYear - 1) * 12;
            return (int) (trueMonthStart((long) (month + 12)) - trueMonthStart((long) month));
        } else if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            return MUHARRAM;
        } else {
            for (int i = MUHARRAM; i < 12; i += SAFAR) {
                length += handleGetMonthLength(extendedYear, i);
            }
            return length;
        }
    }

    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        return (int) (((this.cType == CalculationType.ISLAMIC_TBLA ? ASTRONOMICAL_EPOC : CIVIL_EPOC) + monthStart(eyear, month)) - 1);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, SAFAR) == 19) {
            return internalGet(19, SAFAR);
        }
        return internalGet(SAFAR, SAFAR);
    }

    protected void handleComputeFields(int julianDay) {
        int year = MUHARRAM;
        int month = MUHARRAM;
        long days = ((long) julianDay) - CIVIL_EPOC;
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA) {
            if (this.cType == CalculationType.ISLAMIC_TBLA) {
                days = ((long) julianDay) - ASTRONOMICAL_EPOC;
            }
            year = (int) Math.floor(((double) ((30 * days) + 10646)) / 10631.0d);
            month = Math.min((int) Math.ceil(((double) ((days - 29) - yearStart(year))) / 29.5d), DHU_AL_HIJJAH);
        } else {
            if (this.cType == CalculationType.ISLAMIC) {
                int months = (int) Math.floor(((double) days) / CalendarAstronomer.SYNODIC_MONTH);
                if (days - ((long) Math.floor((((double) months) * CalendarAstronomer.SYNODIC_MONTH) - 1.0d)) >= 25 && moonAge(internalGetTimeInMillis()) > 0.0d) {
                    months += SAFAR;
                }
                while (true) {
                    if (trueMonthStart((long) months) <= days) {
                        break;
                    }
                    months--;
                }
                year = (months / 12) + SAFAR;
                month = months % 12;
            } else {
                if (this.cType == CalculationType.ISLAMIC_UMALQURA) {
                    if (days < yearStart(UMALQURA_YEAR_START)) {
                        year = (int) Math.floor(((double) ((30 * days) + 10646)) / 10631.0d);
                        month = Math.min((int) Math.ceil(((double) ((days - 29) - yearStart(year))) / 29.5d), DHU_AL_HIJJAH);
                    } else {
                        int y = 1299;
                        int m = MUHARRAM;
                        long d = 1;
                        while (d > 0) {
                            y += SAFAR;
                            d = (days - yearStart(y)) + 1;
                            if (d != ((long) handleGetYearLength(y))) {
                                if (d < ((long) handleGetYearLength(y))) {
                                    int monthLen = handleGetMonthLength(y, MUHARRAM);
                                    m = MUHARRAM;
                                    while (true) {
                                        if (d <= ((long) monthLen)) {
                                            break;
                                        }
                                        d -= (long) monthLen;
                                        m += SAFAR;
                                        monthLen = handleGetMonthLength(y, m);
                                    }
                                }
                            } else {
                                m = DHU_AL_HIJJAH;
                                break;
                            }
                        }
                        year = y;
                        month = m;
                    }
                }
            }
        }
        int dayOfMonth = ((int) (days - monthStart(year, month))) + SAFAR;
        int dayOfYear = (int) ((days - monthStart(year, MUHARRAM)) + 1);
        internalSet(MUHARRAM, MUHARRAM);
        internalSet(SAFAR, year);
        internalSet(19, year);
        internalSet(RABI_1, month);
        internalSet(JUMADA_2, dayOfMonth);
        internalSet(RAJAB, dayOfYear);
    }

    public void setCalculationType(CalculationType type) {
        this.cType = type;
        if (this.cType == CalculationType.ISLAMIC_CIVIL) {
            this.civil = true;
        } else {
            this.civil = false;
        }
    }

    public CalculationType getCalculationType() {
        return this.cType;
    }

    private void setCalcTypeForLocale(ULocale locale) {
        String localeCalType = CalendarUtil.getCalendarType(locale);
        if ("islamic-civil".equals(localeCalType)) {
            setCalculationType(CalculationType.ISLAMIC_CIVIL);
        } else if ("islamic-umalqura".equals(localeCalType)) {
            setCalculationType(CalculationType.ISLAMIC_UMALQURA);
        } else if ("islamic-tbla".equals(localeCalType)) {
            setCalculationType(CalculationType.ISLAMIC_TBLA);
        } else if (localeCalType.startsWith("islamic")) {
            setCalculationType(CalculationType.ISLAMIC);
        } else {
            setCalculationType(CalculationType.ISLAMIC_CIVIL);
        }
    }

    public String getType() {
        if (this.cType == null) {
            return "islamic";
        }
        return this.cType.bcpType();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.cType == CalculationType.ISLAMIC_CIVIL && !this.civil) {
            this.cType = CalculationType.ISLAMIC;
        }
    }
}
