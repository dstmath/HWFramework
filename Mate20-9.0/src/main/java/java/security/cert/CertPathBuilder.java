package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import sun.security.jca.GetInstance;

public class CertPathBuilder {
    private static final String CPB_TYPE = "certpathbuilder.type";
    private final String algorithm;
    private final CertPathBuilderSpi builderSpi;
    private final Provider provider;

    protected CertPathBuilder(CertPathBuilderSpi builderSpi2, Provider provider2, String algorithm2) {
        this.builderSpi = builderSpi2;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    public static CertPathBuilder getInstance(String algorithm2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("CertPathBuilder", (Class<?>) CertPathBuilderSpi.class, algorithm2);
        return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm2);
    }

    public static CertPathBuilder getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        GetInstance.Instance instance = GetInstance.getInstance("CertPathBuilder", (Class<?>) CertPathBuilderSpi.class, algorithm2, provider2);
        return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm2);
    }

    public static CertPathBuilder getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("CertPathBuilder", (Class<?>) CertPathBuilderSpi.class, algorithm2, provider2);
        return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm2);
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
