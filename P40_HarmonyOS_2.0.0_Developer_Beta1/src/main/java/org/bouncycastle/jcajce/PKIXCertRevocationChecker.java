package org.bouncycastle.jcajce;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;

public interface PKIXCertRevocationChecker {
    void check(Certificate certificate) throws CertPathValidatorException;

    void initialize(PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters);

    void setParameter(String str, Object obj);
}
