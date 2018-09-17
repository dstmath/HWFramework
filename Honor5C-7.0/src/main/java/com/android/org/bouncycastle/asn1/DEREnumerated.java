package com.android.org.bouncycastle.asn1;

import java.math.BigInteger;

public class DEREnumerated extends ASN1Enumerated {
    DEREnumerated(byte[] bytes) {
        super(bytes);
    }

    public DEREnumerated(BigInteger value) {
        super(value);
    }

    public DEREnumerated(int value) {
        super(value);
    }
}
