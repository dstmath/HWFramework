package org.bouncycastle.cert.jcajce;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

abstract class CertHelper {
    CertHelper() {
    }

    /* access modifiers changed from: protected */
    public abstract CertificateFactory createCertificateFactory(String str) throws CertificateException, NoSuchProviderException;

    public CertificateFactory getCertificateFactory(String str) throws NoSuchProviderException, CertificateException {
        return createCertificateFactory(str);
    }
}
