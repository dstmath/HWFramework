package android.security.keystore;

import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import java.security.InvalidKeyException;

abstract class AndroidKeyStoreRSASignatureSpi extends AndroidKeyStoreSignatureSpiBase {
    private final int mKeymasterDigest;
    private final int mKeymasterPadding;

    static abstract class PKCS1Padding extends AndroidKeyStoreRSASignatureSpi {
        PKCS1Padding(int keymasterDigest) {
            super(keymasterDigest, 5);
        }

        protected final int getAdditionalEntropyAmountForSign() {
            return 0;
        }
    }

    public static final class MD5WithPKCS1Padding extends PKCS1Padding {
        public MD5WithPKCS1Padding() {
            super(1);
        }
    }

    public static final class NONEWithPKCS1Padding extends PKCS1Padding {
        public NONEWithPKCS1Padding() {
            super(0);
        }
    }

    static abstract class PSSPadding extends AndroidKeyStoreRSASignatureSpi {
        private static final int SALT_LENGTH_BYTES = 20;

        PSSPadding(int keymasterDigest) {
            super(keymasterDigest, 3);
        }

        protected final int getAdditionalEntropyAmountForSign() {
            return 20;
        }
    }

    public static final class SHA1WithPKCS1Padding extends PKCS1Padding {
        public SHA1WithPKCS1Padding() {
            super(2);
        }
    }

    public static final class SHA1WithPSSPadding extends PSSPadding {
        public SHA1WithPSSPadding() {
            super(2);
        }
    }

    public static final class SHA224WithPKCS1Padding extends PKCS1Padding {
        public SHA224WithPKCS1Padding() {
            super(3);
        }
    }

    public static final class SHA224WithPSSPadding extends PSSPadding {
        public SHA224WithPSSPadding() {
            super(3);
        }
    }

    public static final class SHA256WithPKCS1Padding extends PKCS1Padding {
        public SHA256WithPKCS1Padding() {
            super(4);
        }
    }

    public static final class SHA256WithPSSPadding extends PSSPadding {
        public SHA256WithPSSPadding() {
            super(4);
        }
    }

    public static final class SHA384WithPKCS1Padding extends PKCS1Padding {
        public SHA384WithPKCS1Padding() {
            super(5);
        }
    }

    public static final class SHA384WithPSSPadding extends PSSPadding {
        public SHA384WithPSSPadding() {
            super(5);
        }
    }

    public static final class SHA512WithPKCS1Padding extends PKCS1Padding {
        public SHA512WithPKCS1Padding() {
            super(6);
        }
    }

    public static final class SHA512WithPSSPadding extends PSSPadding {
        public SHA512WithPSSPadding() {
            super(6);
        }
    }

    AndroidKeyStoreRSASignatureSpi(int keymasterDigest, int keymasterPadding) {
        this.mKeymasterDigest = keymasterDigest;
        this.mKeymasterPadding = keymasterPadding;
    }

    protected final void initKey(AndroidKeyStoreKey key) throws InvalidKeyException {
        if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(key.getAlgorithm())) {
            super.initKey(key);
            return;
        }
        throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only" + KeyProperties.KEY_ALGORITHM_RSA + " supported");
    }

    protected final void resetAll() {
        super.resetAll();
    }

    protected final void resetWhilePreservingInitState() {
        super.resetWhilePreservingInitState();
    }

    protected final void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 1);
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
    }
}
