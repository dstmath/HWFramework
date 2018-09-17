package com.android.org.bouncycastle.crypto.ec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParametersHolder;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.endo.GLVTypeBEndomorphism;
import com.android.org.bouncycastle.math.ec.endo.GLVTypeBParameters;
import com.android.org.bouncycastle.util.Strings;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class CustomNamedCurves {
    static final Hashtable nameToCurve = null;
    static final Hashtable nameToOID = null;
    static final Vector names = null;
    static final Hashtable oidToCurve = null;
    static final Hashtable oidToName = null;
    static X9ECParametersHolder secp192k1;
    static X9ECParametersHolder secp192r1;
    static X9ECParametersHolder secp224k1;
    static X9ECParametersHolder secp224r1;
    static X9ECParametersHolder secp256k1;
    static X9ECParametersHolder secp256r1;
    static X9ECParametersHolder secp384r1;
    static X9ECParametersHolder secp521r1;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.ec.CustomNamedCurves.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.ec.CustomNamedCurves.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.ec.CustomNamedCurves.<clinit>():void");
    }

    private static ECCurve configureCurve(ECCurve curve) {
        return curve;
    }

    private static ECCurve configureCurveGLV(ECCurve c, GLVTypeBParameters p) {
        return c.configure().setEndomorphism(new GLVTypeBEndomorphism(c, p)).create();
    }

    static void defineCurve(String name, X9ECParametersHolder holder) {
        names.addElement(name);
        nameToCurve.put(Strings.toLowerCase(name), holder);
    }

    static void defineCurveWithOID(String name, ASN1ObjectIdentifier oid, X9ECParametersHolder holder) {
        names.addElement(name);
        oidToName.put(oid, name);
        oidToCurve.put(oid, holder);
        name = Strings.toLowerCase(name);
        nameToOID.put(name, oid);
        nameToCurve.put(name, holder);
    }

    static void defineCurveAlias(String name, ASN1ObjectIdentifier oid) {
        Object curve = oidToCurve.get(oid);
        if (curve == null) {
            throw new IllegalStateException();
        }
        name = Strings.toLowerCase(name);
        nameToOID.put(name, oid);
        nameToCurve.put(name, curve);
    }

    public static X9ECParameters getByName(String name) {
        X9ECParametersHolder holder = (X9ECParametersHolder) nameToCurve.get(Strings.toLowerCase(name));
        if (holder == null) {
            return null;
        }
        return holder.getParameters();
    }

    public static X9ECParameters getByOID(ASN1ObjectIdentifier oid) {
        X9ECParametersHolder holder = (X9ECParametersHolder) oidToCurve.get(oid);
        if (holder == null) {
            return null;
        }
        return holder.getParameters();
    }

    public static ASN1ObjectIdentifier getOID(String name) {
        return (ASN1ObjectIdentifier) nameToOID.get(Strings.toLowerCase(name));
    }

    public static String getName(ASN1ObjectIdentifier oid) {
        return (String) oidToName.get(oid);
    }

    public static Enumeration getNames() {
        return names.elements();
    }
}
