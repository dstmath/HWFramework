package java.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

public class BigInteger extends Number implements Comparable<BigInteger>, Serializable {
    static final BigInteger MINUS_ONE = new BigInteger(-1, 1);
    public static final BigInteger ONE = new BigInteger(1, 1);
    static final BigInteger[] SMALL_VALUES = {ZERO, ONE, new BigInteger(1, 2), new BigInteger(1, 3), new BigInteger(1, 4), new BigInteger(1, 5), new BigInteger(1, 6), new BigInteger(1, 7), new BigInteger(1, 8), new BigInteger(1, 9), TEN};
    public static final BigInteger TEN = new BigInteger(1, 10);
    public static final BigInteger ZERO = new BigInteger(0, 0);
    private static final long serialVersionUID = -8287574255936472291L;
    private transient BigInt bigInt;
    transient int[] digits;
    private transient int firstNonzeroDigit = -2;
    private transient int hashCode = 0;
    private transient boolean javaIsValid = false;
    private byte[] magnitude;
    private transient boolean nativeIsValid = false;
    transient int numberLength;
    transient int sign;
    private int signum;

    BigInteger(BigInt bigInt2) {
        if (bigInt2 == null || !bigInt2.hasNativeBignum()) {
            throw new AssertionError();
        }
        setBigInt(bigInt2);
    }

    BigInteger(int sign2, long value) {
        boolean z = false;
        BigInt bigInt2 = new BigInt();
        bigInt2.putULongInt(value, sign2 < 0 ? true : z);
        setBigInt(bigInt2);
    }

    BigInteger(int sign2, int numberLength2, int[] digits2) {
        setJavaRepresentation(sign2, numberLength2, digits2);
    }

    public BigInteger(int numBits, Random random) {
        if (numBits >= 0) {
            if (numBits == 0) {
                setJavaRepresentation(0, 1, new int[]{0});
            } else {
                int numberLength2 = (numBits + 31) >> 5;
                int[] digits2 = new int[numberLength2];
                for (int i = 0; i < numberLength2; i++) {
                    digits2[i] = random.nextInt();
                }
                int i2 = numberLength2 - 1;
                digits2[i2] = digits2[i2] >>> ((-numBits) & 31);
                setJavaRepresentation(1, numberLength2, digits2);
            }
            this.javaIsValid = true;
            return;
        }
        throw new IllegalArgumentException("numBits < 0: " + numBits);
    }

    public BigInteger(int bitLength, int certainty, Random random) {
        int candidate;
        if (bitLength < 2) {
            throw new ArithmeticException("bitLength < 2: " + bitLength);
        } else if (bitLength < 16) {
            do {
                candidate = (random.nextInt() & ((1 << bitLength) - 1)) | (1 << (bitLength - 1));
                candidate = bitLength > 2 ? candidate | 1 : candidate;
            } while (!isSmallPrime(candidate));
            BigInt prime = new BigInt();
            prime.putULongInt((long) candidate, false);
            setBigInt(prime);
        } else {
            do {
                setBigInt(BigInt.generatePrimeDefault(bitLength));
            } while (bitLength() != bitLength);
        }
    }

    private static boolean isSmallPrime(int x) {
        if (x == 2) {
            return true;
        }
        if (x % 2 == 0) {
            return false;
        }
        int max = (int) Math.sqrt((double) x);
        for (int i = 3; i <= max; i += 2) {
            if (x % i == 0) {
                return false;
            }
        }
        return true;
    }

    public BigInteger(String value) {
        BigInt bigInt2 = new BigInt();
        bigInt2.putDecString(value);
        setBigInt(bigInt2);
    }

    public BigInteger(String value, int radix) {
        if (value == null) {
            throw new NullPointerException("value == null");
        } else if (radix == 10) {
            BigInt bigInt2 = new BigInt();
            bigInt2.putDecString(value);
            setBigInt(bigInt2);
        } else if (radix == 16) {
            BigInt bigInt3 = new BigInt();
            bigInt3.putHexString(value);
            setBigInt(bigInt3);
        } else if (radix < 2 || radix > 36) {
            throw new NumberFormatException("Invalid radix: " + radix);
        } else if (!value.isEmpty()) {
            parseFromString(this, value, radix);
        } else {
            throw new NumberFormatException("value.isEmpty()");
        }
    }

