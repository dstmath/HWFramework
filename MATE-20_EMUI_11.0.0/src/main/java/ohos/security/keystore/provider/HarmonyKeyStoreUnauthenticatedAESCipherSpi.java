package ohos.security.keystore.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AlgorithmParameters;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

class HarmonyKeyStoreUnauthenticatedAESCipherSpi extends HarmonyKeyStoreCipherSpiBase {
    private static final int BLOCK_FACTOR = 3;
    private static final int BLOCK_SIZE_BYTES = 16;
    private static final HiLogLabel LABEL = KeyStoreLogger.getLabel(TAG);
    private static final String TAG = "HarmonyKeyStoreUnauthenticatedAESCipherSpi";

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 16;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetOutputSize(int i) {
        return i + 48;
    }

    static abstract class ECB extends HarmonyKeyStoreUnauthenticatedAESCipherSpi {
        protected ECB(int i) {
            super(1, i, false);
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

    static abstract class CBC extends HarmonyKeyStoreUnauthenticatedAESCipherSpi {
        protected CBC(int i) {
            super(2, i, true);
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

    static abstract class CTR extends HarmonyKeyStoreUnauthenticatedAESCipherSpi {
        protected CTR(int i) {
            super(3, i, true);
        }

        public static class NoPadding extends CTR {
            public NoPadding() {
                super(1);
            }
        }
    }

    HarmonyKeyStoreUnauthenticatedAESCipherSpi(int i, int i2, boolean z) {
        try {
            Constructor<?> declaredConstructor = Class.forName("android.security.keystore.AndroidKeyStoreUnauthenticatedAESCipherSpi").getDeclaredConstructor(Integer.TYPE, Integer.TYPE, Boolean.TYPE);
            declaredConstructor.setAccessible(true);
            this.androidKeyStoreCipherSpi = declaredConstructor.newInstance(Integer.valueOf(i), Integer.valueOf(i2), Boolean.valueOf(z));
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.error(LABEL, "constructor error", new Object[0]);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return (byte[]) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[0], new Object[0], byte[].class).getResult();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.security.keystore.provider.HarmonyKeyStoreCipherSpiBase, javax.crypto.CipherSpi
    public final AlgorithmParameters engineGetParameters() {
        return (AlgorithmParameters) ReflectUtil.invoke(this.androidKeyStoreCipherSpi, new Class[0], new Object[0], AlgorithmParameters.class).getResult();
    }
}
