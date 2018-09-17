package com.android.org.bouncycastle.jcajce.provider.asymmetric;

import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

public class X509 {

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("CertificateFactory.X.509", "com.android.org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory");
            provider.addAlgorithm("Alg.Alias.CertificateFactory.X509", "X.509");
        }
    }
}
