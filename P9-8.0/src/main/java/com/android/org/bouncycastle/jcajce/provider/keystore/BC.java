package com.android.org.bouncycastle.jcajce.provider.keystore;

import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

public class BC {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.keystore.bc.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("KeyStore.BKS", "com.android.org.bouncycastle.jcajce.provider.keystore.bc.BcKeyStoreSpi$Std");
            provider.addAlgorithm("KeyStore.BouncyCastle", "com.android.org.bouncycastle.jcajce.provider.keystore.bc.BcKeyStoreSpi$BouncyCastleStore");
            provider.addAlgorithm("Alg.Alias.KeyStore.UBER", "BouncyCastle");
            provider.addAlgorithm("Alg.Alias.KeyStore.BOUNCYCASTLE", "BouncyCastle");
            provider.addAlgorithm("Alg.Alias.KeyStore.bouncycastle", "BouncyCastle");
        }
    }
}
