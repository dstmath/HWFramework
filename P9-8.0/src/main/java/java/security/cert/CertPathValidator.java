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

public class CertPathValidator {
    private static final String CPV_TYPE = "certpathvalidator.type";
    private final String algorithm;
    private final Provider provider;
    private final CertPathValidatorSpi validatorSpi;

    protected CertPathValidator(CertPathValidatorSpi validatorSpi, Provider provider, String algorithm) {
        this.validatorSpi = validatorSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    public static CertPathValidator getInstance(String algorithm) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathValidator", CertPathValidatorSpi.class, algorithm);
        return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm);
    }

    public static CertPathValidator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("CertPathValidator", CertPathValidatorSpi.class, algorithm, provider);
        return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm);
    }

    public static CertPathValidator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("CertPathValidator", CertPathValidatorSpi.class, algorithm, provider);
        return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm);
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
