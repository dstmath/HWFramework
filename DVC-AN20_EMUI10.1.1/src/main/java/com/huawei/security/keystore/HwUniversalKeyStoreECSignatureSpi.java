package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.security.InvalidKeyException;

public class HwUniversalKeyStoreECSignatureSpi extends HwUniversalKeyStoreSignatureSpiBase {
    private int mGroupSizeBits = -1;
    private final int mKeymasterDigest;

    HwUniversalKeyStoreECSignatureSpi(int keymasterDigest) {
        this.mKeymasterDigest = keymasterDigest;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final void initKey(HwUniversalKeyStoreKey key) throws InvalidKeyException {
        if (HwKeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(key.getAlgorithm())) {
            HwKeyCharacteristics keyCharacteristics = new HwKeyCharacteristics();
            int errorCode = this.mKeyStore.getKeyCharacteristics(key.getAlias(), null, null, key.getUid(), keyCharacteristics);
            if (errorCode == 1) {
                long keySizeBits = keyCharacteristics.getUnsignedInt(HwKeymasterDefs.KM_TAG_KEY_SIZE, -1);
                if (keySizeBits == -1) {
                    throw new InvalidKeyException("Size of key not known");
                } else if (keySizeBits <= 2147483647L) {
                    this.mGroupSizeBits = (int) keySizeBits;
                    super.initKey(key);
                } else {
                    throw new InvalidKeyException("Key too large: " + keySizeBits + " bits");
                }
            } else {
                throw this.mKeyStore.getInvalidKeyException(key.getAlias(), key.getUid(), errorCode);
            }
        } else {
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only" + HwKeyProperties.KEY_ALGORITHM_EC + " supported");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final void resetAll() {
        this.mGroupSizeBits = -1;
        super.resetAll();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final void resetWhilePreservingInitState() {
        super.resetWhilePreservingInitState();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments keymasterArgs) {
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, 3);
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_PADDING, 1);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final int getAdditionalEntropyAmountForSign() {
        return (this.mGroupSizeBits + 7) / 8;
    }

    /* access modifiers changed from: protected */
    public final int getGroupSizeBits() {
        int i = this.mGroupSizeBits;
        if (i != -1) {
            return i;
        }
        throw new IllegalStateException("Not initialized");
    }

    public static final class SHA256 extends HwUniversalKeyStoreECSignatureSpi {
        public SHA256() {
            super(4);
        }
    }

    public static final class SHA384 extends HwUniversalKeyStoreECSignatureSpi {
        public SHA384() {
            super(5);
        }
    }

    public static final class SHA512 extends HwUniversalKeyStoreECSignatureSpi {
        public SHA512() {
            super(6);
        }
    }
}
