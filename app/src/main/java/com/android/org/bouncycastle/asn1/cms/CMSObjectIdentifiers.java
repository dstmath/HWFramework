package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMSObjectIdentifiers {
    public static final ASN1ObjectIdentifier authEnvelopedData = null;
    public static final ASN1ObjectIdentifier authenticatedData = null;
    public static final ASN1ObjectIdentifier compressedData = null;
    public static final ASN1ObjectIdentifier data = null;
    public static final ASN1ObjectIdentifier digestedData = null;
    public static final ASN1ObjectIdentifier encryptedData = null;
    public static final ASN1ObjectIdentifier envelopedData = null;
    public static final ASN1ObjectIdentifier id_ri = null;
    public static final ASN1ObjectIdentifier id_ri_ocsp_response = null;
    public static final ASN1ObjectIdentifier id_ri_scvp = null;
    public static final ASN1ObjectIdentifier signedAndEnvelopedData = null;
    public static final ASN1ObjectIdentifier signedData = null;
    public static final ASN1ObjectIdentifier timestampedData = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.cms.CMSObjectIdentifiers.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.cms.CMSObjectIdentifiers.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.cms.CMSObjectIdentifiers.<clinit>():void");
    }
}
