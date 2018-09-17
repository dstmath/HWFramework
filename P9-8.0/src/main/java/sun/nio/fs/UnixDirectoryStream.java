package sun.nio.fs;

import dalvik.system.CloseGuard;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UnixDirectoryStream implements DirectoryStream<Path> {
    private final UnixPath dir;
    private final long dp;
    private final Filter<? super Path> filter;
    private final CloseGuard guard = CloseGuard.get();
    private volatile boolean isClosed;
    private Iterator<Path> iterator;
    private final ReentrantReadWriteLock streamLock = new ReentrantReadWriteLock(true);

    private class UnixDirectoryIterator implements Iterator<Path> {
        static final /* synthetic */ boolean -assertionsDisabled = (UnixDirectoryIterator.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        private boolean atEof = false;
        private Path nextEntry;
        private final DirectoryStream<Path> stream;

        UnixDirectoryIterator(DirectoryStream<Path> stream) {
            this.stream = stream;
        }

        private boolean isSelfOrParent(byte[] nameAsBytes) {
            return nameAsBytes[0] == (byte) 46 && (nameAsBytes.length == 1 || (nameAsBytes.length == 2 && nameAsBytes[1] == (byte) 46));
        }

        private Path readNextEntry() {
            if (-assertionsDisabled || Thread.holdsLock(this)) {
                Path entry;
                while (true) {
                    byte[] nameAsBytes = null;
                    UnixDirectoryStream.this.readLock().lock();
                    try {
                        if (UnixDirectoryStream.this.isOpen()) {
                            nameAsBytes = UnixNativeDispatcher.readdir(UnixDirectoryStream.this.dp);
                        }
                        UnixDirectoryStream.this.readLock().unlock();
                        if (nameAsBytes == null) {
                            this.atEof = true;
                            return null;
                        } else if (!isSelfOrParent(nameAsBytes)) {
                            entry = UnixDirectoryStream.this.dir.resolve(nameAsBytes);
                            try {
                                if (UnixDirectoryStream.this.filter == null || UnixDirectoryStream.this.filter.accept(entry)) {
                                    return entry;
                                }
                            } catch (IOException ioe) {
                                throw new DirectoryIteratorException(ioe);
                            }
                        }
                    } catch (UnixException x) {
                        throw new DirectoryIteratorException(x.asIOException(UnixDirectoryStream.this.dir));
                    } catch (Throwable th) {
                        UnixDirectoryStream.this.readLock().unlock();
                    }
                }
                return entry;
            }
            throw new AssertionError();
        }

        public synchronized boolean hasNext() {
            if (this.nextEntry == null && (this.atEof ^ 1) != 0) {
                this.nextEntry = readNextEntry();
            }
            return this.nextEntry != null;
        }

        public synchronized Path next() {
            Path result;
            if (this.nextEntry != null || (this.atEof ^ 1) == 0) {
                result = this.nextEntry;
                this.nextEntry = null;
            } else {
                result = readNextEntry();
            }
            if (result == null) {
                throw new NoSuchElementException();
            }
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    UnixDirectoryStream(UnixPath dir, long dp, Filter<? super Path> filter) {
        this.dir = dir;
        this.dp = dp;
        this.filter = filter;
        this.guard.open("close");
    }

    protected final UnixPath directory() {
        return this.dir;
    }

    protected final Lock readLock() {
        return this.streamLock.readLock();
    }

    protected final Lock writeLock() {
        return this.streamLock.writeLock();
    }

    protected final boolean isOpen() {
        return this.isClosed ^ 1;
    }

    protected final boolean closeImpl() throws IOException {
        if (this.isClosed) {
            return false;
        }
        this.isClosed = true;
        try {
            UnixNativeDispatcher.closedir(this.dp);
            this.guard.close();
            return true;
        } catch (UnixException x) {
            throw new IOException(x.errorString());
        }
    }

    public void close() throws IOException {
        writeLock().lock();
        try {
            closeImpl();
        } finally {
            writeLock().unlock();
        }
    }

    protected final Iterator<Path> iterator(DirectoryStream<Path> ds) {
        if (this.isClosed) {
            throw new IllegalStateException("Directory stream is closed");
        }
        Iterator<Path> it;
        synchronized (this) {
            if (this.iterator != null) {
                throw new IllegalStateException("Iterator already obtained");
            }
            this.iterator = new UnixDirectoryIterator(ds);
            it = this.iterator;
        }
        return it;
    }

    public Iterator<Path> iterator() {
        return iterator(this);
    }

    protected void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }
}
