package com.android.org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CertStoreCollectionSpi extends CertStoreSpi {
    private CollectionCertStoreParameters params;

    public CertStoreCollectionSpi(CertStoreParameters params) throws InvalidAlgorithmParameterException {
        super(params);
        if (params instanceof CollectionCertStoreParameters) {
            this.params = (CollectionCertStoreParameters) params;
            return;
        }
        throw new InvalidAlgorithmParameterException("org.bouncycastle.jce.provider.CertStoreCollectionSpi: parameter must be a CollectionCertStoreParameters object\n" + params.toString());
    }

    public Collection engineGetCertificates(CertSelector selector) throws CertStoreException {
        List col = new ArrayList();
        Iterator iter = this.params.getCollection().iterator();
        Object obj;
        if (selector == null) {
            while (iter.hasNext()) {
                obj = iter.next();
                if (obj instanceof Certificate) {
                    col.add(obj);
                }
            }
        } else {
            while (iter.hasNext()) {
                obj = iter.next();
                if ((obj instanceof Certificate) && selector.match((Certificate) obj)) {
                    col.add(obj);
                }
            }
        }
        return col;
    }

    public Collection engineGetCRLs(CRLSelector selector) throws CertStoreException {
        List col = new ArrayList();
        Iterator iter = this.params.getCollection().iterator();
        Object obj;
        if (selector == null) {
            while (iter.hasNext()) {
                obj = iter.next();
                if (obj instanceof CRL) {
                    col.add(obj);
                }
            }
        } else {
            while (iter.hasNext()) {
                obj = iter.next();
                if ((obj instanceof CRL) && selector.match((CRL) obj)) {
                    col.add(obj);
                }
            }
        }
        return col;
    }
}
