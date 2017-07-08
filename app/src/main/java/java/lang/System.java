package java.lang;

import android.system.StructUtsname;
import dalvik.system.VMRuntime;
import dalvik.system.VMStack;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import libcore.icu.ICU;
import libcore.io.Libcore;

public final class System {
    private static final int ARRAYCOPY_SHORT_BOOLEAN_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_BYTE_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_CHAR_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_DOUBLE_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_FLOAT_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_INT_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_LONG_ARRAY_THRESHOLD = 32;
    private static final int ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD = 32;
    public static final boolean DEBUG = false;
    private static final Object LOCK = null;
    private static Console cons;
    public static final PrintStream err = null;
    public static final InputStream in = null;
    private static boolean justRanFinalization;
    private static String lineSeparator;
    public static final PrintStream out = null;
    private static Properties props;
    private static boolean runGC;
    private static Properties unchangeableProps;

    static final class PropertiesWithNonOverrideableDefaults extends Properties {
        PropertiesWithNonOverrideableDefaults(Properties defaults) {
            super(defaults);
        }

        public Object put(Object key, Object value) {
            if (!this.defaults.containsKey(key)) {
                return super.put(key, value);
            }
            System.logE("Ignoring attempt to set property \"" + key + "\" to value \"" + value + "\".");
            return this.defaults.get(key);
        }

        public Object remove(Object key) {
            if (!this.defaults.containsKey(key)) {
                return super.remove(key);
            }
            System.logE("Ignoring attempt to remove property \"" + key + "\".");
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.System.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.System.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.System.<clinit>():void");
    }

    public static native void arraycopy(Object obj, int i, Object obj2, int i2, int i3);

    private static native void arraycopyBooleanUnchecked(boolean[] zArr, int i, boolean[] zArr2, int i2, int i3);

    private static native void arraycopyByteUnchecked(byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    private static native void arraycopyCharUnchecked(char[] cArr, int i, char[] cArr2, int i2, int i3);

    private static native void arraycopyDoubleUnchecked(double[] dArr, int i, double[] dArr2, int i2, int i3);

    private static native void arraycopyFloatUnchecked(float[] fArr, int i, float[] fArr2, int i2, int i3);

    private static native void arraycopyIntUnchecked(int[] iArr, int i, int[] iArr2, int i2, int i3);

    private static native void arraycopyLongUnchecked(long[] jArr, int i, long[] jArr2, int i2, int i3);

    private static native void arraycopyShortUnchecked(short[] sArr, int i, short[] sArr2, int i2, int i3);

    public static native long currentTimeMillis();

    public static native int identityHashCode(Object obj);

    private static native void log(char c, String str, Throwable th);

    public static native String mapLibraryName(String str);

    public static native long nanoTime();

    private static native void setErr0(PrintStream printStream);

    private static native void setIn0(InputStream inputStream);

    private static native void setOut0(PrintStream printStream);

    private static native String[] specialProperties();

    private System() {
    }

    public static void setIn(InputStream in) {
        setIn0(in);
    }

    public static void setOut(PrintStream out) {
        setOut0(out);
    }

    public static void setErr(PrintStream err) {
        setErr0(err);
    }

    public static Console console() {
        Console console;
        synchronized (System.class) {
            if (cons == null) {
                cons = Console.console();
            }
            console = cons;
        }
        return console;
    }

    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }

    public static void setSecurityManager(SecurityManager sm) {
        if (sm != null) {
            throw new SecurityException();
        }
    }

    public static SecurityManager getSecurityManager() {
        return null;
    }

