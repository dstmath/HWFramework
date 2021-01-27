package org.bouncycastle.math.ec;

import java.math.BigInteger;

class SimpleBigDecimal {
    private static final long serialVersionUID = 1;
    private final BigInteger bigInt;
    private final int scale;

    public SimpleBigDecimal(BigInteger bigInteger, int i) {
        if (i >= 0) {
            this.bigInt = bigInteger;
            this.scale = i;
            return;
        }
        throw new IllegalArgumentException("scale may not be negative");
    }

    private void checkScale(SimpleBigDecimal simpleBigDecimal) {
        if (this.scale != simpleBigDecimal.scale) {
            throw new IllegalArgumentException("Only SimpleBigDecimal of same scale allowed in arithmetic operations");
        }
    }

    public static SimpleBigDecimal getInstance(BigInteger bigInteger, int i) {
        return new SimpleBigDecimal(bigInteger.shiftLeft(i), i);
    }

    public SimpleBigDecimal add(BigInteger bigInteger) {
        return new SimpleBigDecimal(this.bigInt.add(bigInteger.shiftLeft(this.scale)), this.scale);
    }

    public SimpleBigDecimal add(SimpleBigDecimal simpleBigDecimal) {
        checkScale(simpleBigDecimal);
        return new SimpleBigDecimal(this.bigInt.add(simpleBigDecimal.bigInt), this.scale);
    }

    public SimpleBigDecimal adjustScale(int i) {
        if (i >= 0) {
            int i2 = this.scale;
            return i == i2 ? this : new SimpleBigDecimal(this.bigInt.shiftLeft(i - i2), i);
        }
        throw new IllegalArgumentException("scale may not be negative");
    }

    public int compareTo(BigInteger bigInteger) {
        return this.bigInt.compareTo(bigInteger.shiftLeft(this.scale));
    }

    public int compareTo(SimpleBigDecimal simpleBigDecimal) {
        checkScale(simpleBigDecimal);
        return this.bigInt.compareTo(simpleBigDecimal.bigInt);
    }

    public SimpleBigDecimal divide(BigInteger bigInteger) {
        return new SimpleBigDecimal(this.bigInt.divide(bigInteger), this.scale);
    }

    public SimpleBigDecimal divide(SimpleBigDecimal simpleBigDecimal) {
        checkScale(simpleBigDecimal);
        return new SimpleBigDecimal(this.bigInt.shiftLeft(this.scale).divide(simpleBigDecimal.bigInt), this.scale);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleBigDecimal)) {
            return false;
        }
        SimpleBigDecimal simpleBigDecimal = (SimpleBigDecimal) obj;
        return this.bigInt.equals(simpleBigDecimal.bigInt) && this.scale == simpleBigDecimal.scale;
    }

    public BigInteger floor() {
        return this.bigInt.shiftRight(this.scale);
    }

    public int getScale() {
        return this.scale;
    }

    public int hashCode() {
        return this.bigInt.hashCode() ^ this.scale;
    }

    public int intValue() {
        return floor().intValue();
    }

    public long longValue() {
        return floor().longValue();
    }

    public SimpleBigDecimal multiply(BigInteger bigInteger) {
        return new SimpleBigDecimal(this.bigInt.multiply(bigInteger), this.scale);
    }

    public SimpleBigDecimal multiply(SimpleBigDecimal simpleBigDecimal) {
        checkScale(simpleBigDecimal);
        BigInteger multiply = this.bigInt.multiply(simpleBigDecimal.bigInt);
        int i = this.scale;
        return new SimpleBigDecimal(multiply, i + i);
    }

    public SimpleBigDecimal negate() {
        return new SimpleBigDecimal(this.bigInt.negate(), this.scale);
    }

    public BigInteger round() {
        return add(new SimpleBigDecimal(ECConstants.ONE, 1).adjustScale(this.scale)).floor();
    }

    public SimpleBigDecimal shiftLeft(int i) {
        return new SimpleBigDecimal(this.bigInt.shiftLeft(i), this.scale);
    }

    public SimpleBigDecimal subtract(BigInteger bigInteger) {
        return new SimpleBigDecimal(this.bigInt.subtract(bigInteger.shiftLeft(this.scale)), this.scale);
    }

    public SimpleBigDecimal subtract(SimpleBigDecimal simpleBigDecimal) {
        return add(simpleBigDecimal.negate());
    }

    public String toString() {
        if (this.scale == 0) {
            return this.bigInt.toString();
        }
        BigInteger floor = floor();
        BigInteger subtract = this.bigInt.subtract(floor.shiftLeft(this.scale));
        if (this.bigInt.signum() == -1) {
            subtract = ECConstants.ONE.shiftLeft(this.scale).subtract(subtract);
        }
        if (floor.signum() == -1 && !subtract.equals(ECConstants.ZERO)) {
            floor = floor.add(ECConstants.ONE);
        }
        String bigInteger = floor.toString();
        char[] cArr = new char[this.scale];
        String bigInteger2 = subtract.toString(2);
        int length = bigInteger2.length();
        int i = this.scale - length;
        for (int i2 = 0; i2 < i; i2++) {
            cArr[i2] = '0';
        }
        for (int i3 = 0; i3 < length; i3++) {
            cArr[i + i3] = bigInteger2.charAt(i3);
        }
        String str = new String(cArr);
        StringBuffer stringBuffer = new StringBuffer(bigInteger);
        stringBuffer.append(".");
        stringBuffer.append(str);
        return stringBuffer.toString();
    }
}
