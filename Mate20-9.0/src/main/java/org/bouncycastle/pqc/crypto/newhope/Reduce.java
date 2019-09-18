package org.bouncycastle.pqc.crypto.newhope;

class Reduce {
    static final int QInv = 12287;
    static final int RLog = 18;
    static final int RMask = 262143;

    Reduce() {
    }

    static short barrett(short s) {
        short s2 = s & 65535;
        return (short) (s2 - (((s2 * 5) >>> 16) * 12289));
    }

    static short montgomery(int i) {
        return (short) (((((i * QInv) & RMask) * 12289) + i) >>> 18);
    }
}
