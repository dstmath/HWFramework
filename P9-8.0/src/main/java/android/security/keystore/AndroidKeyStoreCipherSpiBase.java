package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.MainDataStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import libcore.util.EmptyArray;

abstract class AndroidKeyStoreCipherSpiBase extends CipherSpi implements KeyStoreCryptoOperation {
    private KeyStoreCryptoOperationStreamer mAdditionalAuthenticationDataStreamer;
    private boolean mAdditionalAuthenticationDataStreamerClosed;
    private Exception mCachedException;
    private boolean mEncrypting;
    private AndroidKeyStoreKey mKey;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private int mKeymasterPurposeOverride = -1;
    private KeyStoreCryptoOperationStreamer mMainDataStreamer;
    private long mOperationHandle;
    private IBinder mOperationToken;
    private SecureRandom mRng;

    protected abstract void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArguments);

    protected abstract AlgorithmParameters engineGetParameters();

    protected abstract int getAdditionalEntropyAmountForBegin();

    protected abstract int getAdditionalEntropyAmountForFinish();

    protected abstract void initAlgorithmSpecificParameters() throws InvalidKeyException;

    protected abstract void initAlgorithmSpecificParameters(AlgorithmParameters algorithmParameters) throws InvalidAlgorithmParameterException;

    protected abstract void initAlgorithmSpecificParameters(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException;

    protected abstract void initKey(int i, Key key) throws InvalidKeyException;

    protected abstract void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArguments);

    AndroidKeyStoreCipherSpiBase() {
    }

    protected final void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        resetAll();
        try {
            init(opmode, key, random);
            initAlgorithmSpecificParameters();
            ensureKeystoreOperationInitialized();
            if (!true) {
                resetAll();
            }
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e);
        } catch (Throwable th) {
            if (!false) {
                resetAll();
            }
        }
    }

    protected final void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        resetAll();
        boolean success = false;
        try {
            init(opmode, key, random);
            initAlgorithmSpecificParameters(params);
            ensureKeystoreOperationInitialized();
            success = true;
        } finally {
            if (!success) {
                resetAll();
            }
        }
    }

    protected final void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        resetAll();
        boolean success = false;
        try {
            init(opmode, key, random);
            initAlgorithmSpecificParameters(params);
            ensureKeystoreOperationInitialized();
            success = true;
        } finally {
            if (!success) {
                resetAll();
            }
        }
    }

    private void init(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        switch (opmode) {
            case 1:
            case 3:
                this.mEncrypting = true;
                break;
            case 2:
            case 4:
                this.mEncrypting = false;
                break;
            default:
                throw new InvalidParameterException("Unsupported opmode: " + opmode);
        }
        initKey(opmode, key);
        if (this.mKey == null) {
            throw new ProviderException("initKey did not initialize the key");
        }
        this.mRng = random;
    }

    protected void resetAll() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mEncrypting = false;
        this.mKeymasterPurposeOverride = -1;
        this.mKey = null;
        this.mRng = null;
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mMainDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamerClosed = false;
        this.mCachedException = null;
    }

    protected void resetWhilePreservingInitState() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mMainDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamerClosed = false;
        this.mCachedException = null;
    }

    private void ensureKeystoreOperationInitialized() throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.mMainDataStreamer != null || this.mCachedException != null) {
            return;
        }
        if (this.mKey == null) {
            throw new IllegalStateException("Not initialized");
        }
        KeymasterArguments keymasterInputArgs = new KeymasterArguments();
        addAlgorithmSpecificParametersToBegin(keymasterInputArgs);
        byte[] additionalEntropy = KeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, getAdditionalEntropyAmountForBegin());
        int purpose = this.mKeymasterPurposeOverride != -1 ? this.mKeymasterPurposeOverride : this.mEncrypting ? 0 : 1;
        OperationResult opResult = this.mKeyStore.begin(this.mKey.getAlias(), purpose, true, keymasterInputArgs, additionalEntropy, this.mKey.getUid());
        if (opResult == null) {
            throw new KeyStoreConnectException();
        }
        this.mOperationToken = opResult.token;
        this.mOperationHandle = opResult.operationHandle;
        GeneralSecurityException e = KeyStoreCryptoOperationUtils.getExceptionForCipherInit(this.mKeyStore, this.mKey, opResult.resultCode);
        if (e != null) {
            if (e instanceof InvalidKeyException) {
                throw ((InvalidKeyException) e);
            } else if (e instanceof InvalidAlgorithmParameterException) {
                throw ((InvalidAlgorithmParameterException) e);
            } else {
                throw new ProviderException("Unexpected exception type", e);
            }
        } else if (this.mOperationToken == null) {
            throw new ProviderException("Keystore returned null operation token");
        } else if (this.mOperationHandle == 0) {
            throw new ProviderException("Keystore returned invalid operation handle");
        } else {
            loadAlgorithmSpecificParametersFromBeginResult(opResult.outParams);
            this.mMainDataStreamer = createMainDataStreamer(this.mKeyStore, opResult.token);
            this.mAdditionalAuthenticationDataStreamer = createAdditionalAuthenticationDataStreamer(this.mKeyStore, opResult.token);
            this.mAdditionalAuthenticationDataStreamerClosed = false;
        }
    }

    protected KeyStoreCryptoOperationStreamer createMainDataStreamer(KeyStore keyStore, IBinder operationToken) {
        return new KeyStoreCryptoOperationChunkedStreamer(new MainDataStream(keyStore, operationToken));
    }

    protected KeyStoreCryptoOperationStreamer createAdditionalAuthenticationDataStreamer(KeyStore keyStore, IBinder operationToken) {
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x000c A:{Splitter: B:3:0x0006, ExcHandler: java.security.InvalidKeyException (r1_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:7:0x000c, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x000d, code:
            r5.mCachedException = r1;
     */
    /* JADX WARNING: Missing block: B:9:0x000f, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (this.mCachedException != null) {
            return null;
        }
        try {
            ensureKeystoreOperationInitialized();
            if (inputLen == 0) {
                return null;
            }
            try {
                flushAAD();
                byte[] output = this.mMainDataStreamer.update(input, inputOffset, inputLen);
                if (output.length == 0) {
                    return null;
                }
                return output;
            } catch (KeyStoreException e) {
                this.mCachedException = e;
                return null;
            }
        } catch (GeneralSecurityException e2) {
        }
    }

    private void flushAAD() throws KeyStoreException {
        if (this.mAdditionalAuthenticationDataStreamer != null && (this.mAdditionalAuthenticationDataStreamerClosed ^ 1) != 0) {
            try {
                byte[] output = this.mAdditionalAuthenticationDataStreamer.doFinal(EmptyArray.BYTE, 0, 0, null, null);
                if (output != null && output.length > 0) {
                    throw new ProviderException("AAD update unexpectedly returned data: " + output.length + " bytes");
                }
            } finally {
                this.mAdditionalAuthenticationDataStreamerClosed = true;
            }
        }
    }

    protected final int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        byte[] outputCopy = engineUpdate(input, inputOffset, inputLen);
        if (outputCopy == null) {
            return 0;
        }
        int outputAvailable = output.length - outputOffset;
        if (outputCopy.length > outputAvailable) {
            throw new ShortBufferException("Output buffer too short. Produced: " + outputCopy.length + ", available: " + outputAvailable);
        }
        System.arraycopy(outputCopy, 0, output, outputOffset, outputCopy.length);
        return outputCopy.length;
    }

    protected final int engineUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        if (input == null) {
            throw new NullPointerException("input == null");
        } else if (output == null) {
            throw new NullPointerException("output == null");
        } else {
            byte[] outputArray;
            int inputSize = input.remaining();
            if (input.hasArray()) {
                outputArray = engineUpdate(input.array(), input.arrayOffset() + input.position(), inputSize);
                input.position(input.position() + inputSize);
            } else {
                byte[] inputArray = new byte[inputSize];
                input.get(inputArray);
                outputArray = engineUpdate(inputArray, 0, inputSize);
            }
            int outputSize = outputArray != null ? outputArray.length : 0;
            if (outputSize > 0) {
                int outputBufferAvailable = output.remaining();
                try {
                    output.put(outputArray);
                } catch (BufferOverflowException e) {
                    throw new ShortBufferException("Output buffer too small. Produced: " + outputSize + ", available: " + outputBufferAvailable);
                }
            }
            return outputSize;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0015 A:{Splitter: B:3:0x0005, ExcHandler: java.security.InvalidKeyException (r1_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:9:0x0015, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0016, code:
            r6.mCachedException = r1;
     */
    /* JADX WARNING: Missing block: B:11:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final void engineUpdateAAD(byte[] input, int inputOffset, int inputLen) {
        if (this.mCachedException == null) {
            try {
                ensureKeystoreOperationInitialized();
                if (this.mAdditionalAuthenticationDataStreamerClosed) {
                    throw new IllegalStateException("AAD can only be provided before Cipher.update is invoked");
                } else if (this.mAdditionalAuthenticationDataStreamer == null) {
                    throw new IllegalStateException("This cipher does not support AAD");
                } else {
                    try {
                        byte[] output = this.mAdditionalAuthenticationDataStreamer.update(input, inputOffset, inputLen);
                        if (output != null && output.length > 0) {
                            throw new ProviderException("AAD update unexpectedly produced output: " + output.length + " bytes");
                        }
                    } catch (KeyStoreException e) {
                        this.mCachedException = e;
                    }
                }
            } catch (GeneralSecurityException e2) {
            }
        }
    }

    protected final void engineUpdateAAD(ByteBuffer src) {
        if (src == null) {
            throw new IllegalArgumentException("src == null");
        } else if (src.hasRemaining()) {
            byte[] input;
            int inputOffset;
            int inputLen;
            if (src.hasArray()) {
                input = src.array();
                inputOffset = src.arrayOffset() + src.position();
                inputLen = src.remaining();
                src.position(src.limit());
            } else {
                input = new byte[src.remaining()];
                inputOffset = 0;
                inputLen = input.length;
                src.get(input);
            }
            engineUpdateAAD(input, inputOffset, inputLen);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0030 A:{Splitter: B:4:0x0012, ExcHandler: java.security.InvalidKeyException (r7_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:10:0x0030, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:12:0x003c, code:
            throw ((javax.crypto.IllegalBlockSizeException) new javax.crypto.IllegalBlockSizeException().initCause(r7));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        if (this.mCachedException != null) {
            throw ((IllegalBlockSizeException) new IllegalBlockSizeException().initCause(this.mCachedException));
        }
        try {
            ensureKeystoreOperationInitialized();
            try {
                flushAAD();
                byte[] output = this.mMainDataStreamer.doFinal(input, inputOffset, inputLen, null, KeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, getAdditionalEntropyAmountForFinish()));
                resetWhilePreservingInitState();
                return output;
            } catch (KeyStoreException e) {
                switch (e.getErrorCode()) {
                    case KeymasterDefs.KM_ERROR_INVALID_ARGUMENT /*-38*/:
                        throw ((BadPaddingException) new BadPaddingException().initCause(e));
                    case KeymasterDefs.KM_ERROR_VERIFICATION_FAILED /*-30*/:
                        throw ((AEADBadTagException) new AEADBadTagException().initCause(e));
                    case KeymasterDefs.KM_ERROR_INVALID_INPUT_LENGTH /*-21*/:
                        throw ((IllegalBlockSizeException) new IllegalBlockSizeException().initCause(e));
                    default:
                        throw ((IllegalBlockSizeException) new IllegalBlockSizeException().initCause(e));
                }
            }
        } catch (GeneralSecurityException e2) {
        }
    }

    protected final int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] outputCopy = engineDoFinal(input, inputOffset, inputLen);
        if (outputCopy == null) {
            return 0;
        }
        int outputAvailable = output.length - outputOffset;
        if (outputCopy.length > outputAvailable) {
            throw new ShortBufferException("Output buffer too short. Produced: " + outputCopy.length + ", available: " + outputAvailable);
        }
        System.arraycopy(outputCopy, 0, output, outputOffset, outputCopy.length);
        return outputCopy.length;
    }

    protected final int engineDoFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (input == null) {
            throw new NullPointerException("input == null");
        } else if (output == null) {
            throw new NullPointerException("output == null");
        } else {
            byte[] outputArray;
            int inputSize = input.remaining();
            if (input.hasArray()) {
                outputArray = engineDoFinal(input.array(), input.arrayOffset() + input.position(), inputSize);
                input.position(input.position() + inputSize);
            } else {
                byte[] inputArray = new byte[inputSize];
                input.get(inputArray);
                outputArray = engineDoFinal(inputArray, 0, inputSize);
            }
            int outputSize = outputArray != null ? outputArray.length : 0;
            if (outputSize > 0) {
                int outputBufferAvailable = output.remaining();
                try {
                    output.put(outputArray);
                } catch (BufferOverflowException e) {
                    throw new ShortBufferException("Output buffer too small. Produced: " + outputSize + ", available: " + outputBufferAvailable);
                }
            }
            return outputSize;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0060 A:{Splitter: B:17:0x003f, ExcHandler: java.security.NoSuchAlgorithmException (r0_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0096 A:{Splitter: B:31:0x0081, ExcHandler: java.security.NoSuchAlgorithmException (r0_1 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00cc A:{Splitter: B:42:0x00b7, ExcHandler: java.security.NoSuchAlgorithmException (r0_2 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:22:0x0060, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:24:0x0069, code:
            throw new java.security.InvalidKeyException("Failed to wrap key because it does not export its key material", r0);
     */
    /* JADX WARNING: Missing block: B:33:0x0096, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:35:0x009f, code:
            throw new java.security.InvalidKeyException("Failed to wrap key because it does not export its key material", r0);
     */
    /* JADX WARNING: Missing block: B:44:0x00cc, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:46:0x00d5, code:
            throw new java.security.InvalidKeyException("Failed to wrap key because it does not export its key material", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        if (this.mKey == null) {
            throw new IllegalStateException("Not initilized");
        } else if (!isEncrypting()) {
            throw new IllegalStateException("Cipher must be initialized in Cipher.WRAP_MODE to wrap keys");
        } else if (key == null) {
            throw new NullPointerException("key == null");
        } else {
            byte[] encoded = null;
            if (key instanceof SecretKey) {
                if ("RAW".equalsIgnoreCase(key.getFormat())) {
                    encoded = key.getEncoded();
                }
                if (encoded == null) {
                    try {
                        encoded = ((SecretKeySpec) SecretKeyFactory.getInstance(key.getAlgorithm()).getKeySpec((SecretKey) key, SecretKeySpec.class)).getEncoded();
                    } catch (GeneralSecurityException e) {
                    }
                }
            } else if (key instanceof PrivateKey) {
                if ("PKCS8".equalsIgnoreCase(key.getFormat())) {
                    encoded = key.getEncoded();
                }
                if (encoded == null) {
                    try {
                        encoded = ((PKCS8EncodedKeySpec) KeyFactory.getInstance(key.getAlgorithm()).getKeySpec(key, PKCS8EncodedKeySpec.class)).getEncoded();
                    } catch (GeneralSecurityException e2) {
                    }
                }
            } else if (key instanceof PublicKey) {
                if ("X.509".equalsIgnoreCase(key.getFormat())) {
                    encoded = key.getEncoded();
                }
                if (encoded == null) {
                    try {
                        encoded = ((X509EncodedKeySpec) KeyFactory.getInstance(key.getAlgorithm()).getKeySpec(key, X509EncodedKeySpec.class)).getEncoded();
                    } catch (GeneralSecurityException e3) {
                    }
                }
            } else {
                throw new InvalidKeyException("Unsupported key type: " + key.getClass().getName());
            }
            if (encoded == null) {
                throw new InvalidKeyException("Failed to wrap key because it does not export its key material");
            }
            try {
                return engineDoFinal(encoded, 0, encoded.length);
            } catch (BadPaddingException e4) {
                throw ((IllegalBlockSizeException) new IllegalBlockSizeException().initCause(e4));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x004a A:{Splitter: B:11:0x0027, ExcHandler: javax.crypto.IllegalBlockSizeException (r0_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:16:0x004a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x0053, code:
            throw new java.security.InvalidKeyException("Failed to unwrap key", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        if (this.mKey == null) {
            throw new IllegalStateException("Not initilized");
        } else if (isEncrypting()) {
            throw new IllegalStateException("Cipher must be initialized in Cipher.WRAP_MODE to wrap keys");
        } else if (wrappedKey == null) {
            throw new NullPointerException("wrappedKey == null");
        } else {
            try {
                byte[] encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
                switch (wrappedKeyType) {
                    case 1:
                        try {
                            return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePublic(new X509EncodedKeySpec(encoded));
                        } catch (InvalidKeySpecException e) {
                            throw new InvalidKeyException("Failed to create public key from its X.509 encoded form", e);
                        }
                    case 2:
                        try {
                            return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(encoded));
                        } catch (InvalidKeySpecException e2) {
                            throw new InvalidKeyException("Failed to create private key from its PKCS#8 encoded form", e2);
                        }
                    case 3:
                        return new SecretKeySpec(encoded, wrappedKeyAlgorithm);
                    default:
                        throw new InvalidParameterException("Unsupported wrappedKeyType: " + wrappedKeyType);
                }
            } catch (GeneralSecurityException e3) {
            }
        }
    }

    protected final void engineSetMode(String mode) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    protected final void engineSetPadding(String arg0) throws NoSuchPaddingException {
        throw new UnsupportedOperationException();
    }

    protected final int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    public void finalize() throws Throwable {
        try {
            IBinder operationToken = this.mOperationToken;
            if (operationToken != null) {
                this.mKeyStore.abort(operationToken);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public final long getOperationHandle() {
        return this.mOperationHandle;
    }

    protected final void setKey(AndroidKeyStoreKey key) {
        this.mKey = key;
    }

    protected final void setKeymasterPurposeOverride(int keymasterPurpose) {
        this.mKeymasterPurposeOverride = keymasterPurpose;
    }

    protected final int getKeymasterPurposeOverride() {
        return this.mKeymasterPurposeOverride;
    }

    protected final boolean isEncrypting() {
        return this.mEncrypting;
    }

    protected final KeyStore getKeyStore() {
        return this.mKeyStore;
    }

    protected final long getConsumedInputSizeBytes() {
        if (this.mMainDataStreamer != null) {
            return this.mMainDataStreamer.getConsumedInputSizeBytes();
        }
        throw new IllegalStateException("Not initialized");
    }

    protected final long getProducedOutputSizeBytes() {
        if (this.mMainDataStreamer != null) {
            return this.mMainDataStreamer.getProducedOutputSizeBytes();
        }
        throw new IllegalStateException("Not initialized");
    }

    static String opmodeToString(int opmode) {
        switch (opmode) {
            case 1:
                return "ENCRYPT_MODE";
            case 2:
                return "DECRYPT_MODE";
            case 3:
                return "WRAP_MODE";
            case 4:
                return "UNWRAP_MODE";
            default:
                return String.valueOf(opmode);
        }
    }
}
