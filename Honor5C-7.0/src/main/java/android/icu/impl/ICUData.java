package android.icu.impl;

import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.MissingResourceException;
import java.util.logging.Logger;

public final class ICUData {
    public static final String ICU_BASE_NAME = "android/icu/impl/data/icudt56b";
    public static final String ICU_BRKITR_BASE_NAME = "android/icu/impl/data/icudt56b/brkitr";
    public static final String ICU_BRKITR_NAME = "brkitr";
    public static final String ICU_BUNDLE = "data/icudt56b";
    public static final String ICU_COLLATION_BASE_NAME = "android/icu/impl/data/icudt56b/coll";
    public static final String ICU_CURR_BASE_NAME = "android/icu/impl/data/icudt56b/curr";
    static final String ICU_DATA_PATH = "android/icu/impl/";
    public static final String ICU_LANG_BASE_NAME = "android/icu/impl/data/icudt56b/lang";
    public static final String ICU_RBNF_BASE_NAME = "android/icu/impl/data/icudt56b/rbnf";
    public static final String ICU_REGION_BASE_NAME = "android/icu/impl/data/icudt56b/region";
    public static final String ICU_TRANSLIT_BASE_NAME = "android/icu/impl/data/icudt56b/translit";
    public static final String ICU_UNIT_BASE_NAME = "android/icu/impl/data/icudt56b/unit";
    public static final String ICU_ZONE_BASE_NAME = "android/icu/impl/data/icudt56b/zone";
    static final String PACKAGE_NAME = "icudt56b";
    private static final boolean logBinaryDataFromInputStream = false;
    private static final Logger logger = null;

    /* renamed from: android.icu.impl.ICUData.1 */
    static class AnonymousClass1 implements PrivilegedAction<URL> {
        final /* synthetic */ String val$resourceName;

        AnonymousClass1(String val$resourceName) {
            this.val$resourceName = val$resourceName;
        }

        public URL run() {
            return ICUData.class.getResource(this.val$resourceName);
        }
    }

    /* renamed from: android.icu.impl.ICUData.2 */
    static class AnonymousClass2 implements PrivilegedAction<InputStream> {
        final /* synthetic */ String val$resourceName;
        final /* synthetic */ Class val$root;

        AnonymousClass2(Class val$root, String val$resourceName) {
            this.val$root = val$root;
            this.val$resourceName = val$resourceName;
        }

        public InputStream run() {
            return this.val$root.getResourceAsStream(this.val$resourceName);
        }
    }

    /* renamed from: android.icu.impl.ICUData.3 */
    static class AnonymousClass3 implements PrivilegedAction<InputStream> {
        final /* synthetic */ ClassLoader val$loader;
        final /* synthetic */ String val$resourceName;

        AnonymousClass3(ClassLoader val$loader, String val$resourceName) {
            this.val$loader = val$loader;
            this.val$resourceName = val$resourceName;
        }

        public InputStream run() {
            return this.val$loader.getResourceAsStream(this.val$resourceName);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUData.<clinit>():void");
    }

    public static boolean exists(String resourceName) {
        URL i;
        if (System.getSecurityManager() != null) {
            i = (URL) AccessController.doPrivileged(new AnonymousClass1(resourceName));
        } else {
            i = ICUData.class.getResource(resourceName);
        }
        return i != null;
    }

    private static InputStream getStream(Class<?> root, String resourceName, boolean required) {
        InputStream i;
        if (System.getSecurityManager() != null) {
            i = (InputStream) AccessController.doPrivileged(new AnonymousClass2(root, resourceName));
        } else {
            i = root.getResourceAsStream(resourceName);
        }
        if (i == null && required) {
            throw new MissingResourceException("could not locate data " + resourceName, root.getPackage().getName(), resourceName);
        }
        checkStreamForBinaryData(i, resourceName);
        return i;
    }

    static InputStream getStream(ClassLoader loader, String resourceName, boolean required) {
        InputStream i;
        if (System.getSecurityManager() != null) {
            i = (InputStream) AccessController.doPrivileged(new AnonymousClass3(loader, resourceName));
        } else {
            i = loader.getResourceAsStream(resourceName);
        }
        if (i == null && required) {
            throw new MissingResourceException("could not locate data", loader.toString(), resourceName);
        }
        checkStreamForBinaryData(i, resourceName);
        return i;
    }

    private static void checkStreamForBinaryData(InputStream is, String resourceName) {
    }

    public static InputStream getStream(ClassLoader loader, String resourceName) {
        return getStream(loader, resourceName, false);
    }

    public static InputStream getRequiredStream(ClassLoader loader, String resourceName) {
        return getStream(loader, resourceName, true);
    }

    public static InputStream getStream(String resourceName) {
        return getStream(ICUData.class, resourceName, false);
    }

    public static InputStream getRequiredStream(String resourceName) {
        return getStream(ICUData.class, resourceName, true);
    }

    public static InputStream getStream(Class<?> root, String resourceName) {
        return getStream((Class) root, resourceName, false);
    }

    public static InputStream getRequiredStream(Class<?> root, String resourceName) {
        return getStream((Class) root, resourceName, true);
    }
}
