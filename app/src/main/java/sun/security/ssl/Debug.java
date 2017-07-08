package sun.security.ssl;

import java.io.PrintStream;
import java.security.AccessController;
import java.util.Locale;
import java.util.jar.Pack200.Unpacker;
import sun.security.action.GetPropertyAction;

public class Debug {
    private static String args;
    private String prefix;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.Debug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.Debug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.Debug.<clinit>():void");
    }

    public static void Help() {
        System.err.println();
        System.err.println("all            turn on all debugging");
        System.err.println("ssl            turn on ssl debugging");
        System.err.println();
        System.err.println("The following can be used with ssl:");
        System.err.println("\trecord       enable per-record tracing");
        System.err.println("\thandshake    print each handshake message");
        System.err.println("\tkeygen       print key generation data");
        System.err.println("\tsession      print session activity");
        System.err.println("\tdefaultctx   print default SSL initialization");
        System.err.println("\tsslctx       print SSLContext tracing");
        System.err.println("\tsessioncache print session cache tracing");
        System.err.println("\tkeymanager   print key manager tracing");
        System.err.println("\ttrustmanager print trust manager tracing");
        System.err.println("\tpluggability print pluggability tracing");
        System.err.println();
        System.err.println("\thandshake debugging can be widened with:");
        System.err.println("\tdata         hex dump of each handshake message");
        System.err.println("\tverbose      verbose handshake message printing");
        System.err.println();
        System.err.println("\trecord debugging can be widened with:");
        System.err.println("\tplaintext    hex dump of record plaintext");
        System.err.println("\tpacket       print raw SSL/TLS packets");
        System.err.println();
        System.exit(0);
    }

    public static Debug getInstance(String option) {
        return getInstance(option, option);
    }

    public static Debug getInstance(String option, String prefix) {
        if (!isOn(option)) {
            return null;
        }
        Debug d = new Debug();
        d.prefix = prefix;
        return d;
    }

    public static boolean isOn(String option) {
        boolean z = true;
        if (args == null) {
            return false;
        }
        option = option.toLowerCase(Locale.ENGLISH);
        if (args.indexOf("all") != -1) {
            return true;
        }
        int n = args.indexOf("ssl");
        if (n != -1 && args.indexOf("sslctx", n) == -1) {
            boolean z2;
            if (option.equals("data") || option.equals("packet")) {
                z2 = true;
            } else {
                z2 = option.equals("plaintext");
            }
            if (!z2) {
                return true;
            }
        }
        if (args.indexOf(option) == -1) {
            z = false;
        }
        return z;
    }

    public void println(String message) {
        System.err.println(this.prefix + ": " + message);
    }

    public void println() {
        System.err.println(this.prefix + ":");
    }

    public static void println(String prefix, String message) {
        System.err.println(prefix + ": " + message);
    }

    public static void println(PrintStream s, String name, byte[] data) {
        s.print(name + ":  { ");
        if (data == null) {
            s.print("null");
        } else {
            for (int i = 0; i < data.length; i++) {
                if (i != 0) {
                    s.print(", ");
                }
                s.print(data[i] & 255);
            }
        }
        s.println(" }");
    }

    static boolean getBooleanProperty(String propName, boolean defaultValue) {
        String b = (String) AccessController.doPrivileged(new GetPropertyAction(propName));
        if (b == null) {
            return defaultValue;
        }
        if (b.equalsIgnoreCase(Unpacker.FALSE)) {
            return false;
        }
        if (b.equalsIgnoreCase(Unpacker.TRUE)) {
            return true;
        }
        throw new RuntimeException("Value of " + propName + " must either be 'true' or 'false'");
    }

    static String toString(byte[] b) {
        return sun.security.util.Debug.toString(b);
    }
}
