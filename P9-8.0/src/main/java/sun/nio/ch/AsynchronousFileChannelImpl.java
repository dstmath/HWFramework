package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

abstract class AsynchronousFileChannelImpl extends AsynchronousFileChannel {
    protected final ReadWriteLock closeLock = new ReentrantReadWriteLock();
    protected volatile boolean closed;
    protected final ExecutorService executor;
    protected final FileDescriptor fdObj;
    private volatile FileLockTable fileLockTable;
    protected final boolean reading;
    protected final boolean writing;

    abstract <A> Future<FileLock> implLock(long j, long j2, boolean z, A a, CompletionHandler<FileLock, ? super A> completionHandler);

    abstract <A> Future<Integer> implRead(ByteBuffer byteBuffer, long j, A a, CompletionHandler<Integer, ? super A> completionHandler);

    protected abstract void implRelease(FileLockImpl fileLockImpl) throws IOException;

    abstract <A> Future<Integer> implWrite(ByteBuffer byteBuffer, long j, A a, CompletionHandler<Integer, ? super A> completionHandler);

    protected AsynchronousFileChannelImpl(FileDescriptor fdObj, boolean reading, boolean writing, ExecutorService executor) {
        this.fdObj = fdObj;
        this.reading = reading;
        this.writing = writing;
        this.executor = executor;
    }

    final ExecutorService executor() {
        return this.executor;
    }

    public final boolean isOpen() {
        return this.closed ^ 1;
    }

    protected final void begin() throws IOException {
        this.closeLock.readLock().lock();
        if (this.closed) {
            throw new ClosedChannelException();
        }
    }

    protected final void end() {
        this.closeLock.readLock().unlock();
    }

    protected final void end(boolean completed) throws IOException {
        end();
        if (!completed && (isOpen() ^ 1) != 0) {
            throw new AsynchronousCloseException();
        }
    }

    public final Future<FileLock> lock(long position, long size, boolean shared) {
        return implLock(position, size, shared, null, null);
    }

    public final <A> void lock(long position, long size, boolean shared, A attachment, CompletionHandler<FileLock, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        }
        implLock(position, size, shared, attachment, handler);
    }

    final void ensureFileLockTableInitialized() throws IOException {
        if (this.fileLockTable == null) {
            synchronized (this) {
                if (this.fileLockTable == null) {
                    this.fileLockTable = FileLockTable.newSharedFileLockTable(this, this.fdObj);
                }
            }
        }
    }

    final void invalidateAllLocks() throws IOException {
        if (this.fileLockTable != null) {
            for (FileLock fl : this.fileLockTable.removeAll()) {
                synchronized (fl) {
                    if (fl.isValid()) {
                        FileLockImpl fli = (FileLockImpl) fl;
                        implRelease(fli);
                        fli.invalidate();
                    }
                }
            }
        }
    }

    protected final FileLockImpl addToFileLockTable(long position, long size, boolean shared) {
        try {
            this.closeLock.readLock().lock();
            if (this.closed) {
                end();
                return null;
            }
            ensureFileLockTableInitialized();
            FileLockImpl fli = new FileLockImpl((AsynchronousFileChannel) this, position, size, shared);
            this.fileLockTable.add(fli);
            end();
            return fli;
        } catch (Object x) {
            throw new AssertionError(x);
        } catch (Throwable th) {
            end();
        }
    }

    protected final void removeFromFileLockTable(FileLockImpl fli) {
        this.fileLockTable.remove(fli);
    }

    final void release(FileLockImpl fli) throws IOException {
        try {
            begin();
            implRelease(fli);
            removeFromFileLockTable(fli);
        } finally {
            end();
        }
    }

    public final Future<Integer> read(ByteBuffer dst, long position) {
        return implRead(dst, position, null, null);
    }

    public final <A> void read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        }
        implRead(dst, position, attachment, handler);
    }

    public final Future<Integer> write(ByteBuffer src, long position) {
        return implWrite(src, position, null, null);
    }

    public final <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        }
        implWrite(src, position, attachment, handler);
    }
}
