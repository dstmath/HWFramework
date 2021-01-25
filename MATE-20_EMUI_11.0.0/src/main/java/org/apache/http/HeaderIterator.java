package org.apache.http;

import java.util.Iterator;

@Deprecated
public interface HeaderIterator extends Iterator {
    @Override // java.util.Iterator
    boolean hasNext();

    Header nextHeader();
}
