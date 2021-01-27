package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreHmacSpi;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class HarmonyKeyStoreHmacSpi extends AndroidKeyStoreHmacSpi {

    public static class HmacSHA1 extends HarmonyKeyStoreHmacSpi {
        public HmacSHA1() {
            super(2);
        }
    }

    public static class HmacSHA224 extends HarmonyKeyStoreHmacSpi {
        public HmacSHA224() {
            super(3);
        }
    }

    public static class HmacSHA256 extends HarmonyKeyStoreHmacSpi {
        public HmacSHA256() {
            super(4);
        }
    }

    public static class HmacSHA384 extends HarmonyKeyStoreHmacSpi {
        public HmacSHA384() {
            super(5);
        }
    }

    public static class HmacSHA512 extends HarmonyKeyStoreHmacSpi {
        public HmacSHA512() {
            super(6);
        }
    }

    protected HarmonyKeyStoreHmacSpi(int i) {
        super(i);
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        HarmonyKeyStoreHmacSpi.super.engineInit(TransferUtils.toAndroidKey(key), algorithmParameterSpec);
    }
}
