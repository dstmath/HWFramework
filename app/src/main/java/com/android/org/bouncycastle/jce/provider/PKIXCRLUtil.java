package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.jcajce.PKIXCRLStoreSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PKIXCRLUtil {
    PKIXCRLUtil() {
    }

    public Set findCRLs(PKIXCRLStoreSelector crlselect, Date validityDate, List certStores, List pkixCrlStores) throws AnnotatedException {
        Set<X509CRL> initialSet = new HashSet();
        try {
            initialSet.addAll(findCRLs(crlselect, pkixCrlStores));
            initialSet.addAll(findCRLs(crlselect, certStores));
            Set finalSet = new HashSet();
            for (X509CRL crl : initialSet) {
                if (crl.getNextUpdate().after(validityDate)) {
                    X509Certificate cert = crlselect.getCertificateChecking();
                    if (cert == null) {
                        finalSet.add(crl);
                    } else if (crl.getThisUpdate().before(cert.getNotAfter())) {
                        finalSet.add(crl);
                    }
                }
            }
            return finalSet;
        } catch (AnnotatedException e) {
            throw new AnnotatedException("Exception obtaining complete CRLs.", e);
        }
    }

    private final Collection findCRLs(PKIXCRLStoreSelector crlSelect, List crlStores) throws AnnotatedException {
        Set crls = new HashSet();
        AnnotatedException lastException = null;
        boolean foundValidStore = false;
        for (CertStore store : crlStores) {
            try {
                crls.addAll(PKIXCRLStoreSelector.getCRLs(crlSelect, store));
                foundValidStore = true;
            } catch (CertStoreException e) {
                lastException = new AnnotatedException("Exception searching in X.509 CRL store.", e);
            }
        }
        if (foundValidStore || lastException == null) {
            return crls;
        }
        throw lastException;
    }
}
