package org.bouncycastle.cert.jcajce;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

class DefaultCertHelper extends CertHelper {
    DefaultCertHelper() {
    }

    /* access modifiers changed from: protected */
    public CertificateFactory createCertificateFactory(String str) throws CertificateException {
        return CertificateFactory.getInstance(str);
    }
}
