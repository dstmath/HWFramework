package com.android.org.bouncycastle.crypto.params;

import java.security.SecureRandom;

public class DSAParameterGenerationParameters {
    public static final int DIGITAL_SIGNATURE_USAGE = 1;
    public static final int KEY_ESTABLISHMENT_USAGE = 2;
    private final int certainty;
    private final int l;
    private final int n;
    private final SecureRandom random;
    private final int usageIndex;

    public DSAParameterGenerationParameters(int L, int N, int certainty2, SecureRandom random2) {
        this(L, N, certainty2, random2, -1);
    }

    public DSAParameterGenerationParameters(int L, int N, int certainty2, SecureRandom random2, int usageIndex2) {
        this.l = L;
        this.n = N;
        this.certainty = certainty2;
        this.usageIndex = usageIndex2;
        this.random = random2;
    }

    public int getL() {
        return this.l;
    }

    public int getN() {
        return this.n;
    }

    public int getCertainty() {
        return this.certainty;
    }

    public SecureRandom getRandom() {
        return this.random;
    }

    public int getUsageIndex() {
        return this.usageIndex;
    }
}
