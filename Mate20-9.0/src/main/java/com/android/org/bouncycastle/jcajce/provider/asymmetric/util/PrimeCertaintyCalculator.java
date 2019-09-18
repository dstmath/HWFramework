package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

public class PrimeCertaintyCalculator {
    private PrimeCertaintyCalculator() {
    }

    public static int getDefaultCertainty(int keySizeInBits) {
        if (keySizeInBits <= 1024) {
            return 80;
        }
        return 96 + (16 * ((keySizeInBits - 1) / 1024));
    }
}
