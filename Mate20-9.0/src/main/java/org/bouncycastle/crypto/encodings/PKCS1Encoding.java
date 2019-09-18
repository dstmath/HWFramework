package org.bouncycastle.crypto.encodings;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.util.Arrays;

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

    public PKCS1Encoding(AsymmetricBlockCipher asymmetricBlockCipher) {
        this.engine = asymmetricBlockCipher;
        this.useStrictLength = useStrict();
    }

    public PKCS1Encoding(AsymmetricBlockCipher asymmetricBlockCipher, int i) {
        this.engine = asymmetricBlockCipher;
        this.useStrictLength = useStrict();
        this.pLen = i;
    }

    public PKCS1Encoding(AsymmetricBlockCipher asymmetricBlockCipher, byte[] bArr) {
        this.engine = asymmetricBlockCipher;
        this.useStrictLength = useStrict();
        this.fallback = bArr;
        this.pLen = bArr.length;
    }

    private static int checkPkcs1Encoding(byte[] bArr, int i) {
        byte b = 0 | (bArr[0] ^ 2);
        int i2 = i + 1;
        int length = bArr.length - i2;
        byte b2 = b;
        for (int i3 = 1; i3 < length; i3++) {
            byte b3 = bArr[i3];
            byte b4 = b3 | (b3 >> 1);
            byte b5 = b4 | (b4 >> 2);
            b2 |= ((b5 | (b5 >> 4)) & 1) - 1;
        }
        byte b6 = bArr[bArr.length - i2] | b2;
        byte b7 = b6 | (b6 >> 1);
        byte b8 = b7 | (b7 >> 2);
        return ~(((b8 | (b8 >> 4)) & 1) - 1);
    }

    private byte[] decodeBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        if (this.pLen != -1) {
            return decodeBlockOrRandom(bArr, i, i2);
        }
        byte[] processBlock = this.engine.processBlock(bArr, i, i2);
        boolean z = true;
        boolean z2 = this.useStrictLength & (processBlock.length != this.engine.getOutputBlockSize());
        if (processBlock.length < getOutputBlockSize()) {
            processBlock = this.blockBuffer;
        }
        byte b = processBlock[0];
        boolean z3 = !this.forPrivateKey ? b != 1 : b != 2;
        int findStart = findStart(b, processBlock) + 1;
        if (findStart >= 10) {
            z = false;
        }
        if (z3 || z) {
            Arrays.fill(processBlock, (byte) 0);
            throw new InvalidCipherTextException("block incorrect");
        } else if (!z2) {
            byte[] bArr2 = new byte[(processBlock.length - findStart)];
            System.arraycopy(processBlock, findStart, bArr2, 0, bArr2.length);
            return bArr2;
        } else {
            Arrays.fill(processBlock, (byte) 0);
            throw new InvalidCipherTextException("block incorrect size");
        }
    }

    private byte[] decodeBlockOrRandom(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        byte[] bArr2;
        if (this.forPrivateKey) {
            byte[] processBlock = this.engine.processBlock(bArr, i, i2);
            if (this.fallback == null) {
                bArr2 = new byte[this.pLen];
                this.random.nextBytes(bArr2);
            } else {
                bArr2 = this.fallback;
            }
            if (this.useStrictLength && (processBlock.length != this.engine.getOutputBlockSize())) {
                processBlock = this.blockBuffer;
            }
            int checkPkcs1Encoding = checkPkcs1Encoding(processBlock, this.pLen);
            byte[] bArr3 = new byte[this.pLen];
            for (int i3 = 0; i3 < this.pLen; i3++) {
                bArr3[i3] = (byte) ((processBlock[(processBlock.length - this.pLen) + i3] & (~checkPkcs1Encoding)) | (bArr2[i3] & checkPkcs1Encoding));
            }
            Arrays.fill(processBlock, (byte) 0);
            return bArr3;
        }
        throw new InvalidCipherTextException("sorry, this method is only for decryption, not for signing");
    }

    private byte[] encodeBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        if (i2 <= getInputBlockSize()) {
            byte[] bArr2 = new byte[this.engine.getInputBlockSize()];
            if (this.forPrivateKey) {
                bArr2[0] = 1;
                for (int i3 = 1; i3 != (bArr2.length - i2) - 1; i3++) {
                    bArr2[i3] = -1;
                }
            } else {
                this.random.nextBytes(bArr2);
                bArr2[0] = 2;
                for (int i4 = 1; i4 != (bArr2.length - i2) - 1; i4++) {
                    while (bArr2[i4] == 0) {
                        bArr2[i4] = (byte) this.random.nextInt();
                    }
                }
            }
            bArr2[(bArr2.length - i2) - 1] = 0;
            System.arraycopy(bArr, i, bArr2, bArr2.length - i2, i2);
            return this.engine.processBlock(bArr2, 0, bArr2.length);
        }
        throw new IllegalArgumentException("input data too large");
    }

    private int findStart(byte b, byte[] bArr) throws InvalidCipherTextException {
        int i = -1;
        boolean z = false;
        for (int i2 = 1; i2 != bArr.length; i2++) {
            byte b2 = bArr[i2];
            if ((b2 == 0) && (i < 0)) {
                i = i2;
            }
            z |= (b2 != -1) & (b == 1) & (i < 0);
        }
        if (z) {
            return -1;
        }
        return i;
    }

    private boolean useStrict() {
        String str = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(PKCS1Encoding.STRICT_LENGTH_ENABLED_PROPERTY);
            }
        });
        String str2 = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(PKCS1Encoding.NOT_STRICT_LENGTH_ENABLED_PROPERTY);
            }
        });
        boolean z = true;
        if (str2 != null) {
            return !str2.equals("true");
        }
        if (str != null) {
            if (str.equals("true")) {
                return true;
            }
            z = false;
        }
        return z;
    }

    public int getInputBlockSize() {
        int inputBlockSize = this.engine.getInputBlockSize();
        return this.forEncryption ? inputBlockSize - 10 : inputBlockSize;
    }

    public int getOutputBlockSize() {
        int outputBlockSize = this.engine.getOutputBlockSize();
        return this.forEncryption ? outputBlockSize : outputBlockSize - 10;
    }

    public AsymmetricBlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        AsymmetricKeyParameter asymmetricKeyParameter;
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.random = parametersWithRandom.getRandom();
            asymmetricKeyParameter = (AsymmetricKeyParameter) parametersWithRandom.getParameters();
        } else {
            asymmetricKeyParameter = (AsymmetricKeyParameter) cipherParameters;
            if (!asymmetricKeyParameter.isPrivate() && z) {
                this.random = CryptoServicesRegistrar.getSecureRandom();
            }
        }
        this.engine.init(z, cipherParameters);
        this.forPrivateKey = asymmetricKeyParameter.isPrivate();
        this.forEncryption = z;
        this.blockBuffer = new byte[this.engine.getOutputBlockSize()];
        if (this.pLen > 0 && this.fallback == null && this.random == null) {
            throw new IllegalArgumentException("encoder requires random");
        }
    }

    public byte[] processBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        return this.forEncryption ? encodeBlock(bArr, i, i2) : decodeBlock(bArr, i, i2);
    }
}
