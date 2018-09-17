package java.security.spec;

public class ECPublicKeySpec implements KeySpec {
    private ECParameterSpec params;
    private ECPoint w;

    public ECPublicKeySpec(ECPoint w, ECParameterSpec params) {
        if (w == null) {
            throw new NullPointerException("w is null");
        } else if (params == null) {
            throw new NullPointerException("params is null");
        } else if (w == ECPoint.POINT_INFINITY) {
            throw new IllegalArgumentException("w is ECPoint.POINT_INFINITY");
        } else {
            this.w = w;
            this.params = params;
        }
    }

    public ECPoint getW() {
        return this.w;
    }

    public ECParameterSpec getParams() {
        return this.params;
    }
}
