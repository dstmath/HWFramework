package com.android.org.bouncycastle.asn1.oiw;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface OIWObjectIdentifiers {
    public static final ASN1ObjectIdentifier desCBC = null;
    public static final ASN1ObjectIdentifier desCFB = null;
    public static final ASN1ObjectIdentifier desECB = null;
    public static final ASN1ObjectIdentifier desEDE = null;
    public static final ASN1ObjectIdentifier desOFB = null;
    public static final ASN1ObjectIdentifier dsaWithSHA1 = null;
    public static final ASN1ObjectIdentifier elGamalAlgorithm = null;
    public static final ASN1ObjectIdentifier idSHA1 = null;
    public static final ASN1ObjectIdentifier md4WithRSA = null;
    public static final ASN1ObjectIdentifier md4WithRSAEncryption = null;
    public static final ASN1ObjectIdentifier md5WithRSA = null;
    public static final ASN1ObjectIdentifier sha1WithRSA = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers.<clinit>():void");
    }
}
