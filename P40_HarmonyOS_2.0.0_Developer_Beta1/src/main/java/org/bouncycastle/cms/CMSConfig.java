package org.bouncycastle.cms;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class CMSConfig {
    public static void setSigningDigestAlgorithmMapping(String str, String str2) {
        new ASN1ObjectIdentifier(str);
    }

    public static void setSigningEncryptionAlgorithmMapping(String str, String str2) {
        CMSSignedHelper.INSTANCE.setSigningEncryptionAlgorithmMapping(new ASN1ObjectIdentifier(str), str2);
    }
}
