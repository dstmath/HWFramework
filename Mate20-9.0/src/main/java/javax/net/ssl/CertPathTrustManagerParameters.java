package javax.net.ssl;

import java.security.cert.CertPathParameters;

public class CertPathTrustManagerParameters implements ManagerFactoryParameters {
    private final CertPathParameters parameters;

    public CertPathTrustManagerParameters(CertPathParameters parameters2) {
        this.parameters = (CertPathParameters) parameters2.clone();
    }

    public CertPathParameters getParameters() {
        return (CertPathParameters) this.parameters.clone();
    }
}
