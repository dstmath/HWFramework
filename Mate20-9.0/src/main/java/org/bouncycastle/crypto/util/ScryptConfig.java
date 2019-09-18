package org.bouncycastle.crypto.util;

import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;

public class ScryptConfig extends PBKDFConfig {
    private final int blockSize;
    private final int costParameter;
    private final int parallelizationParameter;
    private final int saltLength;

    public static class Builder {
        /* access modifiers changed from: private */
        public final int blockSize;
        /* access modifiers changed from: private */
        public final int costParameter;
        /* access modifiers changed from: private */
        public final int parallelizationParameter;
        /* access modifiers changed from: private */
        public int saltLength = 16;

        public Builder(int i, int i2, int i3) {
            if (i <= 1 || !isPowerOf2(i)) {
                throw new IllegalArgumentException("Cost parameter N must be > 1 and a power of 2");
            }
            this.costParameter = i;
            this.blockSize = i2;
            this.parallelizationParameter = i3;
        }

        private static boolean isPowerOf2(int i) {
            return (i & (i + -1)) == 0;
        }

        public ScryptConfig build() {
            return new ScryptConfig(this);
        }

        public Builder withSaltLength(int i) {
            this.saltLength = i;
            return this;
        }
    }

    private ScryptConfig(Builder builder) {
        super(MiscObjectIdentifiers.id_scrypt);
        this.costParameter = builder.costParameter;
        this.blockSize = builder.blockSize;
        this.parallelizationParameter = builder.parallelizationParameter;
        this.saltLength = builder.saltLength;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getCostParameter() {
        return this.costParameter;
    }

    public int getParallelizationParameter() {
        return this.parallelizationParameter;
    }

    public int getSaltLength() {
        return this.saltLength;
    }
}
