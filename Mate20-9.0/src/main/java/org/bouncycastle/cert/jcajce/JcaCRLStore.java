package org.bouncycastle.cert.jcajce;

import java.io.IOException;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.Collection;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.util.CollectionStore;

public class JcaCRLStore extends CollectionStore {
    public JcaCRLStore(Collection collection) throws CRLException {
        super(convertCRLs(collection));
    }

    private static Collection convertCRLs(Collection collection) throws CRLException {
        ArrayList arrayList = new ArrayList(collection.size());
        for (Object next : collection) {
            if (next instanceof X509CRL) {
                try {
                    arrayList.add(new X509CRLHolder(((X509CRL) next).getEncoded()));
                } catch (IOException e) {
                    throw new CRLException("cannot read encoding: " + e.getMessage());
                }
            } else {
                arrayList.add((X509CRLHolder) next);
            }
        }
        return arrayList;
    }
}
