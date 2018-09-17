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

    public ECPoint(BigInteger x, BigInteger y) {
        if (x == null || y == null) {
            throw new NullPointerException("affine coordinate x or y is null");
        }
        this.x = x;
        this.y = y;
    }

    public BigInteger getAffineX() {
        return this.x;
    }

    public BigInteger getAffineY() {
        return this.y;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this == POINT_INFINITY || !(obj instanceof ECPoint)) {
            return false;
        }
        boolean equals;
        if (this.x.equals(((ECPoint) obj).x)) {
            equals = this.y.equals(((ECPoint) obj).y);
        } else {
            equals = false;
        }
        return equals;
    }

    public int hashCode() {
        if (this == POINT_INFINITY) {
            return 0;
        }
        return this.x.hashCode() << (this.y.hashCode() + 5);
    }
}