    public BigInteger(int signum2, byte[] magnitude2) {
        boolean z = false;
        if (magnitude2 == null) {
            throw new NullPointerException("magnitude == null");
        } else if (signum2 < -1 || signum2 > 1) {
            throw new NumberFormatException("Invalid signum: " + signum2);
        } else {
            if (signum2 == 0) {
                int length = magnitude2.length;
                int i = 0;
                while (i < length) {
                    if (magnitude2[i] == 0) {
                        i++;
                    } else {
                        throw new NumberFormatException("signum-magnitude mismatch");
                    }
                }
            }
            BigInt bigInt2 = new BigInt();
            bigInt2.putBigEndian(magnitude2, signum2 < 0 ? true : z);
            setBigInt(bigInt2);
        }
    }

    public BigInteger(byte[] value) {
        if (value.length != 0) {
            BigInt bigInt2 = new BigInt();
            bigInt2.putBigEndianTwosComplement(value);
            setBigInt(bigInt2);
            return;
        }
        throw new NumberFormatException("value.length == 0");
    }

    /* access modifiers changed from: package-private */
    public BigInt getBigInt() {
        if (this.nativeIsValid) {
            return this.bigInt;
        }
        synchronized (this) {
            if (this.nativeIsValid) {
                BigInt bigInt2 = this.bigInt;
                return bigInt2;
            }
            BigInt bigInt3 = new BigInt();
            bigInt3.putLittleEndianInts(this.digits, this.sign < 0);
            setBigInt(bigInt3);
            return bigInt3;
        }
    }

    private void setBigInt(BigInt bigInt2) {
        this.bigInt = bigInt2;
        this.nativeIsValid = true;
    }

    private void setJavaRepresentation(int sign2, int numberLength2, int[] digits2) {
        while (numberLength2 > 0) {
            numberLength2--;
            if (digits2[numberLength2] != 0) {
                break;
            }
        }
        int numberLength3 = numberLength2 + 1;
        if (digits2[numberLength2] == 0) {
            sign2 = 0;
        }
        this.sign = sign2;
        this.digits = digits2;
        this.numberLength = numberLength3;
        this.javaIsValid = true;
    }

    /* access modifiers changed from: package-private */
    public void prepareJavaRepresentation() {
        if (!this.javaIsValid) {
            synchronized (this) {
                if (!this.javaIsValid) {
                    int sign2 = this.bigInt.sign();
                    int[] digits2 = sign2 != 0 ? this.bigInt.littleEndianIntsMagnitude() : new int[]{0};
                    setJavaRepresentation(sign2, digits2.length, digits2);
                }
            }
        }
    }

    public static BigInteger valueOf(long value) {
        if (value < 0) {
            if (value != -1) {
                return new BigInteger(-1, -value);
            }
            return MINUS_ONE;
        } else if (value < ((long) SMALL_VALUES.length)) {
            return SMALL_VALUES[(int) value];
        } else {
            return new BigInteger(1, value);
        }
    }

    public byte[] toByteArray() {
        return twosComplement();
    }

    public BigInteger abs() {
        BigInt bigInt2 = getBigInt();
        if (bigInt2.sign() >= 0) {
            return this;
        }
        BigInt a = bigInt2.copy();
        a.setSign(1);
        return new BigInteger(a);
    }

    public BigInteger negate() {
        BigInt bigInt2 = getBigInt();
        int sign2 = bigInt2.sign();
        if (sign2 == 0) {
            return this;
        }
        BigInt a = bigInt2.copy();
        a.setSign(-sign2);
        return new BigInteger(a);
    }

    public BigInteger add(BigInteger value) {
        BigInt lhs = getBigInt();
        BigInt rhs = value.getBigInt();
        if (rhs.sign() == 0) {
            return this;
        }
        if (lhs.sign() == 0) {
            return value;
        }
        return new BigInteger(BigInt.addition(lhs, rhs));
    }

    public BigInteger subtract(BigInteger value) {
        BigInt lhs = getBigInt();
        BigInt rhs = value.getBigInt();
        if (rhs.sign() == 0) {
            return this;
        }
        return new BigInteger(BigInt.subtraction(lhs, rhs));
    }

    public int signum() {
        if (this.javaIsValid) {
            return this.sign;
        }
        return getBigInt().sign();
    }

    public BigInteger shiftRight(int n) {
        return shiftLeft(-n);
    }

