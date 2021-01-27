package org.bouncycastle.est;

import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.Store;

public class CACertsResponse {
    private Store<X509CRLHolder> crlHolderStore;
    private final ESTRequest requestToRetry;
    private final Source session;
    private final Store<X509CertificateHolder> store;
    private final boolean trusted;

    public CACertsResponse(Store<X509CertificateHolder> store2, Store<X509CRLHolder> store3, ESTRequest eSTRequest, Source source, boolean z) {
        this.store = store2;
        this.requestToRetry = eSTRequest;
        this.session = source;
        this.trusted = z;
        this.crlHolderStore = store3;
    }

    public Store<X509CertificateHolder> getCertificateStore() {
        Store<X509CertificateHolder> store2 = this.store;
        if (store2 != null) {
            return store2;
        }
        throw new IllegalStateException("Response has no certificates.");
    }

    public Store<X509CRLHolder> getCrlStore() {
        Store<X509CRLHolder> store2 = this.crlHolderStore;
        if (store2 != null) {
            return store2;
        }
        throw new IllegalStateException("Response has no CRLs.");
    }

    public ESTRequest getRequestToRetry() {
        return this.requestToRetry;
    }

    public Object getSession() {
        return this.session.getSession();
    }

    public boolean hasCRLs() {
        return this.crlHolderStore != null;
    }

    public boolean hasCertificates() {
        return this.store != null;
    }

    public boolean isTrusted() {
        return this.trusted;
    }
}
