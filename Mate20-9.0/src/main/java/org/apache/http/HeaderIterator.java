package org.apache.http;

import java.util.Iterator;

@Deprecated
public interface HeaderIterator extends Iterator {
    boolean hasNext();

    Header nextHeader();
}
