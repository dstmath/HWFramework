package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileTreeWalker;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

class FileTreeIterator implements Iterator<FileTreeWalker.Event>, Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private FileTreeWalker.Event next;
    private final FileTreeWalker walker;

    FileTreeIterator(Path start, int maxDepth, FileVisitOption... options) throws IOException {
        this.walker = new FileTreeWalker(Arrays.asList(options), maxDepth);
        this.next = this.walker.walk(start);
        IOException ioe = this.next.ioeException();
        if (ioe != null) {
            throw ioe;
        }
    }

    private void fetchNextIfNeeded() {
        if (this.next == null) {
            FileTreeWalker.Event ev = this.walker.next();
            while (ev != null) {
                IOException ioe = ev.ioeException();
                if (ioe != null) {
                    throw new UncheckedIOException(ioe);
                } else if (ev.type() != FileTreeWalker.EventType.END_DIRECTORY) {
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
        }
        throw new IllegalStateException();
    }

    public FileTreeWalker.Event next() {
        if (this.walker.isOpen()) {
            fetchNextIfNeeded();
            if (this.next != null) {
                FileTreeWalker.Event result = this.next;
                this.next = null;
                return result;
            }
            throw new NoSuchElementException();
        }
        throw new IllegalStateException();
    }

    public void close() {
        this.walker.close();
    }
}
