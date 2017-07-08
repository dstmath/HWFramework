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
import java.util.Map;

public final class BouncyCastleProvider extends Provider implements ConfigurableProvider {
    private static final String[] ASYMMETRIC_CIPHERS = null;
    private static final String[] ASYMMETRIC_GENERIC = null;
    private static final String ASYMMETRIC_PACKAGE = "org.bouncycastle.jcajce.provider.asymmetric.";
    public static final ProviderConfiguration CONFIGURATION = null;
    private static final String[] DIGESTS = null;
    private static final String DIGEST_PACKAGE = "org.bouncycastle.jcajce.provider.digest.";
    private static final String[] KEYSTORES = null;
    private static final String KEYSTORE_PACKAGE = "org.bouncycastle.jcajce.provider.keystore.";
    public static final String PROVIDER_NAME = "BC";
    private static final String[] SYMMETRIC_CIPHERS = null;
    private static final String[] SYMMETRIC_GENERIC = null;
    private static final String[] SYMMETRIC_MACS = null;
    private static final String SYMMETRIC_PACKAGE = "org.bouncycastle.jcajce.provider.symmetric.";
    private static String info;
    private static final Map keyInfoConverters = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jce.provider.BouncyCastleProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jce.provider.BouncyCastleProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jce.provider.BouncyCastleProvider.<clinit>():void");
    }

    public BouncyCastleProvider() {
        super(PROVIDER_NAME, 1.54d, info);
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
        keyInfoConverters.put(oid, keyInfoConverter);
    }

    public static PublicKey getPublicKey(SubjectPublicKeyInfo publicKeyInfo) throws IOException {
        AsymmetricKeyInfoConverter converter = (AsymmetricKeyInfoConverter) keyInfoConverters.get(publicKeyInfo.getAlgorithm().getAlgorithm());
        if (converter == null) {
            return null;
        }
        return converter.generatePublic(publicKeyInfo);
    }

    public static PrivateKey getPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        AsymmetricKeyInfoConverter converter = (AsymmetricKeyInfoConverter) keyInfoConverters.get(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm());
        if (converter == null) {
            return null;
        }
        return converter.generatePrivate(privateKeyInfo);
    }
}
