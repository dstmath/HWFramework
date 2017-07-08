package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface X509ObjectIdentifiers {
    public static final ASN1ObjectIdentifier commonName = null;
    public static final ASN1ObjectIdentifier countryName = null;
    public static final ASN1ObjectIdentifier crlAccessMethod = null;
    public static final ASN1ObjectIdentifier id_SHA1 = null;
    public static final ASN1ObjectIdentifier id_ad = null;
    public static final ASN1ObjectIdentifier id_ad_caIssuers = null;
    public static final ASN1ObjectIdentifier id_ad_ocsp = null;
    public static final ASN1ObjectIdentifier id_at_name = null;
    public static final ASN1ObjectIdentifier id_at_telephoneNumber = null;
    public static final ASN1ObjectIdentifier id_ce = null;
    public static final ASN1ObjectIdentifier id_ea_rsa = null;
    public static final ASN1ObjectIdentifier id_pe = null;
    public static final ASN1ObjectIdentifier id_pkix = null;
    public static final ASN1ObjectIdentifier localityName = null;
    public static final ASN1ObjectIdentifier ocspAccessMethod = null;
    public static final ASN1ObjectIdentifier organization = null;
    public static final ASN1ObjectIdentifier organizationalUnitName = null;
    public static final ASN1ObjectIdentifier ripemd160 = null;
    public static final ASN1ObjectIdentifier ripemd160WithRSAEncryption = null;
    public static final ASN1ObjectIdentifier stateOrProvinceName = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers.<clinit>():void");
    }
}
