package com.huawei.security.keystore;

import android.os.IBinder;
import android.security.keystore.KeyStoreCryptoOperation;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.ArrayUtils;
import com.huawei.security.keystore.HwUniversalKeyStoreCryptoOperationStreamer;
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

abstract class HwUniversalKeyStoreCipherSpiBase extends CipherSpi implements KeyStoreCryptoOperation {
    private HwUniversalKeyStoreCryptoOperationStreamer mAdditionalAuthenticationDataStreamer;
    private boolean mAdditionalAuthenticationDataStreamerClosed;
    private Exception mCachedException;
    private boolean mIsEncrypting;
    private HwUniversalKeyStoreKey mKey;
    private final HwKeystoreManager mKeyStore = HwKeystoreManager.getInstance();
    private int mKeymasterPurposeOverride = -1;
    private HwUniversalKeyStoreCryptoOperationStreamer mMainDataStreamer;
    private long mOperationHandle;
    private IBinder mOperationToken;
    private SecureRandom mRng;

    /* access modifiers changed from: protected */
    public abstract void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments hwKeymasterArguments);

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    @Nullable
    public abstract AlgorithmParameters engineGetParameters();

    /* access modifiers changed from: protected */
    public abstract int getAdditionalEntropyAmountForBegin();

    /* access modifiers changed from: protected */
    public abstract int getAdditionalEntropyAmountForFinish();

    /* access modifiers changed from: protected */
    public abstract void initAlgorithmSpecificParameters() throws InvalidKeyException;

    /* access modifiers changed from: protected */
    public abstract void initAlgorithmSpecificParameters(@Nullable AlgorithmParameters algorithmParameters) throws InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void initAlgorithmSpecificParameters(@Nullable AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void initKey(int i, @Nullable Key key) throws InvalidKeyException;

    /* access modifiers changed from: protected */
    public abstract void loadAlgorithmSpecificParametersFromBeginResult(@NonNull HwKeymasterArguments hwKeymasterArguments);

    HwUniversalKeyStoreCipherSpiBase() {
    }

    static String opModeToString(int mode) {
        if (mode == 1) {
            return "ENCRYPT_MODE";
        }
        if (mode == 2) {
            return "DECRYPT_MODE";
        }
        if (mode == 3) {
            return "WRAP_MODE";
        }
        if (mode == 4) {
            return "UNWRAP_MODE";
        }
        return String.valueOf(mode);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineSetMode(String mode) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException("Stub!");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineSetPadding(String arg0) throws NoSuchPaddingException {
        throw new UnsupportedOperationException("Stub!");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException("Stub!");
    }

    /* access modifiers changed from: protected */
    public void resetAll() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mIsEncrypting = false;
        this.mMainDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamerClosed = false;
        this.mCachedException = null;
        this.mKeymasterPurposeOverride = -1;
        this.mKey = null;
        this.mRng = null;
        this.mOperationToken = null;
        this.mOperationHandle = 0;
    }

    /* access modifiers changed from: protected */
    public void resetWhilePreservingInitState() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mAdditionalAuthenticationDataStreamer = null;
        this.mAdditionalAuthenticationDataStreamerClosed = false;
        this.mCachedException = null;
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mMainDataStreamer = null;
    }

    private void ensureKeystoreOperationInitialized() throws InvalidKeyException, InvalidAlgorithmParameterException {
        int purpose;
        if (this.mMainDataStreamer != null || this.mCachedException != null) {
            return;
        }
        if (this.mKey != null) {
            HwKeymasterArguments keymasterInputArgs = new HwKeymasterArguments();
            addAlgorithmSpecificParametersToBegin(keymasterInputArgs);
            byte[] additionalEntropy = HwUniversalKeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, getAdditionalEntropyAmountForBegin());
            if (this.mKeymasterPurposeOverride == -1) {
                purpose = !this.mIsEncrypting ? 1 : 0;
            } else {
                purpose = this.mKeymasterPurposeOverride;
            }
            HwOperationResult opResult = this.mKeyStore.begin(this.mKey.getAlias(), purpose, true, keymasterInputArgs, additionalEntropy, this.mKey.getUid());
            if (opResult != null) {
                this.mOperationToken = opResult.token;
                this.mOperationHandle = opResult.operationHandle;
                throwExceptionIfNeeded(opResult);
                if (this.mOperationToken == null) {
                    throw new ProviderException("HwKeyStore returned null operation token");
                } else if (this.mOperationHandle != 0) {
                    loadAlgorithmSpecificParametersFromBeginResult(opResult.outParams);
                    this.mMainDataStreamer = createMainDataStreamer(this.mKeyStore, opResult.token);
                    this.mAdditionalAuthenticationDataStreamer = createAdditionalAuthenticationDataStreamer(this.mKeyStore, opResult.token);
                    this.mAdditionalAuthenticationDataStreamerClosed = false;
                } else {
                    throw new ProviderException("HwKeyStore returned invalid operation handle");
                }
            } else {
                throw new ProviderException("Failed to communicate with HwKeyStore service");
            }
        } else {
            throw new IllegalStateException("Key Not inited");
        }
    }

    private void throwExceptionIfNeeded(HwOperationResult opResult) throws InvalidKeyException, InvalidAlgorithmParameterException {
        GeneralSecurityException exception = HwUniversalKeyStoreCryptoOperationUtils.getExceptionForCipherInit(this.mKeyStore, this.mKey, opResult.resultCode);
        if (exception == null) {
            return;
        }
        if (exception instanceof InvalidKeyException) {
            throw ((InvalidKeyException) exception);
        } else if (exception instanceof InvalidAlgorithmParameterException) {
            throw ((InvalidAlgorithmParameterException) exception);
        } else {
            throw new ProviderException("Unexpected exception type", exception);
        }
    }

    /* access modifiers changed from: protected */
    @NonNull
    public HwUniversalKeyStoreCryptoOperationStreamer createMainDataStreamer(HwKeystoreManager keyStore, IBinder operationToken) {
        return new HwUniversalKeyStoreCryptoOperationStreamer(new HwUniversalKeyStoreCryptoOperationStreamer.MainDataStream(keyStore, operationToken));
    }

    /* access modifiers changed from: protected */
    @Nullable
    public HwUniversalKeyStoreCryptoOperationStreamer createAdditionalAuthenticationDataStreamer(HwKeystoreManager keyStore, IBinder operationToken) {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean adjustConfigForEncryptingWithPrivateKey() {
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0035  */
    private void init(int mode, Key key, SecureRandom secRandom) throws InvalidKeyException {
        if (mode != 1) {
            if (mode != 2) {
                if (mode != 3) {
                    if (mode != 4) {
                        throw new InvalidParameterException("Unsupported mode: " + mode);
                    }
                }
            }
            this.mIsEncrypting = false;
            initKey(mode, key);
            if (this.mKey == null) {
                this.mRng = secRandom;
                return;
            }
            throw new ProviderException("initKey did not initialize the key");
        }
        this.mIsEncrypting = true;
        initKey(mode, key);
        if (this.mKey == null) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public void engineInit(int mode, Key key, AlgorithmParameters algParams, SecureRandom secRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        resetAll();
        boolean flag = false;
        try {
            init(mode, key, secRandom);
            initAlgorithmSpecificParameters(algParams);
            ensureKeystoreOperationInitialized();
            flag = true;
        } finally {
            if (!flag) {
                resetAll();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public void engineInit(int mode, Key key, AlgorithmParameterSpec algParams, SecureRandom secRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        resetAll();
        boolean flag = false;
        try {
            init(mode, key, secRandom);
            initAlgorithmSpecificParameters(algParams);
            ensureKeystoreOperationInitialized();
            flag = true;
        } finally {
            if (!flag) {
                resetAll();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public void engineInit(int mode, Key key, SecureRandom secRandom) throws InvalidKeyException {
        resetAll();
        boolean flag = false;
        try {
            init(mode, key, secRandom);
            initAlgorithmSpecificParameters();
            try {
                ensureKeystoreOperationInitialized();
                flag = true;
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        } finally {
            if (!flag) {
                resetAll();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        byte[] output = new byte[0];
        if (this.mCachedException != null) {
            return output;
        }
        try {
            ensureKeystoreOperationInitialized();
            if (inputLen == 0) {
                return output;
            }
            try {
                flushAAD();
                return this.mMainDataStreamer.update(input, inputOffset, inputLen);
            } catch (HwUniversalKeyStoreException e) {
                this.mCachedException = e;
                return new byte[0];
            }
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e2) {
            this.mCachedException = e2;
            return output;
        }
    }

    private void flushAAD() throws HwUniversalKeyStoreException {
        HwUniversalKeyStoreCryptoOperationStreamer hwUniversalKeyStoreCryptoOperationStreamer = this.mAdditionalAuthenticationDataStreamer;
        if (hwUniversalKeyStoreCryptoOperationStreamer != null && !this.mAdditionalAuthenticationDataStreamerClosed) {
            try {
                byte[] out = hwUniversalKeyStoreCryptoOperationStreamer.doFinal(ArrayUtils.EmptyArray.BYTE, 0, 0, null, null);
                if (out != null && out.length > 0) {
                    throw new ProviderException("AAD update unexpectedly returned data: " + out.length + " bytes");
                }
            } finally {
                this.mAdditionalAuthenticationDataStreamerClosed = true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        byte[] outCopy = engineUpdate(input, inputOffset, inputLen);
        if (outCopy == null || outCopy.length == 0) {
            return 0;
        }
        int outAvailable = output.length - outputOffset;
        if (outCopy.length <= outAvailable) {
            System.arraycopy(outCopy, 0, output, outputOffset, outCopy.length);
            return outCopy.length;
        }
        throw new ShortBufferException("Output buffer too short. Output len: " + outCopy.length + ", available len: " + outAvailable);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineUpdateAAD(byte[] input, int inputOffset, int inputLen) {
        if (this.mCachedException == null) {
            try {
                ensureKeystoreOperationInitialized();
                if (!this.mAdditionalAuthenticationDataStreamerClosed) {
                    HwUniversalKeyStoreCryptoOperationStreamer hwUniversalKeyStoreCryptoOperationStreamer = this.mAdditionalAuthenticationDataStreamer;
                    if (hwUniversalKeyStoreCryptoOperationStreamer != null) {
                        try {
                            byte[] out = hwUniversalKeyStoreCryptoOperationStreamer.update(input, inputOffset, inputLen);
                            if (out != null && out.length > 0) {
                                throw new ProviderException("Update AAD unexpectedly out: " + out.length + " bytes");
                            }
                        } catch (HwUniversalKeyStoreException ex) {
                            this.mCachedException = ex;
                        }
                    } else {
                        throw new IllegalStateException("Cipher does not support AAD!");
                    }
                } else {
                    throw new IllegalStateException("AAD should be provided before Cipher.update is invoked");
                }
            } catch (InvalidAlgorithmParameterException | InvalidKeyException ex2) {
                this.mCachedException = ex2;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r1v3 byte[]: [D('inArray' byte[]), D('outArray' byte[])] */
    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        byte[] inArray;
        if (input == null || output == null) {
            throw new NullPointerException("input == null or output == null");
        }
        int inSize = input.remaining();
        int outSize = 0;
        if (input.hasArray()) {
            inArray = engineUpdate(input.array(), input.arrayOffset() + input.position(), inSize);
            input.position(input.position() + inSize);
        } else {
            byte[] outArray = new byte[inSize];
            input.get(outArray);
            inArray = engineUpdate(outArray, 0, inSize);
        }
        if (inArray != null && inArray.length > 0) {
            outSize = inArray.length;
        }
        if (outSize > 0) {
            int outBufferAvailable = output.remaining();
            try {
                output.put(inArray);
            } catch (BufferOverflowException e) {
                throw new ShortBufferException("Output buffer too small. Produced: " + outSize + ", available: " + outBufferAvailable);
            }
        }
        return outSize;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineUpdateAAD(ByteBuffer srcBuffer) {
        int inLen;
        int inOffset;
        byte[] in;
        if (srcBuffer == null) {
            throw new IllegalArgumentException("srcBuffer == null");
        } else if (srcBuffer.hasRemaining()) {
            if (srcBuffer.hasArray()) {
                in = srcBuffer.array();
                inOffset = srcBuffer.arrayOffset() + srcBuffer.position();
                inLen = srcBuffer.remaining();
                srcBuffer.position(srcBuffer.limit());
            } else {
                in = new byte[srcBuffer.remaining()];
                inOffset = 0;
                inLen = in.length;
                srcBuffer.get(in);
            }
            engineUpdateAAD(in, inOffset, inLen);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        Exception exc = this.mCachedException;
        if (exc == null) {
            try {
                ensureKeystoreOperationInitialized();
                flushAAD();
                byte[] out = this.mMainDataStreamer.doFinal(input, inputOffset, inputLen, null, HwUniversalKeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, getAdditionalEntropyAmountForFinish()));
                resetWhilePreservingInitState();
                return out;
            } catch (InvalidAlgorithmParameterException | InvalidKeyException ex) {
                IllegalBlockSizeException newEx = new IllegalBlockSizeException(ex.getMessage());
                newEx.initCause(ex);
                throw newEx;
            } catch (HwUniversalKeyStoreException ex2) {
                int errorCode = ex2.getErrorCode();
                if (errorCode == -38) {
                    BadPaddingException newBadEx = new BadPaddingException(ex2.getMessage());
                    newBadEx.initCause(ex2);
                    throw newBadEx;
                } else if (errorCode == -30) {
                    AEADBadTagException newAeadEx = new AEADBadTagException(ex2.getMessage());
                    newAeadEx.initCause(ex2);
                    throw newAeadEx;
                } else if (errorCode != -21) {
                    IllegalBlockSizeException newDefaultEx = new IllegalBlockSizeException(ex2.getMessage());
                    newDefaultEx.initCause(ex2);
                    throw newDefaultEx;
                } else {
                    IllegalBlockSizeException newInvalidEx = new IllegalBlockSizeException(ex2.getMessage());
                    newInvalidEx.initCause(ex2);
                    throw newInvalidEx;
                }
            }
        } else {
            IllegalBlockSizeException newE = new IllegalBlockSizeException(exc.getMessage());
            newE.initCause(this.mCachedException);
            throw newE;
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] outCopy = engineDoFinal(input, inputOffset, inputLen);
        if (outCopy == null) {
            return 0;
        }
        int outAvailable = output.length - outputOffset;
        if (outCopy.length <= outAvailable) {
            System.arraycopy(outCopy, 0, output, outputOffset, outCopy.length);
            return outCopy.length;
        }
        throw new ShortBufferException("Output buffer too short. Output len: " + outCopy.length + ", available len: " + outAvailable);
    }

    /* JADX INFO: Multiple debug info for r1v3 byte[]: [D('inArray' byte[]), D('outArray' byte[])] */
    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineDoFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] inArray;
        if (input == null || output == null) {
            throw new NullPointerException("input == null or output == null");
        }
        int inSize = input.remaining();
        int outSize = 0;
        if (input.hasArray()) {
            inArray = engineDoFinal(input.array(), input.arrayOffset() + input.position(), inSize);
            input.position(input.position() + inSize);
        } else {
            byte[] outArray = new byte[inSize];
            input.get(outArray);
            inArray = engineDoFinal(outArray, 0, inSize);
        }
        if (inArray != null) {
            outSize = inArray.length;
        }
        if (outSize > 0) {
            int outBufferAvailable = output.remaining();
            try {
                output.put(inArray);
            } catch (BufferOverflowException e) {
                throw new ShortBufferException("Output buffer too small. Output: " + outSize + ", available: " + outBufferAvailable);
            }
        }
        return outSize;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        if (this.mKey == null) {
            throw new IllegalStateException("Not inited");
        } else if (isEncrypting()) {
            HwAttestationUtils.checkNotNull(key == null, "key == null");
            byte[] encoded = null;
            try {
                if (key instanceof SecretKey) {
                    if ("RAW".equalsIgnoreCase(key.getFormat())) {
                        encoded = key.getEncoded();
                    }
                    if (encoded == null) {
                        encoded = tryWrapToSecretKey(key);
                    }
                } else if (key instanceof PrivateKey) {
                    if ("PKCS8".equalsIgnoreCase(key.getFormat())) {
                        encoded = key.getEncoded();
                    }
                    if (encoded == null) {
                        encoded = tryWrapToPKCS8(key);
                    }
                } else if (key instanceof PublicKey) {
                    if ("X.509".equalsIgnoreCase(key.getFormat())) {
                        encoded = key.getEncoded();
                    }
                    if (encoded == null) {
                        encoded = tryWrapToX509(key);
                    }
                } else {
                    throw new InvalidKeyException("Unsupported key type: " + key.getClass().getName());
                }
                if (encoded != null) {
                    return engineDoFinal(encoded, 0, encoded.length);
                }
                throw new InvalidKeyException("Failed to wrap key because it does not export its key material");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new InvalidKeyException("Failed to wrap key because it does not export its key material" + e.getMessage());
            } catch (BadPaddingException e2) {
                IllegalBlockSizeException newEx = new IllegalBlockSizeException(e2.getMessage());
                newEx.initCause(e2);
                throw newEx;
            }
        } else {
            throw new IllegalStateException("Cipher must be inited in Cipher.WRAP_MODE to wrap keys");
        }
    }

    private byte[] tryWrapToSecretKey(Key key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ((SecretKeySpec) SecretKeyFactory.getInstance(key.getAlgorithm()).getKeySpec((SecretKey) key, SecretKeySpec.class)).getEncoded();
    }

    private byte[] tryWrapToPKCS8(Key key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ((PKCS8EncodedKeySpec) KeyFactory.getInstance(key.getAlgorithm()).getKeySpec(key, PKCS8EncodedKeySpec.class)).getEncoded();
    }

    private byte[] tryWrapToX509(Key key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return ((X509EncodedKeySpec) KeyFactory.getInstance(key.getAlgorithm()).getKeySpec(key, X509EncodedKeySpec.class)).getEncoded();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        if (this.mKey == null) {
            throw new IllegalStateException("Not inited");
        } else if (isEncrypting()) {
            throw new IllegalStateException("Cipher must be inited in Cipher.WRAP_MODE to wrap keys");
        } else if (wrappedKey != null) {
            try {
                byte[] encodedData = engineDoFinal(wrappedKey, 0, wrappedKey.length);
                if (wrappedKeyType == 1) {
                    try {
                        return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePublic(new X509EncodedKeySpec(encodedData));
                    } catch (InvalidKeySpecException ex) {
                        throw new InvalidKeyException("Failed to create public key from its X.509 encoded form" + ex.getMessage());
                    }
                } else if (wrappedKeyType == 2) {
                    try {
                        return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(encodedData));
                    } catch (InvalidKeySpecException ex2) {
                        throw new InvalidKeyException("Failed to create private key from its PKCS#8 encoded form" + ex2.getMessage());
                    }
                } else if (wrappedKeyType == 3) {
                    return new SecretKeySpec(encodedData, wrappedKeyAlgorithm);
                } else {
                    throw new InvalidParameterException("Unsupported wrappedKeyType: " + wrappedKeyType);
                }
            } catch (BadPaddingException | IllegalBlockSizeException ex3) {
                throw new InvalidKeyException("Failed to unwrap key" + ex3.getMessage());
            }
        } else {
            throw new NullPointerException("wrappedKey == null");
        }
    }

    /* access modifiers changed from: protected */
    public final int getKeymasterPurposeOverride() {
        return this.mKeymasterPurposeOverride;
    }

    /* access modifiers changed from: protected */
    public final void setKeymasterPurposeOverride(int keymasterPurpose) {
        this.mKeymasterPurposeOverride = keymasterPurpose;
    }

    /* access modifiers changed from: protected */
    public final boolean isEncrypting() {
        return this.mIsEncrypting;
    }

    @Override // android.security.keystore.KeyStoreCryptoOperation
    public final long getOperationHandle() {
        return this.mOperationHandle;
    }

    /* access modifiers changed from: protected */
    public final HwUniversalKeyStoreKey getKey() {
        return this.mKey;
    }

    /* access modifiers changed from: protected */
    public final void setKey(@NonNull HwUniversalKeyStoreKey key) {
        this.mKey = key;
    }

    /* access modifiers changed from: protected */
    @NonNull
    public final HwKeystoreManager getKeyStore() {
        return this.mKeyStore;
    }

    /* access modifiers changed from: protected */
    public final long getConsumedInputSizeBytes() {
        HwUniversalKeyStoreCryptoOperationStreamer hwUniversalKeyStoreCryptoOperationStreamer = this.mMainDataStreamer;
        if (hwUniversalKeyStoreCryptoOperationStreamer != null) {
            return hwUniversalKeyStoreCryptoOperationStreamer.getConsumedInputSizeBytes();
        }
        throw new IllegalStateException("Not inited");
    }

    /* access modifiers changed from: protected */
    public final long getProducedOutputSizeBytes() {
        HwUniversalKeyStoreCryptoOperationStreamer hwUniversalKeyStoreCryptoOperationStreamer = this.mMainDataStreamer;
        if (hwUniversalKeyStoreCryptoOperationStreamer != null) {
            return hwUniversalKeyStoreCryptoOperationStreamer.getProducedOutputSizeBytes();
        }
        throw new IllegalStateException("Not inited");
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    @CallSuper
    public void finalize() throws Throwable {
        HwKeystoreManager hwKeystoreManager;
        IBinder opToken = this.mOperationToken;
        if (!(opToken == null || (hwKeystoreManager = this.mKeyStore) == null)) {
            hwKeystoreManager.abort(opToken);
        }
        super.finalize();
    }
}
