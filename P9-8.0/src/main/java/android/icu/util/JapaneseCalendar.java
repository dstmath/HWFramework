package android.icu.util;

import dalvik.bytecode.Opcodes;
import java.util.Date;
import java.util.Locale;

public class JapaneseCalendar extends GregorianCalendar {
    public static final int CURRENT_ERA = ((ERAS.length / 3) - 1);
    private static final int[] ERAS = new int[]{645, 6, 19, 650, 2, 15, 672, 1, 1, 686, 7, 20, 701, 3, 21, 704, 5, 10, 708, 1, 11, 715, 9, 2, 717, 11, 17, 724, 2, 4, 729, 8, 5, 749, 4, 14, 749, 7, 2, 757, 8, 18, 765, 1, 7, Opcodes.OP_INSTANCE_OF_JUMBO, 8, 16, 770, 10, 1, 781, 1, 1, 782, 8, 19, 806, 5, 18, 810, 9, 19, 824, 1, 5, 834, 1, 3, 848, 6, 13, 851, 4, 28, 854, 11, 30, 857, 2, 21, 859, 4, 15, 877, 4, 16, 885, 2, 21, 889, 4, 27, 898, 4, 26, 901, 7, 15, 923, 4, 11, 931, 4, 26, 938, 5, 22, 947, 4, 22, 957, 10, 27, 961, 2, 16, 964, 7, 10, 968, 8, 13, 970, 3, 25, 973, 12, 20, 976, 7, 13, 978, 11, 29, 983, 4, 15, 985, 4, 27, 987, 4, 5, 989, 8, 8, 990, 11, 7, 995, 2, 22, 999, 1, 13, 1004, 7, 20, 1012, 12, 25, 1017, 4, 23, 1021, 2, 2, 1024, 7, 13, 1028, 7, 25, 1037, 4, 21, 1040, 11, 10, 1044, 11, 24, 1046, 4, 14, 1053, 1, 11, 1058, 8, 29, 1065, 8, 2, 1069, 4, 13, 1074, 8, 23, 1077, 11, 17, 1081, 2, 10, 1084, 2, 7, 1087, 4, 7, 1094, 12, 15, 1096, 12, 17, 1097, 11, 21, 1099, 8, 28, 1104, 2, 10, 1106, 4, 9, 1108, 8, 3, 1110, 7, 13, 1113, 7, 13, 1118, 4, 3, 1120, 4, 10, 1124, 4, 3, 1126, 1, 22, 1131, 1, 29, 1132, 8, 11, 1135, 4, 27, 1141, 7, 10, 1142, 4, 28, 1144, 2, 23, 1145, 7, 22, 1151, 1, 26, 1154, 10, 28, 1156, 4, 27, 1159, 4, 20, 1160, 1, 10, 1161, 9, 4, 1163, 3, 29, 1165, 6, 5, 1166, 8, 27, 1169, 4, 8, 1171, 4, 21, 1175, 7, 28, 1177, 8, 4, 1181, 7, 14, 1182, 5, 27, 1184, 4, 16, 1185, 8, 14, 1190, 4, 11, 1199, 4, 27, 1201, 2, 13, 1204, 2, 20, 1206, 4, 27, 1207, 10, 25, 1211, 3, 9, 1213, 12, 6, 1219, 4, 12, 1222, 4, 13, 1224, 11, 20, 1225, 4, 20, 1227, 12, 10, 1229, 3, 5, 1232, 4, 2, 1233, 4, 15, 1234, 11, 5, 1235, 9, 19, 1238, 11, 23, 1239, 2, 7, 1240, 7, 16, 1243, 2, 26, 1247, 2, 28, 1249, 3, 18, 1256, 10, 5, 1257, 3, 14, 1259, 3, 26, 1260, 4, 13, 1261, 2, 20, 1264, 2, 28, 1275, 4, 25, 1278, 2, 29, 1288, 4, 28, 1293, 8, 55, 1299, 4, 25, 1302, 11, 21, 1303, 8, 5, 1306, 12, 14, 1308, 10, 9, 1311, 4, 28, 1312, 3, 20, 1317, 2, 3, 1319, 4, 28, 1321, 2, 23, 1324, 12, 9, 1326, 4, 26, 1329, 8, 29, 1331, 8, 9, 1334, 1, 29, 1336, 2, 29, 1340, 4, 28, 1346, 12, 8, 1370, 7, 24, 1372, 4, 1, 1375, 5, 27, 1379, 3, 22, 1381, 2, 10, 1384, 4, 28, 1384, 2, 27, 1387, 8, 23, 1389, 2, 9, 1390, 3, 26, 1394, 7, 5, 1428, 4, 27, 1429, 9, 5, 1441, 2, 17, 1444, 2, 5, 1449, 7, 28, 1452, 7, 25, 1455, 7, 25, 1457, 9, 28, 1460, 12, 21, 1466, 2, 28, 1467, 3, 3, 1469, 4, 28, 1487, 7, 29, 1489, 8, 21, 1492, 7, 19, 1501, 2, 29, 1504, 2, 30, 1521, 8, 23, 1528, 8, 20, 1532, 7, 29, 1555, 10, 23, 1558, 2, 28, 1570, 4, 23, 1573, 7, 28, 1592, 12, 8, 1596, 10, 27, 1615, 7, 13, 1624, 2, 30, 1644, 12, 16, 1648, 2, 15, 1652, 9, 18, 1655, 4, 13, 1658, 7, 23, 1661, 4, 25, 1673, 9, 21, 1681, 9, 29, 1684, 2, 21, 1688, 9, 30, 1704, 3, 13, 1711, 4, 25, 1716, 6, 22, 1736, 4, 28, 1741, 2, 27, 1744, 2, 21, 1748, 7, 12, 1751, 10, 27, 1764, 6, 2, 1772, 11, 16, 1781, 4, 2, 1789, 1, 25, 1801, 2, 5, 1804, 2, 11, 1818, 4, 22, 1830, 12, 10, 1844, 12, 2, 1848, 2, 28, 1854, 11, 27, 1860, 3, 18, 1861, 2, 19, 1864, 2, 20, 1865, 4, 7, 1868, 9, 8, 1912, 7, 30, 1926, 12, 25, 1989, 1, 8};
    private static final int GREGORIAN_EPOCH = 1970;
    public static final int HEISEI = CURRENT_ERA;
    public static final int MEIJI = (CURRENT_ERA - 3);
    public static final int SHOWA = (CURRENT_ERA - 1);
    public static final int TAISHO = (CURRENT_ERA - 2);
    private static final long serialVersionUID = -2977189902603704691L;

