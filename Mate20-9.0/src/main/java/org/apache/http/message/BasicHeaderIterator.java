package org.apache.http.message;

import java.util.NoSuchElementException;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;

@Deprecated
public class BasicHeaderIterator implements HeaderIterator {
    protected final Header[] allHeaders;
    protected int currentIndex;
    protected String headerName;

    public BasicHeaderIterator(Header[] headers, String name) {
        if (headers != null) {
            this.allHeaders = headers;
            this.headerName = name;
            this.currentIndex = findNext(-1);
            return;
        }
        throw new IllegalArgumentException("Header array must not be null.");
    }

    /* access modifiers changed from: protected */
    public int findNext(int from) {
        int i = -1;
        if (from < -1) {
            return -1;
        }
        int to = this.allHeaders.length - 1;
        boolean found = false;
        while (!found && from < to) {
            from++;
            found = filterHeader(from);
        }
        if (found) {
            i = from;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public boolean filterHeader(int index) {
        return this.headerName == null || this.headerName.equalsIgnoreCase(this.allHeaders[index].getName());
    }

    public boolean hasNext() {
        return this.currentIndex >= 0;
    }

    public Header nextHeader() throws NoSuchElementException {
        int current = this.currentIndex;
        if (current >= 0) {
            this.currentIndex = findNext(current);
            return this.allHeaders[current];
        }
        throw new NoSuchElementException("Iteration already finished.");
    }

    public final Object next() throws NoSuchElementException {
        return nextHeader();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing headers is not supported.");
    }
}
