package android.icu.impl;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public class ICUConfig {
    private static final Properties CONFIG_PROPS = null;
    public static final String CONFIG_PROPS_FILE = "/android/icu/ICUConfig.properties";

    /* renamed from: android.icu.impl.ICUConfig.1 */
    static class AnonymousClass1 implements PrivilegedAction<String> {
        final /* synthetic */ String val$fname;

        AnonymousClass1(String val$fname) {
            this.val$fname = val$fname;
        }

        public String run() {
            return System.getProperty(this.val$fname);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUConfig.<clinit>():void");
    }

    public static String get(String name) {
        return get(name, null);
    }

    public static String get(String name, String def) {
        String val = null;
        String fname = name;
        if (System.getSecurityManager() != null) {
            try {
                val = (String) AccessController.doPrivileged(new AnonymousClass1(name));
            } catch (AccessControlException e) {
            }
        } else {
            val = System.getProperty(name);
        }
        if (val == null) {
            return CONFIG_PROPS.getProperty(name, def);
        }
        return val;
    }
}
