package org.bouncycastle.cert.jcajce;

import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

class ProviderCertHelper extends CertHelper {
    private final Provider provider;

    ProviderCertHelper(Provider provider2) {
        this.provider = provider2;
    }

    /* access modifiers changed from: protected */
    public CertificateFactory createCertificateFactory(String str) throws CertificateException {
        return CertificateFactory.getInstance(str, this.provider);
    }
}
