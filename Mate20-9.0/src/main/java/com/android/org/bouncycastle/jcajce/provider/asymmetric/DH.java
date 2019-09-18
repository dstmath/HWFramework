package com.android.org.bouncycastle.jcajce.provider.asymmetric;

import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.KeyFactorySpi;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import java.util.HashMap;
import java.util.Map;

public class DH {
    private static final String PREFIX = "com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.";
    /* access modifiers changed from: private */
    public static final Map<String, String> generalDhAttributes = new HashMap();

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("KeyPairGenerator.DH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.KeyPairGeneratorSpi");
            provider.addAlgorithm("Alg.Alias.KeyPairGenerator.DIFFIEHELLMAN", "DH");
            provider.addAttributes("KeyAgreement.DH", DH.generalDhAttributes);
            provider.addAlgorithm("KeyAgreement.DH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.KeyAgreementSpi");
            provider.addAlgorithm("Alg.Alias.KeyAgreement.DIFFIEHELLMAN", "DH");
            provider.addAlgorithm("KeyFactory.DH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.KeyFactorySpi");
            provider.addAlgorithm("Alg.Alias.KeyFactory.DIFFIEHELLMAN", "DH");
            provider.addAlgorithm("AlgorithmParameters.DH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.AlgorithmParametersSpi");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameters.DIFFIEHELLMAN", "DH");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameterGenerator.DIFFIEHELLMAN", "DH");
            provider.addAlgorithm("AlgorithmParameterGenerator.DH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dh.AlgorithmParameterGeneratorSpi");
            registerOid(provider, PKCSObjectIdentifiers.dhKeyAgreement, "DH", new KeyFactorySpi());
            registerOid(provider, X9ObjectIdentifiers.dhpublicnumber, "DH", new KeyFactorySpi());
        }
    }

    static {
        generalDhAttributes.put("SupportedKeyClasses", "javax.crypto.interfaces.DHPublicKey|javax.crypto.interfaces.DHPrivateKey");
        generalDhAttributes.put("SupportedKeyFormats", "PKCS#8|X.509");
    }
}
