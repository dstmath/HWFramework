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

class AndroidKeyStoreUnauthenticatedAESCipherSpi extends AndroidKeyStoreCipherSpiBase {
    private static final int BLOCK_SIZE_BYTES = 16;
    private byte[] mIv;
    private boolean mIvHasBeenUsed;
    private final boolean mIvRequired;
    private final int mKeymasterBlockMode;
    private final int mKeymasterPadding;

    static abstract class CBC extends AndroidKeyStoreUnauthenticatedAESCipherSpi {

        public static class NoPadding extends CBC {
            public NoPadding() {
                super(1);
            }
        }

        public static class PKCS7Padding extends CBC {
            public PKCS7Padding() {
                super(64);
            }
        }

        protected CBC(int keymasterPadding) {
            super(2, keymasterPadding, true);
        }
    }

    static abstract class CTR extends AndroidKeyStoreUnauthenticatedAESCipherSpi {

        public static class NoPadding extends CTR {
            public NoPadding() {
                super(1);
            }
        }

        protected CTR(int keymasterPadding) {
            super(3, keymasterPadding, true);
        }
    }

    static abstract class ECB extends AndroidKeyStoreUnauthenticatedAESCipherSpi {

        public static class NoPadding extends ECB {
            public NoPadding() {
                super(1);
            }
        }

        public static class PKCS7Padding extends ECB {
            public PKCS7Padding() {
                super(64);
            }
        }

        protected ECB(int keymasterPadding) {
            super(1, keymasterPadding, false);
        }
    }

    AndroidKeyStoreUnauthenticatedAESCipherSpi(int keymasterBlockMode, int keymasterPadding, boolean ivRequired) {
        this.mKeymasterBlockMode = keymasterBlockMode;
        this.mKeymasterPadding = keymasterPadding;
        this.mIvRequired = ivRequired;
    }

    protected final void resetAll() {
        this.mIv = null;
        this.mIvHasBeenUsed = false;
        super.resetAll();
    }

    protected final void resetWhilePreservingInitState() {
        super.resetWhilePreservingInitState();
    }

    protected final void initKey(int opmode, Key key) throws InvalidKeyException {
        if (!(key instanceof AndroidKeyStoreSecretKey)) {
            throw new InvalidKeyException("Unsupported key: " + (key != null ? key.getClass().getName() : "null"));
        } else if (KeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(key.getAlgorithm())) {
            setKey((AndroidKeyStoreSecretKey) key);
        } else {
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only " + KeyProperties.KEY_ALGORITHM_AES + " supported");
        }
    }

    protected final void initAlgorithmSpecificParameters() throws InvalidKeyException {
        if (this.mIvRequired && !isEncrypting()) {
            throw new InvalidKeyException("IV required when decrypting. Use IvParameterSpec or AlgorithmParameters to provide it.");
        }
    }

    protected final void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (this.mIvRequired) {
            if (params == null) {
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
        } else if (params != null) {
            throw new InvalidAlgorithmParameterException("Unsupported parameters: " + params);
        }
    }

    protected final void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
        if (this.mIvRequired) {
            if (params == null) {
                if (!isEncrypting()) {
                    throw new InvalidAlgorithmParameterException("IV required when decrypting. Use IvParameterSpec or AlgorithmParameters to provide it.");
                }
            } else if (KeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(params.getAlgorithm())) {
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
                throw new InvalidAlgorithmParameterException("Unsupported AlgorithmParameters algorithm: " + params.getAlgorithm() + ". Supported: AES");
            }
        } else if (params != null) {
            throw new InvalidAlgorithmParameterException("Unsupported parameters: " + params);
        }
    }

    protected final int getAdditionalEntropyAmountForBegin() {
        if (this.mIvRequired && this.mIv == null && isEncrypting()) {
            return 16;
        }
        return 0;
    }

    protected final int getAdditionalEntropyAmountForFinish() {
        return 0;
    }

    protected final void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
        if (isEncrypting() && this.mIvRequired && this.mIvHasBeenUsed) {
            throw new IllegalStateException("IV has already been used. Reusing IV in encryption mode violates security best practices.");
        }
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 32);
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockMode);
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
        if (this.mIvRequired && this.mIv != null) {
            keymasterArgs.addBytes(KeymasterDefs.KM_TAG_NONCE, this.mIv);
        }
    }

    protected final void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
        this.mIvHasBeenUsed = true;
        byte[] returnedIv = keymasterArgs.getBytes(KeymasterDefs.KM_TAG_NONCE, null);
        if (returnedIv != null && returnedIv.length == 0) {
            returnedIv = null;
        }
        if (this.mIvRequired) {
            if (this.mIv == null) {
                this.mIv = returnedIv;
            } else if (returnedIv != null && (Arrays.equals(returnedIv, this.mIv) ^ 1) != 0) {
                throw new ProviderException("IV in use differs from provided IV");
            }
        } else if (returnedIv != null) {
            throw new ProviderException("IV in use despite IV not being used by this transformation");
        }
    }

    protected final int engineGetBlockSize() {
        return 16;
    }

    protected final int engineGetOutputSize(int inputLen) {
        return inputLen + 48;
    }

    protected final byte[] engineGetIV() {
        return ArrayUtils.cloneIfNotEmpty(this.mIv);
    }

    protected final AlgorithmParameters engineGetParameters() {
        if (!this.mIvRequired || this.mIv == null || this.mIv.length <= 0) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(KeyProperties.KEY_ALGORITHM_AES);
            params.init(new IvParameterSpec(this.mIv));
            return params;
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("Failed to obtain AES AlgorithmParameters", e);
        } catch (InvalidParameterSpecException e2) {
            throw new ProviderException("Failed to initialize AES AlgorithmParameters with an IV", e2);
        }
    }
}
