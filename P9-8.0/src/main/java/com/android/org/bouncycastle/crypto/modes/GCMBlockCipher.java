package com.android.org.bouncycastle.crypto.modes;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.modes.gcm.GCMExponentiator;
import com.android.org.bouncycastle.crypto.modes.gcm.GCMMultiplier;
import com.android.org.bouncycastle.crypto.modes.gcm.GCMUtil;
import com.android.org.bouncycastle.crypto.modes.gcm.Tables1kGCMExponentiator;
import com.android.org.bouncycastle.crypto.modes.gcm.Tables8kGCMMultiplier;
import com.android.org.bouncycastle.crypto.params.AEADParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Pack;

public class GCMBlockCipher implements AEADBlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final long MAX_INPUT_SIZE = 68719476704L;
    private byte[] H;
    private byte[] J0;
    private byte[] S;
    private byte[] S_at;
    private byte[] S_atPre;
    private byte[] atBlock;
    private int atBlockPos;
    private long atLength;
    private long atLengthPre;
    private int blocksRemaining;
    private byte[] bufBlock;
    private int bufOff;
    private BlockCipher cipher;
    private byte[] counter;
    private GCMExponentiator exp;
    private boolean forEncryption;
    private byte[] initialAssociatedText;
    private byte[] lastKey;
    private byte[] macBlock;
    private int macSize;
    private GCMMultiplier multiplier;
    private byte[] nonce;
    private long totalLength;

    public GCMBlockCipher(BlockCipher c) {
        this(c, null);
    }

    public GCMBlockCipher(BlockCipher c, GCMMultiplier m) {
        if (c.getBlockSize() != 16) {
            throw new IllegalArgumentException("cipher required with a block size of 16.");
        }
        if (m == null) {
            m = new Tables8kGCMMultiplier();
        }
        this.cipher = c;
        this.multiplier = m;
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/GCM";
    }

    public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException {
        byte[] newNonce;
        KeyParameter keyParam;
        this.forEncryption = forEncryption;
        this.macBlock = null;
        if (params instanceof AEADParameters) {
            AEADParameters param = (AEADParameters) params;
            newNonce = param.getNonce();
            this.initialAssociatedText = param.getAssociatedText();
            int macSizeBits = param.getMacSize();
            if (macSizeBits < 32 || macSizeBits > 128 || macSizeBits % 8 != 0) {
                throw new IllegalArgumentException("Invalid value for MAC size: " + macSizeBits);
            }
            this.macSize = macSizeBits / 8;
            keyParam = param.getKey();
        } else if (params instanceof ParametersWithIV) {
            ParametersWithIV param2 = (ParametersWithIV) params;
            newNonce = param2.getIV();
            this.initialAssociatedText = null;
            this.macSize = 16;
            keyParam = (KeyParameter) param2.getParameters();
        } else {
            throw new IllegalArgumentException("invalid parameters passed to GCM");
        }
        this.bufBlock = new byte[(forEncryption ? 16 : this.macSize + 16)];
        if (newNonce == null || newNonce.length < 1) {
            throw new IllegalArgumentException("IV must be at least 1 byte");
        }
        if (forEncryption && this.nonce != null && Arrays.areEqual(this.nonce, newNonce)) {
            if (keyParam == null) {
                throw new IllegalArgumentException("cannot reuse nonce for GCM encryption");
            } else if (this.lastKey != null && Arrays.areEqual(this.lastKey, keyParam.getKey())) {
                throw new IllegalArgumentException("cannot reuse nonce for GCM encryption");
            }
        }
        this.nonce = newNonce;
        if (keyParam != null) {
            this.lastKey = keyParam.getKey();
        }
        if (keyParam != null) {
            this.cipher.init(true, keyParam);
            this.H = new byte[16];
            this.cipher.processBlock(this.H, 0, this.H, 0);
            this.multiplier.init(this.H);
            this.exp = null;
        } else if (this.H == null) {
            throw new IllegalArgumentException("Key must be specified in initial init");
        }
        this.J0 = new byte[16];
        if (this.nonce.length == 12) {
            System.arraycopy(this.nonce, 0, this.J0, 0, this.nonce.length);
            this.J0[15] = (byte) 1;
        } else {
            gHASH(this.J0, this.nonce, this.nonce.length);
            byte[] X = new byte[16];
            Pack.longToBigEndian(((long) this.nonce.length) * 8, X, 8);
            gHASHBlock(this.J0, X);
        }
        this.S = new byte[16];
        this.S_at = new byte[16];
        this.S_atPre = new byte[16];
        this.atBlock = new byte[16];
        this.atBlockPos = 0;
        this.atLength = 0;
        this.atLengthPre = 0;
        this.counter = Arrays.clone(this.J0);
        this.blocksRemaining = -2;
        this.bufOff = 0;
        this.totalLength = 0;
        if (this.initialAssociatedText != null) {
            processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
        }
    }

    public byte[] getMac() {
        if (this.macBlock == null) {
            return new byte[this.macSize];
        }
        return Arrays.clone(this.macBlock);
    }

    public int getOutputSize(int len) {
        int totalData = len + this.bufOff;
        if (this.forEncryption) {
            return this.macSize + totalData;
        }
        return totalData < this.macSize ? 0 : totalData - this.macSize;
    }

    private long getTotalInputSizeAfterNewInput(int newInputLen) {
        return (this.totalLength + ((long) newInputLen)) + ((long) this.bufOff);
    }

    public int getUpdateOutputSize(int len) {
        int totalData = len + this.bufOff;
        if (!this.forEncryption) {
            if (totalData < this.macSize) {
                return 0;
            }
            totalData -= this.macSize;
        }
        return totalData - (totalData % 16);
    }

    public void processAADByte(byte in) {
        if (getTotalInputSizeAfterNewInput(1) > MAX_INPUT_SIZE) {
            throw new DataLengthException("Input exceeded 68719476704 bytes");
        }
        this.atBlock[this.atBlockPos] = in;
        int i = this.atBlockPos + 1;
        this.atBlockPos = i;
        if (i == 16) {
            gHASHBlock(this.S_at, this.atBlock);
            this.atBlockPos = 0;
            this.atLength += 16;
        }
    }

    public void processAADBytes(byte[] in, int inOff, int len) {
        if (getTotalInputSizeAfterNewInput(len) > MAX_INPUT_SIZE) {
            throw new DataLengthException("Input exceeded 68719476704 bytes");
        }
        for (int i = 0; i < len; i++) {
            this.atBlock[this.atBlockPos] = in[inOff + i];
            int i2 = this.atBlockPos + 1;
            this.atBlockPos = i2;
            if (i2 == 16) {
                gHASHBlock(this.S_at, this.atBlock);
                this.atBlockPos = 0;
                this.atLength += 16;
            }
        }
    }

    private void initCipher() {
        if (this.atLength > 0) {
            System.arraycopy(this.S_at, 0, this.S_atPre, 0, 16);
            this.atLengthPre = this.atLength;
        }
        if (this.atBlockPos > 0) {
            gHASHPartial(this.S_atPre, this.atBlock, 0, this.atBlockPos);
            this.atLengthPre += (long) this.atBlockPos;
        }
        if (this.atLengthPre > 0) {
            System.arraycopy(this.S_atPre, 0, this.S, 0, 16);
        }
    }

    public int processByte(byte in, byte[] out, int outOff) throws DataLengthException {
        if (getTotalInputSizeAfterNewInput(1) > MAX_INPUT_SIZE) {
            throw new DataLengthException("Input exceeded 68719476704 bytes");
        }
        this.bufBlock[this.bufOff] = in;
        int i = this.bufOff + 1;
        this.bufOff = i;
        if (i != this.bufBlock.length) {
            return 0;
        }
        outputBlock(out, outOff);
        return 16;
    }

    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) throws DataLengthException {
        if (getTotalInputSizeAfterNewInput(len) > MAX_INPUT_SIZE) {
            throw new DataLengthException("Input exceeded 68719476704 bytes");
        } else if (in.length < inOff + len) {
            throw new DataLengthException("Input buffer too short");
        } else {
            int resultLen = 0;
            for (int i = 0; i < len; i++) {
                this.bufBlock[this.bufOff] = in[inOff + i];
                int i2 = this.bufOff + 1;
                this.bufOff = i2;
                if (i2 == this.bufBlock.length) {
                    outputBlock(out, outOff + resultLen);
                    resultLen += 16;
                }
            }
            return resultLen;
        }
    }

    private void outputBlock(byte[] output, int offset) {
        if (output.length < offset + 16) {
            throw new OutputLengthException("Output buffer too short");
        }
        if (this.totalLength == 0) {
            initCipher();
        }
        gCTRBlock(this.bufBlock, output, offset);
        if (this.forEncryption) {
            this.bufOff = 0;
            return;
        }
        System.arraycopy(this.bufBlock, 16, this.bufBlock, 0, this.macSize);
        this.bufOff = this.macSize;
    }

    public int doFinal(byte[] out, int outOff) throws IllegalStateException, InvalidCipherTextException {
        if (this.totalLength == 0) {
            initCipher();
        }
        int extra = this.bufOff;
        if (this.forEncryption) {
            if (out.length < (outOff + extra) + this.macSize) {
                throw new OutputLengthException("Output buffer too short");
            }
        } else if (extra < this.macSize) {
            throw new InvalidCipherTextException("data too short");
        } else {
            extra -= this.macSize;
            if (out.length < outOff + extra) {
                throw new OutputLengthException("Output buffer too short");
            }
        }
        if (extra > 0) {
            gCTRPartial(this.bufBlock, 0, extra, out, outOff);
        }
        this.atLength += (long) this.atBlockPos;
        if (this.atLength > this.atLengthPre) {
            if (this.atBlockPos > 0) {
                gHASHPartial(this.S_at, this.atBlock, 0, this.atBlockPos);
            }
            if (this.atLengthPre > 0) {
                GCMUtil.xor(this.S_at, this.S_atPre);
            }
            long c = ((this.totalLength * 8) + 127) >>> 7;
            byte[] H_c = new byte[16];
            if (this.exp == null) {
                this.exp = new Tables1kGCMExponentiator();
                this.exp.init(this.H);
            }
            this.exp.exponentiateX(c, H_c);
            GCMUtil.multiply(this.S_at, H_c);
            GCMUtil.xor(this.S, this.S_at);
        }
        byte[] X = new byte[16];
        Pack.longToBigEndian(this.atLength * 8, X, 0);
        Pack.longToBigEndian(this.totalLength * 8, X, 8);
        gHASHBlock(this.S, X);
        byte[] tag = new byte[16];
        this.cipher.processBlock(this.J0, 0, tag, 0);
        GCMUtil.xor(tag, this.S);
        int resultLen = extra;
        this.macBlock = new byte[this.macSize];
        System.arraycopy(tag, 0, this.macBlock, 0, this.macSize);
        if (this.forEncryption) {
            System.arraycopy(this.macBlock, 0, out, this.bufOff + outOff, this.macSize);
            resultLen += this.macSize;
        } else {
            byte[] msgMac = new byte[this.macSize];
            System.arraycopy(this.bufBlock, extra, msgMac, 0, this.macSize);
            if (!Arrays.constantTimeAreEqual(this.macBlock, msgMac)) {
                throw new InvalidCipherTextException("mac check in GCM failed");
            }
        }
        reset(false);
        return resultLen;
    }

    public void reset() {
        reset(true);
    }

    private void reset(boolean clearMac) {
        this.cipher.reset();
        this.S = new byte[16];
        this.S_at = new byte[16];
        this.S_atPre = new byte[16];
        this.atBlock = new byte[16];
        this.atBlockPos = 0;
        this.atLength = 0;
        this.atLengthPre = 0;
        this.counter = Arrays.clone(this.J0);
        this.blocksRemaining = -2;
        this.bufOff = 0;
        this.totalLength = 0;
        if (this.bufBlock != null) {
            Arrays.fill(this.bufBlock, (byte) 0);
        }
        if (clearMac) {
            this.macBlock = null;
        }
        if (this.initialAssociatedText != null) {
            processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
        }
    }

    private void gCTRBlock(byte[] block, byte[] out, int outOff) {
        byte[] tmp = getNextCounterBlock();
        GCMUtil.xor(tmp, block);
        System.arraycopy(tmp, 0, out, outOff, 16);
        byte[] bArr = this.S;
        if (!this.forEncryption) {
            tmp = block;
        }
        gHASHBlock(bArr, tmp);
        this.totalLength += 16;
    }

    private void gCTRPartial(byte[] buf, int off, int len, byte[] out, int outOff) {
        byte[] tmp = getNextCounterBlock();
        GCMUtil.xor(tmp, buf, off, len);
        System.arraycopy(tmp, 0, out, outOff, len);
        byte[] bArr = this.S;
        if (!this.forEncryption) {
            tmp = buf;
        }
        gHASHPartial(bArr, tmp, 0, len);
        this.totalLength += (long) len;
    }

    private void gHASH(byte[] Y, byte[] b, int len) {
        for (int pos = 0; pos < len; pos += 16) {
            gHASHPartial(Y, b, pos, Math.min(len - pos, 16));
        }
    }

    private void gHASHBlock(byte[] Y, byte[] b) {
        GCMUtil.xor(Y, b);
        this.multiplier.multiplyH(Y);
    }

    private void gHASHPartial(byte[] Y, byte[] b, int off, int len) {
        GCMUtil.xor(Y, b, off, len);
        this.multiplier.multiplyH(Y);
    }

    private byte[] getNextCounterBlock() {
        if (this.blocksRemaining == 0) {
            throw new IllegalStateException("Attempt to process too many blocks");
        }
        this.blocksRemaining--;
        int c = (this.counter[15] & 255) + 1;
        this.counter[15] = (byte) c;
        c = (c >>> 8) + (this.counter[14] & 255);
        this.counter[14] = (byte) c;
        c = (c >>> 8) + (this.counter[13] & 255);
        this.counter[13] = (byte) c;
        this.counter[12] = (byte) ((c >>> 8) + (this.counter[12] & 255));
        byte[] tmp = new byte[16];
        this.cipher.processBlock(this.counter, 0, tmp, 0);
        return tmp;
    }
}
