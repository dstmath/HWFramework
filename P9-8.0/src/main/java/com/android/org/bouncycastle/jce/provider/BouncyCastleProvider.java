package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public final class BouncyCastleProvider extends Provider implements ConfigurableProvider {
    private static final String[] ASYMMETRIC_CIPHERS = new String[]{"DSA", "DH", "EC", "RSA"};
    private static final String[] ASYMMETRIC_GENERIC = new String[]{"X509"};
    private static final String ASYMMETRIC_PACKAGE = "org.bouncycastle.jcajce.provider.asymmetric.";
    public static final ProviderConfiguration CONFIGURATION = new BouncyCastleProviderConfiguration();
    private static final String[] DIGESTS = new String[]{"MD5", "SHA1", "SHA224", "SHA256", "SHA384", "SHA512"};
    private static final String DIGEST_PACKAGE = "org.bouncycastle.jcajce.provider.digest.";
    private static final String[] KEYSTORES = new String[]{PROVIDER_NAME, "BCFKS", "PKCS12"};
    private static final String KEYSTORE_PACKAGE = "org.bouncycastle.jcajce.provider.keystore.";
    public static final String PROVIDER_NAME = "BC";
    private static final String[] SYMMETRIC_CIPHERS = new String[]{"AES", "ARC4", "Blowfish", "DES", "DESede", "RC2", "Twofish"};
    private static final String[] SYMMETRIC_GENERIC = new String[]{"PBEPBKDF2", "PBEPKCS12", "PBES2AlgorithmParameters"};
    private static final String[] SYMMETRIC_MACS = new String[0];
    private static final String SYMMETRIC_PACKAGE = "org.bouncycastle.jcajce.provider.symmetric.";
    private static String info = "BouncyCastle Security Provider v1.56";
    private static final Map keyInfoConverters = new HashMap();

    public BouncyCastleProvider() {
        super(PROVIDER_NAME, 1.56d, info);
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                BouncyCastleProvider.this.setup();
                return null;
            }
        });
    }

    private void setup() {
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.digest.", DIGESTS);
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.symmetric.", SYMMETRIC_GENERIC);
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.symmetric.", SYMMETRIC_MACS);
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.symmetric.", SYMMETRIC_CIPHERS);
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.asymmetric.", ASYMMETRIC_GENERIC);
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.asymmetric.", ASYMMETRIC_CIPHERS);
        loadAlgorithms("com.android.org.bouncycastle.jcajce.provider.keystore.", KEYSTORES);
        put("CertPathValidator.PKIX", "com.android.org.bouncycastle.jce.provider.PKIXCertPathValidatorSpi");
        put("CertPathBuilder.PKIX", "com.android.org.bouncycastle.jce.provider.PKIXCertPathBuilderSpi");
        put("CertStore.Collection", "com.android.org.bouncycastle.jce.provider.CertStoreCollectionSpi");
    }

    private void loadAlgorithms(String packageName, String[] names) {
        for (int i = 0; i != names.length; i++) {
            Class clazz = null;
            try {
                ClassLoader loader = getClass().getClassLoader();
                if (loader != null) {
                    clazz = loader.loadClass(packageName + names[i] + "$Mappings");
                } else {
                    clazz = Class.forName(packageName + names[i] + "$Mappings");
                }
            } catch (ClassNotFoundException e) {
            }
            if (clazz != null) {
                try {
                    ((AlgorithmProvider) clazz.newInstance()).configure(this);
                } catch (Exception e2) {
                    throw new InternalError("cannot create instance of " + packageName + names[i] + "$Mappings : " + e2);
                }
            }
        }
    }

    public void setParameter(String parameterName, Object parameter) {
        synchronized (CONFIGURATION) {
            ((BouncyCastleProviderConfiguration) CONFIGURATION).setParameter(parameterName, parameter);
        }
    }

    public boolean hasAlgorithm(String type, String name) {
        return !containsKey(new StringBuilder().append(type).append(".").append(name).toString()) ? containsKey("Alg.Alias." + type + "." + name) : true;
    }

    public void addAlgorithm(String key, String value) {
        if (containsKey(key)) {
            throw new IllegalStateException("duplicate provider key (" + key + ") found");
        }
        put(key, value);
    }

    public void addAlgorithm(String type, ASN1ObjectIdentifier oid, String className) {
        addAlgorithm(type + "." + oid, className);
        addAlgorithm(type + ".OID." + oid, className);
    }

    public void addKeyInfoConverter(ASN1ObjectIdentifier oid, AsymmetricKeyInfoConverter keyInfoConverter) {
        synchronized (keyInfoConverters) {
            keyInfoConverters.put(oid, keyInfoConverter);
        }
    }

    private static AsymmetricKeyInfoConverter getAsymmetricKeyInfoConverter(ASN1ObjectIdentifier algorithm) {
        AsymmetricKeyInfoConverter asymmetricKeyInfoConverter;
        synchronized (keyInfoConverters) {
            asymmetricKeyInfoConverter = (AsymmetricKeyInfoConverter) keyInfoConverters.get(algorithm);
        }
        return asymmetricKeyInfoConverter;
    }

    public static PublicKey getPublicKey(SubjectPublicKeyInfo publicKeyInfo) throws IOException {
        AsymmetricKeyInfoConverter converter = getAsymmetricKeyInfoConverter(publicKeyInfo.getAlgorithm().getAlgorithm());
        if (converter == null) {
            return null;
        }
        return converter.generatePublic(publicKeyInfo);
    }

    public static PrivateKey getPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        AsymmetricKeyInfoConverter converter = getAsymmetricKeyInfoConverter(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm());
        if (converter == null) {
            return null;
        }
        return converter.generatePrivate(privateKeyInfo);
    }
}
