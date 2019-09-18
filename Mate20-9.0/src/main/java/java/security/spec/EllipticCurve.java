package java.security.spec;

import java.math.BigInteger;

public class EllipticCurve {
    private final BigInteger a;
    private final BigInteger b;
    private final ECField field;
    private final byte[] seed;

    private static void checkValidity(ECField field2, BigInteger c, String cName) {
        if (field2 instanceof ECFieldFp) {
            if (((ECFieldFp) field2).getP().compareTo(c) != 1) {
                throw new IllegalArgumentException(cName + " is too large");
            } else if (c.signum() < 0) {
                throw new IllegalArgumentException(cName + " is negative");
            }
        } else if ((field2 instanceof ECFieldF2m) && c.bitLength() > ((ECFieldF2m) field2).getM()) {
            throw new IllegalArgumentException(cName + " is too large");
        }
    }

    public EllipticCurve(ECField field2, BigInteger a2, BigInteger b2) {
        this(field2, a2, b2, null);
    }

    public EllipticCurve(ECField field2, BigInteger a2, BigInteger b2, byte[] seed2) {
        if (field2 == null) {
            throw new NullPointerException("field is null");
        } else if (a2 == null) {
            throw new NullPointerException("first coefficient is null");
        } else if (b2 != null) {
            checkValidity(field2, a2, "first coefficient");
            checkValidity(field2, b2, "second coefficient");
            this.field = field2;
            this.a = a2;
            this.b = b2;
            if (seed2 != null) {
                this.seed = (byte[]) seed2.clone();
            } else {
                this.seed = null;
            }
        } else {
            throw new NullPointerException("second coefficient is null");
        }
    }

    public ECField getField() {
        return this.field;
    }

    public BigInteger getA() {
        return this.a;
    }

    public BigInteger getB() {
        return this.b;
    }

    public byte[] getSeed() {
        if (this.seed == null) {
            return null;
        }
        return (byte[]) this.seed.clone();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EllipticCurve) {
            EllipticCurve curve = (EllipticCurve) obj;
            if (this.field.equals(curve.field) && this.a.equals(curve.a) && this.b.equals(curve.b)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return this.field.hashCode() << ((6 + (this.a.hashCode() << 4)) + (this.b.hashCode() << 2));
    }
}
