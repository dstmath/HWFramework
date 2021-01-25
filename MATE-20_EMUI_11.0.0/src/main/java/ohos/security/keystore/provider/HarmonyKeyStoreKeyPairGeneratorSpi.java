package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class HarmonyKeyStoreKeyPairGeneratorSpi extends AndroidKeyStoreKeyPairGeneratorSpi {

    public static class RSA extends HarmonyKeyStoreKeyPairGeneratorSpi {
        public RSA() {
            super(1);
        }
    }

    public static class EC extends HarmonyKeyStoreKeyPairGeneratorSpi {
        public EC() {
            super(3);
        }
    }

    protected HarmonyKeyStoreKeyPairGeneratorSpi(int i) {
        super(i);
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        HarmonyKeyStoreKeyPairGeneratorSpi.super.initialize(TransferUtils.convertParam(algorithmParameterSpec), secureRandom);
    }

    public KeyPair generateKeyPair() {
        return TransferUtils.toHarmonyKeyPair(HarmonyKeyStoreKeyPairGeneratorSpi.super.generateKeyPair());
    }
}
