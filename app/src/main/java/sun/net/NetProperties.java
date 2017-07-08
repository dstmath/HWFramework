package sun.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class NetProperties {
    private static Properties props;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.NetProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.NetProperties.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.NetProperties.<clinit>():void");
    }

    private NetProperties() {
    }

    private static void loadDefaultProperties() {
        String fname = System.getProperty("java.home");
        if (fname == null) {
            throw new Error("Can't find java.home ??");
        }
        try {
            InputStream bin = new BufferedInputStream(new FileInputStream(new File(new File(fname, "lib"), "net.properties").getCanonicalPath()));
            props.load(bin);
            bin.close();
        } catch (Exception e) {
        }
    }

    public static String get(String key) {
        try {
            return System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public static Integer getInteger(String key, int defval) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        if (val != null) {
            try {
                return Integer.decode(val);
            } catch (NumberFormatException e3) {
            }
        }
        return new Integer(defval);
    }

    public static Boolean getBoolean(String key) {
        String val = null;
        try {
            val = System.getProperty(key, props.getProperty(key));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        if (val != null) {
            try {
                return Boolean.valueOf(val);
            } catch (NumberFormatException e3) {
            }
        }
        return null;
    }
}
