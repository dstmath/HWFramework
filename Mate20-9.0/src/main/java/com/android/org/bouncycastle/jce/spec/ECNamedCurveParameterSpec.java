package com.android.org.bouncycastle.jce.spec;

import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import java.math.BigInteger;

public class ECNamedCurveParameterSpec extends ECParameterSpec {
    private String name;

    public ECNamedCurveParameterSpec(String name2, ECCurve curve, ECPoint G, BigInteger n) {
        super(curve, G, n);
        this.name = name2;
    }

    public ECNamedCurveParameterSpec(String name2, ECCurve curve, ECPoint G, BigInteger n, BigInteger h) {
        super(curve, G, n, h);
        this.name = name2;
    }

    public ECNamedCurveParameterSpec(String name2, ECCurve curve, ECPoint G, BigInteger n, BigInteger h, byte[] seed) {
        super(curve, G, n, h, seed);
        this.name = name2;
    }

    public String getName() {
        return this.name;
    }
}