    public BigInteger shiftLeft(int n) {
        if (n == 0) {
            return this;
        }
        int sign2 = signum();
        if (sign2 == 0) {
            return this;
        }
        return (sign2 > 0 || n >= 0) ? new BigInteger(BigInt.shift(getBigInt(), n)) : BitLevel.shiftRight(this, -n);
    }

    /* access modifiers changed from: package-private */
    public BigInteger shiftLeftOneBit() {
        return signum() == 0 ? this : BitLevel.shiftLeftOneBit(this);
    }

    public int bitLength() {
        if (this.nativeIsValid || !this.javaIsValid) {
            return getBigInt().bitLength();
        }
        return BitLevel.bitLength(this);
    }

    public boolean testBit(int n) {
        if (n >= 0) {
            int sign2 = signum();
            if (sign2 > 0 && this.nativeIsValid && !this.javaIsValid) {
                return getBigInt().isBitSet(n);
            }
            prepareJavaRepresentation();
            boolean z = true;
            if (n == 0) {
                if ((this.digits[0] & 1) == 0) {
                    z = false;
                }
                return z;
            }
            int intCount = n >> 5;
            if (intCount >= this.numberLength) {
                if (sign2 >= 0) {
                    z = false;
                }
                return z;
            }
            int digit = this.digits[intCount];
            int n2 = 1 << (n & 31);
            if (sign2 < 0) {
                int firstNonZeroDigit = getFirstNonzeroDigit();
                if (intCount < firstNonZeroDigit) {
                    return false;
                }
                if (firstNonZeroDigit == intCount) {
                    digit = -digit;
                } else {
                    digit = ~digit;
                }
            }
            if ((digit & n2) == 0) {
                z = false;
            }
            return z;
        }
        throw new ArithmeticException("n < 0: " + n);
    }

    public BigInteger setBit(int n) {
        prepareJavaRepresentation();
        if (!testBit(n)) {
            return BitLevel.flipBit(this, n);
        }
        return this;
    }

    public BigInteger clearBit(int n) {
        prepareJavaRepresentation();
        if (testBit(n)) {
            return BitLevel.flipBit(this, n);
        }
        return this;
    }

    public BigInteger flipBit(int n) {
        prepareJavaRepresentation();
        if (n >= 0) {
            return BitLevel.flipBit(this, n);
        }
        throw new ArithmeticException("n < 0: " + n);
    }

    public int getLowestSetBit() {
        prepareJavaRepresentation();
        if (this.sign == 0) {
            return -1;
        }
        int i = getFirstNonzeroDigit();
        return (i << 5) + Integer.numberOfTrailingZeros(this.digits[i]);
    }

    public int bitCount() {
        prepareJavaRepresentation();
        return BitLevel.bitCount(this);
    }

    public BigInteger not() {
        prepareJavaRepresentation();
        return Logical.not(this);
    }

    public BigInteger and(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.and(this, value);
    }

    public BigInteger or(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.or(this, value);
    }

    public BigInteger xor(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.xor(this, value);
    }

    public BigInteger andNot(BigInteger value) {
        prepareJavaRepresentation();
        value.prepareJavaRepresentation();
        return Logical.andNot(this, value);
    }

    public int intValue() {
        if (this.nativeIsValid && this.bigInt.twosCompFitsIntoBytes(4)) {
            return (int) this.bigInt.longInt();
        }
        prepareJavaRepresentation();
        return this.sign * this.digits[0];
    }

    public long longValue() {
        long value;
        if (this.nativeIsValid && this.bigInt.twosCompFitsIntoBytes(8)) {
            return this.bigInt.longInt();
        }
        prepareJavaRepresentation();
        if (this.numberLength > 1) {
            value = (((long) this.digits[0]) & 4294967295L) | (((long) this.digits[1]) << 32);
        } else {
            value = ((long) this.digits[0]) & 4294967295L;
        }
        return ((long) this.sign) * value;
    }

    public float floatValue() {
        return (float) doubleValue();
    }

    public double doubleValue() {
        return Conversion.bigInteger2Double(this);
    }

    public int compareTo(BigInteger value) {
        return BigInt.cmp(getBigInt(), value.getBigInt());
    }

    public BigInteger min(BigInteger value) {
        return compareTo(value) == -1 ? this : value;
    }

