package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;

public class KeyPurposeId extends ASN1Object {
    public static final KeyPurposeId anyExtendedKeyUsage = null;
    private static final ASN1ObjectIdentifier id_kp = null;
    public static final KeyPurposeId id_kp_OCSPSigning = null;
    public static final KeyPurposeId id_kp_capwapAC = null;
    public static final KeyPurposeId id_kp_capwapWTP = null;
    public static final KeyPurposeId id_kp_clientAuth = null;
    public static final KeyPurposeId id_kp_codeSigning = null;
    public static final KeyPurposeId id_kp_dvcs = null;
    public static final KeyPurposeId id_kp_eapOverLAN = null;
    public static final KeyPurposeId id_kp_eapOverPPP = null;
    public static final KeyPurposeId id_kp_emailProtection = null;
    public static final KeyPurposeId id_kp_ipsecEndSystem = null;
    public static final KeyPurposeId id_kp_ipsecIKE = null;
    public static final KeyPurposeId id_kp_ipsecTunnel = null;
    public static final KeyPurposeId id_kp_ipsecUser = null;
    public static final KeyPurposeId id_kp_sbgpCertAAServerAuth = null;
    public static final KeyPurposeId id_kp_scvpClient = null;
    public static final KeyPurposeId id_kp_scvpServer = null;
    public static final KeyPurposeId id_kp_scvp_responder = null;
    public static final KeyPurposeId id_kp_serverAuth = null;
    public static final KeyPurposeId id_kp_smartcardlogon = null;
    public static final KeyPurposeId id_kp_timeStamping = null;
    private ASN1ObjectIdentifier id;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.x509.KeyPurposeId.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.x509.KeyPurposeId.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x509.KeyPurposeId.<clinit>():void");
    }

    private KeyPurposeId(ASN1ObjectIdentifier id) {
        this.id = id;
    }

    public KeyPurposeId(String id) {
        this(new ASN1ObjectIdentifier(id));
    }

    public static KeyPurposeId getInstance(Object o) {
        if (o instanceof KeyPurposeId) {
            return (KeyPurposeId) o;
        }
        if (o != null) {
            return new KeyPurposeId(ASN1ObjectIdentifier.getInstance(o));
        }
        return null;
    }

    public ASN1ObjectIdentifier toOID() {
        return this.id;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.id;
    }

    public String getId() {
        return this.id.getId();
    }

    public String toString() {
        return this.id.toString();
    }
}