    public static void arraycopy(char[] src, int srcPos, char[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyCharUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(byte[] src, int srcPos, byte[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyByteUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(short[] src, int srcPos, short[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyShortUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(int[] src, int srcPos, int[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyIntUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(long[] src, int srcPos, long[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyLongUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(float[] src, int srcPos, float[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyFloatUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(double[] src, int srcPos, double[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyDoubleUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    public static void arraycopy(boolean[] src, int srcPos, boolean[] dst, int dstPos, int length) {
        if (src == null) {
            throw new NullPointerException("src == null");
        } else if (dst == null) {
            throw new NullPointerException("dst == null");
        } else if (srcPos < 0 || dstPos < 0 || length < 0 || srcPos > src.length - length || dstPos > dst.length - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + src.length + " srcPos=" + srcPos + " dst.length=" + dst.length + " dstPos=" + dstPos + " length=" + length);
        } else if (length > ARRAYCOPY_SHORT_SHORT_ARRAY_THRESHOLD) {
            arraycopyBooleanUnchecked(src, srcPos, dst, dstPos, length);
        } else if (src != dst || srcPos >= dstPos || dstPos >= srcPos + length) {
            for (i = 0; i < length; i++) {
                dst[dstPos + i] = src[srcPos + i];
            }
        } else {
            for (i = length - 1; i >= 0; i--) {
                dst[dstPos + i] = src[srcPos + i];
            }
        }
    }

    private static void parsePropertyAssignments(Properties p, String[] assignments) {
        for (String assignment : assignments) {
            int split = assignment.indexOf(61);
            p.put(assignment.substring(0, split), assignment.substring(split + 1));
        }
    }

    private static Properties initUnchangeableSystemProperties() {
        VMRuntime runtime = VMRuntime.getRuntime();
        Properties p = new Properties();
        p.put("java.boot.class.path", runtime.bootClassPath());
        p.put("java.class.path", runtime.classPath());
        String javaHome = getenv("JAVA_HOME");
        if (javaHome == null) {
            javaHome = "/system";
        }
        p.put("java.home", javaHome);
        p.put("java.vm.version", runtime.vmVersion());
        try {
            p.put("user.name", Libcore.os.getpwuid(Libcore.os.getuid()).pw_name);
            StructUtsname info = Libcore.os.uname();
            p.put("os.arch", info.machine);
            if (!(p.get("os.name") == null || p.get("os.name").equals(info.sysname))) {
                logE("Wrong compile-time assumption for os.name: " + p.get("os.name") + " vs " + info.sysname);
                p.put("os.name", info.sysname);
            }
            p.put("os.version", info.release);
            p.put("android.icu.library.version", ICU.getIcuVersion());
            p.put("android.icu.unicode.version", ICU.getUnicodeVersion());
            p.put("android.icu.cldr.version", ICU.getCldrVersion());
            p.put("android.icu.impl.ICUBinary.dataPath", getenv("ANDROID_ROOT") + "/usr/icu");
            parsePropertyAssignments(p, specialProperties());
            parsePropertyAssignments(p, runtime.properties());
            for (String[] pair : AndroidHardcodedSystemProperties.STATIC_PROPERTIES) {
                if (p.containsKey(pair[0])) {
                    logE("Ignoring command line argument: -D" + pair[0]);
                }
                if (pair[1] == null) {
                    p.remove(pair[0]);
                } else {
                    p.put(pair[0], pair[1]);
                }
            }
            return p;
        } catch (Object exception) {
            throw new AssertionError(exception);
        }
    }

    private static Properties initProperties() {
        Properties p = new PropertiesWithNonOverrideableDefaults(unchangeableProps);
        setDefaultChangeableProperties(p);
        return p;
    }

    private static Properties setDefaultChangeableProperties(Properties p) {
        if (!unchangeableProps.containsKey("java.io.tmpdir")) {
            p.put("java.io.tmpdir", "/tmp");
        }
        if (!unchangeableProps.containsKey("user.home")) {
            p.put("user.home", "");
        }
        return p;
    }

    public static void setUnchangeableSystemProperty(String key, String value) {
        checkKey(key);
        unchangeableProps.put(key, value);
    }

    private static void addLegacyLocaleSystemProperties() {
        String locale = getProperty("user.locale", "");
        if (locale.isEmpty()) {
            String language = getProperty("user.language", "");
            String region = getProperty("user.region", "");
            if (language.isEmpty()) {
                setUnchangeableSystemProperty("user.language", "en");
            }
            if (region.isEmpty()) {
                setUnchangeableSystemProperty("user.region", "US");
                return;
            }
            return;
        }
        Locale l = Locale.forLanguageTag(locale);
        setUnchangeableSystemProperty("user.language", l.getLanguage());
        setUnchangeableSystemProperty("user.region", l.getCountry());
        setUnchangeableSystemProperty("user.variant", l.getVariant());
    }

    public static Properties getProperties() {
        return props;
    }

    public static String lineSeparator() {
        return lineSeparator;
    }

    public static void setProperties(Properties props) {
        Properties baseProperties = new PropertiesWithNonOverrideableDefaults(unchangeableProps);
        if (props != null) {
            baseProperties.putAll(props);
        } else {
            setDefaultChangeableProperties(baseProperties);
        }
        props = baseProperties;
    }

    public static String getProperty(String key) {
        checkKey(key);
        return props.getProperty(key);
    }

    public static String getProperty(String key, String def) {
        checkKey(key);
        return props.getProperty(key, def);
    }

    public static String setProperty(String key, String value) {
        checkKey(key);
        return (String) props.setProperty(key, value);
    }

    public static String clearProperty(String key) {
        checkKey(key);
        return (String) props.remove(key);
    }

    private static void checkKey(String key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        } else if (key.equals("")) {
            throw new IllegalArgumentException("key can't be empty");
        }
    }

    public static String getenv(String name) {
        if (name != null) {
            return Libcore.os.getenv(name);
        }
        throw new NullPointerException("name == null");
    }

    public static Map<String, String> getenv() {
        return ProcessEnvironment.getenv();
    }

    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }

    public static void gc() {
        synchronized (LOCK) {
            boolean shouldRunGC = justRanFinalization;
            if (shouldRunGC) {
                justRanFinalization = false;
            } else {
                runGC = true;
            }
        }
        if (shouldRunGC) {
            Runtime.getRuntime().gc();
        }
    }

    public static void runFinalization() {
        synchronized (LOCK) {
            boolean shouldRunGC = runGC;
            runGC = false;
        }
        if (shouldRunGC) {
            Runtime.getRuntime().gc();
        }
        Runtime.getRuntime().runFinalization();
        synchronized (LOCK) {
            justRanFinalization = true;
        }
    }

    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        Runtime.getRuntime();
        Runtime.runFinalizersOnExit(value);
    }

    public static void load(String filename) {
        Runtime.getRuntime().load0(VMStack.getStackClass1(), filename);
    }

    public static void loadLibrary(String libname) {
        Runtime.getRuntime().loadLibrary0(VMStack.getCallingClassLoader(), libname);
    }

    public static void logE(String message) {
        log('E', message, null);
    }

    public static void logE(String message, Throwable th) {
        log('E', message, th);
    }

    public static void logI(String message) {
        log('I', message, null);
    }

    public static void logI(String message, Throwable th) {
        log('I', message, th);
    }

    public static void logW(String message) {
        log('W', message, null);
    }

    public static void logW(String message, Throwable th) {
        log('W', message, th);
    }
}
