package com.android.org.bouncycastle.crypto.encodings;

import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
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
        byte[] block;
        byte[] data = this.engine.processBlock(in, inOff, inLen);
        if (data.length < this.engine.getOutputBlockSize()) {
            block = new byte[this.engine.getOutputBlockSize()];
            System.arraycopy(data, 0, block, block.length - data.length, data.length);
        } else {
            block = data;
        }
        if (block.length < (this.defHash.length * 2) + 1) {
            throw new InvalidCipherTextException("data too short");
        }
        int i;
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
        if (defHashWrong) {
            throw new InvalidCipherTextException("data hash wrong");
        }
        int start = this.defHash.length * 2;
        while (start != block.length && block[start] == null) {
            start++;
        }
        if (start >= block.length - 1 || block[start] != 1) {
            throw new InvalidCipherTextException("data start wrong " + start);
        }
        start++;
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
