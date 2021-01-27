package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;

public class HwGmKeyStoreSM4CipherSpi extends HwUniversalKeyStoreUnauthenticatedAESCipherSpi {
    private static final int KEY_SIZE_BYTES = 16;

    HwGmKeyStoreSM4CipherSpi(int keymasterBlockMode, int keymasterPadding, boolean ivRequired) {
        super(keymasterBlockMode, keymasterPadding, ivRequired);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreUnauthenticatedAESCipherSpi, com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments keymasterArgs) {
        super.addAlgorithmSpecificParametersToBegin(keymasterArgs);
        keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
    }

    static abstract class ECB extends HwGmKeyStoreSM4CipherSpi {
        protected ECB(int keymasterPadding) {
            super(1, keymasterPadding, false);
        }

        public static class NoPadding extends ECB {
            public NoPadding() {
                super(1);
            }
        }

        public static class PKCS7Padding extends ECB {
            public PKCS7Padding() {
                super(64);
            }
        }
    }

    static abstract class CBC extends HwGmKeyStoreSM4CipherSpi {
        protected CBC(int keymasterPadding) {
            super(2, keymasterPadding, true);
        }

        public static class NoPadding extends CBC {
            public NoPadding() {
                super(1);
            }
        }

        public static class PKCS7Padding extends CBC {
            public PKCS7Padding() {
                super(64);
            }
        }
    }

    static abstract class CTR extends HwGmKeyStoreSM4CipherSpi {
        protected CTR(int keymasterPadding) {
            super(3, keymasterPadding, true);
        }

        public static class NoPadding extends CTR {
            public NoPadding() {
                super(1);
            }
        }
    }
}
