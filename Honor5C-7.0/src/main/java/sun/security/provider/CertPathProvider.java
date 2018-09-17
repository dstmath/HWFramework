package sun.security.provider;

import java.security.Provider;

public final class CertPathProvider extends Provider {
    public CertPathProvider() {
        super("CertPathProvider", 1.0d, "Provider of CertPathBuilder and CertPathVerifier");
        put("CertPathBuilder.PKIX", "sun.security.provider.certpath.SunCertPathBuilder");
        put("CertPathBuilder.PKIX ImplementedIn", "Software");
        put("CertPathBuilder.PKIX ValidationAlgorithm", "RFC3280");
        put("CertPathValidator.PKIX", "sun.security.provider.certpath.PKIXCertPathValidator");
        put("CertPathValidator.PKIX ImplementedIn", "Software");
        put("CertPathValidator.PKIX ValidationAlgorithm", "RFC3280");
    }
}
