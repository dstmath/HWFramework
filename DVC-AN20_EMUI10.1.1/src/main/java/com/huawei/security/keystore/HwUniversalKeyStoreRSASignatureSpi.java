package com.huawei.security.keystore;

import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.security.InvalidKeyException;

public class HwUniversalKeyStoreRSASignatureSpi extends HwUniversalKeyStoreSignatureSpiBase {
    private static final String TAG = "HwKeyStoreRSASignature";
    private int mKeymasterDigest = 4;
    private int mKeymasterPadding = 5;

    public HwUniversalKeyStoreRSASignatureSpi() {
    }

    HwUniversalKeyStoreRSASignatureSpi(int keymasterDigest, int keymasterPadding) {
        this.mKeymasterDigest = keymasterDigest;
        this.mKeymasterPadding = keymasterPadding;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    @CallSuper
    public void initKey(HwUniversalKeyStoreKey key) throws InvalidKeyException {
        if (HwKeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(key.getAlgorithm())) {
            this.mKey = key;
            return;
        }
        throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only" + HwKeyProperties.KEY_ALGORITHM_RSA + " supported");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
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
        this.mMessageStreamer = null;
        this.mOperationToken = null;
        this.mOperationHandle = 0;
        this.mCachedException = null;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
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

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments keymasterArgs) {
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, 1);
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public int getAdditionalEntropyAmountForSign() {
        return 0;
    }

    static class PKCS1Padding extends HwUniversalKeyStoreRSASignatureSpi {
        PKCS1Padding(int keymasterDigest) {
            super(keymasterDigest, 5);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase, com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi
        public final int getAdditionalEntropyAmountForSign() {
            return 0;
        }
    }

    public static final class SHA256WithPKCS1Padding extends PKCS1Padding {
        public SHA256WithPKCS1Padding() {
            super(4);
        }
    }

    public static final class SHA384WithPKCS1Padding extends PKCS1Padding {
        public SHA384WithPKCS1Padding() {
            super(5);
        }
    }

    public static final class SHA512WithPKCS1Padding extends PKCS1Padding {
        public SHA512WithPKCS1Padding() {
            super(6);
        }
    }

    public static final class NONEWithPKCS1Padding extends PKCS1Padding {
        public NONEWithPKCS1Padding() {
            super(0);
        }
    }

    static class PSSPadding extends HwUniversalKeyStoreRSASignatureSpi {
        private static final int SALT_LENGTH_BYTES = 20;

        PSSPadding(int keymasterDigest) {
            super(keymasterDigest, 3);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase, com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi
        public final int getAdditionalEntropyAmountForSign() {
            return 20;
        }
    }

    public static final class SHA256WithPSSPadding extends PSSPadding {
        public SHA256WithPSSPadding() {
            super(4);
        }
    }

    public static final class SHA384WithPSSPadding extends PSSPadding {
        public SHA384WithPSSPadding() {
            super(5);
        }
    }

    public static final class SHA512WithPSSPadding extends PSSPadding {
        public SHA512WithPSSPadding() {
            super(6);
        }
    }
}
