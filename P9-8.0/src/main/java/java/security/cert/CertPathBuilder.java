package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

public class CertPathBuilder {
    private static final String CPB_TYPE = "certpathbuilder.type";
    private final String algorithm;
    private final CertPathBuilderSpi builderSpi;
    private final Provider provider;

    protected CertPathBuilder(CertPathBuilderSpi builderSpi, Provider provider, String algorithm) {
        this.builderSpi = builderSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    public static CertPathBuilder getInstance(String algorithm) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathBuilder", CertPathBuilderSpi.class, algorithm);
        return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm);
    }

    public static CertPathBuilder getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("CertPathBuilder", CertPathBuilderSpi.class, algorithm, provider);
        return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm);
    }

    public static CertPathBuilder getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathBuilder", CertPathBuilderSpi.class, algorithm, provider);
        return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final CertPathBuilderResult build(CertPathParameters params) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        return this.builderSpi.engineBuild(params);
    }

    public static final String getDefaultType() {
        String cpbtype = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(CertPathBuilder.CPB_TYPE);
            }
        });
        return cpbtype == null ? "PKIX" : cpbtype;
    }

    public final CertPathChecker getRevocationChecker() {
        return this.builderSpi.engineGetRevocationChecker();
    }
}
