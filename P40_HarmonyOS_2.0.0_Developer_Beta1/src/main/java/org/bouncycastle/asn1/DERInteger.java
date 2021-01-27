package org.bouncycastle.asn1;

import java.math.BigInteger;

public class DERInteger extends ASN1Integer {
    public DERInteger(long j) {
        super(j);
    }

    public DERInteger(BigInteger bigInteger) {
        super(bigInteger);
    }

    public DERInteger(byte[] bArr) {
        super(bArr, true);
    }
}
