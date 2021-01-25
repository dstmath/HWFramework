package com.huawei.security.keystore;

import android.os.IBinder;
import android.security.keystore.KeyStoreCryptoOperation;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwKeymasterUtils;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.HwUniversalKeyStoreCryptoOperationStreamer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;

public abstract class HwUniversalKeyStoreHmacSpi extends MacSpi implements KeyStoreCryptoOperation {
    private HwUniversalKeyStoreCryptoOperationStreamer mChunkedStreamer;
    private HwUniversalKeyStoreSecretKey mKey;
    private final HwKeystoreManager mKeyStore = HwKeystoreManager.getInstance();
    private final int mKeymasterDigest;
    private final int mMacSizeBits;
    private long mOperationHandle;
    private IBinder mOperationToken;

    protected HwUniversalKeyStoreHmacSpi(int keymasterDigest) {
        this.mKeymasterDigest = keymasterDigest;
        this.mMacSizeBits = HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.MacSpi
    public int engineGetMacLength() {
        return (this.mMacSizeBits + 7) / 8;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.MacSpi
    public void engineInit(Key key, AlgorithmParameterSpec algParams) throws InvalidKeyException, InvalidAlgorithmParameterException {
        resetAll();
        boolean flag = false;
        try {
            init(key, algParams);
            ensureKeystoreOperationInitialized();
            flag = true;
        } finally {
            if (!flag) {
                resetAll();
            }
        }
    }

    private void init(Key key, AlgorithmParameterSpec algParams) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (key == null) {
            throw new InvalidKeyException("key == null");
        } else if (key instanceof HwUniversalKeyStoreSecretKey) {
            this.mKey = (HwUniversalKeyStoreSecretKey) key;
            if (algParams != null) {
                throw new InvalidAlgorithmParameterException("Unsupported algorithm parameters: " + algParams);
            }
        } else {
            throw new InvalidKeyException("Only HwUniversal KeyStore secret keys supported.");
        }
    }

    private void resetAll() {
        this.mKey = null;
        resetWhilePreservingInitState();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.MacSpi
    public void engineReset() {
        resetWhilePreservingInitState();
    }

    private void resetWhilePreservingInitState() {
        IBinder operationToken = this.mOperationToken;
        if (operationToken != null) {
            this.mKeyStore.abort(operationToken);
        }
        this.mChunkedStreamer = null;
        this.mOperationToken = null;
        this.mOperationHandle = 0;
    }

    private void ensureKeystoreOperationInitialized() throws InvalidKeyException {
        if (this.mChunkedStreamer == null) {
            if (this.mKey != null) {
                HwKeymasterArguments keymasterArgs = new HwKeymasterArguments();
                keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, 128);
                keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
                keymasterArgs.addUnsignedInt(HwKeymasterDefs.KM_TAG_MAC_LENGTH, (long) this.mMacSizeBits);
                HwOperationResult opResult = this.mKeyStore.begin(this.mKey.getAlias(), 2, true, keymasterArgs, null, this.mKey.getUid());
                if (opResult != null) {
                    this.mOperationToken = opResult.token;
                    this.mOperationHandle = opResult.operationHandle;
                    InvalidKeyException e = HwUniversalKeyStoreCryptoOperationUtils.getInvalidKeyExceptionForInit(this.mKeyStore, this.mKey, opResult.resultCode);
                    if (e == null) {
                        IBinder iBinder = this.mOperationToken;
                        if (iBinder == null) {
                            throw new ProviderException("HwKeyStore returned null operation token");
                        } else if (this.mOperationHandle != 0) {
                            this.mChunkedStreamer = new HwUniversalKeyStoreCryptoOperationStreamer(new HwUniversalKeyStoreCryptoOperationStreamer.MainDataStream(this.mKeyStore, iBinder));
                        } else {
                            throw new ProviderException("HwKeyStore returned invalid operation handle");
                        }
                    } else {
                        throw e;
                    }
                } else {
                    throw new ProviderException("Failed to communicate with HwKeyStore service");
                }
            } else {
                throw new IllegalStateException("Key Not initialized");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.MacSpi
    public void engineUpdate(byte[] in, int offset, int len) {
        try {
            ensureKeystoreOperationInitialized();
            try {
                byte[] out = this.mChunkedStreamer.update(in, offset, len);
                if (out != null && out.length != 0) {
                    throw new ProviderException("Update operation unexpectedly out put");
                }
            } catch (HwUniversalKeyStoreException e) {
                throw new ProviderException("HwKeystore operation failed", e);
            }
        } catch (InvalidKeyException e2) {
            throw new ProviderException("Failed to reinitialize MAC engine", e2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.MacSpi
    public void engineUpdate(byte in) {
        engineUpdate(new byte[]{in}, 0, 1);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.MacSpi
    public byte[] engineDoFinal() {
        try {
            ensureKeystoreOperationInitialized();
            try {
                byte[] returnVal = this.mChunkedStreamer.doFinal(null, 0, 0, null, null);
                resetWhilePreservingInitState();
                return returnVal;
            } catch (HwUniversalKeyStoreException ex) {
                throw new ProviderException("HwKeystore operation failed", ex);
            }
        } catch (InvalidKeyException ex2) {
            throw new ProviderException("Failed to reinitialize HMAC", ex2);
        }
    }

    @Override // android.security.keystore.KeyStoreCryptoOperation
    public long getOperationHandle() {
        return this.mOperationHandle;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        HwKeystoreManager hwKeystoreManager;
        IBinder opToken = this.mOperationToken;
        if (!(opToken == null || (hwKeystoreManager = this.mKeyStore) == null)) {
            hwKeystoreManager.abort(opToken);
        }
        super.finalize();
    }

    public static class HmacSHA256 extends HwUniversalKeyStoreHmacSpi {
        public HmacSHA256() {
            super(4);
        }
    }

    public static class HmacSHA384 extends HwUniversalKeyStoreHmacSpi {
        public HmacSHA384() {
            super(5);
        }
    }

    public static class HmacSHA512 extends HwUniversalKeyStoreHmacSpi {
        public HmacSHA512() {
            super(6);
        }
    }

    public static class HmacSM3 extends HwUniversalKeyStoreHmacSpi {
        public HmacSM3() {
            super(7);
        }
    }
}
