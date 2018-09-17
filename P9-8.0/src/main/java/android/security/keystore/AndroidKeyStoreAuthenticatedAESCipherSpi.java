package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.MainDataStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import javax.crypto.spec.GCMParameterSpec;
import libcore.util.EmptyArray;

abstract class AndroidKeyStoreAuthenticatedAESCipherSpi extends AndroidKeyStoreCipherSpiBase {
    private static final int BLOCK_SIZE_BYTES = 16;
    private byte[] mIv;
    private boolean mIvHasBeenUsed;
    private final int mKeymasterBlockMode;
    private final int mKeymasterPadding;

    private static class AdditionalAuthenticationDataStream implements Stream {
        private final KeyStore mKeyStore;
        private final IBinder mOperationToken;

        /* synthetic */ AdditionalAuthenticationDataStream(KeyStore keyStore, IBinder operationToken, AdditionalAuthenticationDataStream -this2) {
            this(keyStore, operationToken);
        }

        private AdditionalAuthenticationDataStream(KeyStore keyStore, IBinder operationToken) {
            this.mKeyStore = keyStore;
            this.mOperationToken = operationToken;
        }

        public OperationResult update(byte[] input) {
            KeymasterArguments keymasterArgs = new KeymasterArguments();
            keymasterArgs.addBytes(KeymasterDefs.KM_TAG_ASSOCIATED_DATA, input);
            OperationResult result = this.mKeyStore.update(this.mOperationToken, keymasterArgs, null);
            if (result.resultCode == 1) {
                return new OperationResult(result.resultCode, result.token, result.operationHandle, input.length, result.output, result.outParams);
            }
            return result;
        }

        public OperationResult finish(byte[] signature, byte[] additionalEntropy) {
            if (additionalEntropy == null || additionalEntropy.length <= 0) {
                return new OperationResult(1, this.mOperationToken, 0, 0, EmptyArray.BYTE, new KeymasterArguments());
            }
            throw new ProviderException("AAD stream does not support additional entropy");
        }
    }

    private static class BufferAllOutputUntilDoFinalStreamer implements KeyStoreCryptoOperationStreamer {
        private ByteArrayOutputStream mBufferedOutput;
        private final KeyStoreCryptoOperationStreamer mDelegate;
        private long mProducedOutputSizeBytes;

        /* synthetic */ BufferAllOutputUntilDoFinalStreamer(KeyStoreCryptoOperationStreamer delegate, BufferAllOutputUntilDoFinalStreamer -this1) {
            this(delegate);
        }

        private BufferAllOutputUntilDoFinalStreamer(KeyStoreCryptoOperationStreamer delegate) {
            this.mBufferedOutput = new ByteArrayOutputStream();
            this.mDelegate = delegate;
        }

        public byte[] update(byte[] input, int inputOffset, int inputLength) throws KeyStoreException {
            byte[] output = this.mDelegate.update(input, inputOffset, inputLength);
            if (output != null) {
                try {
                    this.mBufferedOutput.write(output);
                } catch (IOException e) {
                    throw new ProviderException("Failed to buffer output", e);
                }
            }
            return EmptyArray.BYTE;
        }

        public byte[] doFinal(byte[] input, int inputOffset, int inputLength, byte[] signature, byte[] additionalEntropy) throws KeyStoreException {
            byte[] output = this.mDelegate.doFinal(input, inputOffset, inputLength, signature, additionalEntropy);
            if (output != null) {
                try {
                    this.mBufferedOutput.write(output);
                } catch (IOException e) {
                    throw new ProviderException("Failed to buffer output", e);
                }
            }
            byte[] result = this.mBufferedOutput.toByteArray();
            this.mBufferedOutput.reset();
            this.mProducedOutputSizeBytes += (long) result.length;
            return result;
        }

        public long getConsumedInputSizeBytes() {
            return this.mDelegate.getConsumedInputSizeBytes();
        }

        public long getProducedOutputSizeBytes() {
            return this.mProducedOutputSizeBytes;
        }
    }

    static abstract class GCM extends AndroidKeyStoreAuthenticatedAESCipherSpi {
        private static final int DEFAULT_TAG_LENGTH_BITS = 128;
        private static final int IV_LENGTH_BYTES = 12;
        private static final int MAX_SUPPORTED_TAG_LENGTH_BITS = 128;
        static final int MIN_SUPPORTED_TAG_LENGTH_BITS = 96;
        private int mTagLengthBits = 128;

        public static final class NoPadding extends GCM {
            public NoPadding() {
                super(1);
            }

            protected final int engineGetOutputSize(int inputLen) {
                long result;
                int tagLengthBytes = (getTagLengthBits() + 7) / 8;
                if (isEncrypting()) {
                    result = ((getConsumedInputSizeBytes() - getProducedOutputSizeBytes()) + ((long) inputLen)) + ((long) tagLengthBytes);
                } else {
                    result = ((getConsumedInputSizeBytes() - getProducedOutputSizeBytes()) + ((long) inputLen)) - ((long) tagLengthBytes);
                }
                if (result < 0) {
                    return 0;
                }
                if (result > 2147483647L) {
                    return Integer.MAX_VALUE;
                }
                return (int) result;
            }
        }

        GCM(int keymasterPadding) {
            super(32, keymasterPadding);
        }

        protected final void resetAll() {
            this.mTagLengthBits = 128;
            super.resetAll();
        }

        protected final void resetWhilePreservingInitState() {
            super.resetWhilePreservingInitState();
        }

        protected final void initAlgorithmSpecificParameters() throws InvalidKeyException {
            if (!isEncrypting()) {
                throw new InvalidKeyException("IV required when decrypting. Use IvParameterSpec or AlgorithmParameters to provide it.");
            }
        }

