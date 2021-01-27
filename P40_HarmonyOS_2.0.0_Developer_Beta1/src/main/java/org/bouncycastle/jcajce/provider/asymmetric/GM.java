package org.bouncycastle.jcajce.provider.asymmetric;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

public class GM {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.asymmetric.ec.";
    private static final Map<String, String> generalSm2Attributes = new HashMap();

    public static class Mappings extends AsymmetricAlgorithmProvider {
        @Override // org.bouncycastle.jcajce.provider.util.AlgorithmProvider
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("Signature.SHA256WITHSM2", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMSignatureSpi$sha256WithSM2");
            configurableProvider.addAlgorithm("Alg.Alias.Signature." + GMObjectIdentifiers.sm2sign_with_sha256, "SHA256WITHSM2");
            configurableProvider.addAlgorithm("Signature.SM3WITHSM2", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMSignatureSpi$sm3WithSM2");
            configurableProvider.addAlgorithm("Alg.Alias.Signature." + GMObjectIdentifiers.sm2sign_with_sm3, "SM3WITHSM2");
            configurableProvider.addAlgorithm("Cipher.SM2", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher.SM2WITHSM3", "SM2");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_sm3, "SM2");
            configurableProvider.addAlgorithm("Cipher.SM2WITHBLAKE2B", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withBlake2b");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_blake2b512, "SM2WITHBLAKE2B");
            configurableProvider.addAlgorithm("Cipher.SM2WITHBLAKE2S", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withBlake2s");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_blake2s256, "SM2WITHBLAKE2S");
            configurableProvider.addAlgorithm("Cipher.SM2WITHWHIRLPOOL", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withWhirlpool");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_whirlpool, "SM2WITHWHIRLPOOL");
            configurableProvider.addAlgorithm("Cipher.SM2WITHMD5", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withMD5");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_md5, "SM2WITHMD5");
            configurableProvider.addAlgorithm("Cipher.SM2WITHRIPEMD160", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withRMD");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_rmd160, "SM2WITHRIPEMD160");
            configurableProvider.addAlgorithm("Cipher.SM2WITHSHA1", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withSha1");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_sha1, "SM2WITHSHA1");
            configurableProvider.addAlgorithm("Cipher.SM2WITHSHA224", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withSha224");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_sha224, "SM2WITHSHA224");
            configurableProvider.addAlgorithm("Cipher.SM2WITHSHA256", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withSha256");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_sha256, "SM2WITHSHA256");
            configurableProvider.addAlgorithm("Cipher.SM2WITHSHA384", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withSha384");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_sha384, "SM2WITHSHA384");
            configurableProvider.addAlgorithm("Cipher.SM2WITHSHA512", "org.bouncycastle.jcajce.provider.asymmetric.ec.GMCipherSpi$SM2withSha512");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher." + GMObjectIdentifiers.sm2encrypt_with_sha512, "SM2WITHSHA512");
        }
    }

    static {
        generalSm2Attributes.put("SupportedKeyClasses", "java.security.interfaces.ECPublicKey|java.security.interfaces.ECPrivateKey");
        generalSm2Attributes.put("SupportedKeyFormats", "PKCS#8|X.509");
    }
}
