package sun.security.ssl;

import java.io.IOException;
import java.security.spec.ECParameterSpec;
import java.util.Map;
import javax.net.ssl.SSLProtocolException;

/* compiled from: HelloExtensions */
final class SupportedEllipticCurvesExtension extends HelloExtension {
    private static final int ARBITRARY_CHAR2 = 65282;
    private static final int ARBITRARY_PRIME = 65281;
    static final SupportedEllipticCurvesExtension DEFAULT = null;
    private static final String[] NAMED_CURVE_OID_TABLE = null;
    private static final Map<String, Integer> curveIndices = null;
    private static final boolean fips = false;
    private final int[] curveIds;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SupportedEllipticCurvesExtension.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SupportedEllipticCurvesExtension.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SupportedEllipticCurvesExtension.<clinit>():void");
    }

    private SupportedEllipticCurvesExtension(int[] curveIds) {
        super(ExtensionType.EXT_ELLIPTIC_CURVES);
        this.curveIds = curveIds;
    }

    SupportedEllipticCurvesExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_ELLIPTIC_CURVES);
        int k = s.getInt16();
        if ((len & 1) == 0 && k + 2 == len) {
            this.curveIds = new int[(k >> 1)];
            for (int i = 0; i < this.curveIds.length; i++) {
                this.curveIds[i] = s.getInt16();
            }
            return;
        }
        throw new SSLProtocolException("Invalid " + this.type + " extension");
    }

    boolean contains(int index) {
        for (int curveId : this.curveIds) {
            if (index == curveId) {
                return true;
            }
        }
        return false;
    }

    int[] curveIds() {
        return this.curveIds;
    }

    int length() {
        return (this.curveIds.length << 1) + 6;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        int k = this.curveIds.length << 1;
        s.putInt16(k + 2);
        s.putInt16(k);
        for (int curveId : this.curveIds) {
            s.putInt16(curveId);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Extension ").append(this.type).append(", curve names: {");
        boolean first = true;
        for (int curveId : this.curveIds) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            String oid = getCurveOid(curveId);
            if (oid != null) {
                ECParameterSpec spec = JsseJce.getECParameterSpec(oid);
                if (spec != null) {
                    sb.append(spec.toString().split(" ")[0]);
                } else {
                    sb.append(oid);
                }
            } else if (curveId == ARBITRARY_PRIME) {
                sb.append("arbitrary_explicit_prime_curves");
            } else if (curveId == ARBITRARY_CHAR2) {
                sb.append("arbitrary_explicit_char2_curves");
            } else {
                sb.append("unknown curve ").append(curveId);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    static boolean isSupported(int index) {
        if (index <= 0 || index >= NAMED_CURVE_OID_TABLE.length) {
            return false;
        }
        if (fips) {
            return DEFAULT.contains(index);
        }
        return true;
    }

    static int getCurveIndex(ECParameterSpec params) {
        int i = -1;
        String oid = JsseJce.getNamedCurveOid(params);
        if (oid == null) {
            return -1;
        }
        Integer n = (Integer) curveIndices.get(oid);
        if (n != null) {
            i = n.intValue();
        }
        return i;
    }

    static String getCurveOid(int index) {
        if (index <= 0 || index >= NAMED_CURVE_OID_TABLE.length) {
            return null;
        }
        return NAMED_CURVE_OID_TABLE[index];
    }
}
