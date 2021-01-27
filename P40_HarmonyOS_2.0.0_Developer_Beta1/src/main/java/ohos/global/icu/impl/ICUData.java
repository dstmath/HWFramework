package ohos.global.icu.impl;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.MissingResourceException;
import java.util.logging.Logger;

public final class ICUData {
    public static final String ICU_BASE_NAME = "ohos/global/icu/impl/data/icudt66b";
    public static final String ICU_BRKITR_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/brkitr";
    public static final String ICU_BRKITR_NAME = "brkitr";
    public static final String ICU_BUNDLE = "data/icudt66b";
    public static final String ICU_COLLATION_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/coll";
    public static final String ICU_CURR_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/curr";
    static final String ICU_DATA_PATH = "ohos/global/icu/impl/";
    public static final String ICU_LANG_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/lang";
    public static final String ICU_RBNF_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/rbnf";
    public static final String ICU_REGION_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/region";
    public static final String ICU_TRANSLIT_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/translit";
    public static final String ICU_UNIT_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/unit";
    public static final String ICU_ZONE_BASE_NAME = "ohos/global/icu/impl/data/icudt66b/zone";
    static final String PACKAGE_NAME = "icudt66b";
    private static final boolean logBinaryDataFromInputStream = false;
    private static final Logger logger = null;

    private static void checkStreamForBinaryData(InputStream inputStream, String str) {
    }

    public static boolean exists(final String str) {
        URL url;
        if (System.getSecurityManager() != null) {
            url = (URL) AccessController.doPrivileged(new PrivilegedAction<URL>() {
                /* class ohos.global.icu.impl.ICUData.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public URL run() {
                    return ICUData.class.getResource(str);
                }
            });
        } else {
            url = ICUData.class.getResource(str);
        }
        return url != null;
    }

    private static InputStream getStream(final Class<?> cls, final String str, boolean z) {
        InputStream inputStream;
        if (System.getSecurityManager() != null) {
            inputStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                /* class ohos.global.icu.impl.ICUData.AnonymousClass2 */

                @Override // java.security.PrivilegedAction
                public InputStream run() {
                    return cls.getResourceAsStream(str);
                }
            });
        } else {
            inputStream = cls.getResourceAsStream(str);
        }
        if (inputStream != null || !z) {
            checkStreamForBinaryData(inputStream, str);
            return inputStream;
        }
        throw new MissingResourceException("could not locate data " + str, cls.getPackage().getName(), str);
    }

    static InputStream getStream(final ClassLoader classLoader, final String str, boolean z) {
        InputStream inputStream;
        PrintStream printStream = System.out;
        printStream.println("resource name is " + str);
        if (System.getSecurityManager() != null) {
            inputStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                /* class ohos.global.icu.impl.ICUData.AnonymousClass3 */

                @Override // java.security.PrivilegedAction
                public InputStream run() {
                    return classLoader.getResourceAsStream(str);
                }
            });
        } else {
            inputStream = classLoader.getResourceAsStream(str);
        }
        if (inputStream != null || !z) {
            checkStreamForBinaryData(inputStream, str);
            return inputStream;
        }
        throw new MissingResourceException("could not locate data", classLoader.toString(), str);
    }

    public static InputStream getStream(ClassLoader classLoader, String str) {
        return getStream(classLoader, str, false);
    }

    public static InputStream getRequiredStream(ClassLoader classLoader, String str) {
        return getStream(classLoader, str, true);
    }

    public static InputStream getStream(String str) {
        return getStream((Class<?>) ICUData.class, str, false);
    }

    public static InputStream getRequiredStream(String str) {
        return getStream((Class<?>) ICUData.class, str, true);
    }

    public static InputStream getStream(Class<?> cls, String str) {
        return getStream(cls, str, false);
    }

    public static InputStream getRequiredStream(Class<?> cls, String str) {
        return getStream(cls, str, true);
    }
}
