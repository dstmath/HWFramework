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

    /* access modifiers changed from: package-private */
    public abstract <A> Future<FileLock> implLock(long j, long j2, boolean z, A a, CompletionHandler<FileLock, ? super A> completionHandler);

    /* access modifiers changed from: package-private */
    public abstract <A> Future<Integer> implRead(ByteBuffer byteBuffer, long j, A a, CompletionHandler<Integer, ? super A> completionHandler);

    /* access modifiers changed from: protected */
    public abstract void implRelease(FileLockImpl fileLockImpl) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract <A> Future<Integer> implWrite(ByteBuffer byteBuffer, long j, A a, CompletionHandler<Integer, ? super A> completionHandler);

    protected AsynchronousFileChannelImpl(FileDescriptor fdObj2, boolean reading2, boolean writing2, ExecutorService executor2) {
        this.fdObj = fdObj2;
        this.reading = reading2;
        this.writing = writing2;
        this.executor = executor2;
    }

    /* access modifiers changed from: package-private */
    public final ExecutorService executor() {
        return this.executor;
    }

    public final boolean isOpen() {
        return !this.closed;
    }

    /* access modifiers changed from: protected */
    public final void begin() throws IOException {
        this.closeLock.readLock().lock();
        if (this.closed) {
            throw new ClosedChannelException();
        }
    }

    /* access modifiers changed from: protected */
    public final void end() {
        this.closeLock.readLock().unlock();
    }

    /* access modifiers changed from: protected */
    public final void end(boolean completed) throws IOException {
        end();
        if (!completed && !isOpen()) {
            throw new AsynchronousCloseException();
        }
    }

    public final Future<FileLock> lock(long position, long size, boolean shared) {
        return implLock(position, size, shared, null, null);
    }

    public final <A> void lock(long position, long size, boolean shared, A attachment, CompletionHandler<FileLock, ? super A> handler) {
        if (handler != null) {
            implLock(position, size, shared, attachment, handler);
            return;
        }
        throw new NullPointerException("'handler' is null");
    }

    /* access modifiers changed from: package-private */
    public final void ensureFileLockTableInitialized() throws IOException {
        if (this.fileLockTable == null) {
            synchronized (this) {
                if (this.fileLockTable == null) {
                    this.fileLockTable = FileLockTable.newSharedFileLockTable(this, this.fdObj);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void invalidateAllLocks() throws IOException {
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

    /* access modifiers changed from: protected */
    public final FileLockImpl addToFileLockTable(long position, long size, boolean shared) {
        try {
            this.closeLock.readLock().lock();
            if (this.closed) {
                end();
                return null;
            }
            ensureFileLockTableInitialized();
            FileLockImpl fileLockImpl = new FileLockImpl((AsynchronousFileChannel) this, position, size, shared);
            this.fileLockTable.add(fileLockImpl);
            end();
            return fileLockImpl;
        } catch (IOException x) {
            throw new AssertionError((Object) x);
        } catch (Throwable x2) {
            end();
            throw x2;
        }
    }

    /* access modifiers changed from: protected */
    public final void removeFromFileLockTable(FileLockImpl fli) {
        this.fileLockTable.remove(fli);
    }

    /* access modifiers changed from: package-private */
    public final void release(FileLockImpl fli) throws IOException {
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
        if (handler != null) {
            implRead(dst, position, attachment, handler);
            return;
        }
        throw new NullPointerException("'handler' is null");
    }

    public final Future<Integer> write(ByteBuffer src, long position) {
        return implWrite(src, position, null, null);
    }

    public final <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler != null) {
            implWrite(src, position, attachment, handler);
            return;
        }
        throw new NullPointerException("'handler' is null");
    }
}
