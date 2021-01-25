package org.bouncycastle.cert.jcajce;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/* access modifiers changed from: package-private */
public class NamedCertHelper extends CertHelper {
    private final String providerName;

    NamedCertHelper(String str) {
        this.providerName = str;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.cert.jcajce.CertHelper
    public CertificateFactory createCertificateFactory(String str) throws CertificateException, NoSuchProviderException {
        return CertificateFactory.getInstance(str, this.providerName);
    }
}
