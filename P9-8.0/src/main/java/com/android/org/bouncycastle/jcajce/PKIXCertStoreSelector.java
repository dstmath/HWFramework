package com.android.org.bouncycastle.jcajce;

import com.android.org.bouncycastle.util.Selector;
import java.io.IOException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509CertSelector;
import java.util.Collection;

public class PKIXCertStoreSelector<T extends Certificate> implements Selector<T> {
    private final CertSelector baseSelector;

    public static class Builder {
        private final CertSelector baseSelector;

        public Builder(CertSelector certSelector) {
            this.baseSelector = (CertSelector) certSelector.clone();
        }

        public PKIXCertStoreSelector<? extends Certificate> build() {
            return new PKIXCertStoreSelector(this.baseSelector, null);
        }
    }

    private static class SelectorClone extends X509CertSelector {
        private final PKIXCertStoreSelector selector;

        SelectorClone(PKIXCertStoreSelector selector) {
            this.selector = selector;
            if (selector.baseSelector instanceof X509CertSelector) {
                X509CertSelector baseSelector = (X509CertSelector) selector.baseSelector;
                setAuthorityKeyIdentifier(baseSelector.getAuthorityKeyIdentifier());
                setBasicConstraints(baseSelector.getBasicConstraints());
                setCertificate(baseSelector.getCertificate());
                setCertificateValid(baseSelector.getCertificateValid());
                setKeyUsage(baseSelector.getKeyUsage());
                setMatchAllSubjectAltNames(baseSelector.getMatchAllSubjectAltNames());
                setPrivateKeyValid(baseSelector.getPrivateKeyValid());
                setSerialNumber(baseSelector.getSerialNumber());
                setSubjectKeyIdentifier(baseSelector.getSubjectKeyIdentifier());
                setSubjectPublicKey(baseSelector.getSubjectPublicKey());
                try {
                    setExtendedKeyUsage(baseSelector.getExtendedKeyUsage());
                    setIssuer(baseSelector.getIssuerAsBytes());
                    setNameConstraints(baseSelector.getNameConstraints());
                    setPathToNames(baseSelector.getPathToNames());
                    setPolicy(baseSelector.getPolicy());
                    setSubject(baseSelector.getSubjectAsBytes());
                    setSubjectAlternativeNames(baseSelector.getSubjectAlternativeNames());
                    setSubjectPublicKeyAlgID(baseSelector.getSubjectPublicKeyAlgID());
                } catch (IOException e) {
                    throw new IllegalStateException("base selector invalid: " + e.getMessage(), e);
                }
            }
        }

        public boolean match(Certificate certificate) {
            if (this.selector == null) {
                return certificate != null;
            } else {
                return this.selector.match(certificate);
            }
        }
    }

    /* synthetic */ PKIXCertStoreSelector(CertSelector baseSelector, PKIXCertStoreSelector -this1) {
        this(baseSelector);
    }

    private PKIXCertStoreSelector(CertSelector baseSelector) {
        this.baseSelector = baseSelector;
    }

    public boolean match(Certificate cert) {
        return this.baseSelector.match(cert);
    }

    public Object clone() {
        return new PKIXCertStoreSelector(this.baseSelector);
    }

    public static Collection<? extends Certificate> getCertificates(PKIXCertStoreSelector selector, CertStore certStore) throws CertStoreException {
        return certStore.getCertificates(new SelectorClone(selector));
    }
}
