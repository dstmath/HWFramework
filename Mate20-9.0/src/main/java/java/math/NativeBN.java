package java.math;

final class NativeBN {
    public static native void BN_add(long j, long j2, long j3);

    public static native void BN_add_word(long j, int i);

    public static native void BN_bin2bn(byte[] bArr, int i, boolean z, long j);

    public static native byte[] BN_bn2bin(long j);

    public static native String BN_bn2dec(long j);

    public static native String BN_bn2hex(long j);

    public static native int BN_cmp(long j, long j2);

    public static native void BN_copy(long j, long j2);

    public static native int BN_dec2bn(long j, String str);

    public static native void BN_div(long j, long j2, long j3, long j4);

    public static native void BN_exp(long j, long j2, long j3);

    public static native void BN_free(long j);

    public static native void BN_gcd(long j, long j2, long j3);

    public static native void BN_generate_prime_ex(long j, int i, boolean z, long j2, long j3);

    public static native int BN_hex2bn(long j, String str);

    public static native boolean BN_is_bit_set(long j, int i);

    public static native void BN_mod_exp(long j, long j2, long j3, long j4);

    public static native void BN_mod_inverse(long j, long j2, long j3);

    public static native int BN_mod_word(long j, int i);

    public static native void BN_mul(long j, long j2, long j3);

    public static native void BN_mul_word(long j, int i);

    public static native long BN_new();

    public static native void BN_nnmod(long j, long j2, long j3);

    public static native boolean BN_primality_test(long j, int i, boolean z);

    public static native void BN_set_negative(long j, int i);

    public static native void BN_shift(long j, long j2, int i);

    public static native void BN_sub(long j, long j2, long j3);

    public static native int bitLength(long j);

    public static native int[] bn2litEndInts(long j);

    public static native long getNativeFinalizer();

    public static native void litEndInts2bn(int[] iArr, int i, boolean z, long j);

    public static native long longInt(long j);

    public static native void putLongInt(long j, long j2);

    public static native void putULongInt(long j, long j2, boolean z);

    public static native int sign(long j);

    public static native void twosComp2bn(byte[] bArr, int i, long j);

    NativeBN() {
    }

    public static long size() {
        return 36;
    }
}
