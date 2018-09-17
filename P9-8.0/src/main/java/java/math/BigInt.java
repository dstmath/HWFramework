package java.math;

import libcore.util.NativeAllocationRegistry;

final class BigInt {
    private static NativeAllocationRegistry registry = new NativeAllocationRegistry(BigInt.class.getClassLoader(), NativeBN.getNativeFinalizer(), NativeBN.size());
    transient long bignum = 0;

    BigInt() {
    }

    public String toString() {
        return decString();
    }

    long getNativeBIGNUM() {
        return this.bignum;
    }

    private void makeValid() {
        if (this.bignum == 0) {
            this.bignum = NativeBN.BN_new();
            registry.registerNativeAllocation((Object) this, this.bignum);
        }
    }

    private static BigInt newBigInt() {
        Object bi = new BigInt();
        bi.bignum = NativeBN.BN_new();
        registry.registerNativeAllocation(bi, bi.bignum);
        return bi;
    }

    static int cmp(BigInt a, BigInt b) {
        return NativeBN.BN_cmp(a.bignum, b.bignum);
    }

    void putCopy(BigInt from) {
        makeValid();
        NativeBN.BN_copy(this.bignum, from.bignum);
    }

    BigInt copy() {
        BigInt bi = new BigInt();
        bi.putCopy(this);
        return bi;
    }

    void putLongInt(long val) {
        makeValid();
        NativeBN.putLongInt(this.bignum, val);
    }

    void putULongInt(long val, boolean neg) {
        makeValid();
        NativeBN.putULongInt(this.bignum, val, neg);
    }

    private NumberFormatException invalidBigInteger(String s) {
        throw new NumberFormatException("Invalid BigInteger: " + s);
    }

    void putDecString(String original) {
        String s = checkString(original, 10);
        makeValid();
        if (NativeBN.BN_dec2bn(this.bignum, s) < s.length()) {
            throw invalidBigInteger(original);
        }
    }

    void putHexString(String original) {
        String s = checkString(original, 16);
        makeValid();
        if (NativeBN.BN_hex2bn(this.bignum, s) < s.length()) {
            throw invalidBigInteger(original);
        }
    }

    String checkString(String s, int base) {
        if (s == null) {
            throw new NullPointerException("s == null");
        }
        char ch;
        int charCount = s.length();
        int i = 0;
        if (charCount > 0) {
            ch = s.charAt(0);
            if (ch == '+') {
                s = s.substring(1);
                charCount--;
            } else if (ch == '-') {
                i = 1;
            }
        }
        if (charCount - i == 0) {
            throw invalidBigInteger(s);
        }
        boolean nonAscii = false;
        while (i < charCount) {
            ch = s.charAt(i);
            if (Character.digit(ch, base) == -1) {
                throw invalidBigInteger(s);
            }
            if (ch > 128) {
                nonAscii = true;
            }
            i++;
        }
        return nonAscii ? toAscii(s, base) : s;
    }

