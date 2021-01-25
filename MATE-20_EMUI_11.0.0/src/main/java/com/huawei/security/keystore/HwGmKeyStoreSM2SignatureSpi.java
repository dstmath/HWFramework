package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;

public class HwGmKeyStoreSM2SignatureSpi extends HwUniversalKeyStoreECSignatureSpi {
    HwGmKeyStoreSM2SignatureSpi(int keymasterDigest) {
        super(keymasterDigest);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreECSignatureSpi, com.huawei.security.keystore.HwUniversalKeyStoreSignatureSpiBase
    public final void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments keymasterArgs) {
        super.addAlgorithmSpecificParametersToBegin(keymasterArgs);
        keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
    }

    public static final class SM3 extends HwGmKeyStoreSM2SignatureSpi {
        public SM3() {
            super(7);
        }
    }
}