        protected final void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params == null) {
                if (!isEncrypting()) {
                    throw new InvalidAlgorithmParameterException("GCMParameterSpec must be provided when decrypting");
                }
            } else if (params instanceof GCMParameterSpec) {
                GCMParameterSpec spec = (GCMParameterSpec) params;
                byte[] iv = spec.getIV();
                if (iv == null) {
                    throw new InvalidAlgorithmParameterException("Null IV in GCMParameterSpec");
                } else if (iv.length != 12) {
                    throw new InvalidAlgorithmParameterException("Unsupported IV length: " + iv.length + " bytes. Only " + 12 + " bytes long IV supported");
                } else {
                    int tagLengthBits = spec.getTLen();
                    if (tagLengthBits < 96 || tagLengthBits > 128 || tagLengthBits % 8 != 0) {
                        throw new InvalidAlgorithmParameterException("Unsupported tag length: " + tagLengthBits + " bits" + ". Supported lengths: 96, 104, 112, 120, 128");
                    }
                    setIv(iv);
                    this.mTagLengthBits = tagLengthBits;
                }
            } else {
                throw new InvalidAlgorithmParameterException("Only GCMParameterSpec supported");
            }
        }

        protected final void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params == null) {
                if (!isEncrypting()) {
                    throw new InvalidAlgorithmParameterException("IV required when decrypting. Use GCMParameterSpec or GCM AlgorithmParameters to provide it.");
                }
            } else if (KeyProperties.BLOCK_MODE_GCM.equalsIgnoreCase(params.getAlgorithm())) {
                try {
                    initAlgorithmSpecificParameters((GCMParameterSpec) params.getParameterSpec(GCMParameterSpec.class));
                } catch (InvalidParameterSpecException e) {
                    if (isEncrypting()) {
                        setIv(null);
                        return;
                    }
                    throw new InvalidAlgorithmParameterException("IV and tag length required when decrypting, but not found in parameters: " + params, e);
                }
            } else {
                throw new InvalidAlgorithmParameterException("Unsupported AlgorithmParameters algorithm: " + params.getAlgorithm() + ". Supported: GCM");
            }
        }

        protected final AlgorithmParameters engineGetParameters() {
            byte[] iv = getIv();
            if (iv == null || iv.length <= 0) {
                return null;
            }
            try {
                AlgorithmParameters params = AlgorithmParameters.getInstance(KeyProperties.BLOCK_MODE_GCM);
                params.init(new GCMParameterSpec(this.mTagLengthBits, iv));
                return params;
            } catch (NoSuchAlgorithmException e) {
                throw new ProviderException("Failed to obtain GCM AlgorithmParameters", e);
            } catch (InvalidParameterSpecException e2) {
                throw new ProviderException("Failed to initialize GCM AlgorithmParameters", e2);
            }
        }

        protected KeyStoreCryptoOperationStreamer createMainDataStreamer(KeyStore keyStore, IBinder operationToken) {
            KeyStoreCryptoOperationStreamer streamer = new KeyStoreCryptoOperationChunkedStreamer(new MainDataStream(keyStore, operationToken));
            if (isEncrypting()) {
                return streamer;
            }
            return new BufferAllOutputUntilDoFinalStreamer(streamer, null);
        }

        protected final KeyStoreCryptoOperationStreamer createAdditionalAuthenticationDataStreamer(KeyStore keyStore, IBinder operationToken) {
            return new KeyStoreCryptoOperationChunkedStreamer(new AdditionalAuthenticationDataStream(keyStore, operationToken, null));
        }

        protected final int getAdditionalEntropyAmountForBegin() {
            if (getIv() == null && isEncrypting()) {
                return 12;
            }
            return 0;
        }

        protected final int getAdditionalEntropyAmountForFinish() {
            return 0;
        }

        protected final void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
            super.addAlgorithmSpecificParametersToBegin(keymasterArgs);
            keymasterArgs.addUnsignedInt(KeymasterDefs.KM_TAG_MAC_LENGTH, (long) this.mTagLengthBits);
        }

        protected final int getTagLengthBits() {
            return this.mTagLengthBits;
        }
    }

    AndroidKeyStoreAuthenticatedAESCipherSpi(int keymasterBlockMode, int keymasterPadding) {
        this.mKeymasterBlockMode = keymasterBlockMode;
        this.mKeymasterPadding = keymasterPadding;
    }

    protected void resetAll() {
        this.mIv = null;
        this.mIvHasBeenUsed = false;
        super.resetAll();
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

    protected void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
        if (isEncrypting() && this.mIvHasBeenUsed) {
            throw new IllegalStateException("IV has already been used. Reusing IV in encryption mode violates security best practices.");
        }
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 32);
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockMode);
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
        if (this.mIv != null) {
            keymasterArgs.addBytes(KeymasterDefs.KM_TAG_NONCE, this.mIv);
        }
    }

    protected final void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
        this.mIvHasBeenUsed = true;
        byte[] returnedIv = keymasterArgs.getBytes(KeymasterDefs.KM_TAG_NONCE, null);
        if (returnedIv != null && returnedIv.length == 0) {
            returnedIv = null;
        }
        if (this.mIv == null) {
            this.mIv = returnedIv;
        } else if (returnedIv != null && (Arrays.equals(returnedIv, this.mIv) ^ 1) != 0) {
            throw new ProviderException("IV in use differs from provided IV");
        }
    }

    protected final int engineGetBlockSize() {
        return 16;
    }

    protected final byte[] engineGetIV() {
        return ArrayUtils.cloneIfNotEmpty(this.mIv);
    }

    protected void setIv(byte[] iv) {
        this.mIv = iv;
    }

    protected byte[] getIv() {
        return this.mIv;
    }
}
