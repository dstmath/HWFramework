package org.bouncycastle.crypto.prng;

public class ReversedWindowGenerator implements RandomGenerator {
    private final RandomGenerator generator;
    private byte[] window;
    private int windowCount;

    public ReversedWindowGenerator(RandomGenerator randomGenerator, int i) {
        if (randomGenerator == null) {
            throw new IllegalArgumentException("generator cannot be null");
        } else if (i >= 2) {
            this.generator = randomGenerator;
            this.window = new byte[i];
        } else {
            throw new IllegalArgumentException("windowSize must be at least 2");
        }
    }

    private void doNextBytes(byte[] bArr, int i, int i2) {
        synchronized (this) {
            int i3 = 0;
            while (i3 < i2) {
                try {
                    if (this.windowCount < 1) {
                        this.generator.nextBytes(this.window, 0, this.window.length);
                        this.windowCount = this.window.length;
                    }
                    byte[] bArr2 = this.window;
                    int i4 = this.windowCount - 1;
                    this.windowCount = i4;
                    bArr[i3 + i] = bArr2[i4];
                    i3++;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public void addSeedMaterial(long j) {
        synchronized (this) {
            this.windowCount = 0;
            this.generator.addSeedMaterial(j);
        }
    }

    public void addSeedMaterial(byte[] bArr) {
        synchronized (this) {
            this.windowCount = 0;
            this.generator.addSeedMaterial(bArr);
        }
    }

    public void nextBytes(byte[] bArr) {
        doNextBytes(bArr, 0, bArr.length);
    }

    public void nextBytes(byte[] bArr, int i, int i2) {
        doNextBytes(bArr, i, i2);
    }
}
