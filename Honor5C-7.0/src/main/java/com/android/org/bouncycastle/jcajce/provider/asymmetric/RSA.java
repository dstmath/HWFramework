package com.android.org.bouncycastle.jcajce.provider.asymmetric;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;

public class RSA {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.asymmetric.rsa.";

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("AlgorithmParameters.OAEP", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.AlgorithmParametersSpi$OAEP");
            provider.addAlgorithm("AlgorithmParameters.PSS", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.AlgorithmParametersSpi$PSS");
            provider.addAlgorithm("Cipher.RSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.CipherSpi$NoPadding");
            provider.addAlgorithm("Alg.Alias.Cipher.RSA/RAW", "RSA");
            provider.addAlgorithm("Alg.Alias.Cipher.RSA//RAW", "RSA");
            provider.addAlgorithm("Alg.Alias.Cipher.RSA//NOPADDING", "RSA");
            provider.addAlgorithm("KeyFactory.RSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi");
            provider.addAlgorithm("KeyPairGenerator.RSA", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyPairGeneratorSpi");
            AsymmetricKeyInfoConverter keyFact = new KeyFactorySpi();
            registerOid(provider, PKCSObjectIdentifiers.rsaEncryption, "RSA", keyFact);
            registerOid(provider, X509ObjectIdentifiers.id_ea_rsa, "RSA", keyFact);
            registerOid(provider, PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA", keyFact);
            if (provider.hasAlgorithm("MessageDigest", "MD5")) {
                addDigestSignature(provider, "MD5", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$MD5", PKCSObjectIdentifiers.md5WithRSAEncryption);
            }
            if (provider.hasAlgorithm("MessageDigest", "SHA1")) {
                addDigestSignature(provider, "SHA1", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$SHA1", PKCSObjectIdentifiers.sha1WithRSAEncryption);
                provider.addAlgorithm("Alg.Alias.Signature." + OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
                provider.addAlgorithm("Alg.Alias.Signature.OID." + OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
            }
            addDigestSignature(provider, "SHA224", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$SHA224", PKCSObjectIdentifiers.sha224WithRSAEncryption);
            addDigestSignature(provider, "SHA256", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$SHA256", PKCSObjectIdentifiers.sha256WithRSAEncryption);
            addDigestSignature(provider, "SHA384", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$SHA384", PKCSObjectIdentifiers.sha384WithRSAEncryption);
            addDigestSignature(provider, "SHA512", "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$SHA512", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        }

        private void addDigestSignature(ConfigurableProvider provider, String digest, String className, ASN1ObjectIdentifier oid) {
            String mainName = digest + "WITHRSA";
            String jdk11Variation1 = digest + "withRSA";
            String jdk11Variation2 = digest + "WithRSA";
            String alias = digest + "/" + "RSA";
            String longName = digest + "WITHRSAENCRYPTION";
            String longJdk11Variation1 = digest + "withRSAEncryption";
            String longJdk11Variation2 = digest + "WithRSAEncryption";
            provider.addAlgorithm("Signature." + mainName, className);
            provider.addAlgorithm("Alg.Alias.Signature." + jdk11Variation1, mainName);
            provider.addAlgorithm("Alg.Alias.Signature." + jdk11Variation2, mainName);
            provider.addAlgorithm("Alg.Alias.Signature." + longName, mainName);
            provider.addAlgorithm("Alg.Alias.Signature." + longJdk11Variation1, mainName);
            provider.addAlgorithm("Alg.Alias.Signature." + longJdk11Variation2, mainName);
            provider.addAlgorithm("Alg.Alias.Signature." + alias, mainName);
            if (oid != null) {
                provider.addAlgorithm("Alg.Alias.Signature." + oid, mainName);
                provider.addAlgorithm("Alg.Alias.Signature.OID." + oid, mainName);
            }
        }

        private void addISO9796Signature(ConfigurableProvider provider, String digest, String className) {
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "withRSA/ISO9796-2", digest + "WITHRSA/ISO9796-2");
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "WithRSA/ISO9796-2", digest + "WITHRSA/ISO9796-2");
            provider.addAlgorithm("Signature." + digest + "WITHRSA/ISO9796-2", className);
        }

        private void addPSSSignature(ConfigurableProvider provider, String digest, String className) {
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "withRSA/PSS", digest + "WITHRSAANDMGF1");
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "WithRSA/PSS", digest + "WITHRSAANDMGF1");
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "withRSAandMGF1", digest + "WITHRSAANDMGF1");
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "WithRSAAndMGF1", digest + "WITHRSAANDMGF1");
            provider.addAlgorithm("Signature." + digest + "WITHRSAANDMGF1", className);
        }

        private void addX931Signature(ConfigurableProvider provider, String digest, String className) {
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "withRSA/X9.31", digest + "WITHRSA/X9.31");
            provider.addAlgorithm("Alg.Alias.Signature." + digest + "WithRSA/X9.31", digest + "WITHRSA/X9.31");
            provider.addAlgorithm("Signature." + digest + "WITHRSA/X9.31", className);
        }
    }
}
