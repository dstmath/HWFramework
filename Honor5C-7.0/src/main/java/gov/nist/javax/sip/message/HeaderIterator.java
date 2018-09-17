package gov.nist.javax.sip.message;

import gov.nist.javax.sip.header.SIPHeader;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class HeaderIterator implements ListIterator {
    private int index;
    private SIPHeader sipHeader;
    private SIPMessage sipMessage;
    private boolean toRemove;

    protected HeaderIterator(SIPMessage sipMessage, SIPHeader sipHeader) {
        this.sipMessage = sipMessage;
        this.sipHeader = sipHeader;
    }

    public Object next() throws NoSuchElementException {
        if (this.sipHeader == null || this.index == 1) {
            throw new NoSuchElementException();
        }
        this.toRemove = true;
        this.index = 1;
        return this.sipHeader;
    }

    public Object previous() throws NoSuchElementException {
        if (this.sipHeader == null || this.index == 0) {
            throw new NoSuchElementException();
        }
        this.toRemove = true;
        this.index = 0;
        return this.sipHeader;
    }

    public int nextIndex() {
        return 1;
    }

    public int previousIndex() {
        return this.index == 0 ? -1 : 0;
    }

    public void set(Object header) {
        throw new UnsupportedOperationException();
    }

    public void add(Object header) {
        throw new UnsupportedOperationException();
    }

    public void remove() throws IllegalStateException {
        if (this.sipHeader == null) {
            throw new IllegalStateException();
        } else if (this.toRemove) {
            this.sipHeader = null;
            this.sipMessage.removeHeader(this.sipHeader.getName());
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean hasNext() {
        return this.index == 0;
    }

    public boolean hasPrevious() {
        return this.index == 1;
    }
}
