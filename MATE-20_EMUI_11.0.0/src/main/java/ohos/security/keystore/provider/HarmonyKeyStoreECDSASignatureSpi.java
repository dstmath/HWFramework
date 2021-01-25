package ohos.security.keystore.provider;

abstract class HarmonyKeyStoreECDSASignatureSpi extends HarmonyKeyStoreSignatureSpiBase {
    private static final String TAG = "HarmonyKeyStoreECDSASign";

    public static final class NONE extends HarmonyKeyStoreECDSASignatureSpi {
    }

    public static final class SHA1 extends HarmonyKeyStoreECDSASignatureSpi {
    }

    public static final class SHA224 extends HarmonyKeyStoreECDSASignatureSpi {
    }

    public static final class SHA256 extends HarmonyKeyStoreECDSASignatureSpi {
    }

    public static final class SHA384 extends HarmonyKeyStoreECDSASignatureSpi {
    }

    public static final class SHA512 extends HarmonyKeyStoreECDSASignatureSpi {
    }

    HarmonyKeyStoreECDSASignatureSpi() {
        this.androidSignatureSpi = ReflectUtil.getInstance("android.security.keystore.AndroidKeyStoreECDSASignatureSpi" + TransferUtils.getInnerClassName(getClass().getName()));
    }
}
