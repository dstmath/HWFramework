package com.android.org.bouncycastle.jcajce.provider.asymmetric;

import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.DSAUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.KeyFactorySpi;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;

public class DSA {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.asymmetric.dsa.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("AlgorithmParameters.DSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.AlgorithmParametersSpi");
            provider.addAlgorithm("AlgorithmParameterGenerator.DSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.AlgorithmParameterGeneratorSpi");
            provider.addAlgorithm("KeyPairGenerator.DSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.KeyPairGeneratorSpi");
            provider.addAlgorithm("KeyFactory.DSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.KeyFactorySpi");
            provider.addAlgorithm("Signature.SHA1withDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner$stdDSA");
            provider.addAlgorithm("Signature.NONEWITHDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner$noneDSA");
            provider.addAlgorithm("Alg.Alias.Signature.RAWDSA", "NONEWITHDSA");
            addSignatureAlgorithm(provider, "SHA224", "DSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner$dsa224", NISTObjectIdentifiers.dsa_with_sha224);
            addSignatureAlgorithm(provider, "SHA256", "DSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner$dsa256", NISTObjectIdentifiers.dsa_with_sha256);
            provider.addAlgorithm("Alg.Alias.Signature.DSA", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA/DSA", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1withDSA", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1WITHDSA", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.10040.4.1", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.10040.4.3", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.DSAwithSHA1", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.DSAWITHSHA1", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1WithDSA", "SHA1withDSA");
            provider.addAlgorithm("Alg.Alias.Signature.DSAWithSHA1", "SHA1withDSA");
            AsymmetricKeyInfoConverter keyFact = new KeyFactorySpi();
            for (int i = 0; i != DSAUtil.dsaOids.length; i++) {
                provider.addAlgorithm("Alg.Alias.Signature." + DSAUtil.dsaOids[i], "SHA1withDSA");
                registerOid(provider, DSAUtil.dsaOids[i], "DSA", keyFact);
                registerOidAlgorithmParameters(provider, DSAUtil.dsaOids[i], "DSA");
            }
        }
    }
}
