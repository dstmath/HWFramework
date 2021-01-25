package org.bouncycastle.jce.spec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public class ECNamedCurveParameterSpec extends ECParameterSpec {
    private String name;

    public ECNamedCurveParameterSpec(String str, ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger) {
        super(eCCurve, eCPoint, bigInteger);
        this.name = str;
    }

    public ECNamedCurveParameterSpec(String str, ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2) {
        super(eCCurve, eCPoint, bigInteger, bigInteger2);
        this.name = str;
    }

    public ECNamedCurveParameterSpec(String str, ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2, byte[] bArr) {
        super(eCCurve, eCPoint, bigInteger, bigInteger2, bArr);
        this.name = str;
    }

    public String getName() {
        return this.name;
    }
}
