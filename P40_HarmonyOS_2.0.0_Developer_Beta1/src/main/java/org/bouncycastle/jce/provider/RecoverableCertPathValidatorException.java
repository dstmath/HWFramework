package org.bouncycastle.jce.provider;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;

/* access modifiers changed from: package-private */
public class RecoverableCertPathValidatorException extends CertPathValidatorException {
    public RecoverableCertPathValidatorException(String str, Throwable th, CertPath certPath, int i) {
        super(str, th, certPath, i);
    }
}
