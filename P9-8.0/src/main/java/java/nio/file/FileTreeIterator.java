package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

class FileTreeIterator implements Iterator<Event>, Closeable {
    static final /* synthetic */ boolean -assertionsDisabled = (FileTreeIterator.class.desiredAssertionStatus() ^ 1);
    private Event next;
    private final FileTreeWalker walker;

    FileTreeIterator(Path start, int maxDepth, FileVisitOption... options) throws IOException {
        this.walker = new FileTreeWalker(Arrays.asList(options), maxDepth);
        this.next = this.walker.walk(start);
        if (-assertionsDisabled || this.next.type() == EventType.ENTRY || this.next.type() == EventType.START_DIRECTORY) {
            IOException ioe = this.next.ioeException();
            if (ioe != null) {
                throw ioe;
            }
            return;
        }
        throw new AssertionError();
    }

    private void fetchNextIfNeeded() {
        if (this.next == null) {
            Event ev = this.walker.next();
            while (ev != null) {
                IOException ioe = ev.ioeException();
                if (ioe != null) {
                    throw new UncheckedIOException(ioe);
                } else if (ev.type() != EventType.END_DIRECTORY) {
                    this.next = ev;
                    return;
                } else {
                    ev = this.walker.next();
                }
            }
        }
    }

    public boolean hasNext() {
        if (this.walker.isOpen()) {
            fetchNextIfNeeded();
            return this.next != null;
        } else {
            throw new IllegalStateException();
        }
    }

    public Event next() {
        if (this.walker.isOpen()) {
            fetchNextIfNeeded();
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            Event result = this.next;
            this.next = null;
            return result;
        }
        throw new IllegalStateException();
    }

    public void close() {
        this.walker.close();
    }
}
