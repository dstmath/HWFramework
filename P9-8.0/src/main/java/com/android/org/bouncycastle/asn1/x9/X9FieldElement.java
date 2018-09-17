package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;
import com.android.org.bouncycastle.math.ec.ECFieldElement.Fp;
import java.math.BigInteger;

public class X9FieldElement extends ASN1Object {
    private static X9IntegerConverter converter = new X9IntegerConverter();
    protected ECFieldElement f;

    public X9FieldElement(ECFieldElement f) {
        this.f = f;
    }

    public X9FieldElement(BigInteger p, ASN1OctetString s) {
        this(new Fp(p, new BigInteger(1, s.getOctets())));
    }

    public X9FieldElement(int m, int k1, int k2, int k3, ASN1OctetString s) {
        this(new F2m(m, k1, k2, k3, new BigInteger(1, s.getOctets())));
    }

    public ECFieldElement getValue() {
        return this.f;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DEROctetString(converter.integerToBytes(this.f.toBigInteger(), converter.getByteLength(this.f)));
    }
}
