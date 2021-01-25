package org.bouncycastle.pkix.jcajce;

import java.security.cert.CertPathValidatorException;

class CRLNotFoundException extends CertPathValidatorException {
    CRLNotFoundException(String str) {
        super(str);
    }

    public CRLNotFoundException(String str, Throwable th) {
        super(str, th);
    }
}
