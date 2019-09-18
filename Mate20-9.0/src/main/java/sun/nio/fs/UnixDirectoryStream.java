package sun.nio.fs;

import dalvik.system.CloseGuard;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UnixDirectoryStream implements DirectoryStream<Path> {
    /* access modifiers changed from: private */
    public final UnixPath dir;
    /* access modifiers changed from: private */
    public final long dp;
    /* access modifiers changed from: private */
    public final DirectoryStream.Filter<? super Path> filter;
    private final CloseGuard guard = CloseGuard.get();
    private volatile boolean isClosed;
    private Iterator<Path> iterator;
    private final ReentrantReadWriteLock streamLock = new ReentrantReadWriteLock(true);

    private class UnixDirectoryIterator implements Iterator<Path> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private boolean atEof = false;
        private Path nextEntry;
        private final DirectoryStream<Path> stream;

        static {
            Class<UnixDirectoryStream> cls = UnixDirectoryStream.class;
        }

        UnixDirectoryIterator(DirectoryStream<Path> stream2) {
            this.stream = stream2;
        }

        private boolean isSelfOrParent(byte[] nameAsBytes) {
            if (nameAsBytes[0] == 46 && (nameAsBytes.length == 1 || (nameAsBytes.length == 2 && nameAsBytes[1] == 46))) {
                return true;
            }
            return false;
        }

        private Path readNextEntry() {
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
                    throw th;
                }
            }
            return entry;
        }

        public synchronized boolean hasNext() {
            if (this.nextEntry == null && !this.atEof) {
                this.nextEntry = readNextEntry();
            }
            return this.nextEntry != null;
        }

        public synchronized Path next() {
            Path result;
            if (this.nextEntry != null || this.atEof) {
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

    UnixDirectoryStream(UnixPath dir2, long dp2, DirectoryStream.Filter<? super Path> filter2) {
        this.dir = dir2;
        this.dp = dp2;
        this.filter = filter2;
        this.guard.open("close");
    }

    /* access modifiers changed from: protected */
    public final UnixPath directory() {
        return this.dir;
    }

    /* access modifiers changed from: protected */
    public final Lock readLock() {
        return this.streamLock.readLock();
    }

    /* access modifiers changed from: protected */
    public final Lock writeLock() {
        return this.streamLock.writeLock();
    }

    /* access modifiers changed from: protected */
    public final boolean isOpen() {
        return !this.isClosed;
    }

    /* access modifiers changed from: protected */
    public final boolean closeImpl() throws IOException {
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

    /* access modifiers changed from: protected */
    public final Iterator<Path> iterator(DirectoryStream<Path> ds) {
        Iterator<Path> it;
        if (!this.isClosed) {
            synchronized (this) {
                if (this.iterator == null) {
                    this.iterator = new UnixDirectoryIterator(ds);
                    it = this.iterator;
                } else {
                    throw new IllegalStateException("Iterator already obtained");
                }
            }
            return it;
        }
        throw new IllegalStateException("Directory stream is closed");
    }

    public Iterator<Path> iterator() {
        return iterator(this);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }
}
