package org.bouncycastle.cert.dane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.operator.DigestCalculator;

public class DANECertificateFetcher {
    private final DANEEntryFetcherFactory fetcherFactory;
    private final DANEEntrySelectorFactory selectorFactory;

    public DANECertificateFetcher(DANEEntryFetcherFactory dANEEntryFetcherFactory, DigestCalculator digestCalculator) {
        this.fetcherFactory = dANEEntryFetcherFactory;
        this.selectorFactory = new DANEEntrySelectorFactory(digestCalculator);
    }

    public List fetch(String str) throws DANEException {
        DANEEntrySelector createSelector = this.selectorFactory.createSelector(str);
        List<DANEEntry> entries = this.fetcherFactory.build(createSelector.getDomainName()).getEntries();
        ArrayList arrayList = new ArrayList(entries.size());
        for (DANEEntry dANEEntry : entries) {
            if (createSelector.match(dANEEntry)) {
                arrayList.add(dANEEntry.getCertificate());
            }
        }
        return Collections.unmodifiableList(arrayList);
    }
}
