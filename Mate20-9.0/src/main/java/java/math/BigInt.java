package java.math;

import libcore.util.NativeAllocationRegistry;

final class BigInt {
    private static NativeAllocationRegistry registry;
    private transient long bignum = 0;

    BigInt() {
    }

    static {
        NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(BigInt.class.getClassLoader(), NativeBN.getNativeFinalizer(), NativeBN.size());
        registry = nativeAllocationRegistry;
    }

    public String toString() {
        return decString();
    }

    /* access modifiers changed from: package-private */
    public boolean hasNativeBignum() {
        return this.bignum != 0;
    }

    private void makeValid() {
        if (this.bignum == 0) {
            this.bignum = NativeBN.BN_new();
            registry.registerNativeAllocation((Object) this, this.bignum);
        }
    }

    private static BigInt newBigInt() {
        BigInt bi = new BigInt();
        bi.bignum = NativeBN.BN_new();
        registry.registerNativeAllocation((Object) bi, bi.bignum);
        return bi;
    }

    static int cmp(BigInt a, BigInt b) {
        return NativeBN.BN_cmp(a.bignum, b.bignum);
    }

    /* access modifiers changed from: package-private */
    public void putCopy(BigInt from) {
        makeValid();
        NativeBN.BN_copy(this.bignum, from.bignum);
    }

    /* access modifiers changed from: package-private */
    public BigInt copy() {
        BigInt bi = new BigInt();
        bi.putCopy(this);
        return bi;
    }

    /* access modifiers changed from: package-private */
    public void putLongInt(long val) {
        makeValid();
        NativeBN.putLongInt(this.bignum, val);
    }

    /* access modifiers changed from: package-private */
    public void putULongInt(long val, boolean neg) {
        makeValid();
        NativeBN.putULongInt(this.bignum, val, neg);
    }

    private NumberFormatException invalidBigInteger(String s) {
        throw new NumberFormatException("Invalid BigInteger: " + s);
    }

    /* access modifiers changed from: package-private */
    public void putDecString(String original) {
        String s = checkString(original, 10);
        makeValid();
        if (NativeBN.BN_dec2bn(this.bignum, s) < s.length()) {
            throw invalidBigInteger(original);
        }
    }

    /* access modifiers changed from: package-private */
    public void putHexString(String original) {
        String s = checkString(original, 16);
        makeValid();
        if (NativeBN.BN_hex2bn(this.bignum, s) < s.length()) {
            throw invalidBigInteger(original);
        }
    }

    /* access modifiers changed from: package-private */
    public String checkString(String s, int base) {
        if (s != null) {
            int charCount = s.length();
            int i = 0;
            boolean nonAscii = false;
            if (charCount > 0) {
                char ch = s.charAt(0);
                if (ch == '+') {
                    s = s.substring(1);
                    charCount--;
                } else if (ch == '-') {
                    i = 0 + 1;
                }
            }
            if (charCount - i != 0) {
                while (i < charCount) {
                    char ch2 = s.charAt(i);
                    if (Character.digit(ch2, base) != -1) {
                        if (ch2 > 128) {
                            nonAscii = true;
                        }
                        i++;
                    } else {
                        throw invalidBigInteger(s);
                    }
                }
                return nonAscii ? toAscii(s, base) : s;
            }
            throw invalidBigInteger(s);
        }
        throw new NullPointerException("s == null");
    }

    private static String toAscii(String s, int base) {
        int length = s.length();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            int value = Character.digit(ch, base);
            if (value >= 0 && value <= 9) {
                ch = (char) (48 + value);
            }
            result.append(ch);
        }
        return result.toString();
    }

    /* access modifiers changed from: package-private */
    public void putBigEndian(byte[] a, boolean neg) {
        makeValid();
        NativeBN.BN_bin2bn(a, a.length, neg, this.bignum);
    }

    /* access modifiers changed from: package-private */
    public void putLittleEndianInts(int[] a, boolean neg) {
        makeValid();
        NativeBN.litEndInts2bn(a, a.length, neg, this.bignum);
    }

    /* access modifiers changed from: package-private */
    public void putBigEndianTwosComplement(byte[] a) {
        makeValid();
        NativeBN.twosComp2bn(a, a.length, this.bignum);
    }

    /* access modifiers changed from: package-private */
    public long longInt() {
        return NativeBN.longInt(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public String decString() {
        return NativeBN.BN_bn2dec(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public String hexString() {
        return NativeBN.BN_bn2hex(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public byte[] bigEndianMagnitude() {
        return NativeBN.BN_bn2bin(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public int[] littleEndianIntsMagnitude() {
        return NativeBN.bn2litEndInts(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public int sign() {
        return NativeBN.sign(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public void setSign(int val) {
        if (val > 0) {
            NativeBN.BN_set_negative(this.bignum, 0);
        } else if (val < 0) {
            NativeBN.BN_set_negative(this.bignum, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean twosCompFitsIntoBytes(int desiredByteCount) {
        return (NativeBN.bitLength(this.bignum) + 7) / 8 <= desiredByteCount;
    }

    /* access modifiers changed from: package-private */
    public int bitLength() {
        return NativeBN.bitLength(this.bignum);
    }

    /* access modifiers changed from: package-private */
    public boolean isBitSet(int n) {
        return NativeBN.BN_is_bit_set(this.bignum, n);
    }

    static BigInt shift(BigInt a, int n) {
        BigInt r = newBigInt();
        NativeBN.BN_shift(r.bignum, a.bignum, n);
        return r;
    }

    /* access modifiers changed from: package-private */
    public void shift(int n) {
        NativeBN.BN_shift(this.bignum, this.bignum, n);
    }

    /* access modifiers changed from: package-private */
    public void addPositiveInt(int w) {
        NativeBN.BN_add_word(this.bignum, w);
    }

    /* access modifiers changed from: package-private */
    public void multiplyByPositiveInt(int w) {
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

    /* access modifiers changed from: package-private */
    public void add(BigInt a) {
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
        long rem = 0;
        if (quotient != null) {
            quotient.makeValid();
            quot = quotient.bignum;
        } else {
            quot = 0;
        }
        if (remainder != null) {
            remainder.makeValid();
            rem = remainder.bignum;
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

    /* access modifiers changed from: package-private */
    public boolean isPrime(int certainty) {
        return NativeBN.BN_primality_test(this.bignum, certainty, false);
    }
}