    private static String toAscii(String s, int base) {
        int length = s.length();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            int value = Character.digit(ch, base);
            if (value >= 0 && value <= 9) {
                ch = (char) (value + 48);
            }
            result.append(ch);
        }
        return result.toString();
    }

    void putBigEndian(byte[] a, boolean neg) {
        makeValid();
        NativeBN.BN_bin2bn(a, a.length, neg, this.bignum);
    }

    void putLittleEndianInts(int[] a, boolean neg) {
        makeValid();
        NativeBN.litEndInts2bn(a, a.length, neg, this.bignum);
    }

    void putBigEndianTwosComplement(byte[] a) {
        makeValid();
        NativeBN.twosComp2bn(a, a.length, this.bignum);
    }

    long longInt() {
        return NativeBN.longInt(this.bignum);
    }

    String decString() {
        return NativeBN.BN_bn2dec(this.bignum);
    }

    String hexString() {
        return NativeBN.BN_bn2hex(this.bignum);
    }

    byte[] bigEndianMagnitude() {
        return NativeBN.BN_bn2bin(this.bignum);
    }

    int[] littleEndianIntsMagnitude() {
        return NativeBN.bn2litEndInts(this.bignum);
    }

    int sign() {
        return NativeBN.sign(this.bignum);
    }

    void setSign(int val) {
        if (val > 0) {
            NativeBN.BN_set_negative(this.bignum, 0);
        } else if (val < 0) {
            NativeBN.BN_set_negative(this.bignum, 1);
        }
    }

    boolean twosCompFitsIntoBytes(int desiredByteCount) {
        return (NativeBN.bitLength(this.bignum) + 7) / 8 <= desiredByteCount;
    }

    int bitLength() {
        return NativeBN.bitLength(this.bignum);
    }

    boolean isBitSet(int n) {
        return NativeBN.BN_is_bit_set(this.bignum, n);
    }

    static BigInt shift(BigInt a, int n) {
        BigInt r = newBigInt();
        NativeBN.BN_shift(r.bignum, a.bignum, n);
        return r;
    }

    void shift(int n) {
        NativeBN.BN_shift(this.bignum, this.bignum, n);
    }

    void addPositiveInt(int w) {
        NativeBN.BN_add_word(this.bignum, w);
    }

    void multiplyByPositiveInt(int w) {
        NativeBN.BN_mul_word(this.bignum, w);
    }

    static int remainderByPositiveInt(BigInt a, int w) {
        return NativeBN.BN_mod_word(a.bignum, w);
    }

    static BigInt addition(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_add(r.bignum, a.bignum, b.bignum);
        return r;
    }

    void add(BigInt a) {
        NativeBN.BN_add(this.bignum, this.bignum, a.bignum);
    }

    static BigInt subtraction(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_sub(r.bignum, a.bignum, b.bignum);
        return r;
    }

    static BigInt gcd(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_gcd(r.bignum, a.bignum, b.bignum);
        return r;
    }

    static BigInt product(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        NativeBN.BN_mul(r.bignum, a.bignum, b.bignum);
        return r;
    }

    static BigInt bigExp(BigInt a, BigInt p) {
        BigInt r = newBigInt();
        NativeBN.BN_exp(r.bignum, a.bignum, p.bignum);
        return r;
    }

    static BigInt exp(BigInt a, int p) {
        BigInt power = new BigInt();
        power.putLongInt((long) p);
        return bigExp(a, power);
    }

    static void division(BigInt dividend, BigInt divisor, BigInt quotient, BigInt remainder) {
        long quot;
        long rem;
        if (quotient != null) {
            quotient.makeValid();
            quot = quotient.bignum;
        } else {
            quot = 0;
        }
        if (remainder != null) {
            remainder.makeValid();
            rem = remainder.bignum;
        } else {
            rem = 0;
        }
        NativeBN.BN_div(quot, rem, dividend.bignum, divisor.bignum);
    }

    static BigInt modulus(BigInt a, BigInt m) {
        BigInt r = newBigInt();
        NativeBN.BN_nnmod(r.bignum, a.bignum, m.bignum);
        return r;
    }

    static BigInt modExp(BigInt a, BigInt p, BigInt m) {
        BigInt r = newBigInt();
        NativeBN.BN_mod_exp(r.bignum, a.bignum, p.bignum, m.bignum);
        return r;
    }

    static BigInt modInverse(BigInt a, BigInt m) {
        BigInt r = newBigInt();
        NativeBN.BN_mod_inverse(r.bignum, a.bignum, m.bignum);
        return r;
    }

    static BigInt generatePrimeDefault(int bitLength) {
        BigInt r = newBigInt();
        NativeBN.BN_generate_prime_ex(r.bignum, bitLength, false, 0, 0);
        return r;
    }

    boolean isPrime(int certainty) {
        return NativeBN.BN_primality_test(this.bignum, certainty, false);
    }
}
