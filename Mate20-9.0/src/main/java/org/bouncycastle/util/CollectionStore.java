package org.bouncycastle.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CollectionStore<T> implements Store<T>, Iterable<T> {
    private Collection<T> _local;

    public CollectionStore(Collection<T> collection) {
        this._local = new ArrayList(collection);
    }

    public Collection<T> getMatches(Selector<T> selector) {
        if (selector == null) {
            return new ArrayList(this._local);
        }
        ArrayList arrayList = new ArrayList();
        for (T next : this._local) {
            if (selector.match(next)) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public Iterator<T> iterator() {
        return getMatches(null).iterator();
    }
}
