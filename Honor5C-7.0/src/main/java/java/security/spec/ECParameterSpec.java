package java.security.spec;

import java.math.BigInteger;

public class ECParameterSpec implements AlgorithmParameterSpec {
    private final EllipticCurve curve;
    private String curveName;
    private final ECPoint g;
    private final int h;
    private final BigInteger n;

    public ECParameterSpec(EllipticCurve curve, ECPoint g, BigInteger n, int h) {
        if (curve == null) {
            throw new NullPointerException("curve is null");
        } else if (g == null) {
            throw new NullPointerException("g is null");
        } else if (n == null) {
            throw new NullPointerException("n is null");
        } else if (n.signum() != 1) {
            throw new IllegalArgumentException("n is not positive");
        } else if (h <= 0) {
            throw new IllegalArgumentException("h is not positive");
        } else {
            this.curve = curve;
            this.g = g;
            this.n = n;
            this.h = h;
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

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public String getCurveName() {
        return this.curveName;
    }
}
