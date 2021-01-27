package com.huawei.security.keystore;

import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;

public class HwGmKeyStoreKeyPairGeneratorSpi extends HwUniversalKeyStoreKeyPairGeneratorSpi {
    protected HwGmKeyStoreKeyPairGeneratorSpi(int keymasterAlgorithm) {
        super(keymasterAlgorithm);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi
    public void addExtraParameters(HwKeymasterArguments keymasterArgs) {
        super.addExtraParameters(keymasterArgs);
        keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
    }

    public static class SM2 extends HwGmKeyStoreKeyPairGeneratorSpi {
        public SM2() {
            super(3);
        }
    }
}
