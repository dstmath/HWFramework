package com.huawei.security.keystore;

import android.os.IBinder;
import android.security.keystore.KeyStoreCryptoOperation;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.HwUniversalKeyStoreCryptoOperationStreamer;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import libcore.util.EmptyArray;

abstract class HwUniversalKeyStoreSignatureSpiBase extends SignatureSpi implements KeyStoreCryptoOperation {
    protected Exception mCachedException;
    protected HwUniversalKeyStoreKey mKey;
    protected final HwKeystoreManager mKeyStore = HwKeystoreManager.getInstance();
    protected HwUniversalKeyStoreCryptoOperationStreamer mMessageStreamer;
    protected long mOperationHandle;
    protected IBinder mOperationToken;
    protected boolean mSigning;

    /* access modifiers changed from: protected */
    public abstract void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments hwKeymasterArguments);

    /* access modifiers changed from: protected */
    public abstract int getAdditionalEntropyAmountForSign();

    HwUniversalKeyStoreSignatureSpiBase() {
    }

    /* access modifiers changed from: protected */
    public final void engineInitSign(PrivateKey key) throws InvalidKeyException {
        engineInitSign(key, null);
    }

    /* access modifiers changed from: protected */
    public final void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        resetAll();
        boolean success = false;
        if (privateKey != null) {
            try {
                if (privateKey instanceof HwUniversalKeyStorePrivateKey) {
                    this.mSigning = true;
                    initKey((HwUniversalKeyStoreKey) privateKey);
                    this.appRandom = random;
                    ensureKeystoreOperationInitialized();
                    success = true;
                    if (success) {
                        return;
                    }
                    return;
                }
                throw new InvalidKeyException("Unsupported private key type: " + privateKey);
            } finally {
                if (!success) {
                    resetAll();
                }
            }
        } else {
            throw new InvalidKeyException("Unsupported key: null");
        }
    }

    /* access modifiers changed from: protected */
    public final void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        resetAll();
        boolean success = false;
        if (publicKey != null) {
            try {
                if (publicKey instanceof HwUniversalKeyStorePublicKey) {
                    this.mSigning = false;
                    initKey((HwUniversalKeyStorePublicKey) publicKey);
                    this.appRandom = null;
                    ensureKeystoreOperationInitialized();
                    success = true;
                    if (success) {
                        return;
                    }
                    return;
                }
                throw new InvalidKeyException("Unsupported public key type: " + publicKey);
            } finally {
                if (!success) {
                    resetAll();
                }
            }
        } else {
            throw new InvalidKeyException("Unsupported key: null");
        }
    }

    /* access modifiers changed from: protected */
    @CallSuper
    public void initKey(HwUniversalKeyStoreKey key) throws InvalidKeyException {
        this.mKey = key;
    }

    /* access modifiers changed from: protected */
    @CallSuper
    public void resetAll() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mOperationToken = null;
            this.mKeyStore.abort(operationToken);
        }
        this.mSigning = false;
        this.mKey = null;
        this.appRandom = null;
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mMessageStreamer = null;
        this.mCachedException = null;
    }

    /* access modifiers changed from: protected */
    @CallSuper
    public void resetWhilePreservingInitState() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mOperationToken = null;
            this.mKeyStore.abort(operationToken);
        }
        this.mOperationHandle = 0;
        this.mMessageStreamer = null;
        this.mCachedException = null;
    }

    private void ensureKeystoreOperationInitialized() throws InvalidKeyException {
        if (this.mMessageStreamer != null || this.mCachedException != null) {
            return;
        }
        if (this.mKey != null) {
            HwKeymasterArguments keymasterInputArgs = new HwKeymasterArguments();
            addAlgorithmSpecificParametersToBegin(keymasterInputArgs);
            HwOperationResult opResult = this.mKeyStore.begin(this.mKey.getAlias(), this.mSigning ? 2 : 3, true, keymasterInputArgs, null, this.mKey.getUid());
            if (opResult != null) {
                this.mOperationToken = opResult.token;
                this.mOperationHandle = opResult.operationHandle;
                InvalidKeyException e = HwUniversalKeyStoreCryptoOperationUtils.getInvalidKeyExceptionForInit(this.mKeyStore, this.mKey, opResult.resultCode);
                if (e != null) {
                    throw e;
                } else if (this.mOperationToken == null) {
                    throw new ProviderException("Keystore returned null operation token");
                } else if (this.mOperationHandle != 0) {
                    this.mMessageStreamer = createMainDataStreamer(this.mKeyStore, opResult.token);
                } else {
                    throw new ProviderException("Keystore returned invalid operation handle");
                }
            } else {
                throw new ProviderException("Failed to communicate with keystore service");
            }
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }

    /* access modifiers changed from: protected */
    @NonNull
    public HwUniversalKeyStoreCryptoOperationStreamer createMainDataStreamer(HwKeystoreManager keyStore, IBinder operationToken) {
        return new HwUniversalKeyStoreCryptoOperationStreamer(new HwUniversalKeyStoreCryptoOperationStreamer.MainDataStream(keyStore, operationToken));
    }

    public final long getOperationHandle() {
        return this.mOperationHandle;
    }

    /* access modifiers changed from: protected */
    public final void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        if (this.mCachedException == null) {
            try {
                ensureKeystoreOperationInitialized();
                if (len != 0) {
                    try {
                        byte[] output = this.mMessageStreamer.update(b, off, len);
                        if (output.length != 0) {
                            throw new ProviderException("Update operation unexpectedly produced output: " + output.length + " bytes");
                        }
                    } catch (HwUniversalKeyStoreException e) {
                        throw new SignatureException(e);
                    }
                }
            } catch (InvalidKeyException e2) {
                throw new SignatureException(e2);
            }
        } else {
            throw new SignatureException(this.mCachedException);
        }
    }

    /* access modifiers changed from: protected */
    public final void engineUpdate(byte b) throws SignatureException {
        engineUpdate(new byte[]{b}, 0, 1);
    }

    /* access modifiers changed from: protected */
    public final void engineUpdate(ByteBuffer input) {
        int off;
        byte[] b;
        int len = input.remaining();
        if (input.hasArray()) {
            b = input.array();
            off = input.arrayOffset() + input.position();
            input.position(input.limit());
        } else {
            b = new byte[len];
            off = 0;
            input.get(b);
        }
        try {
            engineUpdate(b, off, len);
        } catch (SignatureException e) {
            this.mCachedException = e;
        }
    }

    /* access modifiers changed from: protected */
    public final int engineSign(byte[] out, int outOffset, int outLen) throws SignatureException {
        return super.engineSign(out, outOffset, outLen);
    }

    /* access modifiers changed from: protected */
    public final byte[] engineSign() throws SignatureException {
        if (this.mCachedException == null) {
            try {
                ensureKeystoreOperationInitialized();
                byte[] signature = this.mMessageStreamer.doFinal(EmptyArray.BYTE, 0, 0, null, HwUniversalKeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.appRandom, getAdditionalEntropyAmountForSign()));
                resetWhilePreservingInitState();
                return signature;
            } catch (HwUniversalKeyStoreException | InvalidKeyException e) {
                throw new SignatureException(e);
            }
        } else {
            throw new SignatureException(this.mCachedException);
        }
    }

    /* access modifiers changed from: protected */
    public final boolean engineVerify(byte[] signature) throws SignatureException {
        boolean verified;
        if (this.mCachedException == null) {
            try {
                ensureKeystoreOperationInitialized();
                try {
                    if (this.mMessageStreamer.doFinal(EmptyArray.BYTE, 0, 0, signature, null).length == 0) {
                        verified = true;
                        resetWhilePreservingInitState();
                        return verified;
                    }
                    throw new ProviderException("Signature verification unexpected produced output: " + output.length + " bytes");
                } catch (HwUniversalKeyStoreException e) {
                    if (e.getErrorCode() == -30) {
                        verified = false;
                    } else {
                        throw new SignatureException(e);
                    }
                }
            } catch (InvalidKeyException e2) {
                throw new SignatureException(e2);
            }
        } else {
            throw new SignatureException(this.mCachedException);
        }
    }

    /* access modifiers changed from: protected */
    public final boolean engineVerify(byte[] sigBytes, int offset, int len) throws SignatureException {
        return engineVerify(ArrayUtils.subarray(sigBytes, offset, len));
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public final Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException();
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public final void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new InvalidParameterException();
    }

    /* access modifiers changed from: protected */
    public final boolean isSigning() {
        return this.mSigning;
    }
}
