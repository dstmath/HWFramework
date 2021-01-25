package org.bouncycastle.util;

import java.util.Collection;

public interface Store<T> {
    Collection<T> getMatches(Selector<T> selector) throws StoreException;
}
