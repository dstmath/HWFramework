package java.security.spec;

import java.math.BigInteger;

public class ECParameterSpec implements AlgorithmParameterSpec {
    private final EllipticCurve curve;
    private String curveName;
    private final ECPoint g;
    private final int h;
    private final BigInteger n;

    public ECParameterSpec(EllipticCurve curve2, ECPoint g2, BigInteger n2, int h2) {
        if (curve2 == null) {
            throw new NullPointerException("curve is null");
        } else if (g2 == null) {
            throw new NullPointerException("g is null");
        } else if (n2 == null) {
            throw new NullPointerException("n is null");
        } else if (n2.signum() != 1) {
            throw new IllegalArgumentException("n is not positive");
        } else if (h2 > 0) {
            this.curve = curve2;
            this.g = g2;
            this.n = n2;
            this.h = h2;
        } else {
            throw new IllegalArgumentException("h is not positive");
        }
    }

    public EllipticCurve getCurve() {
        return this.curve;
    }

    public ECPoint getGenerator() {
        return this.g;
    }

    public BigInteger getOrder() {
        return this.n;
    }

    public int getCofactor() {
        return this.h;
    }

    public void setCurveName(String curveName2) {
        this.curveName = curveName2;
    }

    public String getCurveName() {
        return this.curveName;
    }
}
