package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;

public class CFBBlockCipherMac implements Mac {
    private byte[] buf;
    private int bufOff;
    private MacCFBBlockCipher cipher;
    private byte[] mac;
    private int macSize;
    private BlockCipherPadding padding;

    public CFBBlockCipherMac(BlockCipher blockCipher) {
        this(blockCipher, 8, (blockCipher.getBlockSize() * 8) / 2, null);
    }

    public CFBBlockCipherMac(BlockCipher blockCipher, int i, int i2) {
        this(blockCipher, i, i2, null);
    }

    public CFBBlockCipherMac(BlockCipher blockCipher, int i, int i2, BlockCipherPadding blockCipherPadding) {
        this.padding = null;
        if (i2 % 8 == 0) {
            this.mac = new byte[blockCipher.getBlockSize()];
            this.cipher = new MacCFBBlockCipher(blockCipher, i);
            this.padding = blockCipherPadding;
            this.macSize = i2 / 8;
            this.buf = new byte[this.cipher.getBlockSize()];
            this.bufOff = 0;
            return;
        }
        throw new IllegalArgumentException("MAC size must be multiple of 8");
    }

    public CFBBlockCipherMac(BlockCipher blockCipher, BlockCipherPadding blockCipherPadding) {
        this(blockCipher, 8, (blockCipher.getBlockSize() * 8) / 2, blockCipherPadding);
    }

    public int doFinal(byte[] bArr, int i) {
        int blockSize = this.cipher.getBlockSize();
        if (this.padding == null) {
            while (this.bufOff < blockSize) {
                this.buf[this.bufOff] = 0;
                this.bufOff++;
            }
        } else {
            this.padding.addPadding(this.buf, this.bufOff);
        }
        this.cipher.processBlock(this.buf, 0, this.mac, 0);
        this.cipher.getMacBlock(this.mac);
        System.arraycopy(this.mac, 0, bArr, i, this.macSize);
        reset();
        return this.macSize;
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName();
    }

    public int getMacSize() {
        return this.macSize;
    }

    public void init(CipherParameters cipherParameters) {
        reset();
        this.cipher.init(cipherParameters);
    }

    public void reset() {
        for (int i = 0; i < this.buf.length; i++) {
            this.buf[i] = 0;
        }
        this.bufOff = 0;
        this.cipher.reset();
    }

    public void update(byte b) {
        if (this.bufOff == this.buf.length) {
            this.cipher.processBlock(this.buf, 0, this.mac, 0);
            this.bufOff = 0;
        }
        byte[] bArr = this.buf;
        int i = this.bufOff;
        this.bufOff = i + 1;
        bArr[i] = b;
    }

    public void update(byte[] bArr, int i, int i2) {
        if (i2 >= 0) {
            int blockSize = this.cipher.getBlockSize();
            int i3 = blockSize - this.bufOff;
            if (i2 > i3) {
                System.arraycopy(bArr, i, this.buf, this.bufOff, i3);
                this.cipher.processBlock(this.buf, 0, this.mac, 0);
                this.bufOff = 0;
                i2 -= i3;
                i += i3;
                while (i2 > blockSize) {
                    this.cipher.processBlock(bArr, i, this.mac, 0);
                    i2 -= blockSize;
                    i += blockSize;
                }
            }
            System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
            this.bufOff += i2;
            return;
        }
        throw new IllegalArgumentException("Can't have a negative input length!");
    }
}
