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
            if (this.blockSize < this.IV.length) {
                throw new IllegalArgumentException("CTR/SIC mode requires IV no greater than: " + this.blockSize + " bytes.");
            }
            int maxCounterSize = 8 > this.blockSize / 2 ? this.blockSize / 2 : 8;
            if (this.blockSize - this.IV.length > maxCounterSize) {
                throw new IllegalArgumentException("CTR/SIC mode requires IV of at least: " + (this.blockSize - maxCounterSize) + " bytes.");
            }
            if (ivParam.getParameters() != null) {
                this.cipher.init(true, ivParam.getParameters());
            }
            reset();
            return;
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

    protected byte calculateByte(byte in) throws DataLengthException, IllegalStateException {
        byte[] bArr;
        int i;
        if (this.byteCount == 0) {
            this.cipher.processBlock(this.counter, 0, this.counterOut, 0);
            bArr = this.counterOut;
            i = this.byteCount;
            this.byteCount = i + 1;
            return (byte) (bArr[i] ^ in);
        }
        bArr = this.counterOut;
        i = this.byteCount;
        this.byteCount = i + 1;
        byte rv = (byte) (bArr[i] ^ in);
        if (this.byteCount == this.counter.length) {
            this.byteCount = 0;
            incrementCounterAt(0);
            checkCounter();
        }
        return rv;
    }

    private void checkCounter() {
        if (this.IV.length < this.blockSize) {
            for (int i = 0; i != this.IV.length; i++) {
                if (this.counter[i] != this.IV[i]) {
                    throw new IllegalStateException("Counter in CTR/SIC mode out of range.");
                }
            }
        }
    }

    private void incrementCounterAt(int pos) {
        int i = this.counter.length - pos;
        byte b;
        do {
            i--;
            if (i >= 0) {
                byte[] bArr = this.counter;
                b = (byte) (bArr[i] + 1);
                bArr[i] = b;
            } else {
                return;
            }
        } while (b == (byte) 0);
    }

    private void incrementCounter(int offSet) {
        byte old = this.counter[this.counter.length - 1];
        byte[] bArr = this.counter;
        int length = this.counter.length - 1;
        bArr[length] = (byte) (bArr[length] + offSet);
        if (old != (byte) 0 && this.counter[this.counter.length - 1] < old) {
            incrementCounterAt(1);
        }
    }

    private void decrementCounterAt(int pos) {
        int i = this.counter.length - pos;
        byte b;
        do {
            i--;
            if (i >= 0) {
                byte[] bArr = this.counter;
                b = (byte) (bArr[i] - 1);
                bArr[i] = b;
            } else {
                return;
            }
        } while (b == (byte) -1);
    }

    private void adjustCounter(long n) {
        long numBlocks;
        long rem;
        int i;
        long diff;
        if (n >= 0) {
            numBlocks = (((long) this.byteCount) + n) / ((long) this.blockSize);
            rem = numBlocks;
            if (numBlocks > 255) {
                for (i = 5; i >= 1; i--) {
                    diff = 1 << (i * 8);
                    while (rem >= diff) {
                        incrementCounterAt(i);
                        rem -= diff;
                    }
                }
            }
            incrementCounter((int) rem);
            this.byteCount = (int) ((((long) this.byteCount) + n) - (((long) this.blockSize) * numBlocks));
            return;
        }
        numBlocks = ((-n) - ((long) this.byteCount)) / ((long) this.blockSize);
        rem = numBlocks;
        if (numBlocks > 255) {
            for (i = 5; i >= 1; i--) {
                diff = 1 << (i * 8);
                while (rem > diff) {
                    decrementCounterAt(i);
                    rem -= diff;
                }
            }
        }
        for (long i2 = 0; i2 != rem; i2++) {
            decrementCounterAt(0);
        }
        int gap = (int) ((((long) this.byteCount) + n) + (((long) this.blockSize) * numBlocks));
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
        byte[] res = new byte[this.counter.length];
        System.arraycopy(this.counter, 0, res, 0, res.length);
        for (int i = res.length - 1; i >= 1; i--) {
            int v;
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