    public BigInteger max(BigInteger value) {
        return compareTo(value) == 1 ? this : value;
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            prepareJavaRepresentation();
            int hash = 0;
            for (int i = 0; i < this.numberLength; i++) {
                hash = (hash * 33) + this.digits[i];
            }
            this.hashCode = this.sign * hash;
        }
        return this.hashCode;
    }

    public boolean equals(Object x) {
        boolean z = true;
        if (this == x) {
            return true;
        }
        if (!(x instanceof BigInteger)) {
            return false;
        }
        if (compareTo((BigInteger) x) != 0) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return getBigInt().decString();
    }

    public String toString(int radix) {
        if (radix == 10) {
            return getBigInt().decString();
        }
        prepareJavaRepresentation();
        return Conversion.bigInteger2String(this, radix);
    }

    public BigInteger gcd(BigInteger value) {
        return new BigInteger(BigInt.gcd(getBigInt(), value.getBigInt()));
    }

    public BigInteger multiply(BigInteger value) {
        return new BigInteger(BigInt.product(getBigInt(), value.getBigInt()));
    }

    public BigInteger pow(int exp) {
        if (exp >= 0) {
            return new BigInteger(BigInt.exp(getBigInt(), exp));
        }
        throw new ArithmeticException("exp < 0: " + exp);
    }

    public BigInteger[] divideAndRemainder(BigInteger divisor) {
        BigInt divisorBigInt = divisor.getBigInt();
        BigInt quotient = new BigInt();
        BigInt remainder = new BigInt();
        BigInt.division(getBigInt(), divisorBigInt, quotient, remainder);
        return new BigInteger[]{new BigInteger(quotient), new BigInteger(remainder)};
    }

    public BigInteger divide(BigInteger divisor) {
        BigInt quotient = new BigInt();
        BigInt.division(getBigInt(), divisor.getBigInt(), quotient, null);
        return new BigInteger(quotient);
    }

    public BigInteger remainder(BigInteger divisor) {
        BigInt remainder = new BigInt();
        BigInt.division(getBigInt(), divisor.getBigInt(), null, remainder);
        return new BigInteger(remainder);
    }

    public BigInteger modInverse(BigInteger m) {
        if (m.signum() > 0) {
            return new BigInteger(BigInt.modInverse(getBigInt(), m.getBigInt()));
        }
        throw new ArithmeticException("modulus not positive");
    }

    public BigInteger modPow(BigInteger exponent, BigInteger modulus) {
        if (modulus.signum() > 0) {
            int exponentSignum = exponent.signum();
            if (exponentSignum == 0) {
                return ONE.mod(modulus);
            }
            return new BigInteger(BigInt.modExp((exponentSignum < 0 ? modInverse(modulus) : this).getBigInt(), exponent.getBigInt(), modulus.getBigInt()));
        }
        throw new ArithmeticException("modulus.signum() <= 0");
    }

    public BigInteger mod(BigInteger m) {
        if (m.signum() > 0) {
            return new BigInteger(BigInt.modulus(getBigInt(), m.getBigInt()));
        }
        throw new ArithmeticException("m.signum() <= 0");
    }

    public boolean isProbablePrime(int certainty) {
        if (certainty <= 0) {
            return true;
        }
        return getBigInt().isPrime(certainty);
    }

    public BigInteger nextProbablePrime() {
        if (this.sign >= 0) {
            return Primality.nextProbablePrime(this);
        }
        throw new ArithmeticException("sign < 0");
    }

    public static BigInteger probablePrime(int bitLength, Random random) {
        return new BigInteger(bitLength, 100, random);
    }

    private byte[] twosComplement() {
        int highBytes;
        prepareJavaRepresentation();
        if (this.sign == 0) {
            return new byte[]{0};
        }
        int bitLen = bitLength();
        int iThis = getFirstNonzeroDigit();
        int bytesLen = (bitLen >> 3) + 1;
        byte[] bytes = new byte[bytesLen];
        int firstByteNumber = 0;
        int bytesInInteger = 4;
        if (bytesLen - (this.numberLength << 2) == 1) {
            bytes[0] = (byte) (this.sign < 0 ? -1 : 0);
            highBytes = 4;
            firstByteNumber = 0 + 1;
        } else {
            int highBytes2 = bytesLen & 3;
            highBytes = highBytes2 == 0 ? 4 : highBytes2;
        }
        int digitIndex = iThis;
        int bytesLen2 = bytesLen - (iThis << 2);
        if (this.sign < 0) {
            int digit = -this.digits[digitIndex];
            int digitIndex2 = digitIndex + 1;
            if (digitIndex2 == this.numberLength) {
                bytesInInteger = highBytes;
            }
            int i = 0;
            while (i < bytesInInteger) {
                bytesLen2--;
                bytes[bytesLen2] = (byte) digit;
                i++;
                digit >>= 8;
            }
            while (bytesLen2 > firstByteNumber) {
                int digit2 = ~this.digits[digitIndex2];
                digitIndex2++;
                if (digitIndex2 == this.numberLength) {
                    bytesInInteger = highBytes;
                }
                int digit3 = digit2;
                int i2 = 0;
                while (i2 < bytesInInteger) {
                    bytesLen2--;
                    bytes[bytesLen2] = (byte) digit3;
                    i2++;
                    digit3 >>= 8;
                }
            }
        } else {
            while (bytesLen2 > firstByteNumber) {
                int digit4 = this.digits[digitIndex];
                digitIndex++;
                if (digitIndex == this.numberLength) {
                    bytesInInteger = highBytes;
                }
                int bytesLen3 = bytesLen2;
                int digit5 = digit4;
                int i3 = 0;
                while (i3 < bytesInInteger) {
                    bytesLen3--;
                    bytes[bytesLen3] = (byte) digit5;
                    i3++;
                    digit5 >>= 8;
                }
                bytesLen2 = bytesLen3;
            }
        }
        return bytes;
    }

    static int multiplyByInt(int[] res, int[] a, int aSize, int factor) {
        long carry = 0;
        for (int i = 0; i < aSize; i++) {
            long carry2 = carry + ((((long) a[i]) & 4294967295L) * (4294967295L & ((long) factor)));
            res[i] = (int) carry2;
            carry = carry2 >>> 32;
        }
        return (int) carry;
    }

    static int inplaceAdd(int[] a, int aSize, int addend) {
        long carry = ((long) addend) & 4294967295L;
        int i = 0;
        while (carry != 0 && i < aSize) {
            long carry2 = carry + (((long) a[i]) & 4294967295L);
            a[i] = (int) carry2;
            carry = carry2 >> 32;
            i++;
        }
        return (int) carry;
    }

    private static void parseFromString(BigInteger bi, String value, int radix) {
        int startChar;
        String str = value;
        int i = radix;
        int stringLength = value.length();
        int endChar = stringLength;
        int sign2 = 0;
        if (str.charAt(0) == '-') {
            stringLength--;
            startChar = -1;
            sign2 = 1;
        } else {
            startChar = 1;
        }
        int charsPerInt = Conversion.digitFitInInt[i];
        int bigRadixDigitsLength = stringLength / charsPerInt;
        int topChars = stringLength % charsPerInt;
        if (topChars != 0) {
            bigRadixDigitsLength++;
        }
        int[] digits2 = new int[bigRadixDigitsLength];
        int bigRadix = Conversion.bigRadices[i - 2];
        int substrEnd = (topChars == 0 ? charsPerInt : topChars) + sign2;
        int digitIndex = 0;
        int substrStart = sign2;
        while (substrStart < endChar) {
            digits2[digitIndex] = multiplyByInt(digits2, digits2, digitIndex, bigRadix) + inplaceAdd(digits2, digitIndex, Integer.parseInt(str.substring(substrStart, substrEnd), i));
            substrStart = substrEnd;
            substrEnd = substrStart + charsPerInt;
            digitIndex++;
        }
        bi.setJavaRepresentation(startChar, digitIndex, digits2);
    }

    /* access modifiers changed from: package-private */
    public int getFirstNonzeroDigit() {
        int i;
        if (this.firstNonzeroDigit == -2) {
            if (this.sign == 0) {
                i = -1;
            } else {
                i = 0;
                while (this.digits[i] == 0) {
                    i++;
                }
            }
            this.firstNonzeroDigit = i;
        }
        return this.firstNonzeroDigit;
    }

    /* access modifiers changed from: package-private */
    public BigInteger copy() {
        prepareJavaRepresentation();
        int[] copyDigits = new int[this.numberLength];
        System.arraycopy(this.digits, 0, copyDigits, 0, this.numberLength);
        return new BigInteger(this.sign, this.numberLength, copyDigits);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        BigInt bigInt2 = new BigInt();
        bigInt2.putBigEndian(this.magnitude, this.signum < 0);
        setBigInt(bigInt2);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        BigInt bigInt2 = getBigInt();
        this.signum = bigInt2.sign();
        this.magnitude = bigInt2.bigEndianMagnitude();
        out.defaultWriteObject();
    }
}
