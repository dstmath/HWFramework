package org.bouncycastle.cert.dane;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.DigestCalculator;

public class DANEEntryFactory {
    private final DANEEntrySelectorFactory selectorFactory;

    public DANEEntryFactory(DigestCalculator digestCalculator) {
        this.selectorFactory = new DANEEntrySelectorFactory(digestCalculator);
    }

    public DANEEntry createEntry(String str, int i, X509CertificateHolder x509CertificateHolder) throws DANEException {
        if (i < 0 || i > 3) {
            throw new DANEException("unknown certificate usage: " + i);
        }
        return new DANEEntry(this.selectorFactory.createSelector(str).getDomainName(), new byte[]{(byte) i, 0, 0}, x509CertificateHolder);
    }

    public DANEEntry createEntry(String str, X509CertificateHolder x509CertificateHolder) throws DANEException {
        return createEntry(str, 3, x509CertificateHolder);
    }
}
