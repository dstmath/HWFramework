package com.android.org.bouncycastle.asn1.sec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParametersHolder;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.endo.GLVTypeBEndomorphism;
import com.android.org.bouncycastle.math.ec.endo.GLVTypeBParameters;
import com.android.org.bouncycastle.util.Strings;
import com.android.org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;

public class SECNamedCurves {
    static final Hashtable curves = null;
    static final Hashtable names = null;
    static final Hashtable objIds = null;
    static X9ECParametersHolder secp112r1;
    static X9ECParametersHolder secp112r2;
    static X9ECParametersHolder secp128r1;
    static X9ECParametersHolder secp128r2;
    static X9ECParametersHolder secp160k1;
    static X9ECParametersHolder secp160r1;
    static X9ECParametersHolder secp160r2;
    static X9ECParametersHolder secp192k1;
    static X9ECParametersHolder secp192r1;
    static X9ECParametersHolder secp224k1;
    static X9ECParametersHolder secp224r1;
    static X9ECParametersHolder secp256k1;
    static X9ECParametersHolder secp256r1;
    static X9ECParametersHolder secp384r1;
    static X9ECParametersHolder secp521r1;
    static X9ECParametersHolder sect113r1;
    static X9ECParametersHolder sect113r2;
    static X9ECParametersHolder sect131r1;
    static X9ECParametersHolder sect131r2;
    static X9ECParametersHolder sect163k1;
    static X9ECParametersHolder sect163r1;
    static X9ECParametersHolder sect163r2;
    static X9ECParametersHolder sect193r1;
    static X9ECParametersHolder sect193r2;
    static X9ECParametersHolder sect233k1;
    static X9ECParametersHolder sect233r1;
    static X9ECParametersHolder sect239k1;
    static X9ECParametersHolder sect283k1;
    static X9ECParametersHolder sect283r1;
    static X9ECParametersHolder sect409k1;
    static X9ECParametersHolder sect409r1;
    static X9ECParametersHolder sect571k1;
    static X9ECParametersHolder sect571r1;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.sec.SECNamedCurves.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.sec.SECNamedCurves.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.sec.SECNamedCurves.<clinit>():void");
    }

    private static ECCurve configureCurve(ECCurve curve) {
        return curve;
    }

    private static ECCurve configureCurveGLV(ECCurve c, GLVTypeBParameters p) {
        return c.configure().setEndomorphism(new GLVTypeBEndomorphism(c, p)).create();
    }

    private static BigInteger fromHex(String hex) {
        return new BigInteger(1, Hex.decode(hex));
    }

    static void defineCurve(String name, ASN1ObjectIdentifier oid, X9ECParametersHolder holder) {
        objIds.put(name.toLowerCase(), oid);
        names.put(oid, name);
        curves.put(oid, holder);
    }

    public static X9ECParameters getByName(String name) {
        ASN1ObjectIdentifier oid = getOID(name);
        if (oid == null) {
            return null;
        }
        return getByOID(oid);
    }

    public static X9ECParameters getByOID(ASN1ObjectIdentifier oid) {
        X9ECParametersHolder holder = (X9ECParametersHolder) curves.get(oid);
        if (holder == null) {
            return null;
        }
        return holder.getParameters();
    }

    public static ASN1ObjectIdentifier getOID(String name) {
        return (ASN1ObjectIdentifier) objIds.get(Strings.toLowerCase(name));
    }

    public static String getName(ASN1ObjectIdentifier oid) {
        return (String) names.get(oid);
    }

    public static Enumeration getNames() {
        return names.elements();
    }
}
