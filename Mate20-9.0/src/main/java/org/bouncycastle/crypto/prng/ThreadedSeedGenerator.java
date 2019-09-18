package org.bouncycastle.crypto.prng;

public class ThreadedSeedGenerator {

    private class SeedGenerator implements Runnable {
        private volatile int counter;
        private volatile boolean stop;

        private SeedGenerator() {
            this.counter = 0;
            this.stop = false;
        }

        public byte[] generateSeed(int i, boolean z) {
            Thread thread = new Thread(this);
            byte[] bArr = new byte[i];
            this.counter = 0;
            this.stop = false;
            thread.start();
            if (!z) {
                i *= 8;
            }
            int i2 = 0;
            for (int i3 = 0; i3 < i; i3++) {
                while (this.counter == i2) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                i2 = this.counter;
                if (z) {
                    bArr[i3] = (byte) (i2 & 255);
                } else {
                    int i4 = i3 / 8;
                    bArr[i4] = (byte) ((bArr[i4] << 1) | (i2 & 1));
                }
            }
            this.stop = true;
            return bArr;
        }

        public void run() {
            while (!this.stop) {
                this.counter++;
            }
        }
    }

    public byte[] generateSeed(int i, boolean z) {
        return new SeedGenerator().generateSeed(i, z);
    }
}
