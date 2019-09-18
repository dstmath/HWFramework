package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.Wrapper;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.modes.CBCBlockCipher;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.util.Arrays;
import java.security.SecureRandom;

public class DESedeWrapEngine implements Wrapper {
    private static final byte[] IV2 = {74, -35, -94, 44, 121, -24, 33, 5};
    byte[] digest = new byte[20];
    private CBCBlockCipher engine;
    private boolean forWrapping;
    private byte[] iv;
    private KeyParameter param;
    private ParametersWithIV paramPlusIV;
    Digest sha1 = AndroidDigestFactory.getSHA1();

    public void init(boolean forWrapping2, CipherParameters param2) {
        SecureRandom sr;
        this.forWrapping = forWrapping2;
        this.engine = new CBCBlockCipher(new DESedeEngine());
        if (param2 instanceof ParametersWithRandom) {
            ParametersWithRandom pr = (ParametersWithRandom) param2;
            param2 = pr.getParameters();
            sr = pr.getRandom();
        } else {
            sr = new SecureRandom();
        }
        if (param2 instanceof KeyParameter) {
            this.param = (KeyParameter) param2;
            if (this.forWrapping) {
                this.iv = new byte[8];
                sr.nextBytes(this.iv);
                this.paramPlusIV = new ParametersWithIV(this.param, this.iv);
            }
        } else if (param2 instanceof ParametersWithIV) {
            this.paramPlusIV = (ParametersWithIV) param2;
            this.iv = this.paramPlusIV.getIV();
            this.param = (KeyParameter) this.paramPlusIV.getParameters();
            if (!this.forWrapping) {
                throw new IllegalArgumentException("You should not supply an IV for unwrapping");
            } else if (this.iv == null || this.iv.length != 8) {
                throw new IllegalArgumentException("IV is not 8 octets");
            }
        }
    }

    public String getAlgorithmName() {
        return "DESede";
    }

    public byte[] wrap(byte[] in, int inOff, int inLen) {
        if (this.forWrapping) {
            byte[] keyToBeWrapped = new byte[inLen];
            System.arraycopy(in, inOff, keyToBeWrapped, 0, inLen);
            byte[] CKS = calculateCMSKeyChecksum(keyToBeWrapped);
            byte[] WKCKS = new byte[(keyToBeWrapped.length + CKS.length)];
            System.arraycopy(keyToBeWrapped, 0, WKCKS, 0, keyToBeWrapped.length);
            System.arraycopy(CKS, 0, WKCKS, keyToBeWrapped.length, CKS.length);
            int blockSize = this.engine.getBlockSize();
            if (WKCKS.length % blockSize == 0) {
                this.engine.init(true, this.paramPlusIV);
                byte[] TEMP1 = new byte[WKCKS.length];
                for (int currentBytePos = 0; currentBytePos != WKCKS.length; currentBytePos += blockSize) {
                    this.engine.processBlock(WKCKS, currentBytePos, TEMP1, currentBytePos);
                }
                byte[] TEMP2 = new byte[(this.iv.length + TEMP1.length)];
                System.arraycopy(this.iv, 0, TEMP2, 0, this.iv.length);
                System.arraycopy(TEMP1, 0, TEMP2, this.iv.length, TEMP1.length);
                byte[] TEMP3 = reverse(TEMP2);
                this.engine.init(true, new ParametersWithIV(this.param, IV2));
                for (int currentBytePos2 = 0; currentBytePos2 != TEMP3.length; currentBytePos2 += blockSize) {
                    this.engine.processBlock(TEMP3, currentBytePos2, TEMP3, currentBytePos2);
                }
                return TEMP3;
            }
            throw new IllegalStateException("Not multiple of block length");
        }
        throw new IllegalStateException("Not initialized for wrapping");
    }

    public byte[] unwrap(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.forWrapping) {
            throw new IllegalStateException("Not set for unwrapping");
        } else if (in != null) {
            int blockSize = this.engine.getBlockSize();
            if (inLen % blockSize == 0) {
                this.engine.init(false, new ParametersWithIV(this.param, IV2));
                byte[] TEMP3 = new byte[inLen];
                for (int currentBytePos = 0; currentBytePos != inLen; currentBytePos += blockSize) {
                    this.engine.processBlock(in, inOff + currentBytePos, TEMP3, currentBytePos);
                }
                byte[] TEMP2 = reverse(TEMP3);
                this.iv = new byte[8];
                byte[] TEMP1 = new byte[(TEMP2.length - 8)];
                System.arraycopy(TEMP2, 0, this.iv, 0, 8);
                System.arraycopy(TEMP2, 8, TEMP1, 0, TEMP2.length - 8);
                this.paramPlusIV = new ParametersWithIV(this.param, this.iv);
                this.engine.init(false, this.paramPlusIV);
                byte[] WKCKS = new byte[TEMP1.length];
                for (int currentBytePos2 = 0; currentBytePos2 != WKCKS.length; currentBytePos2 += blockSize) {
                    this.engine.processBlock(TEMP1, currentBytePos2, WKCKS, currentBytePos2);
                }
                byte[] result = new byte[(WKCKS.length - 8)];
                byte[] CKStoBeVerified = new byte[8];
                System.arraycopy(WKCKS, 0, result, 0, WKCKS.length - 8);
                System.arraycopy(WKCKS, WKCKS.length - 8, CKStoBeVerified, 0, 8);
                if (checkCMSKeyChecksum(result, CKStoBeVerified)) {
                    return result;
                }
                throw new InvalidCipherTextException("Checksum inside ciphertext is corrupted");
            }
            throw new InvalidCipherTextException("Ciphertext not multiple of " + blockSize);
        } else {
            throw new InvalidCipherTextException("Null pointer as ciphertext");
        }
    }

    private byte[] calculateCMSKeyChecksum(byte[] key) {
        byte[] result = new byte[8];
        this.sha1.update(key, 0, key.length);
        this.sha1.doFinal(this.digest, 0);
        System.arraycopy(this.digest, 0, result, 0, 8);
        return result;
    }

    private boolean checkCMSKeyChecksum(byte[] key, byte[] checksum) {
        return Arrays.constantTimeAreEqual(calculateCMSKeyChecksum(key), checksum);
    }

    private static byte[] reverse(byte[] bs) {
        byte[] result = new byte[bs.length];
        for (int i = 0; i < bs.length; i++) {
            result[i] = bs[bs.length - (i + 1)];
        }
        return result;
    }
}
