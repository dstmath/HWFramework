package javax.net.ssl;

import java.security.cert.CertPathParameters;

public class CertPathTrustManagerParameters implements ManagerFactoryParameters {
    private final CertPathParameters parameters;

    public CertPathTrustManagerParameters(CertPathParameters parameters) {
        this.parameters = (CertPathParameters) parameters.clone();
    }

    public CertPathParameters getParameters() {
        return (CertPathParameters) this.parameters.clone();
    }
}
