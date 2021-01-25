package ohos.global.i18n;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import ohos.global.icu.text.DateTimePatternGenerator;
import ohos.global.icu.util.ULocale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ICUImpl extends ICU {
    private static final LruCache CACHED_PATTERNS = new LruCache(8);
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ICUEx");

    public static String getBestDateTimePattern(String str, Locale locale) {
        String str2;
        String languageTag = locale.toLanguageTag();
        String str3 = str + "\t" + languageTag;
        synchronized (CACHED_PATTERNS) {
            str2 = CACHED_PATTERNS.get(str3);
            if (str2 == null) {
                if (str == null) {
                    str2 = null;
                } else {
                    DateTimePatternGenerator instance = DateTimePatternGenerator.getInstance(new ULocale(languageTag));
                    if (instance != null) {
                        str2 = instance.getBestPattern(str);
                    }
                }
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

    static class LruCache {
        private final Object mLock = new Object();
        private final LinkedHashMap<String, String> map;
        private int maxSize;

        public LruCache(int i) {
            if (i > 0) {
                this.maxSize = i;
                this.map = new LinkedHashMap<>(0, 0.75f, true);
                return;
            }
            throw new IllegalArgumentException("max size <= 0");
        }

        public void resize(int i) {
            if (i > 0) {
                synchronized (this.mLock) {
                    this.maxSize = i;
                }
                trimToSize(i);
                return;
            }
            throw new IllegalArgumentException("max size <= 0");
        }

        public final String get(String str) {
            if (str != null) {
                synchronized (this.mLock) {
                    String str2 = this.map.get(str);
                    if (str2 != null) {
                        return str2;
                    }
                    return str2;
                }
            }
            throw new NullPointerException("key is null");
        }

        public final String put(String str, String str2) {
            String put;
            if (str == null || str2 == null) {
                throw new NullPointerException("key is null or value is null");
            }
            synchronized (this.mLock) {
                put = this.map.put(str, str2);
            }
            trimToSize(this.maxSize);
            return put;
        }

        public void trimToSize(int i) {
            while (true) {
                synchronized (this.mLock) {
                    if (this.map.size() > i) {
                        Map.Entry eldest = this.map.eldest();
                        if (eldest != null) {
                            this.map.remove((String) eldest.getKey());
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }
}
