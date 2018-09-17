package java.security.spec;

import java.math.BigInteger;

public class EllipticCurve {
    private final BigInteger a;
    private final BigInteger b;
    private final ECField field;
    private final byte[] seed;

    private static void checkValidity(ECField field, BigInteger c, String cName) {
        if (field instanceof ECFieldFp) {
            if (((ECFieldFp) field).getP().compareTo(c) != 1) {
                throw new IllegalArgumentException(cName + " is too large");
            } else if (c.signum() < 0) {
                throw new IllegalArgumentException(cName + " is negative");
            }
        } else if ((field instanceof ECFieldF2m) && c.bitLength() > ((ECFieldF2m) field).getM()) {
            throw new IllegalArgumentException(cName + " is too large");
        }
    }

    public EllipticCurve(ECField field, BigInteger a, BigInteger b) {
        this(field, a, b, null);
    }

    public EllipticCurve(ECField field, BigInteger a, BigInteger b, byte[] seed) {
        if (field == null) {
            throw new NullPointerException("field is null");
        } else if (a == null) {
            throw new NullPointerException("first coefficient is null");
        } else if (b == null) {
            throw new NullPointerException("second coefficient is null");
        } else {
            checkValidity(field, a, "first coefficient");
            checkValidity(field, b, "second coefficient");
            this.field = field;
            this.a = a;
            this.b = b;
            if (seed != null) {
                this.seed = (byte[]) seed.clone();
            } else {
                this.seed = null;
            }
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
        return this.field.hashCode() << (((this.a.hashCode() << 4) + 6) + (this.b.hashCode() << 2));
    }
}
