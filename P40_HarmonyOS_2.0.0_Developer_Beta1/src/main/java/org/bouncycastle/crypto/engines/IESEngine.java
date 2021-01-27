package org.bouncycastle.crypto.engines;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.EphemeralKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.KeyParser;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.generators.EphemeralKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.IESParameters;
import org.bouncycastle.crypto.params.IESWithCipherParameters;
import org.bouncycastle.crypto.params.KDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Pack;

public class IESEngine {
    private byte[] IV;
    byte[] V;
    BasicAgreement agree;
    BufferedBlockCipher cipher;
    boolean forEncryption;
    DerivationFunction kdf;
    private EphemeralKeyPairGenerator keyPairGenerator;
    private KeyParser keyParser;
    Mac mac;
    byte[] macBuf;
    IESParameters param;
    CipherParameters privParam;
    CipherParameters pubParam;

    public IESEngine(BasicAgreement basicAgreement, DerivationFunction derivationFunction, Mac mac2) {
        this.agree = basicAgreement;
        this.kdf = derivationFunction;
        this.mac = mac2;
        this.macBuf = new byte[mac2.getMacSize()];
        this.cipher = null;
    }

    public IESEngine(BasicAgreement basicAgreement, DerivationFunction derivationFunction, Mac mac2, BufferedBlockCipher bufferedBlockCipher) {
        this.agree = basicAgreement;
        this.kdf = derivationFunction;
        this.mac = mac2;
        this.macBuf = new byte[mac2.getMacSize()];
        this.cipher = bufferedBlockCipher;
    }

