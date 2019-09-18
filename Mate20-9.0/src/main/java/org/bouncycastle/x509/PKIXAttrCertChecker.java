package org.bouncycastle.x509;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.util.Collection;
import java.util.Set;

public abstract class PKIXAttrCertChecker implements Cloneable {
    public abstract void check(X509AttributeCertificate x509AttributeCertificate, CertPath certPath, CertPath certPath2, Collection collection) throws CertPathValidatorException;

    public abstract Object clone();

    public abstract Set getSupportedExtensions();
}
