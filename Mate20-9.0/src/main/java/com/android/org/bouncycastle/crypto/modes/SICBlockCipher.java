package com.android.org.bouncycastle.crypto.modes;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.SkippingStreamCipher;
import com.android.org.bouncycastle.crypto.StreamBlockCipher;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Pack;

public class SICBlockCipher extends StreamBlockCipher implements SkippingStreamCipher {
    private byte[] IV = new byte[this.blockSize];
    private final int blockSize = this.cipher.getBlockSize();
    private int byteCount = 0;
    private final BlockCipher cipher;
    private byte[] counter = new byte[this.blockSize];
    private byte[] counterOut = new byte[this.blockSize];

    public SICBlockCipher(BlockCipher c) {
        super(c);
        this.cipher = c;
    }

    public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException {
        if (params instanceof ParametersWithIV) {
            ParametersWithIV ivParam = (ParametersWithIV) params;
            this.IV = Arrays.clone(ivParam.getIV());
            if (this.blockSize >= this.IV.length) {
                int i = 8;
                if (8 > this.blockSize / 2) {
                    i = this.blockSize / 2;
                }
                int maxCounterSize = i;
                if (this.blockSize - this.IV.length <= maxCounterSize) {
                    if (ivParam.getParameters() != null) {
                        this.cipher.init(true, ivParam.getParameters());
                    }
                    reset();
                    return;
                }
                throw new IllegalArgumentException("CTR/SIC mode requires IV of at least: " + (this.blockSize - maxCounterSize) + " bytes.");
            }
            throw new IllegalArgumentException("CTR/SIC mode requires IV no greater than: " + this.blockSize + " bytes.");
        }
        throw new IllegalArgumentException("CTR/SIC mode requires ParametersWithIV");
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/SIC";
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        processBytes(in, inOff, this.blockSize, out, outOff);
        return this.blockSize;
    }

    /* access modifiers changed from: protected */
    public byte calculateByte(byte in) throws DataLengthException, IllegalStateException {
        if (this.byteCount == 0) {
            this.cipher.processBlock(this.counter, 0, this.counterOut, 0);
            byte[] bArr = this.counterOut;
            int i = this.byteCount;
            this.byteCount = i + 1;
            return (byte) (bArr[i] ^ in);
        }
        byte[] bArr2 = this.counterOut;
        int i2 = this.byteCount;
        this.byteCount = i2 + 1;
        byte rv = (byte) (bArr2[i2] ^ in);
        if (this.byteCount == this.counter.length) {
            this.byteCount = 0;
            incrementCounterAt(0);
            checkCounter();
        }
        return rv;
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

    private void incrementCounterAt(int pos) {
        byte b;
        int i = this.counter.length - pos;
        do {
            i--;
            if (i >= 0) {
                byte[] bArr = this.counter;
                b = (byte) (bArr[i] + 1);
                bArr[i] = b;
            } else {
                return;
            }
        } while (b == 0);
    }

    private void incrementCounter(int offSet) {
        byte old = this.counter[this.counter.length - 1];
        byte[] bArr = this.counter;
        int length = this.counter.length - 1;
        bArr[length] = (byte) (bArr[length] + offSet);
        if (old != 0 && this.counter[this.counter.length - 1] < old) {
            incrementCounterAt(1);
        }
    }

    private void decrementCounterAt(int pos) {
        byte b;
        int i = this.counter.length - pos;
        do {
            i--;
            if (i >= 0) {
                b = (byte) (this.counter[i] - 1);
                this.counter[i] = b;
            } else {
                return;
            }
        } while (b == -1);
    }

    private void adjustCounter(long n) {
        long j = n;
        int i = 5;
        if (j >= 0) {
            long numBlocks = (((long) this.byteCount) + j) / ((long) this.blockSize);
            long rem = numBlocks;
            if (rem > 255) {
                while (true) {
                    int i2 = i;
                    if (i2 < 1) {
                        break;
                    }
                    long diff = 1 << (8 * i2);
                    while (rem >= diff) {
                        incrementCounterAt(i2);
                        rem -= diff;
                    }
                    i = i2 - 1;
                }
            }
            incrementCounter((int) rem);
            this.byteCount = (int) ((((long) this.byteCount) + j) - (((long) this.blockSize) * numBlocks));
            return;
        }
        long numBlocks2 = ((-j) - ((long) this.byteCount)) / ((long) this.blockSize);
        long rem2 = numBlocks2;
        if (rem2 > 255) {
            while (true) {
                int i3 = i;
                if (i3 < 1) {
                    break;
                }
                long diff2 = 1 << (8 * i3);
                while (rem2 > diff2) {
                    decrementCounterAt(i3);
                    rem2 -= diff2;
                }
                i = i3 - 1;
            }
        }
        long i4 = 0;
        while (true) {
            long i5 = i4;
            if (i5 == rem2) {
                break;
            }
            decrementCounterAt(0);
            i4 = i5 + 1;
        }
        int gap = (int) (((long) this.byteCount) + j + (((long) this.blockSize) * numBlocks2));
        if (gap >= 0) {
            this.byteCount = 0;
            return;
        }
        decrementCounterAt(0);
        this.byteCount = this.blockSize + gap;
    }

    public void reset() {
        Arrays.fill(this.counter, (byte) 0);
        System.arraycopy(this.IV, 0, this.counter, 0, this.IV.length);
        this.cipher.reset();
        this.byteCount = 0;
    }

    public long skip(long numberOfBytes) {
        adjustCounter(numberOfBytes);
        checkCounter();
        this.cipher.processBlock(this.counter, 0, this.counterOut, 0);
        return numberOfBytes;
    }

    public long seekTo(long position) {
        reset();
        return skip(position);
    }

    public long getPosition() {
        int v;
        byte[] res = new byte[this.counter.length];
        System.arraycopy(this.counter, 0, res, 0, res.length);
        for (int i = res.length - 1; i >= 1; i--) {
            if (i < this.IV.length) {
                v = (res[i] & 255) - (this.IV[i] & 255);
            } else {
                v = res[i] & 255;
            }
            if (v < 0) {
                int i2 = i - 1;
                res[i2] = (byte) (res[i2] - 1);
                v += 256;
            }
            res[i] = (byte) v;
        }
        return (Pack.bigEndianToLong(res, res.length - 8) * ((long) this.blockSize)) + ((long) this.byteCount);
    }
}
