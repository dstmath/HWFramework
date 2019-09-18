package org.bouncycastle.jcajce.provider.asymmetric.util;

public class PrimeCertaintyCalculator {
    private PrimeCertaintyCalculator() {
    }

    public static int getDefaultCertainty(int i) {
        if (i <= 1024) {
            return 80;
        }
        return 96 + (16 * ((i - 1) / 1024));
    }
}