    public JapaneseCalendar(TimeZone zone) {
        super(zone);
    }

    public JapaneseCalendar(Locale aLocale) {
        super(aLocale);
    }

    public JapaneseCalendar(ULocale locale) {
        super(locale);
    }

    public JapaneseCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    public JapaneseCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    public JapaneseCalendar(Date date) {
        this();
        setTime(date);
    }

    public JapaneseCalendar(int era, int year, int month, int date) {
        super(year, month, date);
        set(0, era);
    }

    public JapaneseCalendar(int year, int month, int date) {
        super(year, month, date);
        set(0, CURRENT_ERA);
    }

    public JapaneseCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
        set(0, CURRENT_ERA);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19 && newerField(19, 0) == 19) {
            return internalGet(19, GREGORIAN_EPOCH);
        }
        return (internalGet(1, 1) + ERAS[internalGet(0, CURRENT_ERA) * 3]) - 1;
    }

    protected int getDefaultMonthInYear(int extendedYear) {
        int era = internalGet(0, CURRENT_ERA);
        if (extendedYear == ERAS[era * 3]) {
            return ERAS[(era * 3) + 1] - 1;
        }
        return super.getDefaultMonthInYear(extendedYear);
    }

    protected int getDefaultDayInMonth(int extendedYear, int month) {
        int era = internalGet(0, CURRENT_ERA);
        if (extendedYear == ERAS[era * 3] && month == ERAS[(era * 3) + 1] - 1) {
            return ERAS[(era * 3) + 2];
        }
        return super.getDefaultDayInMonth(extendedYear, month);
    }

    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int year = internalGet(19);
        int low = 0;
        if (year > ERAS[ERAS.length - 3]) {
            low = CURRENT_ERA;
        } else {
            int high = ERAS.length / 3;
            while (low < high - 1) {
                int i = (low + high) / 2;
                int diff = year - ERAS[i * 3];
                if (diff == 0) {
                    diff = internalGet(2) - (ERAS[(i * 3) + 1] - 1);
                    if (diff == 0) {
                        diff = internalGet(5) - ERAS[(i * 3) + 2];
                    }
                }
                if (diff >= 0) {
                    low = i;
                } else {
                    high = i;
                }
            }
        }
        internalSet(0, low);
        internalSet(1, (year - ERAS[low * 3]) + 1);
    }

    protected int handleGetLimit(int field, int limitType) {
        switch (field) {
            case 0:
                if (limitType == 0 || limitType == 1) {
                    return 0;
                }
                return CURRENT_ERA;
            case 1:
                switch (limitType) {
                    case 0:
                    case 1:
                        return 1;
                    case 2:
                        return 1;
                    case 3:
                        return super.handleGetLimit(field, 3) - ERAS[CURRENT_ERA * 3];
                }
                break;
        }
        return super.handleGetLimit(field, limitType);
    }

    public String getType() {
        return "japanese";
    }

    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    public int getActualMaximum(int field) {
        if (field != 1) {
            return super.getActualMaximum(field);
        }
        int era = get(0);
        if (era == CURRENT_ERA) {
            return handleGetLimit(1, 3);
        }
        int nextEraYear = ERAS[(era + 1) * 3];
        int nextEraMonth = ERAS[((era + 1) * 3) + 1];
        int nextEraDate = ERAS[((era + 1) * 3) + 2];
        int maxYear = (nextEraYear - ERAS[era * 3]) + 1;
        if (nextEraMonth == 1 && nextEraDate == 1) {
            maxYear--;
        }
        return maxYear;
    }
}
