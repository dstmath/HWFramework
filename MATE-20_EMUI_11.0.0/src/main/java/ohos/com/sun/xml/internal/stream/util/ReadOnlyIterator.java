package ohos.com.sun.xml.internal.stream.util;

import java.util.Iterator;

public class ReadOnlyIterator implements Iterator {
    Iterator iterator = null;

    public ReadOnlyIterator() {
    }

    public ReadOnlyIterator(Iterator it) {
        this.iterator = it;
    }

    @Override // java.util.Iterator
    public boolean hasNext() {
        Iterator it = this.iterator;
        if (it != null) {
            return it.hasNext();
        }
        return false;
    }

    @Override // java.util.Iterator
    public Object next() {
        Iterator it = this.iterator;
        if (it != null) {
            return it.next();
        }
        return null;
    }

    @Override // java.util.Iterator
    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported");
    }
}
