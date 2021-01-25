package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.KeyStoreCryptoOperationChunkedStreamer;
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

    static abstract class GCM extends AndroidKeyStoreAuthenticatedAESCipherSpi {
        private static final int DEFAULT_TAG_LENGTH_BITS = 128;
        private static final int IV_LENGTH_BYTES = 12;
        private static final int MAX_SUPPORTED_TAG_LENGTH_BITS = 128;
        static final int MIN_SUPPORTED_TAG_LENGTH_BITS = 96;
        private int mTagLengthBits = 128;

        GCM(int keymasterPadding) {
            super(32, keymasterPadding);
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void resetAll() {
            this.mTagLengthBits = 128;
            AndroidKeyStoreAuthenticatedAESCipherSpi.super.resetAll();
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void resetWhilePreservingInitState() {
            AndroidKeyStoreAuthenticatedAESCipherSpi.super.resetWhilePreservingInitState();
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void initAlgorithmSpecificParameters() throws InvalidKeyException {
            if (!isEncrypting()) {
                throw new InvalidKeyException("IV required when decrypting. Use IvParameterSpec or AlgorithmParameters to provide it.");
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params == null) {
                if (!isEncrypting()) {
                    throw new InvalidAlgorithmParameterException("GCMParameterSpec must be provided when decrypting");
                }
            } else if (params instanceof GCMParameterSpec) {
                GCMParameterSpec spec = (GCMParameterSpec) params;
                byte[] iv = spec.getIV();
                if (iv == null) {
                    throw new InvalidAlgorithmParameterException("Null IV in GCMParameterSpec");
                } else if (iv.length == 12) {
                    int tagLengthBits = spec.getTLen();
                    if (tagLengthBits < 96 || tagLengthBits > 128 || tagLengthBits % 8 != 0) {
                        throw new InvalidAlgorithmParameterException("Unsupported tag length: " + tagLengthBits + " bits. Supported lengths: 96, 104, 112, 120, 128");
                    }
                    setIv(iv);
                    this.mTagLengthBits = tagLengthBits;
                } else {
                    throw new InvalidAlgorithmParameterException("Unsupported IV length: " + iv.length + " bytes. Only 12 bytes long IV supported");
                }
            } else {
                throw new InvalidAlgorithmParameterException("Only GCMParameterSpec supported");
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
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

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public final AlgorithmParameters engineGetParameters() {
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

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public KeyStoreCryptoOperationStreamer createMainDataStreamer(KeyStore keyStore, IBinder operationToken) {
            KeyStoreCryptoOperationStreamer streamer = new KeyStoreCryptoOperationChunkedStreamer(new KeyStoreCryptoOperationChunkedStreamer.MainDataStream(keyStore, operationToken));
            if (isEncrypting()) {
                return streamer;
            }
            return new BufferAllOutputUntilDoFinalStreamer(streamer);
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final KeyStoreCryptoOperationStreamer createAdditionalAuthenticationDataStreamer(KeyStore keyStore, IBinder operationToken) {
            return new KeyStoreCryptoOperationChunkedStreamer(new AdditionalAuthenticationDataStream(keyStore, operationToken));
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForBegin() {
            if (getIv() != null || !isEncrypting()) {
                return 0;
            }
            return 12;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForFinish() {
            return 0;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
            AndroidKeyStoreAuthenticatedAESCipherSpi.super.addAlgorithmSpecificParametersToBegin(keymasterArgs);
            keymasterArgs.addUnsignedInt(KeymasterDefs.KM_TAG_MAC_LENGTH, (long) this.mTagLengthBits);
        }

        /* access modifiers changed from: protected */
        public final int getTagLengthBits() {
            return this.mTagLengthBits;
        }

        public static final class NoPadding extends GCM {
            @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
            public /* bridge */ /* synthetic */ void finalize() throws Throwable {
                super.finalize();
            }

            public NoPadding() {
                super(1);
            }

            /* access modifiers changed from: protected */
            @Override // javax.crypto.CipherSpi
            public final int engineGetOutputSize(int inputLen) {
                long result;
                int tagLengthBytes = (getTagLengthBits() + 7) / 8;
                if (isEncrypting()) {
                    result = (getConsumedInputSizeBytes() - getProducedOutputSizeBytes()) + ((long) inputLen) + ((long) tagLengthBytes);
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
    }

    AndroidKeyStoreAuthenticatedAESCipherSpi(int keymasterBlockMode, int keymasterPadding) {
        this.mKeymasterBlockMode = keymasterBlockMode;
        this.mKeymasterPadding = keymasterPadding;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void resetAll() {
        this.mIv = null;
        this.mIvHasBeenUsed = false;
        super.resetAll();
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final void initKey(int opmode, Key key) throws InvalidKeyException {
        if (!(key instanceof AndroidKeyStoreSecretKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unsupported key: ");
            sb.append(key != null ? key.getClass().getName() : "null");
            throw new InvalidKeyException(sb.toString());
        } else if (KeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(key.getAlgorithm())) {
            setKey((AndroidKeyStoreSecretKey) key);
        } else {
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only " + KeyProperties.KEY_ALGORITHM_AES + " supported");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
        if (!isEncrypting() || !this.mIvHasBeenUsed) {
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 32);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockMode);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
            byte[] bArr = this.mIv;
            if (bArr != null) {
                keymasterArgs.addBytes(KeymasterDefs.KM_TAG_NONCE, bArr);
                return;
            }
            return;
        }
        throw new IllegalStateException("IV has already been used. Reusing IV in encryption mode violates security best practices.");
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
        this.mIvHasBeenUsed = true;
        byte[] returnedIv = keymasterArgs.getBytes(KeymasterDefs.KM_TAG_NONCE, null);
        if (returnedIv != null && returnedIv.length == 0) {
            returnedIv = null;
        }
        byte[] bArr = this.mIv;
        if (bArr == null) {
            this.mIv = returnedIv;
        } else if (returnedIv != null && !Arrays.equals(returnedIv, bArr)) {
            throw new ProviderException("IV in use differs from provided IV");
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 16;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return ArrayUtils.cloneIfNotEmpty(this.mIv);
    }

    /* access modifiers changed from: protected */
    public void setIv(byte[] iv) {
        this.mIv = iv;
    }

    /* access modifiers changed from: protected */
    public byte[] getIv() {
        return this.mIv;
    }

    private static class BufferAllOutputUntilDoFinalStreamer implements KeyStoreCryptoOperationStreamer {
        private ByteArrayOutputStream mBufferedOutput;
        private final KeyStoreCryptoOperationStreamer mDelegate;
        private long mProducedOutputSizeBytes;

        private BufferAllOutputUntilDoFinalStreamer(KeyStoreCryptoOperationStreamer delegate) {
            this.mBufferedOutput = new ByteArrayOutputStream();
            this.mDelegate = delegate;
        }

        @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
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

        @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
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

        @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
        public long getConsumedInputSizeBytes() {
            return this.mDelegate.getConsumedInputSizeBytes();
        }

        @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
        public long getProducedOutputSizeBytes() {
            return this.mProducedOutputSizeBytes;
        }
    }

    private static class AdditionalAuthenticationDataStream implements KeyStoreCryptoOperationChunkedStreamer.Stream {
        private final KeyStore mKeyStore;
        private final IBinder mOperationToken;

        private AdditionalAuthenticationDataStream(KeyStore keyStore, IBinder operationToken) {
            this.mKeyStore = keyStore;
            this.mOperationToken = operationToken;
        }

        @Override // android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.Stream
        public OperationResult update(byte[] input) {
            KeymasterArguments keymasterArgs = new KeymasterArguments();
            keymasterArgs.addBytes(KeymasterDefs.KM_TAG_ASSOCIATED_DATA, input);
            OperationResult result = this.mKeyStore.update(this.mOperationToken, keymasterArgs, null);
            if (result.resultCode == 1) {
                return new OperationResult(result.resultCode, result.token, result.operationHandle, input.length, result.output, result.outParams);
            }
            return result;
        }

        @Override // android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.Stream
        public OperationResult finish(byte[] signature, byte[] additionalEntropy) {
            if (additionalEntropy == null || additionalEntropy.length <= 0) {
                return new OperationResult(1, this.mOperationToken, 0, 0, EmptyArray.BYTE, new KeymasterArguments());
            }
            throw new ProviderException("AAD stream does not support additional entropy");
        }
    }
}
