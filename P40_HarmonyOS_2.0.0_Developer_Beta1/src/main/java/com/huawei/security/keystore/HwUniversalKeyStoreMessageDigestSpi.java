package com.huawei.security.keystore;

import android.os.IBinder;
import android.security.keystore.KeyStoreCryptoOperation;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.HwUniversalKeyStoreCryptoOperationStreamer;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.MessageDigestSpi;
import java.security.ProviderException;

public abstract class HwUniversalKeyStoreMessageDigestSpi extends MessageDigestSpi implements KeyStoreCryptoOperation {
    private HwUniversalKeyStoreCryptoOperationStreamer mChunkedStreamer;
    private final HwKeystoreManager mKeyStore = HwKeystoreManager.getInstance();
    private final int mKeymasterDigest;
    private long mOperationHandle;
    private IBinder mOperationToken;

    protected HwUniversalKeyStoreMessageDigestSpi(int keymasterDigest) {
        this.mKeymasterDigest = keymasterDigest;
        this.mChunkedStreamer = null;
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

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public int engineGetDigestLength() {
        int i = this.mKeymasterDigest;
        if (i == 4) {
            return 32;
        }
        if (i == 5) {
            return 48;
        }
        if (i == 6) {
            return 64;
        }
        if (i == 7) {
            return 32;
        }
        throw new ProviderException("Unsupported Digest Type " + this.mKeymasterDigest);
    }

    private void ensureKeystoreOperationInitialized() throws InvalidKeyException {
        if (this.mChunkedStreamer == null) {
            HwKeymasterArguments keymasterArgs = new HwKeymasterArguments();
            keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, HwKeymasterDefs.KM_ALGORITHM_DIGEST);
            keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
            HwOperationResult opResult = this.mKeyStore.begin(null, 2, true, keymasterArgs, null, 0);
            if (opResult != null) {
                this.mOperationToken = opResult.token;
                this.mOperationHandle = opResult.operationHandle;
                if (opResult.resultCode == 1) {
                    IBinder iBinder = this.mOperationToken;
                    if (iBinder == null) {
                        throw new ProviderException("HwKeyStore returned null operation token");
                    } else if (this.mOperationHandle != 0) {
                        this.mChunkedStreamer = new HwUniversalKeyStoreCryptoOperationStreamer(new HwUniversalKeyStoreCryptoOperationStreamer.MainDataStream(this.mKeyStore, iBinder));
                    } else {
                        throw new ProviderException("HwKeyStore returned invalid operation handle");
                    }
                } else {
                    throw new ProviderException("Failed to communicate with HwKeyStore service by result code");
                }
            } else {
                throw new ProviderException("Failed to communicate with HwKeyStore service");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte[] in, int offset, int len) {
        try {
            ensureKeystoreOperationInitialized();
            try {
                byte[] out = this.mChunkedStreamer.update(in, offset, len);
                if (out != null && out.length != 0) {
                    throw new ProviderException("Update operation unexpectedly out put");
                }
            } catch (HwUniversalKeyStoreException ex) {
                throw new ProviderException("HwKeyStore operation failed", ex);
            }
        } catch (InvalidKeyException ex2) {
            throw new ProviderException("Failed to reinitialize Digest Engine", ex2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte in) {
        engineUpdate(new byte[]{in}, 0, 1);
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineUpdate(ByteBuffer in) {
        if (in != null) {
            int inSize = in.remaining();
            if (in.hasArray()) {
                engineUpdate(in.array(), in.arrayOffset() + in.position(), inSize);
                in.position(in.position() + inSize);
                return;
            }
            byte[] inArray = new byte[inSize];
            in.get(inArray);
            engineUpdate(inArray, 0, inSize);
            return;
        }
        throw new NullPointerException("in put == null");
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public byte[] engineDigest() {
        try {
            ensureKeystoreOperationInitialized();
            try {
                byte[] returnVal = this.mChunkedStreamer.doFinal(null, 0, 0, null, null);
                resetWhilePreservingInitState();
                return returnVal;
            } catch (HwUniversalKeyStoreException ex) {
                throw new ProviderException("HwKeyStore operation failed", ex);
            }
        } catch (InvalidKeyException ex2) {
            throw new ProviderException("Failed to reinitialize Digest Engine", ex2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        try {
            byte[] outCopy = engineDigest();
            if (outCopy == null || outCopy.length == 0) {
                return 0;
            }
            int outAvailable = buf.length - offset;
            if (outAvailable < len) {
                throw new DigestException("Output Available buffer < len param. len: " + len + ", available: " + outAvailable);
            } else if (outCopy.length <= len) {
                System.arraycopy(outCopy, 0, buf, offset, outCopy.length);
                return outCopy.length;
            } else {
                throw new DigestException("Output buffer too short. Produced: " + outCopy.length + ", available: " + outAvailable);
            }
        } catch (ProviderException ex) {
            throw new DigestException(ex);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.security.MessageDigestSpi
    public void engineReset() {
        resetWhilePreservingInitState();
    }

    @Override // android.security.keystore.KeyStoreCryptoOperation
    public long getOperationHandle() {
        return this.mOperationHandle;
    }

    public static class SHA256 extends HwUniversalKeyStoreMessageDigestSpi {
        public SHA256() {
            super(4);
        }
    }

    public static class SHA384 extends HwUniversalKeyStoreMessageDigestSpi {
        public SHA384() {
            super(5);
        }
    }

    public static class SHA512 extends HwUniversalKeyStoreMessageDigestSpi {
        public SHA512() {
            super(6);
        }
    }

    public static class SM3 extends HwUniversalKeyStoreMessageDigestSpi {
        public SM3() {
            super(7);
        }
    }
}
