package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

interface State extends Cloneable {
    Object clone();

    boolean isInitial();

    boolean keyParamsNeeded();

    void updateState(X509Certificate x509Certificate) throws CertificateException, IOException, CertPathValidatorException;
}
