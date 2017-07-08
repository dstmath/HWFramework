package com.android.org.bouncycastle.jcajce;

import com.android.org.bouncycastle.util.Selector;
import com.android.org.bouncycastle.util.Store;
import com.android.org.bouncycastle.util.StoreException;
import java.security.cert.CRL;
import java.util.Collection;

public interface PKIXCRLStore<T extends CRL> extends Store<T> {
    Collection<T> getMatches(Selector<T> selector) throws StoreException;
}
