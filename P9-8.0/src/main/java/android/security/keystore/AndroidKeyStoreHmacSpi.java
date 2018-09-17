package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.MainDataStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;

public abstract class AndroidKeyStoreHmacSpi extends MacSpi implements KeyStoreCryptoOperation {
    private KeyStoreCryptoOperationChunkedStreamer mChunkedStreamer;
    private AndroidKeyStoreSecretKey mKey;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private final int mKeymasterDigest;
    private final int mMacSizeBits;
    private long mOperationHandle;
    private IBinder mOperationToken;

    public static class HmacSHA1 extends AndroidKeyStoreHmacSpi {
        public HmacSHA1() {
            super(2);
        }
    }

    public static class HmacSHA224 extends AndroidKeyStoreHmacSpi {
        public HmacSHA224() {
            super(3);
        }
    }

    public static class HmacSHA256 extends AndroidKeyStoreHmacSpi {
        public HmacSHA256() {
            super(4);
        }
    }

    public static class HmacSHA384 extends AndroidKeyStoreHmacSpi {
        public HmacSHA384() {
            super(5);
        }
    }

    public static class HmacSHA512 extends AndroidKeyStoreHmacSpi {
        public HmacSHA512() {
            super(6);
        }
    }

    protected AndroidKeyStoreHmacSpi(int keymasterDigest) {
        this.mKeymasterDigest = keymasterDigest;
        this.mMacSizeBits = KeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
    }

    protected int engineGetMacLength() {
        return (this.mMacSizeBits + 7) / 8;
    }

    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        resetAll();
        boolean success = false;
        try {
            init(key, params);
            ensureKeystoreOperationInitialized();
            success = true;
        } finally {
            if (!success) {
                resetAll();
            }
        }
    }

    private void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (key == null) {
            throw new InvalidKeyException("key == null");
        } else if (key instanceof AndroidKeyStoreSecretKey) {
            this.mKey = (AndroidKeyStoreSecretKey) key;
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unsupported algorithm parameters: " + params);
            }
        } else {
            throw new InvalidKeyException("Only Android KeyStore secret keys supported. Key: " + key);
        }
    }

    private void resetAll() {
        this.mKey = null;
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mChunkedStreamer = null;
    }

    private void resetWhilePreservingInitState() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mChunkedStreamer = null;
    }

    protected void engineReset() {
        resetWhilePreservingInitState();
    }

    private void ensureKeystoreOperationInitialized() throws InvalidKeyException {
        if (this.mChunkedStreamer == null) {
            if (this.mKey == null) {
                throw new IllegalStateException("Not initialized");
            }
            KeymasterArguments keymasterArgs = new KeymasterArguments();
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 128);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
            keymasterArgs.addUnsignedInt(KeymasterDefs.KM_TAG_MAC_LENGTH, (long) this.mMacSizeBits);
            OperationResult opResult = this.mKeyStore.begin(this.mKey.getAlias(), 2, true, keymasterArgs, null, this.mKey.getUid());
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
                this.mChunkedStreamer = new KeyStoreCryptoOperationChunkedStreamer(new MainDataStream(this.mKeyStore, this.mOperationToken));
            }
        }
    }

    protected void engineUpdate(byte input) {
        engineUpdate(new byte[]{input}, 0, 1);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        try {
            ensureKeystoreOperationInitialized();
            try {
                byte[] output = this.mChunkedStreamer.update(input, offset, len);
                if (output != null && output.length != 0) {
                    throw new ProviderException("Update operation unexpectedly produced output");
                }
            } catch (KeyStoreException e) {
                throw new ProviderException("Keystore operation failed", e);
            }
        } catch (InvalidKeyException e2) {
            throw new ProviderException("Failed to reinitialize MAC", e2);
        }
    }

    protected byte[] engineDoFinal() {
        try {
            ensureKeystoreOperationInitialized();
            try {
                byte[] result = this.mChunkedStreamer.doFinal(null, 0, 0, null, null);
                resetWhilePreservingInitState();
                return result;
            } catch (KeyStoreException e) {
                throw new ProviderException("Keystore operation failed", e);
            }
        } catch (InvalidKeyException e2) {
            throw new ProviderException("Failed to reinitialize MAC", e2);
        }
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

    public long getOperationHandle() {
        return this.mOperationHandle;
    }
}
