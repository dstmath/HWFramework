package sun.security.ssl;

public final class ProtocolVersion implements Comparable<ProtocolVersion> {
    static final ProtocolVersion DEFAULT = null;
    static final ProtocolVersion DEFAULT_HELLO = null;
    private static final boolean FIPS = false;
    static final int LIMIT_MAX_VALUE = 65535;
    static final int LIMIT_MIN_VALUE = 0;
    static final ProtocolVersion MAX = null;
    static final ProtocolVersion MIN = null;
    static final ProtocolVersion NONE = null;
    static final ProtocolVersion SSL20Hello = null;
    static final ProtocolVersion SSL30 = null;
    static final ProtocolVersion TLS10 = null;
    static final ProtocolVersion TLS11 = null;
    static final ProtocolVersion TLS12 = null;
    public final byte major;
    public final byte minor;
    final String name;
    public final int v;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.ProtocolVersion.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.ProtocolVersion.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.ProtocolVersion.<clinit>():void");
    }

    private ProtocolVersion(int v, String name) {
        this.v = v;
        this.name = name;
        this.major = (byte) (v >>> 8);
        this.minor = (byte) (v & 255);
    }

    private static ProtocolVersion valueOf(int v) {
        if (v == SSL30.v) {
            return SSL30;
        }
        if (v == TLS10.v) {
            return TLS10;
        }
        if (v == TLS11.v) {
            return TLS11;
        }
        if (v == TLS12.v) {
            return TLS12;
        }
        if (v == SSL20Hello.v) {
            return SSL20Hello;
        }
        return new ProtocolVersion(v, "Unknown-" + ((v >>> 8) & 255) + "." + (v & 255));
    }

    public static ProtocolVersion valueOf(int major, int minor) {
        return valueOf(((major & 255) << 8) | (minor & 255));
    }

    static ProtocolVersion valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Protocol cannot be null");
        } else if (FIPS && (name.equals(SSL30.name) || name.equals(SSL20Hello.name))) {
            throw new IllegalArgumentException("Only TLS 1.0 or later allowed in FIPS mode");
        } else if (name.equals(SSL30.name)) {
            return SSL30;
        } else {
            if (name.equals(TLS10.name)) {
                return TLS10;
            }
            if (name.equals(TLS11.name)) {
                return TLS11;
            }
            if (name.equals(TLS12.name)) {
                return TLS12;
            }
            if (name.equals(SSL20Hello.name)) {
                return SSL20Hello;
            }
            throw new IllegalArgumentException(name);
        }
    }

    public String toString() {
        return this.name;
    }

    public int compareTo(ProtocolVersion protocolVersion) {
        return this.v - protocolVersion.v;
    }
}