    private byte[] decryptBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        int i3;
        byte[] bArr2;
        byte[] bArr3;
        if (i2 >= this.V.length + this.mac.getMacSize()) {
            if (this.cipher == null) {
                byte[] bArr4 = new byte[((i2 - this.V.length) - this.mac.getMacSize())];
                bArr2 = new byte[(this.param.getMacKeySize() / 8)];
                byte[] bArr5 = new byte[(bArr4.length + bArr2.length)];
                this.kdf.generateBytes(bArr5, 0, bArr5.length);
                if (this.V.length != 0) {
                    System.arraycopy(bArr5, 0, bArr2, 0, bArr2.length);
                    System.arraycopy(bArr5, bArr2.length, bArr4, 0, bArr4.length);
                } else {
                    System.arraycopy(bArr5, 0, bArr4, 0, bArr4.length);
                    System.arraycopy(bArr5, bArr4.length, bArr2, 0, bArr2.length);
                }
                byte[] bArr6 = new byte[bArr4.length];
                for (int i4 = 0; i4 != bArr4.length; i4++) {
                    bArr6[i4] = (byte) (bArr[(this.V.length + i) + i4] ^ bArr4[i4]);
                }
                bArr3 = bArr6;
                i3 = 0;
            } else {
                byte[] bArr7 = new byte[(((IESWithCipherParameters) this.param).getCipherKeySize() / 8)];
                bArr2 = new byte[(this.param.getMacKeySize() / 8)];
                byte[] bArr8 = new byte[(bArr7.length + bArr2.length)];
                this.kdf.generateBytes(bArr8, 0, bArr8.length);
                System.arraycopy(bArr8, 0, bArr7, 0, bArr7.length);
                System.arraycopy(bArr8, bArr7.length, bArr2, 0, bArr2.length);
                CipherParameters keyParameter = new KeyParameter(bArr7);
                byte[] bArr9 = this.IV;
                if (bArr9 != null) {
                    keyParameter = new ParametersWithIV(keyParameter, bArr9);
                }
                this.cipher.init(false, keyParameter);
                bArr3 = new byte[this.cipher.getOutputSize((i2 - this.V.length) - this.mac.getMacSize())];
                BufferedBlockCipher bufferedBlockCipher = this.cipher;
                byte[] bArr10 = this.V;
                i3 = bufferedBlockCipher.processBytes(bArr, bArr10.length + i, (i2 - bArr10.length) - this.mac.getMacSize(), bArr3, 0);
            }
            byte[] encodingV = this.param.getEncodingV();
            byte[] bArr11 = null;
            if (this.V.length != 0) {
                bArr11 = getLengthTag(encodingV);
            }
            int i5 = i + i2;
            byte[] copyOfRange = Arrays.copyOfRange(bArr, i5 - this.mac.getMacSize(), i5);
            byte[] bArr12 = new byte[copyOfRange.length];
            this.mac.init(new KeyParameter(bArr2));
            Mac mac2 = this.mac;
            byte[] bArr13 = this.V;
            mac2.update(bArr, i + bArr13.length, (i2 - bArr13.length) - bArr12.length);
            if (encodingV != null) {
                this.mac.update(encodingV, 0, encodingV.length);
            }
            if (this.V.length != 0) {
                this.mac.update(bArr11, 0, bArr11.length);
            }
            this.mac.doFinal(bArr12, 0);
            if (Arrays.constantTimeAreEqual(copyOfRange, bArr12)) {
                BufferedBlockCipher bufferedBlockCipher2 = this.cipher;
                return bufferedBlockCipher2 == null ? bArr3 : Arrays.copyOfRange(bArr3, 0, i3 + bufferedBlockCipher2.doFinal(bArr3, i3));
            }
            throw new InvalidCipherTextException("invalid MAC");
        }
        throw new InvalidCipherTextException("Length of input must be greater than the MAC and V combined");
    }

    private byte[] encryptBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        byte[] bArr2;
        byte[] bArr3;
        CipherParameters cipherParameters;
        BufferedBlockCipher bufferedBlockCipher;
        if (this.cipher == null) {
            byte[] bArr4 = new byte[i2];
            bArr2 = new byte[(this.param.getMacKeySize() / 8)];
            byte[] bArr5 = new byte[(bArr4.length + bArr2.length)];
            this.kdf.generateBytes(bArr5, 0, bArr5.length);
            if (this.V.length != 0) {
                System.arraycopy(bArr5, 0, bArr2, 0, bArr2.length);
                System.arraycopy(bArr5, bArr2.length, bArr4, 0, bArr4.length);
            } else {
                System.arraycopy(bArr5, 0, bArr4, 0, bArr4.length);
                System.arraycopy(bArr5, i2, bArr2, 0, bArr2.length);
            }
            byte[] bArr6 = new byte[i2];
            for (int i3 = 0; i3 != i2; i3++) {
                bArr6[i3] = (byte) (bArr[i + i3] ^ bArr4[i3]);
            }
            bArr3 = bArr6;
        } else {
            byte[] bArr7 = new byte[(((IESWithCipherParameters) this.param).getCipherKeySize() / 8)];
            bArr2 = new byte[(this.param.getMacKeySize() / 8)];
            byte[] bArr8 = new byte[(bArr7.length + bArr2.length)];
            this.kdf.generateBytes(bArr8, 0, bArr8.length);
            System.arraycopy(bArr8, 0, bArr7, 0, bArr7.length);
            System.arraycopy(bArr8, bArr7.length, bArr2, 0, bArr2.length);
            if (this.IV != null) {
                bufferedBlockCipher = this.cipher;
                cipherParameters = new ParametersWithIV(new KeyParameter(bArr7), this.IV);
            } else {
                bufferedBlockCipher = this.cipher;
                cipherParameters = new KeyParameter(bArr7);
            }
            bufferedBlockCipher.init(true, cipherParameters);
            bArr3 = new byte[this.cipher.getOutputSize(i2)];
            int processBytes = this.cipher.processBytes(bArr, i, i2, bArr3, 0);
            i2 = processBytes + this.cipher.doFinal(bArr3, processBytes);
        }
        byte[] encodingV = this.param.getEncodingV();
        byte[] bArr9 = null;
        if (this.V.length != 0) {
            bArr9 = getLengthTag(encodingV);
        }
        byte[] bArr10 = new byte[this.mac.getMacSize()];
        this.mac.init(new KeyParameter(bArr2));
        this.mac.update(bArr3, 0, bArr3.length);
        if (encodingV != null) {
            this.mac.update(encodingV, 0, encodingV.length);
        }
        if (this.V.length != 0) {
            this.mac.update(bArr9, 0, bArr9.length);
        }
        this.mac.doFinal(bArr10, 0);
        byte[] bArr11 = this.V;
        byte[] bArr12 = new byte[(bArr11.length + i2 + bArr10.length)];
        System.arraycopy(bArr11, 0, bArr12, 0, bArr11.length);
        System.arraycopy(bArr3, 0, bArr12, this.V.length, i2);
        System.arraycopy(bArr10, 0, bArr12, this.V.length + i2, bArr10.length);
        return bArr12;
    }

    private void extractParams(CipherParameters cipherParameters) {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            this.IV = parametersWithIV.getIV();
            cipherParameters = parametersWithIV.getParameters();
        } else {
            this.IV = null;
        }
        this.param = (IESParameters) cipherParameters;
    }

    public BufferedBlockCipher getCipher() {
        return this.cipher;
    }

    /* access modifiers changed from: protected */
    public byte[] getLengthTag(byte[] bArr) {
        byte[] bArr2 = new byte[8];
        if (bArr != null) {
            Pack.longToBigEndian(((long) bArr.length) * 8, bArr2, 0);
        }
        return bArr2;
    }

    public Mac getMac() {
        return this.mac;
    }

    public void init(AsymmetricKeyParameter asymmetricKeyParameter, CipherParameters cipherParameters, KeyParser keyParser2) {
        this.forEncryption = false;
        this.privParam = asymmetricKeyParameter;
        this.keyParser = keyParser2;
        extractParams(cipherParameters);
    }

    public void init(AsymmetricKeyParameter asymmetricKeyParameter, CipherParameters cipherParameters, EphemeralKeyPairGenerator ephemeralKeyPairGenerator) {
        this.forEncryption = true;
        this.pubParam = asymmetricKeyParameter;
        this.keyPairGenerator = ephemeralKeyPairGenerator;
        extractParams(cipherParameters);
    }

    public void init(boolean z, CipherParameters cipherParameters, CipherParameters cipherParameters2, CipherParameters cipherParameters3) {
        this.forEncryption = z;
        this.privParam = cipherParameters;
        this.pubParam = cipherParameters2;
        this.V = new byte[0];
        extractParams(cipherParameters3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00ab A[Catch:{ all -> 0x00b8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00b0 A[Catch:{ all -> 0x00b8 }] */
    public byte[] processBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        byte[] asUnsignedByteArray;
        byte[] bArr2;
        byte[] copyOfRange;
        if (this.forEncryption) {
            EphemeralKeyPairGenerator ephemeralKeyPairGenerator = this.keyPairGenerator;
            if (ephemeralKeyPairGenerator != null) {
                EphemeralKeyPair generate = ephemeralKeyPairGenerator.generate();
                this.privParam = generate.getKeyPair().getPrivate();
                copyOfRange = generate.getEncodedPublicKey();
            }
            this.agree.init(this.privParam);
            asUnsignedByteArray = BigIntegers.asUnsignedByteArray(this.agree.getFieldSize(), this.agree.calculateAgreement(this.pubParam));
            bArr2 = this.V;
            if (bArr2.length != 0) {
                byte[] concatenate = Arrays.concatenate(bArr2, asUnsignedByteArray);
                Arrays.fill(asUnsignedByteArray, (byte) 0);
                asUnsignedByteArray = concatenate;
            }
            this.kdf.init(new KDFParameters(asUnsignedByteArray, this.param.getDerivationV()));
            return this.forEncryption ? encryptBlock(bArr, i, i2) : decryptBlock(bArr, i, i2);
        }
        if (this.keyParser != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr, i, i2);
            try {
                this.pubParam = this.keyParser.readKey(byteArrayInputStream);
                copyOfRange = Arrays.copyOfRange(bArr, i, (i2 - byteArrayInputStream.available()) + i);
            } catch (IOException e) {
                throw new InvalidCipherTextException("unable to recover ephemeral public key: " + e.getMessage(), e);
            } catch (IllegalArgumentException e2) {
                throw new InvalidCipherTextException("unable to recover ephemeral public key: " + e2.getMessage(), e2);
            }
        }
        this.agree.init(this.privParam);
        asUnsignedByteArray = BigIntegers.asUnsignedByteArray(this.agree.getFieldSize(), this.agree.calculateAgreement(this.pubParam));
        bArr2 = this.V;
        if (bArr2.length != 0) {
        }
        this.kdf.init(new KDFParameters(asUnsignedByteArray, this.param.getDerivationV()));
        return this.forEncryption ? encryptBlock(bArr, i, i2) : decryptBlock(bArr, i, i2);
        this.V = copyOfRange;
        this.agree.init(this.privParam);
        asUnsignedByteArray = BigIntegers.asUnsignedByteArray(this.agree.getFieldSize(), this.agree.calculateAgreement(this.pubParam));
        bArr2 = this.V;
        if (bArr2.length != 0) {
        }
        try {
            this.kdf.init(new KDFParameters(asUnsignedByteArray, this.param.getDerivationV()));
            return this.forEncryption ? encryptBlock(bArr, i, i2) : decryptBlock(bArr, i, i2);
        } finally {
            Arrays.fill(asUnsignedByteArray, (byte) 0);
        }
    }
}
