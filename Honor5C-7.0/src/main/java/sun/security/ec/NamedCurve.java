package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;

public final class NamedCurve extends ECParameterSpec {
    private static final int B = 2;
    private static final int BD = 6;
    private static final int P = 1;
    private static final int PD = 5;
    private static Pattern SPLIT_PATTERN;
    private static final Map<Integer, NamedCurve> lengthMap = null;
    private static final Map<String, NamedCurve> nameMap = null;
    private static final Map<String, NamedCurve> oidMap = null;
    private final byte[] encoded;
    private final String name;
    private final ObjectIdentifier oid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ec.NamedCurve.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ec.NamedCurve.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ec.NamedCurve.<clinit>():void");
    }

    private NamedCurve(String name, ObjectIdentifier oid, EllipticCurve curve, ECPoint g, BigInteger n, int h) throws IOException {
        super(curve, g, n, h);
        this.name = name;
        this.oid = oid;
        DerOutputStream out = new DerOutputStream();
        out.putOID(oid);
        this.encoded = out.toByteArray();
    }

    public static ECParameterSpec getECParameterSpec(String name) {
        NamedCurve spec = (NamedCurve) oidMap.get(name);
        return spec != null ? spec : (ECParameterSpec) nameMap.get(name);
    }

    static ECParameterSpec getECParameterSpec(ObjectIdentifier oid) {
        return getECParameterSpec(oid.toString());
    }

    public static ECParameterSpec getECParameterSpec(int length) {
        return (ECParameterSpec) lengthMap.get(Integer.valueOf(length));
    }

    public static Collection<? extends ECParameterSpec> knownECParameterSpecs() {
        return Collections.unmodifiableCollection(oidMap.values());
    }

    byte[] getEncoded() {
        return (byte[]) this.encoded.clone();
    }

    ObjectIdentifier getObjectIdentifier() {
        return this.oid;
    }

    public String toString() {
        return this.name + " (" + this.oid + ")";
    }

    private static BigInteger bi(String s) {
        return new BigInteger(s, 16);
    }

    private static void add(String name, String soid, int type, String sfield, String a, String b, String x, String y, String n, int h) {
        ECField field;
        BigInteger p = bi(sfield);
        if (type == P || type == PD) {
            field = new ECFieldFp(p);
        } else if (type == B || type == BD) {
            field = new ECFieldF2m(p.bitLength() - 1, p);
        } else {
            throw new RuntimeException("Invalid type: " + type);
        }
        try {
            NamedCurve params = new NamedCurve(name, new ObjectIdentifier(soid), new EllipticCurve(field, bi(a), bi(b)), new ECPoint(bi(x), bi(y)), bi(n), h);
            if (oidMap.put(soid, params) != null) {
                throw new RuntimeException("Duplication oid: " + soid);
            }
            String[] commonNames = SPLIT_PATTERN.split(name);
            int length = commonNames.length;
            for (int i = 0; i < length; i += P) {
                String commonName = commonNames[i];
                if (nameMap.put(commonName.trim(), params) != null) {
                    throw new RuntimeException("Duplication name: " + commonName);
                }
            }
            int len = field.getFieldSize();
            if (!(type == PD || type == BD)) {
                if (lengthMap.get(Integer.valueOf(len)) != null) {
                    return;
                }
            }
            lengthMap.put(Integer.valueOf(len), params);
        } catch (IOException e) {
            throw new RuntimeException("Internal error", e);
        }
    }
}
