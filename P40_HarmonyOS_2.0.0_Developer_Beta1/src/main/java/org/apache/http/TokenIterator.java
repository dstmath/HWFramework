package org.apache.http;

import java.util.Iterator;

@Deprecated
public interface TokenIterator extends Iterator {
    @Override // java.util.Iterator
    boolean hasNext();

    String nextToken();
}
