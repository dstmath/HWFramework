package ohos.global.icu.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;
import ohos.global.icu.impl.CalendarAstronomer;
import ohos.global.icu.impl.CalendarCache;
import ohos.global.icu.impl.CalendarUtil;
import ohos.global.icu.util.ULocale;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;
import ohos.workschedulerservice.controller.WorkStatus;

public class IslamicCalendar extends Calendar {
    private static final long ASTRONOMICAL_EPOC = 1948439;
    private static final long CIVIL_EPOC = 1948440;
    public static final int DHU_AL_HIJJAH = 11;
    public static final int DHU_AL_QIDAH = 10;
    private static final long HIJRA_MILLIS = -42521587200000L;
    public static final int JUMADA_1 = 4;
    public static final int JUMADA_2 = 5;
    private static final int[][] LIMITS = {new int[]{0, 0, 0, 0}, new int[]{1, 1, 5000000, 5000000}, new int[]{0, 0, 11, 11}, new int[]{1, 1, 50, 51}, new int[0], new int[]{1, 1, 29, 30}, new int[]{1, 1, 354, 355}, new int[0], new int[]{-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{1, 1, 5000000, 5000000}, new int[0], new int[]{1, 1, 5000000, 5000000}, new int[0], new int[0]};
    public static final int MUHARRAM = 0;
    public static final int RABI_1 = 2;
    public static final int RABI_2 = 3;
    public static final int RAJAB = 6;
    public static final int RAMADAN = 8;
    public static final int SAFAR = 1;
    public static final int SHABAN = 7;
    public static final int SHAWWAL = 9;
    private static final int[] UMALQURA_MONTHLENGTH = {2730, 3412, 3785, 1748, 1770, 876, 2733, 1365, 1705, 1938, 2985, 1492, 2778, 1372, 3373, 1685, 1866, SystemAbilityDefinition.SUBSYS_MSDP_SYS_ABILITY_ID_BEGIN, 2922, 1453, 1198, 2639, 1303, 1675, 1701, 2773, 726, 2395, 1181, 2637, 3366, 3477, 1452, 2486, 698, 2651, 1323, 2709, 1738, 2793, 756, 2422, 694, 2390, 2762, 2980, 3026, 1497, 732, 2413, 1357, 2725, 2898, 2981, 1460, 2486, 1367, 663, 1355, 1699, 1874, 2917, 1386, 2731, 1323, 3221, 3402, 3493, 1482, 2774, 2391, 1195, 2379, 2725, 2898, 2922, 1397, 630, 2231, SystemAbilityDefinition.IPC_MSG_UNREGISTERED_SERVER, 1365, 1449, 1460, 2522, 1245, 622, 2358, 2730, 3412, 3506, 1493, 730, 2395, 1195, 2645, 2889, 2916, 2929, 1460, 2741, 2645, 3365, 3730, 3785, 1748, 2793, 2411, 1195, 2707, 3401, 3492, 3506, 2745, 1210, 2651, 1323, 2709, 2858, SystemAbilityDefinition.MSDP_PROXY_SERVICE_ID, 1372, 1213, 573, 2333, 2709, 2890, 2906, 1389, 694, 2363, 1179, 1621, 1705, 1876, 2922, 1388, 2733, 1365, 2857, 2962, 2985, 1492, 2778, 1370, 2731, 1429, 1865, 1892, 2986, 1461, 694, 2646, 3661, 2853, 2898, 2922, 1453, 686, 2351, 1175, 1611, 1701, 1708, 2774, 1373, 1181, 2637, 3350, 3477, 1450, 1461, 730, 2395, 1197, 1429, 1738, 1764, 2794, 1269, 694, 2390, 2730, SystemAbilityDefinition.SUBSYS_MSDP_SYS_ABILITY_ID_BEGIN, 3026, 1497, 746, 2413, 1197, 2709, 2890, 2981, 1458, 2485, 1238, 2711, 1351, 1683, 1865, SystemAbilityDefinition.MSDP_PROXY_SERVICE_ID, 1386, 2667, 1323, 2699, 3398, 3491, 1482, 2774, 1243, 619, 2379, 2725, 2898, 2921, 1397, 374, 2231, 603, 1323, 1381, 1460, 2522, 1261, 365, 2230, 2726, 3410, 3497, 1492, 2778, 2395, 1195, 1619, 1833, 1890, 2985, 1458, 2741, 1365, 2853, 3474, 3785, 1746, 2793, 1387, 1195, 2645, 3369, 3412, 3498, 2485, 1210, 2619, 1179, 2637, 2730, 2773, 730, 2397, SystemAbilityDefinition.IPC_TEST_SERVICE, 2606, 3226, 3413, 1714, 1721, 1210, 2653, 1325, 2709, 2898, 2984, 2996, 1465, 730, 2394, 2890, 3492, 3793, 1768, 2922, 1389, 1333, 1685, 3402, 3496, 3540, 1754, 1371, 669, 1579, 2837, 2890, 2965, 1450, 2734, 2350, 3215, 1319, 1685, 1706, 2774, 1373, 669};
    private static final int UMALQURA_YEAR_END = 1600;
    private static final int UMALQURA_YEAR_START = 1300;
    private static final byte[] UMALQURA_YEAR_START_ESTIMATE_FIX = {0, 0, -1, 0, -1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, -1, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 1, 1, 0, 0, -1, 0, 1, 0, 1, 1, 0, 0, -1, 0, 1, 0, 0, 0, -1, 0, 1, 0, 1, 0, 0, 0, -1, 0, 0, 0, 0, -1, -1, 0, -1, 0, 1, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, -1, 0, 0, 0, 1, 0, 0, -1, -1, 0, -1, 0, 0, -1, -1, 0, -1, 0, -1, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, 1, 0, 1, 1, 0, 0, -1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, -1, 0, 1, 0, 0, -1, -1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 1, 0, 0, -1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0, 1, 0, 0, 0, -1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 0, -1, -1, 0, -1, 0, 1, 0, 0, -1, -1, 0, 0, 1, 1, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
    private static CalendarAstronomer astro = new CalendarAstronomer();
    private static CalendarCache cache = new CalendarCache();
    private static final long serialVersionUID = -6253365474073869325L;
    private CalculationType cType;
    private boolean civil;

    public IslamicCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public IslamicCalendar(TimeZone timeZone) {
        this(timeZone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public IslamicCalendar(Locale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public IslamicCalendar(ULocale uLocale) {
        this(TimeZone.getDefault(), uLocale);
    }

    public IslamicCalendar(TimeZone timeZone, Locale locale) {
        this(timeZone, ULocale.forLocale(locale));
    }

    public IslamicCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        setCalcTypeForLocale(uLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public IslamicCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        setTime(date);
    }

    public IslamicCalendar(int i, int i2, int i3) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        set(1, i);
        set(2, i2);
        set(5, i3);
    }

    public IslamicCalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.civil = true;
        this.cType = CalculationType.ISLAMIC_CIVIL;
        set(1, i);
        set(2, i2);
        set(5, i3);
        set(11, i4);
        set(12, i5);
        set(13, i6);
    }

    public void setCivil(boolean z) {
        this.civil = z;
        if (z && this.cType != CalculationType.ISLAMIC_CIVIL) {
            long timeInMillis = getTimeInMillis();
            this.cType = CalculationType.ISLAMIC_CIVIL;
            clear();
            setTimeInMillis(timeInMillis);
        } else if (!z && this.cType != CalculationType.ISLAMIC) {
            long timeInMillis2 = getTimeInMillis();
            this.cType = CalculationType.ISLAMIC;
            clear();
            setTimeInMillis(timeInMillis2);
        }
    }

    public boolean isCivil() {
        return this.cType == CalculationType.ISLAMIC_CIVIL;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetLimit(int i, int i2) {
        return LIMITS[i][i2];
    }

    private static final boolean civilLeapYear(int i) {
        return ((i * 11) + 14) % 30 < 11;
    }

    private long yearStart(int i) {
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && (i < 1300 || i > 1600))) {
            return ((long) Math.floor(((double) ((i * 11) + 3)) / 30.0d)) + ((long) ((i - 1) * 354));
        } else if (this.cType == CalculationType.ISLAMIC) {
            return trueMonthStart((long) ((i - 1) * 12));
        } else {
            if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
                return 0;
            }
            int i2 = i - 1300;
            return (long) (((int) ((((double) i2) * 354.3672d) + 460322.05d + 0.5d)) + UMALQURA_YEAR_START_ESTIMATE_FIX[i2]);
        }
    }

    private long monthStart(int i, int i2) {
        int i3 = (i2 / 12) + i;
        int i4 = i2 % 12;
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && i < 1300)) {
            return ((long) Math.ceil(((double) i4) * 29.5d)) + ((long) ((i3 - 1) * 354)) + ((long) Math.floor(((double) ((i3 * 11) + 3)) / 30.0d));
        }
        if (this.cType == CalculationType.ISLAMIC) {
            return trueMonthStart((long) (((i3 - 1) * 12) + i4));
        }
        if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            return 0;
        }
        long yearStart = yearStart(i);
        for (int i5 = 0; i5 < i2; i5++) {
            yearStart += (long) handleGetMonthLength(i, i5);
        }
        return yearStart;
    }

    private static final long trueMonthStart(long j) {
        long j2 = cache.get(j);
        if (j2 != CalendarCache.EMPTY) {
            return j2;
        }
        long floor = (((long) Math.floor(((double) j) * 29.530588853d)) * WorkStatus.RARE_DELAY_TIME) + HIJRA_MILLIS;
        moonAge(floor);
        if (moonAge(floor) >= 0.0d) {
            do {
                floor -= WorkStatus.RARE_DELAY_TIME;
            } while (moonAge(floor) >= 0.0d);
        } else {
            do {
                floor += WorkStatus.RARE_DELAY_TIME;
            } while (moonAge(floor) < 0.0d);
        }
        long j3 = ((floor - HIJRA_MILLIS) / WorkStatus.RARE_DELAY_TIME) + 1;
        cache.put(j, j3);
        return j3;
    }

    static final double moonAge(long j) {
        double moonAge;
        synchronized (astro) {
            astro.setTime(j);
            moonAge = astro.getMoonAge();
        }
        double d = (moonAge * 180.0d) / 3.141592653589793d;
        return d > 180.0d ? d - 360.0d : d;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetMonthLength(int i, int i2) {
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && (i < 1300 || i > 1600))) {
            int i3 = 29 + ((i2 + 1) % 2);
            return (i2 != 11 || !civilLeapYear(i)) ? i3 : i3 + 1;
        } else if (this.cType == CalculationType.ISLAMIC) {
            int i4 = ((i - 1) * 12) + i2;
            return (int) (trueMonthStart((long) (i4 + 1)) - trueMonthStart((long) i4));
        } else {
            if (((1 << (11 - i2)) & UMALQURA_MONTHLENGTH[i - 1300]) == 0) {
                return 29;
            }
            return 30;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetYearLength(int i) {
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA || (this.cType == CalculationType.ISLAMIC_UMALQURA && (i < 1300 || i > 1600))) {
            return (civilLeapYear(i) ? 1 : 0) + 354;
        }
        if (this.cType == CalculationType.ISLAMIC) {
            int i2 = (i - 1) * 12;
            return (int) (trueMonthStart((long) (i2 + 12)) - trueMonthStart((long) i2));
        } else if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            return 0;
        } else {
            int i3 = 0;
            for (int i4 = 0; i4 < 12; i4++) {
                i3 += handleGetMonthLength(i, i4);
            }
            return i3;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleComputeMonthStart(int i, int i2, boolean z) {
        return (int) ((monthStart(i, i2) + (this.cType == CalculationType.ISLAMIC_TBLA ? ASTRONOMICAL_EPOC : CIVIL_EPOC)) - 1);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, 1);
        }
        return internalGet(1, 1);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public void handleComputeFields(int i) {
        int i2;
        int i3;
        int i4;
        long j = (long) i;
        long j2 = j - CIVIL_EPOC;
        if (this.cType == CalculationType.ISLAMIC_CIVIL || this.cType == CalculationType.ISLAMIC_TBLA) {
            if (this.cType == CalculationType.ISLAMIC_TBLA) {
                j2 = j - ASTRONOMICAL_EPOC;
            }
            i3 = (int) Math.floor(((double) ((30 * j2) + 10646)) / 10631.0d);
            i2 = Math.min((int) Math.ceil(((double) ((j2 - 29) - yearStart(i3))) / 29.5d), 11);
        } else if (this.cType == CalculationType.ISLAMIC) {
            int floor = (int) Math.floor(((double) j2) / 29.530588853d);
            if (j2 - ((long) Math.floor((((double) floor) * 29.530588853d) - 1.0d)) >= 25 && moonAge(internalGetTimeInMillis()) > 0.0d) {
                floor++;
            }
            while (trueMonthStart((long) floor) > j2) {
                floor--;
            }
            i2 = floor % 12;
            i3 = (floor / 12) + 1;
        } else if (this.cType != CalculationType.ISLAMIC_UMALQURA) {
            i3 = 0;
            i2 = 0;
        } else if (j2 < yearStart(1300)) {
            i3 = (int) Math.floor(((double) ((30 * j2) + 10646)) / 10631.0d);
            i2 = Math.min((int) Math.ceil(((double) ((j2 - 29) - yearStart(i3))) / 29.5d), 11);
        } else {
            int i5 = 1299;
            long j3 = 1;
            while (true) {
                if (j3 <= 0) {
                    i4 = 0;
                    break;
                }
                i5++;
                j3 = (j2 - yearStart(i5)) + 1;
                if (j3 != ((long) handleGetYearLength(i5))) {
                    if (j3 < ((long) handleGetYearLength(i5))) {
                        int handleGetMonthLength = handleGetMonthLength(i5, 0);
                        i4 = 0;
                        while (true) {
                            long j4 = (long) handleGetMonthLength;
                            if (j3 <= j4) {
                                break;
                            }
                            j3 -= j4;
                            i4++;
                            handleGetMonthLength = handleGetMonthLength(i5, i4);
                        }
                    }
                } else {
                    i4 = 11;
                    break;
                }
            }
            i3 = i5;
            i2 = i4;
        }
        internalSet(0, 0);
        internalSet(1, i3);
        internalSet(19, i3);
        internalSet(2, i2);
        internalSet(5, ((int) (j2 - monthStart(i3, i2))) + 1);
        internalSet(6, (int) ((j2 - monthStart(i3, 0)) + 1));
    }

    public enum CalculationType {
        ISLAMIC("islamic"),
        ISLAMIC_CIVIL("islamic-civil"),
        ISLAMIC_UMALQURA("islamic-umalqura"),
        ISLAMIC_TBLA("islamic-tbla");
        
        private String bcpType;

        private CalculationType(String str) {
            this.bcpType = str;
        }

        /* access modifiers changed from: package-private */
        public String bcpType() {
            return this.bcpType;
        }
    }

    public void setCalculationType(CalculationType calculationType) {
        this.cType = calculationType;
        if (this.cType == CalculationType.ISLAMIC_CIVIL) {
            this.civil = true;
        } else {
            this.civil = false;
        }
    }

    public CalculationType getCalculationType() {
        return this.cType;
    }

    private void setCalcTypeForLocale(ULocale uLocale) {
        String calendarType = CalendarUtil.getCalendarType(uLocale);
        if ("islamic-civil".equals(calendarType)) {
            setCalculationType(CalculationType.ISLAMIC_CIVIL);
        } else if ("islamic-umalqura".equals(calendarType)) {
            setCalculationType(CalculationType.ISLAMIC_UMALQURA);
        } else if ("islamic-tbla".equals(calendarType)) {
            setCalculationType(CalculationType.ISLAMIC_TBLA);
        } else if (calendarType.startsWith("islamic")) {
            setCalculationType(CalculationType.ISLAMIC);
        } else {
            setCalculationType(CalculationType.ISLAMIC_CIVIL);
        }
    }

    @Override // ohos.global.icu.util.Calendar
    public String getType() {
        CalculationType calculationType = this.cType;
        if (calculationType == null) {
            return "islamic";
        }
        return calculationType.bcpType();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        CalculationType calculationType = this.cType;
        if (calculationType == null) {
            this.cType = this.civil ? CalculationType.ISLAMIC_CIVIL : CalculationType.ISLAMIC;
        } else {
            this.civil = calculationType == CalculationType.ISLAMIC_CIVIL;
        }
    }
}
