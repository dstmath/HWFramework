package com.android.org.bouncycastle.crypto.encodings;

import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.util.Arrays;
import java.security.SecureRandom;

public class OAEPEncoding implements AsymmetricBlockCipher {
    private byte[] defHash;
    private AsymmetricBlockCipher engine;
    private boolean forEncryption;
    private Digest mgf1Hash;
    private SecureRandom random;

    public OAEPEncoding(AsymmetricBlockCipher cipher) {
        this(cipher, AndroidDigestFactory.getSHA1(), null);
    }

    public OAEPEncoding(AsymmetricBlockCipher cipher, Digest hash) {
        this(cipher, hash, null);
    }

    public OAEPEncoding(AsymmetricBlockCipher cipher, Digest hash, byte[] encodingParams) {
        this(cipher, hash, hash, encodingParams);
    }

    public OAEPEncoding(AsymmetricBlockCipher cipher, Digest hash, Digest mgf1Hash, byte[] encodingParams) {
        this.engine = cipher;
        this.mgf1Hash = mgf1Hash;
        this.defHash = new byte[hash.getDigestSize()];
        hash.reset();
        if (encodingParams != null) {
            hash.update(encodingParams, 0, encodingParams.length);
        }
        hash.doFinal(this.defHash, 0);
    }

    public AsymmetricBlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public void init(boolean forEncryption, CipherParameters param) {
        if (param instanceof ParametersWithRandom) {
            this.random = ((ParametersWithRandom) param).getRandom();
        } else {
            this.random = new SecureRandom();
        }
        this.engine.init(forEncryption, param);
        this.forEncryption = forEncryption;
    }

    public int getInputBlockSize() {
        int baseBlockSize = this.engine.getInputBlockSize();
        if (this.forEncryption) {
            return (baseBlockSize - 1) - (this.defHash.length * 2);
        }
        return baseBlockSize;
    }

    public int getOutputBlockSize() {
        int baseBlockSize = this.engine.getOutputBlockSize();
        if (this.forEncryption) {
            return baseBlockSize;
        }
        return (baseBlockSize - 1) - (this.defHash.length * 2);
    }

    public byte[] processBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.forEncryption) {
            return encodeBlock(in, inOff, inLen);
        }
        return decodeBlock(in, inOff, inLen);
    }

    public byte[] encodeBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (inLen > getInputBlockSize()) {
            throw new DataLengthException("input data too long");
        }
        int i;
        byte[] block = new byte[((getInputBlockSize() + 1) + (this.defHash.length * 2))];
        System.arraycopy(in, inOff, block, block.length - inLen, inLen);
        block[(block.length - inLen) - 1] = (byte) 1;
        System.arraycopy(this.defHash, 0, block, this.defHash.length, this.defHash.length);
        byte[] seed = new byte[this.defHash.length];
        this.random.nextBytes(seed);
        byte[] mask = maskGeneratorFunction1(seed, 0, seed.length, block.length - this.defHash.length);
        for (i = this.defHash.length; i != block.length; i++) {
            block[i] = (byte) (block[i] ^ mask[i - this.defHash.length]);
        }
        System.arraycopy(seed, 0, block, 0, this.defHash.length);
        mask = maskGeneratorFunction1(block, this.defHash.length, block.length - this.defHash.length, this.defHash.length);
        for (i = 0; i != this.defHash.length; i++) {
            block[i] = (byte) (block[i] ^ mask[i]);
        }
        return this.engine.processBlock(block, 0, block.length);
    }

    public byte[] decodeBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        int i;
        int i2;
        byte[] data = this.engine.processBlock(in, inOff, inLen);
        byte[] block = new byte[this.engine.getOutputBlockSize()];
        System.arraycopy(data, 0, block, block.length - data.length, data.length);
        boolean shortData = block.length < (this.defHash.length * 2) + 1;
        byte[] mask = maskGeneratorFunction1(block, this.defHash.length, block.length - this.defHash.length, this.defHash.length);
        for (i = 0; i != this.defHash.length; i++) {
            block[i] = (byte) (block[i] ^ mask[i]);
        }
        mask = maskGeneratorFunction1(block, 0, this.defHash.length, block.length - this.defHash.length);
        for (i = this.defHash.length; i != block.length; i++) {
            block[i] = (byte) (block[i] ^ mask[i - this.defHash.length]);
        }
        boolean defHashWrong = false;
        for (i = 0; i != this.defHash.length; i++) {
            if (this.defHash[i] != block[this.defHash.length + i]) {
                defHashWrong = true;
            }
        }
        int start = block.length;
        for (int index = this.defHash.length * 2; index != block.length; index++) {
            if (((block[index] != (byte) 0 ? 1 : 0) & (start == block.length ? 1 : 0)) != 0) {
                start = index;
            }
        }
        int i3 = start > block.length + -1 ? 1 : 0;
        if (block[start] != (byte) 1) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        start++;
        if (((defHashWrong | shortData) | (i3 | i2)) != 0) {
            Arrays.fill(block, (byte) 0);
            throw new InvalidCipherTextException("data wrong");
        }
        byte[] output = new byte[(block.length - start)];
        System.arraycopy(block, start, output, 0, output.length);
        return output;
    }

    private void ItoOSP(int i, byte[] sp) {
        sp[0] = (byte) (i >>> 24);
        sp[1] = (byte) (i >>> 16);
        sp[2] = (byte) (i >>> 8);
        sp[3] = (byte) (i >>> 0);
    }

    private byte[] maskGeneratorFunction1(byte[] Z, int zOff, int zLen, int length) {
        byte[] mask = new byte[length];
        byte[] hashBuf = new byte[this.mgf1Hash.getDigestSize()];
        byte[] C = new byte[4];
        int counter = 0;
        this.mgf1Hash.reset();
        while (counter < length / hashBuf.length) {
            ItoOSP(counter, C);
            this.mgf1Hash.update(Z, zOff, zLen);
            this.mgf1Hash.update(C, 0, C.length);
            this.mgf1Hash.doFinal(hashBuf, 0);
            System.arraycopy(hashBuf, 0, mask, hashBuf.length * counter, hashBuf.length);
            counter++;
        }
        if (hashBuf.length * counter < length) {
            ItoOSP(counter, C);
            this.mgf1Hash.update(Z, zOff, zLen);
            this.mgf1Hash.update(C, 0, C.length);
            this.mgf1Hash.doFinal(hashBuf, 0);
            System.arraycopy(hashBuf, 0, mask, hashBuf.length * counter, mask.length - (hashBuf.length * counter));
        }
        return mask;
    }
}
