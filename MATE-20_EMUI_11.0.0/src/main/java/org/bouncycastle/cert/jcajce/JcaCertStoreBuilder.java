package org.bouncycastle.cert.jcajce;

import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.Store;

public class JcaCertStoreBuilder {
    private JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
    private List certs = new ArrayList();
    private JcaX509CRLConverter crlConverter = new JcaX509CRLConverter();
    private List crls = new ArrayList();
    private Object provider;
    private String type = "Collection";

    private CollectionCertStoreParameters convertHolders(JcaX509CertificateConverter jcaX509CertificateConverter, JcaX509CRLConverter jcaX509CRLConverter) throws CertificateException, CRLException {
        ArrayList arrayList = new ArrayList(this.certs.size() + this.crls.size());
        for (X509CertificateHolder x509CertificateHolder : this.certs) {
            arrayList.add(jcaX509CertificateConverter.getCertificate(x509CertificateHolder));
        }
        for (X509CRLHolder x509CRLHolder : this.crls) {
            arrayList.add(jcaX509CRLConverter.getCRL(x509CRLHolder));
        }
        return new CollectionCertStoreParameters(arrayList);
    }

    public JcaCertStoreBuilder addCRL(X509CRLHolder x509CRLHolder) {
        this.crls.add(x509CRLHolder);
        return this;
    }

    public JcaCertStoreBuilder addCRLs(Store store) {
        this.crls.addAll(store.getMatches(null));
        return this;
    }

    public JcaCertStoreBuilder addCertificate(X509CertificateHolder x509CertificateHolder) {
        this.certs.add(x509CertificateHolder);
        return this;
    }

    public JcaCertStoreBuilder addCertificates(Store store) {
        this.certs.addAll(store.getMatches(null));
        return this;
    }

    public CertStore build() throws GeneralSecurityException {
        CollectionCertStoreParameters convertHolders = convertHolders(this.certificateConverter, this.crlConverter);
        Object obj = this.provider;
        return obj instanceof String ? CertStore.getInstance(this.type, convertHolders, (String) obj) : obj instanceof Provider ? CertStore.getInstance(this.type, convertHolders, (Provider) obj) : CertStore.getInstance(this.type, convertHolders);
    }

    public JcaCertStoreBuilder setProvider(String str) {
        this.certificateConverter.setProvider(str);
        this.crlConverter.setProvider(str);
        this.provider = str;
        return this;
    }

    public JcaCertStoreBuilder setProvider(Provider provider2) {
        this.certificateConverter.setProvider(provider2);
        this.crlConverter.setProvider(provider2);
        this.provider = provider2;
        return this;
    }

    public JcaCertStoreBuilder setType(String str) {
        this.type = str;
        return this;
    }
}
