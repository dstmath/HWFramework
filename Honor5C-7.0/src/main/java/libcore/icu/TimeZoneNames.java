package libcore.icu;

import android.icu.text.DateFormat;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
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
    private static final Comparator<String[]> ZONE_STRINGS_COMPARATOR = null;
    private static final String[] availableTimeZoneIds = null;
    private static final ZoneStringsCache cachedZoneStrings = null;

    public static class ZoneStringsCache extends BasicLruCache<Locale, String[][]> {
        public ZoneStringsCache() {
            super(TimeZoneNames.NAME_COUNT);
        }

        protected String[][] create(Locale locale) {
            long start = System.nanoTime();
            int[] iArr = new int[TimeZoneNames.SHORT_NAME];
            iArr[TimeZoneNames.OLSON_NAME] = TimeZoneNames.availableTimeZoneIds.length;
            iArr[TimeZoneNames.LONG_NAME] = TimeZoneNames.NAME_COUNT;
            String[][] result = (String[][]) Array.newInstance(String.class, iArr);
            int i = TimeZoneNames.OLSON_NAME;
            while (true) {
                int length = TimeZoneNames.availableTimeZoneIds.length;
                if (i < r0) {
                    result[i][TimeZoneNames.OLSON_NAME] = TimeZoneNames.availableTimeZoneIds[i];
                    i += TimeZoneNames.LONG_NAME;
                } else {
                    long nativeStart = System.nanoTime();
                    TimeZoneNames.fillZoneStrings(locale.toLanguageTag(), result);
                    long nativeEnd = System.nanoTime();
                    internStrings(result);
                    long end = System.nanoTime();
                    long nativeDuration = TimeUnit.NANOSECONDS.toMillis(nativeEnd - nativeStart);
                    long duration = TimeUnit.NANOSECONDS.toMillis(end - start);
                    System.logI("Loaded time zone names for \"" + locale + "\" in " + duration + DateFormat.MINUTE_SECOND + " (" + nativeDuration + "ms in ICU)");
                    return result;
                }
            }
        }

        private synchronized void internStrings(String[][] result) {
            HashMap<String, String> internTable = new HashMap();
            for (int i = TimeZoneNames.OLSON_NAME; i < result.length; i += TimeZoneNames.LONG_NAME) {
                for (int j = TimeZoneNames.LONG_NAME; j < TimeZoneNames.NAME_COUNT; j += TimeZoneNames.LONG_NAME) {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.icu.TimeZoneNames.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.icu.TimeZoneNames.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.TimeZoneNames.<clinit>():void");
    }

    private static native void fillZoneStrings(String str, String[][] strArr);

    public static native String getExemplarLocation(String str, String str2);

    private TimeZoneNames() {
    }

    public static String getDisplayName(String[][] zoneStrings, String id, boolean daylight, int style) {
        String[] needle = new String[LONG_NAME];
        needle[OLSON_NAME] = id;
        int index = Arrays.binarySearch(zoneStrings, needle, ZONE_STRINGS_COMPARATOR);
        if (index < 0) {
            return null;
        }
        String[] row = zoneStrings[index];
        if (daylight) {
            return style == LONG_NAME ? row[LONG_NAME_DST] : row[SHORT_NAME_DST];
        }
        return style == LONG_NAME ? row[LONG_NAME] : row[SHORT_NAME];
    }

    public static String[][] getZoneStrings(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return (String[][]) cachedZoneStrings.get(locale);
    }

    public static String[] forLocale(Locale locale) {
        String countryCode = locale.getCountry();
        ArrayList<String> ids = new ArrayList();
        String[] split = ZoneInfoDB.getInstance().getZoneTab().split("\n");
        int length = split.length;
        for (int i = OLSON_NAME; i < length; i += LONG_NAME) {
            String line = split[i];
            if (line.startsWith(countryCode)) {
                int olsonIdStart = line.indexOf(9, SHORT_NAME_DST) + LONG_NAME;
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
