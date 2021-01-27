package org.hamcrest.internal;

import java.util.Iterator;
import org.hamcrest.SelfDescribing;

public class SelfDescribingValueIterator<T> implements Iterator<SelfDescribing> {
    private Iterator<T> values;

    public SelfDescribingValueIterator(Iterator<T> values2) {
        this.values = values2;
    }

    @Override // java.util.Iterator
    public boolean hasNext() {
        return this.values.hasNext();
    }

    @Override // java.util.Iterator
    public SelfDescribing next() {
        return new SelfDescribingValue(this.values.next());
    }

    @Override // java.util.Iterator
    public void remove() {
        this.values.remove();
    }
}
