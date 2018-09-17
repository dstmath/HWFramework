package com.android.org.bouncycastle.crypto.encodings;

import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.util.Arrays;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;

public class PKCS1Encoding implements AsymmetricBlockCipher {
    private static final int HEADER_LENGTH = 10;
    public static final String NOT_STRICT_LENGTH_ENABLED_PROPERTY = "org.bouncycastle.pkcs1.not_strict";
    public static final String STRICT_LENGTH_ENABLED_PROPERTY = "org.bouncycastle.pkcs1.strict";
    private byte[] blockBuffer;
    private AsymmetricBlockCipher engine;
    private byte[] fallback = null;
    private boolean forEncryption;
    private boolean forPrivateKey;
    private int pLen = -1;
    private SecureRandom random;
    private boolean useStrictLength;

    public PKCS1Encoding(AsymmetricBlockCipher cipher) {
        this.engine = cipher;
        this.useStrictLength = useStrict();
    }

    public PKCS1Encoding(AsymmetricBlockCipher cipher, int pLen) {
        this.engine = cipher;
        this.useStrictLength = useStrict();
        this.pLen = pLen;
    }

    public PKCS1Encoding(AsymmetricBlockCipher cipher, byte[] fallback) {
        this.engine = cipher;
        this.useStrictLength = useStrict();
        this.fallback = fallback;
        this.pLen = fallback.length;
    }

