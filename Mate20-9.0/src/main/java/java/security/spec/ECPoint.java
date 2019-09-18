package java.security.spec;

import java.math.BigInteger;

public class ECPoint {
    public static final ECPoint POINT_INFINITY = new ECPoint();
    private final BigInteger x;
    private final BigInteger y;

    private ECPoint() {
        this.x = null;
        this.y = null;
    }

    public ECPoint(BigInteger x2, BigInteger y2) {
        if (x2 == null || y2 == null) {
            throw new NullPointerException("affine coordinate x or y is null");
        }
        this.x = x2;
        this.y = y2;
    }

    public BigInteger getAffineX() {
        return this.x;
    }

    public BigInteger getAffineY() {
        return this.y;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (this == POINT_INFINITY || !(obj instanceof ECPoint)) {
            return false;
        }
        if (!this.x.equals(((ECPoint) obj).x) || !this.y.equals(((ECPoint) obj).y)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this == POINT_INFINITY) {
            return 0;
        }
        return this.x.hashCode() << (5 + this.y.hashCode());
    }
}
