package java.security.spec;

import java.math.BigInteger;

public class ECPrivateKeySpec implements KeySpec {
    private ECParameterSpec params;
    private BigInteger s;

    public ECPrivateKeySpec(BigInteger s2, ECParameterSpec params2) {
        if (s2 == null) {
            throw new NullPointerException("s is null");
        } else if (params2 != null) {
            this.s = s2;
            this.params = params2;
        } else {
            throw new NullPointerException("params is null");
        }
    }

    public BigInteger getS() {
        return this.s;
    }

    public ECParameterSpec getParams() {
        return this.params;
    }
}
