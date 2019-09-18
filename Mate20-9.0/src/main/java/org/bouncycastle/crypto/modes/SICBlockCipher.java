package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.SkippingStreamCipher;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class SICBlockCipher extends StreamBlockCipher implements SkippingStreamCipher {
    private byte[] IV = new byte[this.blockSize];
    private final int blockSize = this.cipher.getBlockSize();
    private int byteCount = 0;
    private final BlockCipher cipher;
    private byte[] counter = new byte[this.blockSize];
    private byte[] counterOut = new byte[this.blockSize];

    public SICBlockCipher(BlockCipher blockCipher) {
        super(blockCipher);
        this.cipher = blockCipher;
    }

    private void adjustCounter(long j) {
        long j2;
        long j3;
        long j4 = j;
        int i = 5;
        if (j4 >= 0) {
            long j5 = (((long) this.byteCount) + j4) / ((long) this.blockSize);
            if (j5 > 255) {
                j3 = j5;
                while (i >= 1) {
                    long j6 = 1 << (8 * i);
                    while (j3 >= j6) {
                        incrementCounterAt(i);
                        j3 -= j6;
                    }
                    i--;
                }
            } else {
                j3 = j5;
            }
            incrementCounter((int) j3);
            this.byteCount = (int) ((j4 + ((long) this.byteCount)) - (((long) this.blockSize) * j5));
            return;
        }
        long j7 = ((-j4) - ((long) this.byteCount)) / ((long) this.blockSize);
        if (j7 > 255) {
            j2 = j7;
            while (i >= 1) {
                long j8 = 1 << (8 * i);
                while (j2 > j8) {
                    decrementCounterAt(i);
                    j2 -= j8;
                }
                i--;
            }
        } else {
            j2 = j7;
        }
        for (long j9 = 0; j9 != j2; j9++) {
            decrementCounterAt(0);
        }
        int i2 = (int) (((long) this.byteCount) + j4 + (((long) this.blockSize) * j7));
        if (i2 >= 0) {
            this.byteCount = 0;
            return;
        }
        decrementCounterAt(0);
        this.byteCount = this.blockSize + i2;
    }

    private void checkCounter() {
        if (this.IV.length < this.blockSize) {
            int i = 0;
            while (i != this.IV.length) {
                if (this.counter[i] == this.IV[i]) {
                    i++;
                } else {
                    throw new IllegalStateException("Counter in CTR/SIC mode out of range.");
                }
            }
        }
    }

    private void decrementCounterAt(int i) {
        byte b;
        int length = this.counter.length - i;
        do {
            length--;
            if (length >= 0) {
                b = (byte) (this.counter[length] - 1);
                this.counter[length] = b;
            } else {
                return;
            }
        } while (b == -1);
    }

    private void incrementCounter(int i) {
        byte b = this.counter[this.counter.length - 1];
        byte[] bArr = this.counter;
        int length = this.counter.length - 1;
        bArr[length] = (byte) (bArr[length] + i);
        if (b != 0 && this.counter[this.counter.length - 1] < b) {
            incrementCounterAt(1);
        }
    }

    private void incrementCounterAt(int i) {
        byte b;
        int length = this.counter.length - i;
        do {
            length--;
            if (length >= 0) {
                byte[] bArr = this.counter;
                b = (byte) (bArr[length] + 1);
                bArr[length] = b;
            } else {
                return;
            }
        } while (b == 0);
    }

    /* access modifiers changed from: protected */
    public byte calculateByte(byte b) throws DataLengthException, IllegalStateException {
        if (this.byteCount == 0) {
            this.cipher.processBlock(this.counter, 0, this.counterOut, 0);
            byte[] bArr = this.counterOut;
            int i = this.byteCount;
            this.byteCount = i + 1;
            return (byte) (b ^ bArr[i]);
        }
        byte[] bArr2 = this.counterOut;
        int i2 = this.byteCount;
        this.byteCount = i2 + 1;
        byte b2 = (byte) (b ^ bArr2[i2]);
        if (this.byteCount == this.counter.length) {
            this.byteCount = 0;
            incrementCounterAt(0);
            checkCounter();
        }
        return b2;
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/SIC";
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public long getPosition() {
        byte[] bArr = new byte[this.counter.length];
        System.arraycopy(this.counter, 0, bArr, 0, bArr.length);
        int length = bArr.length - 1;
        while (length >= 1) {
            int i = length < this.IV.length ? (bArr[length] & 255) - (this.IV[length] & 255) : bArr[length] & 255;
            if (i < 0) {
                int i2 = length - 1;
                bArr[i2] = (byte) (bArr[i2] - 1);
                i += 256;
            }
            bArr[length] = (byte) i;
            length--;
        }
        return (Pack.bigEndianToLong(bArr, bArr.length - 8) * ((long) this.blockSize)) + ((long) this.byteCount);
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            this.IV = Arrays.clone(parametersWithIV.getIV());
            if (this.blockSize >= this.IV.length) {
                int i = 8;
                if (8 > this.blockSize / 2) {
                    i = this.blockSize / 2;
                }
                if (this.blockSize - this.IV.length <= i) {
                    if (parametersWithIV.getParameters() != null) {
                        this.cipher.init(true, parametersWithIV.getParameters());
                    }
                    reset();
                    return;
                }
                throw new IllegalArgumentException("CTR/SIC mode requires IV of at least: " + (this.blockSize - i) + " bytes.");
            }
            throw new IllegalArgumentException("CTR/SIC mode requires IV no greater than: " + this.blockSize + " bytes.");
        }
        throw new IllegalArgumentException("CTR/SIC mode requires ParametersWithIV");
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, this.blockSize, bArr2, i2);
        return this.blockSize;
    }

    public void reset() {
        Arrays.fill(this.counter, (byte) 0);
        System.arraycopy(this.IV, 0, this.counter, 0, this.IV.length);
        this.cipher.reset();
        this.byteCount = 0;
    }

    public long seekTo(long j) {
        reset();
        return skip(j);
    }

    public long skip(long j) {
        adjustCounter(j);
        checkCounter();
        this.cipher.processBlock(this.counter, 0, this.counterOut, 0);
        return j;
    }
}
