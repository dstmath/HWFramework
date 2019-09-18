package org.bouncycastle.crypto.encodings;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.util.Arrays;

public class OAEPEncoding implements AsymmetricBlockCipher {
    private byte[] defHash;
    private AsymmetricBlockCipher engine;
    private boolean forEncryption;
    private Digest mgf1Hash;
    private SecureRandom random;

    public OAEPEncoding(AsymmetricBlockCipher asymmetricBlockCipher) {
        this(asymmetricBlockCipher, DigestFactory.createSHA1(), null);
    }

    public OAEPEncoding(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest) {
        this(asymmetricBlockCipher, digest, null);
    }

    public OAEPEncoding(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest, Digest digest2, byte[] bArr) {
        this.engine = asymmetricBlockCipher;
        this.mgf1Hash = digest2;
        this.defHash = new byte[digest.getDigestSize()];
        digest.reset();
        if (bArr != null) {
            digest.update(bArr, 0, bArr.length);
        }
        digest.doFinal(this.defHash, 0);
    }

    public OAEPEncoding(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest, byte[] bArr) {
        this(asymmetricBlockCipher, digest, digest, bArr);
    }

    private void ItoOSP(int i, byte[] bArr) {
        bArr[0] = (byte) (i >>> 24);
        bArr[1] = (byte) (i >>> 16);
        bArr[2] = (byte) (i >>> 8);
        bArr[3] = (byte) (i >>> 0);
    }

    private byte[] maskGeneratorFunction1(byte[] bArr, int i, int i2, int i3) {
        byte[] bArr2 = new byte[i3];
        byte[] bArr3 = new byte[this.mgf1Hash.getDigestSize()];
        byte[] bArr4 = new byte[4];
        this.mgf1Hash.reset();
        int i4 = 0;
        while (i4 < i3 / bArr3.length) {
            ItoOSP(i4, bArr4);
            this.mgf1Hash.update(bArr, i, i2);
            this.mgf1Hash.update(bArr4, 0, bArr4.length);
            this.mgf1Hash.doFinal(bArr3, 0);
            System.arraycopy(bArr3, 0, bArr2, bArr3.length * i4, bArr3.length);
            i4++;
        }
        if (bArr3.length * i4 < i3) {
            ItoOSP(i4, bArr4);
            this.mgf1Hash.update(bArr, i, i2);
            this.mgf1Hash.update(bArr4, 0, bArr4.length);
            this.mgf1Hash.doFinal(bArr3, 0);
            System.arraycopy(bArr3, 0, bArr2, bArr3.length * i4, bArr2.length - (i4 * bArr3.length));
        }
        return bArr2;
    }

    public byte[] decodeBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        byte[] processBlock = this.engine.processBlock(bArr, i, i2);
        byte[] bArr2 = new byte[this.engine.getOutputBlockSize()];
        System.arraycopy(processBlock, 0, bArr2, bArr2.length - processBlock.length, processBlock.length);
        boolean z = bArr2.length < (this.defHash.length * 2) + 1;
        byte[] maskGeneratorFunction1 = maskGeneratorFunction1(bArr2, this.defHash.length, bArr2.length - this.defHash.length, this.defHash.length);
        for (int i3 = 0; i3 != this.defHash.length; i3++) {
            bArr2[i3] = (byte) (bArr2[i3] ^ maskGeneratorFunction1[i3]);
        }
        byte[] maskGeneratorFunction12 = maskGeneratorFunction1(bArr2, 0, this.defHash.length, bArr2.length - this.defHash.length);
        for (int length = this.defHash.length; length != bArr2.length; length++) {
            bArr2[length] = (byte) (bArr2[length] ^ maskGeneratorFunction12[length - this.defHash.length]);
        }
        boolean z2 = false;
        for (int i4 = 0; i4 != this.defHash.length; i4++) {
            if (this.defHash[i4] != bArr2[this.defHash.length + i4]) {
                z2 = true;
            }
        }
        int length2 = bArr2.length;
        for (int length3 = 2 * this.defHash.length; length3 != bArr2.length; length3++) {
            if ((bArr2[length3] != 0) && (length2 == bArr2.length)) {
                length2 = length3;
            }
        }
        boolean z3 = length2 > bArr2.length - 1;
        boolean z4 = bArr2[length2] != 1;
        int i5 = length2 + 1;
        if (!(z | z2) && !(z3 | z4)) {
            byte[] bArr3 = new byte[(bArr2.length - i5)];
            System.arraycopy(bArr2, i5, bArr3, 0, bArr3.length);
            return bArr3;
        }
        Arrays.fill(bArr2, (byte) 0);
        throw new InvalidCipherTextException("data wrong");
    }

    public byte[] encodeBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        if (i2 <= getInputBlockSize()) {
            byte[] bArr2 = new byte[(getInputBlockSize() + 1 + (2 * this.defHash.length))];
            System.arraycopy(bArr, i, bArr2, bArr2.length - i2, i2);
            bArr2[(bArr2.length - i2) - 1] = 1;
            System.arraycopy(this.defHash, 0, bArr2, this.defHash.length, this.defHash.length);
            byte[] bArr3 = new byte[this.defHash.length];
            this.random.nextBytes(bArr3);
            byte[] maskGeneratorFunction1 = maskGeneratorFunction1(bArr3, 0, bArr3.length, bArr2.length - this.defHash.length);
            for (int length = this.defHash.length; length != bArr2.length; length++) {
                bArr2[length] = (byte) (bArr2[length] ^ maskGeneratorFunction1[length - this.defHash.length]);
            }
            System.arraycopy(bArr3, 0, bArr2, 0, this.defHash.length);
            byte[] maskGeneratorFunction12 = maskGeneratorFunction1(bArr2, this.defHash.length, bArr2.length - this.defHash.length, this.defHash.length);
            for (int i3 = 0; i3 != this.defHash.length; i3++) {
                bArr2[i3] = (byte) (bArr2[i3] ^ maskGeneratorFunction12[i3]);
            }
            return this.engine.processBlock(bArr2, 0, bArr2.length);
        }
        throw new DataLengthException("input data too long");
    }

    public int getInputBlockSize() {
        int inputBlockSize = this.engine.getInputBlockSize();
        return this.forEncryption ? (inputBlockSize - 1) - (2 * this.defHash.length) : inputBlockSize;
    }

    public int getOutputBlockSize() {
        int outputBlockSize = this.engine.getOutputBlockSize();
        return this.forEncryption ? outputBlockSize : (outputBlockSize - 1) - (2 * this.defHash.length);
    }

    public AsymmetricBlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.random = cipherParameters instanceof ParametersWithRandom ? ((ParametersWithRandom) cipherParameters).getRandom() : CryptoServicesRegistrar.getSecureRandom();
        this.engine.init(z, cipherParameters);
        this.forEncryption = z;
    }

    public byte[] processBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        return this.forEncryption ? encodeBlock(bArr, i, i2) : decodeBlock(bArr, i, i2);
    }
}
