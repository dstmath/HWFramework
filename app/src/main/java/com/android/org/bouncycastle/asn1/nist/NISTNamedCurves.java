package com.android.org.bouncycastle.asn1.nist;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.sec.SECNamedCurves;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.util.Strings;
import java.util.Enumeration;
import java.util.Hashtable;

public class NISTNamedCurves {
    static final Hashtable names = null;
    static final Hashtable objIds = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.nist.NISTNamedCurves.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.nist.NISTNamedCurves.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.nist.NISTNamedCurves.<clinit>():void");
    }

    static void defineCurveAlias(String name, ASN1ObjectIdentifier oid) {
        objIds.put(name.toUpperCase(), oid);
        names.put(oid, name);
    }

    public static X9ECParameters getByName(String name) {
        ASN1ObjectIdentifier oid = getOID(name);
        if (oid == null) {
            return null;
        }
        return getByOID(oid);
    }

    public static X9ECParameters getByOID(ASN1ObjectIdentifier oid) {
        return SECNamedCurves.getByOID(oid);
    }

    public static ASN1ObjectIdentifier getOID(String name) {
        return (ASN1ObjectIdentifier) objIds.get(Strings.toUpperCase(name));
    }

    public static String getName(ASN1ObjectIdentifier oid) {
        return (String) names.get(oid);
    }

    public static Enumeration getNames() {
        return names.elements();
    }
}
