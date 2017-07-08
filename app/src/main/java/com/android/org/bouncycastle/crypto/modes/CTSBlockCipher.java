package com.android.org.bouncycastle.crypto.modes;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.BufferedBlockCipher;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.StreamBlockCipher;

public class CTSBlockCipher extends BufferedBlockCipher {
    private int blockSize;

    public CTSBlockCipher(BlockCipher cipher) {
        if (cipher instanceof StreamBlockCipher) {
            throw new IllegalArgumentException("CTSBlockCipher can only accept ECB, or CBC ciphers");
        }
        this.cipher = cipher;
        this.blockSize = cipher.getBlockSize();
        this.buf = new byte[(this.blockSize * 2)];
        this.bufOff = 0;
    }

    public int getUpdateOutputSize(int len) {
        int total = len + this.bufOff;
        int leftOver = total % this.buf.length;
        if (leftOver == 0) {
            return total - this.buf.length;
        }
        return total - leftOver;
    }

    public int getOutputSize(int len) {
        return this.bufOff + len;
    }

    public int processByte(byte in, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        int resultLen = 0;
        if (this.bufOff == this.buf.length) {
            resultLen = this.cipher.processBlock(this.buf, 0, out, outOff);
            System.arraycopy(this.buf, this.blockSize, this.buf, 0, this.blockSize);
            this.bufOff = this.blockSize;
        }
        byte[] bArr = this.buf;
        int i = this.bufOff;
        this.bufOff = i + 1;
        bArr[i] = in;
        return resultLen;
    }

    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        if (len < 0) {
            throw new IllegalArgumentException("Can't have a negative input length!");
        }
        int blockSize = getBlockSize();
        int length = getUpdateOutputSize(len);
        if (length <= 0 || outOff + length <= out.length) {
            int resultLen = 0;
            int gapLen = this.buf.length - this.bufOff;
            if (len > gapLen) {
                System.arraycopy(in, inOff, this.buf, this.bufOff, gapLen);
                resultLen = this.cipher.processBlock(this.buf, 0, out, outOff) + 0;
                System.arraycopy(this.buf, blockSize, this.buf, 0, blockSize);
                this.bufOff = blockSize;
                len -= gapLen;
                inOff += gapLen;
                while (len > blockSize) {
                    System.arraycopy(in, inOff, this.buf, this.bufOff, blockSize);
                    resultLen += this.cipher.processBlock(this.buf, 0, out, outOff + resultLen);
                    System.arraycopy(this.buf, blockSize, this.buf, 0, blockSize);
                    len -= blockSize;
                    inOff += blockSize;
                }
            }
            System.arraycopy(in, inOff, this.buf, this.bufOff, len);
            this.bufOff += len;
            return resultLen;
        }
        throw new DataLengthException("output buffer too short");
    }

    public int doFinal(byte[] out, int outOff) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        if (this.bufOff + outOff > out.length) {
            throw new DataLengthException("output buffer to small in doFinal");
        }
        int blockSize = this.cipher.getBlockSize();
        int len = this.bufOff - blockSize;
        byte[] block = new byte[blockSize];
        int i;
        if (this.forEncryption) {
            if (this.bufOff < blockSize) {
                throw new DataLengthException("need at least one block of input for CTS");
            }
            this.cipher.processBlock(this.buf, 0, block, 0);
            if (this.bufOff > blockSize) {
                for (i = this.bufOff; i != this.buf.length; i++) {
                    this.buf[i] = block[i - blockSize];
                }
                for (i = blockSize; i != this.bufOff; i++) {
                    byte[] bArr = this.buf;
                    bArr[i] = (byte) (bArr[i] ^ block[i - blockSize]);
                }
                if (this.cipher instanceof CBCBlockCipher) {
                    ((CBCBlockCipher) this.cipher).getUnderlyingCipher().processBlock(this.buf, blockSize, out, outOff);
                } else {
                    this.cipher.processBlock(this.buf, blockSize, out, outOff);
                }
                System.arraycopy(block, 0, out, outOff + blockSize, len);
            } else {
                System.arraycopy(block, 0, out, outOff, blockSize);
            }
        } else if (this.bufOff < blockSize) {
            throw new DataLengthException("need at least one block of input for CTS");
        } else {
            byte[] lastBlock = new byte[blockSize];
            if (this.bufOff > blockSize) {
                if (this.cipher instanceof CBCBlockCipher) {
                    ((CBCBlockCipher) this.cipher).getUnderlyingCipher().processBlock(this.buf, 0, block, 0);
                } else {
                    this.cipher.processBlock(this.buf, 0, block, 0);
                }
                for (i = blockSize; i != this.bufOff; i++) {
                    lastBlock[i - blockSize] = (byte) (block[i - blockSize] ^ this.buf[i]);
                }
                System.arraycopy(this.buf, blockSize, block, 0, len);
                this.cipher.processBlock(block, 0, out, outOff);
                System.arraycopy(lastBlock, 0, out, outOff + blockSize, len);
            } else {
                this.cipher.processBlock(this.buf, 0, block, 0);
                System.arraycopy(block, 0, out, outOff, blockSize);
            }
        }
        int offset = this.bufOff;
        reset();
        return offset;
    }
}
