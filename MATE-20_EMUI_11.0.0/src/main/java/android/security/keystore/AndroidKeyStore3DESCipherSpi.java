package android.security.keystore;

import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import javax.crypto.spec.IvParameterSpec;

public class AndroidKeyStore3DESCipherSpi extends AndroidKeyStoreCipherSpiBase {
    private static final int BLOCK_SIZE_BYTES = 8;
    private byte[] mIv;
    private boolean mIvHasBeenUsed;
    private final boolean mIvRequired;
    private final int mKeymasterBlockMode;
    private final int mKeymasterPadding;

    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
    public /* bridge */ /* synthetic */ void finalize() throws Throwable {
        super.finalize();
    }

    AndroidKeyStore3DESCipherSpi(int keymasterBlockMode, int keymasterPadding, boolean ivRequired) {
        this.mKeymasterBlockMode = keymasterBlockMode;
        this.mKeymasterPadding = keymasterPadding;
        this.mIvRequired = ivRequired;
    }

    static abstract class ECB extends AndroidKeyStore3DESCipherSpi {
        protected ECB(int keymasterPadding) {
            super(1, keymasterPadding, false);
        }

        public static class NoPadding extends ECB {
            @Override // android.security.keystore.AndroidKeyStore3DESCipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
            public /* bridge */ /* synthetic */ void finalize() throws Throwable {
                super.finalize();
            }

            public NoPadding() {
                super(1);
            }
        }

        public static class PKCS7Padding extends ECB {
            @Override // android.security.keystore.AndroidKeyStore3DESCipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
            public /* bridge */ /* synthetic */ void finalize() throws Throwable {
                super.finalize();
            }

            public PKCS7Padding() {
                super(64);
            }
        }
    }

    static abstract class CBC extends AndroidKeyStore3DESCipherSpi {
        protected CBC(int keymasterPadding) {
            super(2, keymasterPadding, true);
        }

        public static class NoPadding extends CBC {
            @Override // android.security.keystore.AndroidKeyStore3DESCipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
            public /* bridge */ /* synthetic */ void finalize() throws Throwable {
                super.finalize();
            }

            public NoPadding() {
                super(1);
            }
        }

        public static class PKCS7Padding extends CBC {
            @Override // android.security.keystore.AndroidKeyStore3DESCipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
            public /* bridge */ /* synthetic */ void finalize() throws Throwable {
                super.finalize();
            }

            public PKCS7Padding() {
                super(64);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void initKey(int i, Key key) throws InvalidKeyException {
        if (!(key instanceof AndroidKeyStoreSecretKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unsupported key: ");
            sb.append(key != null ? key.getClass().getName() : "null");
            throw new InvalidKeyException(sb.toString());
        } else if (KeyProperties.KEY_ALGORITHM_3DES.equalsIgnoreCase(key.getAlgorithm())) {
            setKey((AndroidKeyStoreSecretKey) key);
        } else {
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only " + KeyProperties.KEY_ALGORITHM_3DES + " supported");
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public int engineGetBlockSize() {
        return 8;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public int engineGetOutputSize(int inputLen) {
        return inputLen + 24;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return ArrayUtils.cloneIfNotEmpty(this.mIv);
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, javax.crypto.CipherSpi
    public AlgorithmParameters engineGetParameters() {
        byte[] bArr;
        if (!this.mIvRequired || (bArr = this.mIv) == null || bArr.length <= 0) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(KeyProperties.KEY_ALGORITHM_3DES);
            params.init(new IvParameterSpec(this.mIv));
            return params;
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("Failed to obtain 3DES AlgorithmParameters", e);
        } catch (InvalidParameterSpecException e2) {
            throw new ProviderException("Failed to initialize 3DES AlgorithmParameters with an IV", e2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void initAlgorithmSpecificParameters() throws InvalidKeyException {
        if (this.mIvRequired && !isEncrypting()) {
            throw new InvalidKeyException("IV required when decrypting. Use IvParameterSpec or AlgorithmParameters to provide it.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (!this.mIvRequired) {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unsupported parameters: " + params);
            }
        } else if (params == null) {
            if (!isEncrypting()) {
                throw new InvalidAlgorithmParameterException("IvParameterSpec must be provided when decrypting");
            }
        } else if (params instanceof IvParameterSpec) {
            this.mIv = ((IvParameterSpec) params).getIV();
            if (this.mIv == null) {
                throw new InvalidAlgorithmParameterException("Null IV in IvParameterSpec");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Only IvParameterSpec supported");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
        if (!this.mIvRequired) {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unsupported parameters: " + params);
            }
        } else if (params == null) {
            if (!isEncrypting()) {
                throw new InvalidAlgorithmParameterException("IV required when decrypting. Use IvParameterSpec or AlgorithmParameters to provide it.");
            }
        } else if (KeyProperties.KEY_ALGORITHM_3DES.equalsIgnoreCase(params.getAlgorithm())) {
            try {
                this.mIv = ((IvParameterSpec) params.getParameterSpec(IvParameterSpec.class)).getIV();
                if (this.mIv == null) {
                    throw new InvalidAlgorithmParameterException("Null IV in AlgorithmParameters");
                }
            } catch (InvalidParameterSpecException e) {
                if (isEncrypting()) {
                    this.mIv = null;
                    return;
                }
                throw new InvalidAlgorithmParameterException("IV required when decrypting, but not found in parameters: " + params, e);
            }
        } else {
            throw new InvalidAlgorithmParameterException("Unsupported AlgorithmParameters algorithm: " + params.getAlgorithm() + ". Supported: DESede");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final int getAdditionalEntropyAmountForBegin() {
        if (!this.mIvRequired || this.mIv != null || !isEncrypting()) {
            return 0;
        }
        return 8;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public int getAdditionalEntropyAmountForFinish() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
        byte[] bArr;
        if (!isEncrypting() || !this.mIvRequired || !this.mIvHasBeenUsed) {
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 33);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockMode);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
            if (this.mIvRequired && (bArr = this.mIv) != null) {
                keymasterArgs.addBytes(KeymasterDefs.KM_TAG_NONCE, bArr);
                return;
            }
            return;
        }
        throw new IllegalStateException("IV has already been used. Reusing IV in encryption mode violates security best practices.");
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
        this.mIvHasBeenUsed = true;
        byte[] returnedIv = keymasterArgs.getBytes(KeymasterDefs.KM_TAG_NONCE, null);
        if (returnedIv != null && returnedIv.length == 0) {
            returnedIv = null;
        }
        if (this.mIvRequired) {
            byte[] bArr = this.mIv;
            if (bArr == null) {
                this.mIv = returnedIv;
            } else if (returnedIv != null && !Arrays.equals(returnedIv, bArr)) {
                throw new ProviderException("IV in use differs from provided IV");
            }
        } else if (returnedIv != null) {
            throw new ProviderException("IV in use despite IV not being used by this transformation");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final void resetAll() {
        this.mIv = null;
        this.mIvHasBeenUsed = false;
        super.resetAll();
    }
}
