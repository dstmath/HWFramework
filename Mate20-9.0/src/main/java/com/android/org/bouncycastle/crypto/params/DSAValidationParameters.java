package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.util.Arrays;

public class DSAValidationParameters {
    private int counter;
    private byte[] seed;
    private int usageIndex;

    public DSAValidationParameters(byte[] seed2, int counter2) {
        this(seed2, counter2, -1);
    }

    public DSAValidationParameters(byte[] seed2, int counter2, int usageIndex2) {
        this.seed = seed2;
        this.counter = counter2;
        this.usageIndex = usageIndex2;
    }

    public int getCounter() {
        return this.counter;
    }

    public byte[] getSeed() {
        return this.seed;
    }

    public int getUsageIndex() {
        return this.usageIndex;
    }

    public int hashCode() {
        return this.counter ^ Arrays.hashCode(this.seed);
    }

    public boolean equals(Object o) {
        if (!(o instanceof DSAValidationParameters)) {
            return false;
        }
        DSAValidationParameters other = (DSAValidationParameters) o;
        if (other.counter != this.counter) {
            return false;
        }
        return Arrays.areEqual(this.seed, other.seed);
    }
}
