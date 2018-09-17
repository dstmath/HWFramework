package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.MainDataStream;
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

abstract class AndroidKeyStoreSignatureSpiBase extends SignatureSpi implements KeyStoreCryptoOperation {
    private Exception mCachedException;
    private AndroidKeyStoreKey mKey;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private KeyStoreCryptoOperationStreamer mMessageStreamer;
    private long mOperationHandle;
    private IBinder mOperationToken;
    private boolean mSigning;

    protected abstract void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArguments);

    protected abstract int getAdditionalEntropyAmountForSign();

    AndroidKeyStoreSignatureSpiBase() {
    }

    protected final void engineInitSign(PrivateKey key) throws InvalidKeyException {
        engineInitSign(key, null);
    }

    protected final void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        resetAll();
        if (privateKey == null) {
            try {
                throw new InvalidKeyException("Unsupported key: null");
            } catch (Throwable th) {
                if (!false) {
                    resetAll();
                }
            }
        } else if (privateKey instanceof AndroidKeyStorePrivateKey) {
            AndroidKeyStoreKey keystoreKey = (AndroidKeyStoreKey) privateKey;
            this.mSigning = true;
            initKey(keystoreKey);
            this.appRandom = random;
            ensureKeystoreOperationInitialized();
            if (!true) {
                resetAll();
            }
        } else {
            throw new InvalidKeyException("Unsupported private key type: " + privateKey);
        }
    }

    protected final void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        resetAll();
        if (publicKey == null) {
            try {
                throw new InvalidKeyException("Unsupported key: null");
            } catch (Throwable th) {
                if (!false) {
                    resetAll();
                }
            }
        } else if (publicKey instanceof AndroidKeyStorePublicKey) {
            AndroidKeyStoreKey keystoreKey = (AndroidKeyStorePublicKey) publicKey;
            this.mSigning = false;
            initKey(keystoreKey);
            this.appRandom = null;
            ensureKeystoreOperationInitialized();
            if (!true) {
                resetAll();
            }
        } else {
            throw new InvalidKeyException("Unsupported public key type: " + publicKey);
        }
    }

    protected void initKey(AndroidKeyStoreKey key) throws InvalidKeyException {
        this.mKey = key;
    }

    protected void resetAll() {
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

    protected void resetWhilePreservingInitState() {
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
        if (this.mKey == null) {
            throw new IllegalStateException("Not initialized");
        }
        KeymasterArguments keymasterInputArgs = new KeymasterArguments();
        addAlgorithmSpecificParametersToBegin(keymasterInputArgs);
        OperationResult opResult = this.mKeyStore.begin(this.mKey.getAlias(), this.mSigning ? 2 : 3, true, keymasterInputArgs, null, this.mKey.getUid());
        if (opResult == null) {
            throw new KeyStoreConnectException();
        }
        this.mOperationToken = opResult.token;
        this.mOperationHandle = opResult.operationHandle;
        InvalidKeyException e = KeyStoreCryptoOperationUtils.getInvalidKeyExceptionForInit(this.mKeyStore, this.mKey, opResult.resultCode);
        if (e != null) {
            throw e;
        } else if (this.mOperationToken == null) {
            throw new ProviderException("Keystore returned null operation token");
        } else if (this.mOperationHandle == 0) {
            throw new ProviderException("Keystore returned invalid operation handle");
        } else {
            this.mMessageStreamer = createMainDataStreamer(this.mKeyStore, opResult.token);
        }
    }

    protected KeyStoreCryptoOperationStreamer createMainDataStreamer(KeyStore keyStore, IBinder operationToken) {
        return new KeyStoreCryptoOperationChunkedStreamer(new MainDataStream(keyStore, operationToken));
    }

    public final long getOperationHandle() {
        return this.mOperationHandle;
    }

    protected final void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        if (this.mCachedException != null) {
            throw new SignatureException(this.mCachedException);
        }
        try {
            ensureKeystoreOperationInitialized();
            if (len != 0) {
                try {
                    byte[] output = this.mMessageStreamer.update(b, off, len);
                    if (output.length != 0) {
                        throw new ProviderException("Update operation unexpectedly produced output: " + output.length + " bytes");
                    }
                } catch (KeyStoreException e) {
                    throw new SignatureException(e);
                }
            }
        } catch (InvalidKeyException e2) {
            throw new SignatureException(e2);
        }
    }

    protected final void engineUpdate(byte b) throws SignatureException {
        engineUpdate(new byte[]{b}, 0, 1);
    }

    protected final void engineUpdate(ByteBuffer input) {
        byte[] b;
        int off;
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

    protected final int engineSign(byte[] out, int outOffset, int outLen) throws SignatureException {
        return super.engineSign(out, outOffset, outLen);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0028 A:{Splitter: B:4:0x000c, ExcHandler: java.security.InvalidKeyException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0028, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x002e, code:
            throw new java.security.SignatureException(r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final byte[] engineSign() throws SignatureException {
        if (this.mCachedException != null) {
            throw new SignatureException(this.mCachedException);
        }
        try {
            ensureKeystoreOperationInitialized();
            byte[] signature = this.mMessageStreamer.doFinal(EmptyArray.BYTE, 0, 0, null, KeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.appRandom, getAdditionalEntropyAmountForSign()));
            resetWhilePreservingInitState();
            return signature;
        } catch (Exception e) {
        }
    }

    protected final boolean engineVerify(byte[] signature) throws SignatureException {
        if (this.mCachedException != null) {
            throw new SignatureException(this.mCachedException);
        }
        try {
            ensureKeystoreOperationInitialized();
            boolean verified;
            try {
                byte[] output = this.mMessageStreamer.doFinal(EmptyArray.BYTE, 0, 0, signature, null);
                if (output.length != 0) {
                    throw new ProviderException("Signature verification unexpected produced output: " + output.length + " bytes");
                }
                verified = true;
                resetWhilePreservingInitState();
                return verified;
            } catch (KeyStoreException e) {
                switch (e.getErrorCode()) {
                    case KeymasterDefs.KM_ERROR_VERIFICATION_FAILED /*-30*/:
                        verified = false;
                        break;
                    default:
                        throw new SignatureException(e);
                }
            }
        } catch (InvalidKeyException e2) {
            throw new SignatureException(e2);
        }
    }

    protected final boolean engineVerify(byte[] sigBytes, int offset, int len) throws SignatureException {
        return engineVerify(ArrayUtils.subarray(sigBytes, offset, len));
    }

    @Deprecated
    protected final Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException();
    }

    @Deprecated
    protected final void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new InvalidParameterException();
    }

    protected final KeyStore getKeyStore() {
        return this.mKeyStore;
    }

    protected final boolean isSigning() {
        return this.mSigning;
    }
}
