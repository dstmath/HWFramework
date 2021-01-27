package ohos.security.keystore.provider;

import java.security.AlgorithmParameters;

abstract class HarmonyKeyStoreAuthenticatedAESCipherSpi extends HarmonyKeyStoreCipherSpiBase {
    private static final int BLOCK_SIZE_BYTES = 16;

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 16;
    }

    static abstract class GCM extends HarmonyKeyStoreAuthenticatedAESCipherSpi {
        GCM() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.security.keystore.provider.HarmonyKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public final AlgorithmParameters engineGetParameters() {
            return (AlgorithmParameters) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[0], new Object[0], AlgorithmParameters.class).getResult();
        }

        public static final class NoPadding extends GCM {
            /* access modifiers changed from: protected */
            @Override // javax.crypto.CipherSpi
            public int engineGetOutputSize(int i) {
                return ((Integer) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(i)}, Integer.class).getResult()).intValue();
            }
        }
    }

    HarmonyKeyStoreAuthenticatedAESCipherSpi() {
        this.androidKeyStoreCipherSpi = ReflectUtil.getInstance("android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi" + TransferUtils.getInnerClassName(getClass().getName()));
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return (byte[]) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[0], new Object[0], byte[].class).getResult();
    }
}
