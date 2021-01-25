package org.bouncycastle.cert.path.validations;

import java.util.Collection;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.path.CertPathValidation;
import org.bouncycastle.cert.path.CertPathValidationContext;
import org.bouncycastle.cert.path.CertPathValidationException;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

public class CRLValidation implements CertPathValidation {
    private Store crls;
    private X500Name workingIssuerName;

    public CRLValidation(X500Name x500Name, Store store) {
        this.workingIssuerName = x500Name;
        this.crls = store;
    }

    @Override // org.bouncycastle.util.Memoable
    public Memoable copy() {
        return new CRLValidation(this.workingIssuerName, this.crls);
    }

    @Override // org.bouncycastle.util.Memoable
    public void reset(Memoable memoable) {
        CRLValidation cRLValidation = (CRLValidation) memoable;
        this.workingIssuerName = cRLValidation.workingIssuerName;
        this.crls = cRLValidation.crls;
    }

    @Override // org.bouncycastle.cert.path.CertPathValidation
    public void validate(CertPathValidationContext certPathValidationContext, X509CertificateHolder x509CertificateHolder) throws CertPathValidationException {
        Collection<X509CRLHolder> matches = this.crls.getMatches(new Selector() {
            /* class org.bouncycastle.cert.path.validations.CRLValidation.AnonymousClass1 */

            @Override // org.bouncycastle.util.Selector, java.lang.Object
            public Object clone() {
                return this;
            }

            @Override // org.bouncycastle.util.Selector
            public boolean match(Object obj) {
                return ((X509CRLHolder) obj).getIssuer().equals(CRLValidation.this.workingIssuerName);
            }
        });
        if (!matches.isEmpty()) {
            for (X509CRLHolder x509CRLHolder : matches) {
                if (x509CRLHolder.getRevokedCertificate(x509CertificateHolder.getSerialNumber()) != null) {
                    throw new CertPathValidationException("Certificate revoked");
                }
            }
            this.workingIssuerName = x509CertificateHolder.getSubject();
            return;
        }
        throw new CertPathValidationException("CRL for " + this.workingIssuerName + " not found");
    }
}
