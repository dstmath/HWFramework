package java.security.spec;

public class ECPublicKeySpec implements KeySpec {
    private ECParameterSpec params;
    private ECPoint w;

    public ECPublicKeySpec(ECPoint w2, ECParameterSpec params2) {
        if (w2 == null) {
            throw new NullPointerException("w is null");
        } else if (params2 == null) {
            throw new NullPointerException("params is null");
        } else if (w2 != ECPoint.POINT_INFINITY) {
            this.w = w2;
            this.params = params2;
        } else {
            throw new IllegalArgumentException("w is ECPoint.POINT_INFINITY");
        }
    }

    public ECPoint getW() {
        return this.w;
    }

    public ECParameterSpec getParams() {
        return this.params;
    }
}
