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
        if (headers == null) {
            throw new IllegalArgumentException("Header list must not be null.");
        }
        this.allHeaders = headers;
        this.headerName = name;
        this.currentIndex = findNext(-1);
        this.lastIndex = -1;
    }

    protected int findNext(int from) {
        if (from < -1) {
            return -1;
        }
        int to = this.allHeaders.size() - 1;
        boolean found = false;
        while (!found && from < to) {
            from++;
            found = filterHeader(from);
        }
        if (!found) {
            from = -1;
        }
        return from;
    }

    protected boolean filterHeader(int index) {
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
        if (current < 0) {
            throw new NoSuchElementException("Iteration already finished.");
        }
        this.lastIndex = current;
        this.currentIndex = findNext(current);
        return (Header) this.allHeaders.get(current);
    }

    public final Object next() throws NoSuchElementException {
        return nextHeader();
    }

    public void remove() throws UnsupportedOperationException {
        if (this.lastIndex < 0) {
            throw new IllegalStateException("No header to remove.");
        }
        this.allHeaders.remove(this.lastIndex);
        this.lastIndex = -1;
        this.currentIndex--;
    }
}
