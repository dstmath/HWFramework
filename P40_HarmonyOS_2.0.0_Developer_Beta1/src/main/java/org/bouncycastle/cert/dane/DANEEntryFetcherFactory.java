package org.bouncycastle.cert.dane;

public interface DANEEntryFetcherFactory {
    DANEEntryFetcher build(String str);
}
