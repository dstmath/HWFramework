package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class ISO9797Alg3Mac implements Mac {
    private byte[] buf;
    private int bufOff;
    private BlockCipher cipher;
    private KeyParameter lastKey2;
    private KeyParameter lastKey3;
    private byte[] mac;
    private int macSize;
    private BlockCipherPadding padding;

    public ISO9797Alg3Mac(BlockCipher blockCipher) {
        this(blockCipher, blockCipher.getBlockSize() * 8, null);
    }

    public ISO9797Alg3Mac(BlockCipher blockCipher, int i) {
        this(blockCipher, i, null);
    }

    public ISO9797Alg3Mac(BlockCipher blockCipher, int i, BlockCipherPadding blockCipherPadding) {
        if (i % 8 != 0) {
            throw new IllegalArgumentException("MAC size must be multiple of 8");
        } else if (blockCipher instanceof DESEngine) {
            this.cipher = new CBCBlockCipher(blockCipher);
            this.padding = blockCipherPadding;
            this.macSize = i / 8;
            this.mac = new byte[blockCipher.getBlockSize()];
            this.buf = new byte[blockCipher.getBlockSize()];
            this.bufOff = 0;
        } else {
            throw new IllegalArgumentException("cipher must be instance of DESEngine");
        }
    }

    public ISO9797Alg3Mac(BlockCipher blockCipher, BlockCipherPadding blockCipherPadding) {
        this(blockCipher, blockCipher.getBlockSize() * 8, blockCipherPadding);
    }

    public int doFinal(byte[] bArr, int i) {
        int blockSize = this.cipher.getBlockSize();
        if (this.padding == null) {
            while (this.bufOff < blockSize) {
                this.buf[this.bufOff] = 0;
                this.bufOff++;
            }
        } else {
            if (this.bufOff == blockSize) {
                this.cipher.processBlock(this.buf, 0, this.mac, 0);
                this.bufOff = 0;
            }
            this.padding.addPadding(this.buf, this.bufOff);
        }
        this.cipher.processBlock(this.buf, 0, this.mac, 0);
        DESEngine dESEngine = new DESEngine();
        dESEngine.init(false, this.lastKey2);
        dESEngine.processBlock(this.mac, 0, this.mac, 0);
        dESEngine.init(true, this.lastKey3);
        dESEngine.processBlock(this.mac, 0, this.mac, 0);
        System.arraycopy(this.mac, 0, bArr, i, this.macSize);
        reset();
        return this.macSize;
    }

    public String getAlgorithmName() {
        return "ISO9797Alg3";
    }

    public int getMacSize() {
        return this.macSize;
    }

    public void init(CipherParameters cipherParameters) {
        KeyParameter keyParameter;
        reset();
        boolean z = cipherParameters instanceof KeyParameter;
        if (z || (cipherParameters instanceof ParametersWithIV)) {
            byte[] key = (z ? (KeyParameter) cipherParameters : (KeyParameter) ((ParametersWithIV) cipherParameters).getParameters()).getKey();
            if (key.length == 16) {
                keyParameter = new KeyParameter(key, 0, 8);
                this.lastKey2 = new KeyParameter(key, 8, 8);
                this.lastKey3 = keyParameter;
            } else if (key.length == 24) {
                keyParameter = new KeyParameter(key, 0, 8);
                this.lastKey2 = new KeyParameter(key, 8, 8);
                this.lastKey3 = new KeyParameter(key, 16, 8);
            } else {
                throw new IllegalArgumentException("Key must be either 112 or 168 bit long");
            }
            if (cipherParameters instanceof ParametersWithIV) {
                this.cipher.init(true, new ParametersWithIV(keyParameter, ((ParametersWithIV) cipherParameters).getIV()));
            } else {
                this.cipher.init(true, keyParameter);
            }
        } else {
            throw new IllegalArgumentException("params must be an instance of KeyParameter or ParametersWithIV");
        }
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