    private boolean useStrict() {
        String strict = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty("com.android.org.bouncycastle.pkcs1.strict");
            }
        });
        String notStrict = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty("com.android.org.bouncycastle.pkcs1.not_strict");
            }
        });
        if (notStrict != null) {
            return notStrict.equals("true") ^ 1;
        }
        return strict != null ? strict.equals("true") : true;
    }

    public AsymmetricBlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public void init(boolean forEncryption, CipherParameters param) {
        AsymmetricKeyParameter kParam;
        if (param instanceof ParametersWithRandom) {
            ParametersWithRandom rParam = (ParametersWithRandom) param;
            this.random = rParam.getRandom();
            kParam = (AsymmetricKeyParameter) rParam.getParameters();
        } else {
            kParam = (AsymmetricKeyParameter) param;
            if (!kParam.isPrivate() && forEncryption) {
                this.random = new SecureRandom();
            }
        }
        this.engine.init(forEncryption, param);
        this.forPrivateKey = kParam.isPrivate();
        this.forEncryption = forEncryption;
        this.blockBuffer = new byte[this.engine.getOutputBlockSize()];
        if (this.pLen > 0 && this.fallback == null && this.random == null) {
            throw new IllegalArgumentException("encoder requires random");
        }
    }

    public int getInputBlockSize() {
        int baseBlockSize = this.engine.getInputBlockSize();
        if (this.forEncryption) {
            return baseBlockSize - 10;
        }
        return baseBlockSize;
    }

    public int getOutputBlockSize() {
        int baseBlockSize = this.engine.getOutputBlockSize();
        if (this.forEncryption) {
            return baseBlockSize;
        }
        return baseBlockSize - 10;
    }

    public byte[] processBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.forEncryption) {
            return encodeBlock(in, inOff, inLen);
        }
        return decodeBlock(in, inOff, inLen);
    }

    private byte[] encodeBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (inLen > getInputBlockSize()) {
            throw new IllegalArgumentException("input data too large");
        }
        byte[] block = new byte[this.engine.getInputBlockSize()];
        int i;
        if (this.forPrivateKey) {
            block[0] = (byte) 1;
            for (i = 1; i != (block.length - inLen) - 1; i++) {
                block[i] = (byte) -1;
            }
        } else {
            this.random.nextBytes(block);
            block[0] = (byte) 2;
            for (i = 1; i != (block.length - inLen) - 1; i++) {
                while (block[i] == (byte) 0) {
                    block[i] = (byte) this.random.nextInt();
                }
            }
        }
        block[(block.length - inLen) - 1] = (byte) 0;
        System.arraycopy(in, inOff, block, block.length - inLen, inLen);
        return this.engine.processBlock(block, 0, block.length);
    }

    private static int checkPkcs1Encoding(byte[] encoded, int pLen) {
        int correct = (encoded[0] ^ 2) | 0;
        int plen = encoded.length - (pLen + 1);
        for (int i = 1; i < plen; i++) {
            int tmp = encoded[i];
            tmp |= tmp >> 1;
            tmp |= tmp >> 2;
            correct |= ((tmp | (tmp >> 4)) & 1) - 1;
        }
        correct |= encoded[encoded.length - (pLen + 1)];
        correct |= correct >> 1;
        correct |= correct >> 2;
        return ~(((correct | (correct >> 4)) & 1) - 1);
    }

    private byte[] decodeBlockOrRandom(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.forPrivateKey) {
            byte[] random;
            int i;
            byte[] block = this.engine.processBlock(in, inOff, inLen);
            if (this.fallback == null) {
                random = new byte[this.pLen];
                this.random.nextBytes(random);
            } else {
                random = this.fallback;
            }
            boolean z = this.useStrictLength;
            if (block.length != this.engine.getOutputBlockSize()) {
                i = 1;
            } else {
                i = 0;
            }
            byte[] data = (i & z) != 0 ? this.blockBuffer : block;
            int correct = checkPkcs1Encoding(data, this.pLen);
            byte[] result = new byte[this.pLen];
            for (int i2 = 0; i2 < this.pLen; i2++) {
                result[i2] = (byte) ((data[(data.length - this.pLen) + i2] & (~correct)) | (random[i2] & correct));
            }
            Arrays.fill(data, (byte) 0);
            return result;
        }
        throw new InvalidCipherTextException("sorry, this method is only for decryption, not for signing");
    }

    private byte[] decodeBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.pLen != -1) {
            return decodeBlockOrRandom(in, inOff, inLen);
        }
        byte[] data;
        byte[] block = this.engine.processBlock(in, inOff, inLen);
        boolean incorrectLength = this.useStrictLength & (block.length != this.engine.getOutputBlockSize() ? 1 : 0);
        if (block.length < getOutputBlockSize()) {
            data = this.blockBuffer;
        } else {
            data = block;
        }
        byte type = data[0];
        boolean badType = this.forPrivateKey ? type != (byte) 2 : type != (byte) 1;
        if (!(type == (byte) 1 && this.forPrivateKey) && (type != (byte) 2 || (this.forPrivateKey ^ 1) == 0)) {
            int start = findStart(type, data) + 1;
            if (((start < 10 ? 1 : 0) | badType) != 0) {
                Arrays.fill(data, (byte) 0);
                throw new InvalidCipherTextException("block incorrect");
            } else if (incorrectLength) {
                Arrays.fill(data, (byte) 0);
                throw new InvalidCipherTextException("block incorrect size");
            } else {
                byte[] result = new byte[(data.length - start)];
                System.arraycopy(data, start, result, 0, result.length);
                return result;
            }
        }
        throw new InvalidCipherTextException("invalid block type " + type);
    }

    private int findStart(byte type, byte[] block) throws InvalidCipherTextException {
        int start = -1;
        int padErr = 0;
        for (int i = 1; i != block.length; i++) {
            int i2;
            int i3;
            byte pad = block[i];
            if (pad == (byte) 0) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (((start < 0 ? 1 : 0) & i2) != 0) {
                start = i;
            }
            if (type == (byte) 1) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (start < 0) {
                i3 = 1;
            } else {
                i3 = 0;
            }
            i2 &= i3;
            if (pad != (byte) -1) {
                i3 = 1;
            } else {
                i3 = 0;
            }
            padErr |= i3 & i2;
        }
        if (padErr != 0) {
            return -1;
        }
        return start;
    }
}
