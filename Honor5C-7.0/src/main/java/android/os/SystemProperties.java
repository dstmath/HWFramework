package android.os;

import java.util.ArrayList;
import java.util.Locale;

public class SystemProperties {
    public static final int PROP_NAME_MAX = 31;
    public static final int PROP_VALUE_MAX = 91;
    private static final ArrayList<Runnable> sChangeCallbacks = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.SystemProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.SystemProperties.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.SystemProperties.<clinit>():void");
    }

    private static native void native_add_change_callback();

    private static native String native_get(String str);

    private static native String native_get(String str, String str2);

    private static native boolean native_get_boolean(String str, boolean z);

    private static native int native_get_int(String str, int i);

    private static native long native_get_long(String str, long j);

    private static native void native_set(String str, String str2);

    public static String get(String key) {
        if (key.length() <= PROP_NAME_MAX) {
            return native_get(key);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static String get(String key, String def) {
        if (key.length() <= PROP_NAME_MAX) {
            return native_get(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static int getInt(String key, int def) {
        if (key.length() <= PROP_NAME_MAX) {
            return native_get_int(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static long getLong(String key, long def) {
        if (key.length() <= PROP_NAME_MAX) {
            return native_get_long(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static boolean getBoolean(String key, boolean def) {
        if (key.length() <= PROP_NAME_MAX) {
            return native_get_boolean(key, def);
        }
        throw new IllegalArgumentException("key.length > 31");
    }

    public static void set(String key, String val) {
        if (key.length() > PROP_NAME_MAX) {
            throw new IllegalArgumentException("key.length > 31");
        } else if (val == null || val.length() <= PROP_VALUE_MAX) {
            native_set(key, val);
        } else {
            throw new IllegalArgumentException("val.length > 91");
        }
    }

    public static void addChangeCallback(Runnable callback) {
        synchronized (sChangeCallbacks) {
            if (sChangeCallbacks.size() == 0) {
                native_add_change_callback();
            }
            sChangeCallbacks.add(callback);
        }
    }

    public static boolean getRTLFlag() {
        if (Locale.getDefault().getLanguage().contains("ar") || Locale.getDefault().getLanguage().contains("iw") || Locale.getDefault().getLanguage().contains("fa") || Locale.getDefault().getLanguage().contains("ur")) {
            return true;
        }
        return Locale.getDefault().getLanguage().contains("ug");
    }

    static void callChangeCallbacks() {
        synchronized (sChangeCallbacks) {
            if (sChangeCallbacks.size() == 0) {
                return;
            }
            ArrayList<Runnable> callbacks = new ArrayList(sChangeCallbacks);
            for (int i = 0; i < callbacks.size(); i++) {
                ((Runnable) callbacks.get(i)).run();
            }
        }
    }
}
