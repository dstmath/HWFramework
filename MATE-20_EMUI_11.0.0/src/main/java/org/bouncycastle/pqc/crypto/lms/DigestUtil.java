package org.bouncycastle.pqc.crypto.lms;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;

class DigestUtil {
    private static Map<String, ASN1ObjectIdentifier> nameToOid = new HashMap();
    private static Map<ASN1ObjectIdentifier, String> oidToName = new HashMap();

    static {
        nameToOid.put("SHA-256", NISTObjectIdentifiers.id_sha256);
        nameToOid.put("SHA-512", NISTObjectIdentifiers.id_sha512);
        nameToOid.put("SHAKE128", NISTObjectIdentifiers.id_shake128);
        nameToOid.put("SHAKE256", NISTObjectIdentifiers.id_shake256);
        oidToName.put(NISTObjectIdentifiers.id_sha256, "SHA-256");
        oidToName.put(NISTObjectIdentifiers.id_sha512, "SHA-512");
        oidToName.put(NISTObjectIdentifiers.id_shake128, "SHAKE128");
        oidToName.put(NISTObjectIdentifiers.id_shake256, "SHAKE256");
    }

    DigestUtil() {
    }

    static Digest getDigest(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        if (aSN1ObjectIdentifier.equals((ASN1Primitive) NISTObjectIdentifiers.id_sha256)) {
            return new SHA256Digest();
        }
        if (aSN1ObjectIdentifier.equals((ASN1Primitive) NISTObjectIdentifiers.id_sha512)) {
            return new SHA512Digest();
        }
        if (aSN1ObjectIdentifier.equals((ASN1Primitive) NISTObjectIdentifiers.id_shake128)) {
            return new SHAKEDigest(128);
        }
        if (aSN1ObjectIdentifier.equals((ASN1Primitive) NISTObjectIdentifiers.id_shake256)) {
            return new SHAKEDigest(256);
        }
        throw new IllegalArgumentException("unrecognized digest OID: " + aSN1ObjectIdentifier);
    }

    static String getDigestName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String str = oidToName.get(aSN1ObjectIdentifier);
        if (str != null) {
            return str;
        }
        throw new IllegalArgumentException("unrecognized digest oid: " + aSN1ObjectIdentifier);
    }

    static ASN1ObjectIdentifier getDigestOID(String str) {
        ASN1ObjectIdentifier aSN1ObjectIdentifier = nameToOid.get(str);
        if (aSN1ObjectIdentifier != null) {
            return aSN1ObjectIdentifier;
        }
        throw new IllegalArgumentException("unrecognized digest name: " + str);
    }

    public static int getDigestSize(Digest digest) {
        boolean z = digest instanceof Xof;
        int digestSize = digest.getDigestSize();
        return z ? digestSize * 2 : digestSize;
    }
}
