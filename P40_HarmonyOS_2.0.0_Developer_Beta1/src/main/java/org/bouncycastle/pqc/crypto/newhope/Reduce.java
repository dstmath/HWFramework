package org.bouncycastle.pqc.crypto.newhope;

/* access modifiers changed from: package-private */
public class Reduce {
    static final int QInv = 12287;
    static final int RLog = 18;
    static final int RMask = 262143;

    Reduce() {
    }

    static short barrett(short s) {
        int i = s & 65535;
        return (short) (i - (((i * 5) >>> 16) * 12289));
    }

    static short montgomery(int i) {
        return (short) (((((i * QInv) & RMask) * 12289) + i) >>> 18);
    }
}
