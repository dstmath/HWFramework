package org.bouncycastle.jce;

import java.security.cert.CertStoreParameters;
import java.util.Collection;

public class MultiCertStoreParameters implements CertStoreParameters {
    private Collection certStores;
    private boolean searchAllStores;

    public MultiCertStoreParameters(Collection collection) {
        this(collection, true);
    }

    public MultiCertStoreParameters(Collection collection, boolean z) {
        this.certStores = collection;
        this.searchAllStores = z;
    }

    public Object clone() {
        return this;
    }

    public Collection getCertStores() {
        return this.certStores;
    }

    public boolean getSearchAllStores() {
        return this.searchAllStores;
    }
}
