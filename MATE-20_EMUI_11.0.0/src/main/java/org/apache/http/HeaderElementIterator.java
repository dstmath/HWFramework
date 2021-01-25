package org.apache.http;

import java.util.Iterator;

@Deprecated
public interface HeaderElementIterator extends Iterator {
    @Override // java.util.Iterator
    boolean hasNext();

    HeaderElement nextElement();
}
