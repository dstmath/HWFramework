package ohos.security.keystore.provider;

import java.security.AlgorithmParameters;

abstract class HarmonyKeyStoreRSACipherSpi extends HarmonyKeyStoreCipherSpiBase {

    public static final class NoPadding extends HarmonyKeyStoreRSACipherSpi {
        /* access modifiers changed from: protected */
        @Override // ohos.security.keystore.provider.HarmonyKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public AlgorithmParameters engineGetParameters() {
            return null;
        }
    }

    public static class OAEPWithSHA1AndMGF1Padding extends OAEPWithMGF1Padding {
    }

    public static class OAEPWithSHA224AndMGF1Padding extends OAEPWithMGF1Padding {
    }

    public static class OAEPWithSHA256AndMGF1Padding extends OAEPWithMGF1Padding {
    }

    public static class OAEPWithSHA384AndMGF1Padding extends OAEPWithMGF1Padding {
    }

    public static class OAEPWithSHA512AndMGF1Padding extends OAEPWithMGF1Padding {
    }

    public static final class PKCS1Padding extends HarmonyKeyStoreRSACipherSpi {
        /* access modifiers changed from: protected */
        @Override // ohos.security.keystore.provider.HarmonyKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public AlgorithmParameters engineGetParameters() {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return new byte[0];
    }

    static abstract class OAEPWithMGF1Padding extends HarmonyKeyStoreRSACipherSpi {
        OAEPWithMGF1Padding() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.security.keystore.provider.HarmonyKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public final AlgorithmParameters engineGetParameters() {
            return (AlgorithmParameters) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[0], new Object[0], AlgorithmParameters.class).getResult();
        }
    }

    HarmonyKeyStoreRSACipherSpi() {
        this.androidKeyStoreCipherSpi = ReflectUtil.getInstance("android.security.keystore.AndroidKeyStoreRSACipherSpi" + TransferUtils.getInnerClassName(getClass().getName()));
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetOutputSize(int i) {
        return ((Integer) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(i)}, Integer.class).getResult()).intValue();
    }
}
