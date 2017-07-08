package com.android.org.bouncycastle.jcajce.util;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import java.io.IOException;
import java.security.AlgorithmParameters;

public class JcaJceUtils {
    private JcaJceUtils() {
    }

    public static ASN1Encodable extractParameters(AlgorithmParameters params) throws IOException {
        try {
            return ASN1Primitive.fromByteArray(params.getEncoded("ASN.1"));
        } catch (Exception e) {
            return ASN1Primitive.fromByteArray(params.getEncoded());
        }
    }

    public static void loadParameters(AlgorithmParameters params, ASN1Encodable sParams) throws IOException {
        try {
            params.init(sParams.toASN1Primitive().getEncoded(), "ASN.1");
        } catch (Exception e) {
            params.init(sParams.toASN1Primitive().getEncoded());
        }
    }

    public static String getDigestAlgName(ASN1ObjectIdentifier digestAlgOID) {
        if (PKCSObjectIdentifiers.md5.equals(digestAlgOID)) {
            return "MD5";
        }
        if (OIWObjectIdentifiers.idSHA1.equals(digestAlgOID)) {
            return "SHA1";
        }
        if (NISTObjectIdentifiers.id_sha224.equals(digestAlgOID)) {
            return "SHA224";
        }
        if (NISTObjectIdentifiers.id_sha256.equals(digestAlgOID)) {
            return "SHA256";
        }
        if (NISTObjectIdentifiers.id_sha384.equals(digestAlgOID)) {
            return "SHA384";
        }
        if (NISTObjectIdentifiers.id_sha512.equals(digestAlgOID)) {
            return "SHA512";
        }
        return digestAlgOID.getId();
    }
}
