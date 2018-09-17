package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import java.math.BigInteger;

public class ECNamedDomainParameters extends ECDomainParameters {
    private ASN1ObjectIdentifier name;

    public ECNamedDomainParameters(ASN1ObjectIdentifier name, ECCurve curve, ECPoint G, BigInteger n) {
        this(name, curve, G, n, null, null);
    }

    public ECNamedDomainParameters(ASN1ObjectIdentifier name, ECCurve curve, ECPoint G, BigInteger n, BigInteger h) {
        this(name, curve, G, n, h, null);
    }

    public ECNamedDomainParameters(ASN1ObjectIdentifier name, ECCurve curve, ECPoint G, BigInteger n, BigInteger h, byte[] seed) {
        super(curve, G, n, h, seed);
        this.name = name;
    }

    public ASN1ObjectIdentifier getName() {
        return this.name;
    }
}
