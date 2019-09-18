package org.bouncycastle.crypto.tls;

public class NamedCurve {
    public static final int arbitrary_explicit_char2_curves = 65282;
    public static final int arbitrary_explicit_prime_curves = 65281;
    public static final int brainpoolP256r1 = 26;
    public static final int brainpoolP384r1 = 27;
    public static final int brainpoolP512r1 = 28;
    public static final int secp160k1 = 15;
    public static final int secp160r1 = 16;
    public static final int secp160r2 = 17;
    public static final int secp192k1 = 18;
    public static final int secp192r1 = 19;
    public static final int secp224k1 = 20;
    public static final int secp224r1 = 21;
    public static final int secp256k1 = 22;
    public static final int secp256r1 = 23;
    public static final int secp384r1 = 24;
    public static final int secp521r1 = 25;
    public static final int sect163k1 = 1;
    public static final int sect163r1 = 2;
    public static final int sect163r2 = 3;
    public static final int sect193r1 = 4;
    public static final int sect193r2 = 5;
    public static final int sect233k1 = 6;
    public static final int sect233r1 = 7;
    public static final int sect239k1 = 8;
    public static final int sect283k1 = 9;
    public static final int sect283r1 = 10;
    public static final int sect409k1 = 11;
    public static final int sect409r1 = 12;
    public static final int sect571k1 = 13;
    public static final int sect571r1 = 14;

    public static boolean isValid(int i) {
        boolean z = true;
        if (i < 1 || i > 28) {
            if (i >= 65281 && i <= 65282) {
                return true;
            }
            z = false;
        }
        return z;
    }

    public static boolean refersToASpecificNamedCurve(int i) {
        switch (i) {
            case 65281:
            case 65282:
                return false;
            default:
                return true;
        }
    }
}
