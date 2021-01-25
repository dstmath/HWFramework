package ohos.security.keystore.provider;

abstract class HarmonyKeyStoreRSASignatureSpi extends HarmonyKeyStoreSignatureSpiBase {

    public static final class MD5WithPKCS1Padding extends PKCS1Padding {
    }

    public static final class NONEWithPKCS1Padding extends PKCS1Padding {
    }

    public static final class SHA1WithPKCS1Padding extends PKCS1Padding {
    }

    public static final class SHA1WithPSSPadding extends PSSPadding {
    }

    public static final class SHA224WithPKCS1Padding extends PKCS1Padding {
    }

    public static final class SHA224WithPSSPadding extends PSSPadding {
    }

    public static final class SHA256WithPKCS1Padding extends PKCS1Padding {
    }

    public static final class SHA256WithPSSPadding extends PSSPadding {
    }

    public static final class SHA384WithPKCS1Padding extends PKCS1Padding {
    }

    public static final class SHA384WithPSSPadding extends PSSPadding {
    }

    public static final class SHA512WithPKCS1Padding extends PKCS1Padding {
    }

    public static final class SHA512WithPSSPadding extends PSSPadding {
    }

    static abstract class PKCS1Padding extends HarmonyKeyStoreRSASignatureSpi {
        PKCS1Padding() {
        }
    }

    static abstract class PSSPadding extends HarmonyKeyStoreRSASignatureSpi {
        PSSPadding() {
        }
    }

    HarmonyKeyStoreRSASignatureSpi() {
        this.androidSignatureSpi = ReflectUtil.getInstance("android.security.keystore.AndroidKeyStoreRSASignatureSpi" + TransferUtils.getInnerClassName(getClass().getName()));
    }
}
