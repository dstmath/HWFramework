package gov.nist.javax.sip.message;

import gov.nist.javax.sip.header.SIPHeader;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class HeaderIterator implements ListIterator {
    private int index;
    private SIPHeader sipHeader;
    private SIPMessage sipMessage;
    private boolean toRemove;

    protected HeaderIterator(SIPMessage sipMessage2, SIPHeader sipHeader2) {
        this.sipMessage = sipMessage2;
        this.sipHeader = sipHeader2;
    }

    @Override // java.util.ListIterator, java.util.Iterator
    public Object next() throws NoSuchElementException {
        SIPHeader sIPHeader = this.sipHeader;
        if (sIPHeader == null || this.index == 1) {
            throw new NoSuchElementException();
        }
        this.toRemove = true;
        this.index = 1;
        return sIPHeader;
    }

    @Override // java.util.ListIterator
    public Object previous() throws NoSuchElementException {
        SIPHeader sIPHeader = this.sipHeader;
        if (sIPHeader == null || this.index == 0) {
            throw new NoSuchElementException();
        }
        this.toRemove = true;
        this.index = 0;
        return sIPHeader;
    }

    @Override // java.util.ListIterator
    public int nextIndex() {
        return 1;
    }

    @Override // java.util.ListIterator
    public int previousIndex() {
        return this.index == 0 ? -1 : 0;
    }

    @Override // java.util.ListIterator
    public void set(Object header) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.ListIterator
    public void add(Object header) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.ListIterator, java.util.Iterator
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

    @Override // java.util.ListIterator, java.util.Iterator
    public boolean hasNext() {
        return this.index == 0;
    }

    @Override // java.util.ListIterator
    public boolean hasPrevious() {
        return this.index == 1;
    }
}
