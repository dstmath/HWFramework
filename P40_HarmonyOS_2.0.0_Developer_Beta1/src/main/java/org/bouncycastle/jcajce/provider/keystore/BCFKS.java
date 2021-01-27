package org.bouncycastle.jcajce.provider.keystore;

import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

public class BCFKS {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.keystore.bcfks.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        @Override // org.bouncycastle.jcajce.provider.util.AlgorithmProvider
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("KeyStore.BCFKS", "org.bouncycastle.jcajce.provider.keystore.bcfks.BcFKSKeyStoreSpi$Std");
            configurableProvider.addAlgorithm("KeyStore.BCFKS-DEF", "org.bouncycastle.jcajce.provider.keystore.bcfks.BcFKSKeyStoreSpi$Def");
            configurableProvider.addAlgorithm("KeyStore.IBCFKS", "org.bouncycastle.jcajce.provider.keystore.bcfks.BcFKSKeyStoreSpi$StdShared");
            configurableProvider.addAlgorithm("KeyStore.IBCFKS-DEF", "org.bouncycastle.jcajce.provider.keystore.bcfks.BcFKSKeyStoreSpi$DefShared");
        }
    }
}
