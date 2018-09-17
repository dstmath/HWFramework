package com.android.org.bouncycastle.jcajce.provider.asymmetric;

import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

public class EC {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.asymmetric.ec.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("AlgorithmParameters.EC", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.AlgorithmParametersSpi");
            provider.addAlgorithm("KeyAgreement.ECDH", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyAgreementSpi$DH");
            registerOid(provider, X9ObjectIdentifiers.id_ecPublicKey, "EC", new com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi.EC());
            registerOid(provider, X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme, "EC", new com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi.EC());
            provider.addAlgorithm("KeyFactory.EC", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi$EC");
            provider.addAlgorithm("KeyPairGenerator.EC", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi$EC");
            provider.addAlgorithm("Signature.SHA1withECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA");
            provider.addAlgorithm("Signature.NONEwithECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSAnone");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSA", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSAwithSHA1", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1WITHECDSA", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSAWITHSHA1", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.SHA1WithECDSA", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.ECDSAWithSHA1", "SHA1withECDSA");
            provider.addAlgorithm("Alg.Alias.Signature.1.2.840.10045.4.1", "SHA1withECDSA");
            addSignatureAlgorithm(provider, "SHA224", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA224", X9ObjectIdentifiers.ecdsa_with_SHA224);
            addSignatureAlgorithm(provider, "SHA256", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA256", X9ObjectIdentifiers.ecdsa_with_SHA256);
            addSignatureAlgorithm(provider, "SHA384", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA384", X9ObjectIdentifiers.ecdsa_with_SHA384);
            addSignatureAlgorithm(provider, "SHA512", "ECDSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi$ecDSA512", X9ObjectIdentifiers.ecdsa_with_SHA512);
        }
    }
}
