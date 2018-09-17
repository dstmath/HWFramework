package com.android.org.bouncycastle.jcajce.provider.keystore;

import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

public class PKCS12 {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.keystore.pkcs12.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("KeyStore.PKCS12", "com.android.org.bouncycastle.jcajce.provider.keystore.pkcs12.PKCS12KeyStoreSpi$BCPKCS12KeyStore");
        }
    }
}
