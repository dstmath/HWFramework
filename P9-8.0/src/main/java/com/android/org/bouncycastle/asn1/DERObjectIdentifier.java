package com.android.org.bouncycastle.asn1;

public class DERObjectIdentifier extends ASN1ObjectIdentifier {
    public DERObjectIdentifier(String identifier) {
        super(identifier);
    }

    DERObjectIdentifier(byte[] bytes) {
        super(bytes);
    }

    DERObjectIdentifier(ASN1ObjectIdentifier oid, String branch) {
        super(oid, branch);
    }
}
