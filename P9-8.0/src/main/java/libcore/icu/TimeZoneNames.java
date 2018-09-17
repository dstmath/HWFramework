package libcore.icu;

import android.icu.text.DateFormat;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import libcore.util.BasicLruCache;
import libcore.util.ZoneInfoDB;

public final class TimeZoneNames {
    public static final int LONG_NAME = 1;
    public static final int LONG_NAME_DST = 3;
    public static final int NAME_COUNT = 5;
    public static final int OLSON_NAME = 0;
    public static final int SHORT_NAME = 2;
    public static final int SHORT_NAME_DST = 4;
    private static final Comparator<String[]> ZONE_STRINGS_COMPARATOR = new Comparator<String[]>() {
        public int compare(String[] lhs, String[] rhs) {
            return lhs[0].compareTo(rhs[0]);
        }
    };
    private static final String[] availableTimeZoneIds = TimeZone.getAvailableIDs();
    private static final ZoneStringsCache cachedZoneStrings = new ZoneStringsCache();

    public static class ZoneStringsCache extends BasicLruCache<Locale, String[][]> {
        public ZoneStringsCache() {
            super(5);
        }

        protected String[][] create(Locale locale) {
            long start = System.nanoTime();
            String[][] result = (String[][]) Array.newInstance(String.class, new int[]{TimeZoneNames.availableTimeZoneIds.length, 5});
            for (int i = 0; i < TimeZoneNames.availableTimeZoneIds.length; i++) {
                result[i][0] = TimeZoneNames.availableTimeZoneIds[i];
            }
            long nativeStart = System.nanoTime();
            TimeZoneNames.fillZoneStrings(locale.toLanguageTag(), result);
            long nativeEnd = System.nanoTime();
            internStrings(result);
            long end = System.nanoTime();
            System.logI("Loaded time zone names for \"" + locale + "\" in " + TimeUnit.NANOSECONDS.toMillis(end - start) + DateFormat.MINUTE_SECOND + " (" + TimeUnit.NANOSECONDS.toMillis(nativeEnd - nativeStart) + "ms in ICU)");
            return result;
        }

        private synchronized void internStrings(String[][] result) {
            HashMap<String, String> internTable = new HashMap();
            for (int i = 0; i < result.length; i++) {
                for (int j = 1; j < 5; j++) {
                    String original = result[i][j];
                    String nonDuplicate = (String) internTable.get(original);
                    if (nonDuplicate == null) {
                        internTable.put(original, original);
                    } else {
                        result[i][j] = nonDuplicate;
                    }
                }
            }
        }
    }

    private static native void fillZoneStrings(String str, String[][] strArr);

    public static native String getExemplarLocation(String str, String str2);

    static {
        cachedZoneStrings.get(Locale.ROOT);
        cachedZoneStrings.get(Locale.US);
        cachedZoneStrings.get(Locale.getDefault());
    }

    private TimeZoneNames() {
    }

    public static String getDisplayName(String[][] zoneStrings, String id, boolean daylight, int style) {
        int index = Arrays.binarySearch(zoneStrings, new String[]{id}, ZONE_STRINGS_COMPARATOR);
        if (index < 0) {
            return null;
        }
        String[] row = zoneStrings[index];
        if (daylight) {
            return style == 1 ? row[3] : row[4];
        }
        return style == 1 ? row[1] : row[2];
    }

    public static String[][] getZoneStrings(Locale locale) {
        Object locale2;
        if (locale2 == null) {
            locale2 = Locale.getDefault();
        }
        return (String[][]) cachedZoneStrings.get(locale2);
    }

    public static String[] forLocale(Locale locale) {
        String countryCode = locale.getCountry();
        ArrayList<String> ids = new ArrayList();
        for (String line : ZoneInfoDB.getInstance().getZoneTab().split("\n")) {
            if (line.startsWith(countryCode)) {
                int olsonIdStart = line.indexOf(9, 4) + 1;
                int olsonIdEnd = line.indexOf(9, olsonIdStart);
                if (olsonIdEnd == -1) {
                    olsonIdEnd = line.length();
                }
                ids.add(line.substring(olsonIdStart, olsonIdEnd));
            }
        }
        return (String[]) ids.toArray(new String[ids.size()]);
    }
}
