package org.apache.http.message;

import java.util.List;
import java.util.NoSuchElementException;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;

@Deprecated
public class BasicListHeaderIterator implements HeaderIterator {
    protected final List allHeaders;
    protected int currentIndex;
    protected String headerName;
    protected int lastIndex;

    public BasicListHeaderIterator(List headers, String name) {
        if (headers != null) {
            this.allHeaders = headers;
            this.headerName = name;
            this.currentIndex = findNext(-1);
            this.lastIndex = -1;
            return;
        }
        throw new IllegalArgumentException("Header list must not be null.");
    }

    /* access modifiers changed from: protected */
    public int findNext(int from) {
        int i = -1;
        if (from < -1) {
            return -1;
        }
        int to = this.allHeaders.size() - 1;
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
        if (this.headerName == null) {
            return true;
        }
        return this.headerName.equalsIgnoreCase(((Header) this.allHeaders.get(index)).getName());
    }

    public boolean hasNext() {
        return this.currentIndex >= 0;
    }

    public Header nextHeader() throws NoSuchElementException {
        int current = this.currentIndex;
        if (current >= 0) {
            this.lastIndex = current;
            this.currentIndex = findNext(current);
            return (Header) this.allHeaders.get(current);
        }
        throw new NoSuchElementException("Iteration already finished.");
    }

    public final Object next() throws NoSuchElementException {
        return nextHeader();
    }

    public void remove() throws UnsupportedOperationException {
        if (this.lastIndex >= 0) {
            this.allHeaders.remove(this.lastIndex);
            this.lastIndex = -1;
            this.currentIndex--;
            return;
        }
        throw new IllegalStateException("No header to remove.");
    }
}
