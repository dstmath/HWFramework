package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreKeyGeneratorSpi;
import android.security.keystore.KeymasterUtils;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;

public abstract class HarmonyKeyStoreKeyGeneratorSpi extends AndroidKeyStoreKeyGeneratorSpi {
    private static final int DEFAULT_KEY_SIZE = 128;
    private static final int KEY_MASTER_DIGEST = -1;

    public static class AES extends HarmonyKeyStoreKeyGeneratorSpi {
        public AES() {
            super(32, 128);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.security.keystore.provider.HarmonyKeyStoreKeyGeneratorSpi
        public void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
            HarmonyKeyStoreKeyGeneratorSpi.super.engineInit(TransferUtils.convertParam(algorithmParameterSpec), secureRandom);
        }
    }

    protected static abstract class HmacBase extends HarmonyKeyStoreKeyGeneratorSpi {
        protected HmacBase(int i) {
            super(128, i, KeymasterUtils.getDigestOutputSizeBits(i));
        }
    }

    public static class HmacSHA1 extends HmacBase {
        public HmacSHA1() {
            super(2);
        }
    }

    public static class HmacSHA224 extends HmacBase {
        public HmacSHA224() {
            super(3);
        }
    }

    public static class HmacSHA256 extends HmacBase {
        public HmacSHA256() {
            super(4);
        }
    }

    public static class HmacSHA384 extends HmacBase {
        public HmacSHA384() {
            super(5);
        }
    }

    public static class HmacSHA512 extends HmacBase {
        public HmacSHA512() {
            super(6);
        }
    }

    protected HarmonyKeyStoreKeyGeneratorSpi(int i, int i2) {
        this(i, -1, i2);
    }

    protected HarmonyKeyStoreKeyGeneratorSpi(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        HarmonyKeyStoreKeyGeneratorSpi.super.engineInit(TransferUtils.convertParam(algorithmParameterSpec), secureRandom);
    }

    /* access modifiers changed from: protected */
    public SecretKey engineGenerateKey() {
        return TransferUtils.toHarmonySecretKey(HarmonyKeyStoreKeyGeneratorSpi.super.engineGenerateKey());
    }
}
