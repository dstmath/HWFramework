package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import sun.security.jca.GetInstance;

public class CertPathValidator {
    private static final String CPV_TYPE = "certpathvalidator.type";
    private final String algorithm;
    private final Provider provider;
    private final CertPathValidatorSpi validatorSpi;

    protected CertPathValidator(CertPathValidatorSpi validatorSpi2, Provider provider2, String algorithm2) {
        this.validatorSpi = validatorSpi2;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    public static CertPathValidator getInstance(String algorithm2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("CertPathValidator", (Class<?>) CertPathValidatorSpi.class, algorithm2);
        return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm2);
    }

    public static CertPathValidator getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        GetInstance.Instance instance = GetInstance.getInstance("CertPathValidator", (Class<?>) CertPathValidatorSpi.class, algorithm2, provider2);
        return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm2);
    }

    public static CertPathValidator getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("CertPathValidator", (Class<?>) CertPathValidatorSpi.class, algorithm2, provider2);
        return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm2);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final CertPathValidatorResult validate(CertPath certPath, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        return this.validatorSpi.engineValidate(certPath, params);
    }

    public static final String getDefaultType() {
        String cpvtype = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(CertPathValidator.CPV_TYPE);
            }
        });
        return cpvtype == null ? "PKIX" : cpvtype;
    }

    public final CertPathChecker getRevocationChecker() {
        return this.validatorSpi.engineGetRevocationChecker();
    }
}
