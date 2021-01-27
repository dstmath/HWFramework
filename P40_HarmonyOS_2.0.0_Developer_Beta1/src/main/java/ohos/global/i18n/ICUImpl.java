package ohos.global.i18n;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ICUImpl {
    private static final Map<String, String> CACHED_PATTERNS = new LinkedHashMap<String, String>(8, 0.75f, true) {
        /* class ohos.global.i18n.ICUImpl.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.util.LinkedHashMap
        public boolean removeEldestEntry(Map.Entry<String, String> entry) {
            return size() > 8;
        }
    };
    private static final int CACHE_SIZE = 8;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ICUImpl");

    private static native String nativeFormatRange(String str, String str2, String str3, long j, long j2);

    private static native String nativeGetBestDateTimePattern(String str, String str2);

    static {
        System.loadLibrary("icu_jni.z");
    }

    public static String getBestDateTimePattern(String str, Locale locale) {
        String str2;
        String nativeGetBestDateTimePattern;
        String languageTag = locale.toLanguageTag();
        String str3 = str + "\t" + languageTag;
        synchronized (CACHED_PATTERNS) {
            str2 = CACHED_PATTERNS.get(str3);
            if (str2 == null) {
                if (str == null) {
                    nativeGetBestDateTimePattern = null;
                } else {
                    nativeGetBestDateTimePattern = nativeGetBestDateTimePattern(str, languageTag);
                }
                str2 = nativeGetBestDateTimePattern;
                CACHED_PATTERNS.put(str3, str2);
            }
        }
        return str2;
    }

    public static char[] getDateFormatOrder(String str) {
        int[] iArr = {0, 0};
        boolean[] zArr = {false, false, false};
        char[] cArr = new char[3];
        for (int i = 0; i < str.length(); i = iArr[0] + 1) {
            iArr[0] = i;
            process(str, cArr, zArr, iArr);
        }
        return cArr;
    }

    private static void process(String str, char[] cArr, boolean[] zArr, int[] iArr) {
        char charAt = str.charAt(iArr[0]);
        if (charAt == 'd' || charAt == 'L' || charAt == 'M' || charAt == 'y') {
            processNormal(charAt, cArr, zArr, iArr);
        } else if ((charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z')) {
            throw new IllegalArgumentException("wrong pattern character '" + charAt + "' in " + str);
        } else if (charAt != '\'') {
            HiLog.debug(LABEL, "unknown charater in getDateFormatOrder.", new Object[0]);
        } else if (iArr[0] >= str.length() - 1 || str.charAt(iArr[0] + 1) != '\'') {
            int indexOf = str.indexOf(39, iArr[0] + 1);
            if (indexOf != -1) {
                iArr[0] = indexOf + 1;
                return;
            }
            throw new IllegalArgumentException("Bad quoting in " + str);
        } else {
            iArr[0] = iArr[0] + 1;
        }
    }

    private static void processNormal(char c, char[] cArr, boolean[] zArr, int[] iArr) {
        int i;
        int i2 = iArr[1];
        if (c == 'd' && !zArr[2]) {
            i = i2 + 1;
            cArr[i2] = 'd';
            zArr[2] = true;
        } else if ((c == 'L' || c == 'M') && !zArr[1]) {
            i = i2 + 1;
            cArr[i2] = 'M';
            zArr[1] = true;
        } else if (c != 'y' || zArr[0]) {
            HiLog.debug(LABEL, "unknown charater in getDateFormatOrder.", new Object[0]);
            i = i2;
        } else {
            i = i2 + 1;
            cArr[i2] = 'y';
            zArr[0] = true;
        }
        iArr[1] = i;
    }

    public static String formatRange(String str, String str2, String str3, long j, long j2) {
        return nativeFormatRange(str, str2, str3, j, j2);
    }
}
