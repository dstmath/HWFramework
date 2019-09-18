package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SimpleAsynchronousFileChannelImpl extends AsynchronousFileChannelImpl {
    /* access modifiers changed from: private */
    public static final FileDispatcher nd = new FileDispatcherImpl();
    /* access modifiers changed from: private */
    public final NativeThreadSet threads = new NativeThreadSet(2);

    private static class DefaultExecutorHolder {
        static final ExecutorService defaultExecutor = ThreadPool.createDefault().executor();

        private DefaultExecutorHolder() {
        }
    }

    SimpleAsynchronousFileChannelImpl(FileDescriptor fdObj, boolean reading, boolean writing, ExecutorService executor) {
        super(fdObj, reading, writing, executor);
    }

    public static AsynchronousFileChannel open(FileDescriptor fdo, boolean reading, boolean writing, ThreadPool pool) {
        return new SimpleAsynchronousFileChannelImpl(fdo, reading, writing, pool == null ? DefaultExecutorHolder.defaultExecutor : pool.executor());
    }

    public void close() throws IOException {
        synchronized (this.fdObj) {
            if (!this.closed) {
                this.closed = true;
                invalidateAllLocks();
                this.threads.signalAndWait();
                this.closeLock.writeLock().lock();
                this.closeLock.writeLock().unlock();
                nd.close(this.fdObj);
            }
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:10:0x002a=Splitter:B:10:0x002a, B:18:0x003a=Splitter:B:18:0x003a} */
    public long size() throws IOException {
        long n;
        int ti = this.threads.add();
        boolean z = false;
        try {
            begin();
            do {
                n = nd.size(this.fdObj);
                if (n != -3) {
                    break;
                }
            } while (isOpen());
            if (n >= 0) {
                z = true;
            }
            end(z);
            this.threads.remove(ti);
            return n;
        } catch (Throwable th) {
            this.threads.remove(ti);
            throw th;
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:22:0x004f=Splitter:B:22:0x004f, B:30:0x005f=Splitter:B:30:0x005f} */
    public AsynchronousFileChannel truncate(long size) throws IOException {
        long n;
        if (size < 0) {
            throw new IllegalArgumentException("Negative size");
        } else if (this.writing) {
            int ti = this.threads.add();
            boolean z = false;
            try {
                begin();
                do {
                    n = nd.size(this.fdObj);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                if (size < n && isOpen()) {
                    do {
                        n = (long) nd.truncate(this.fdObj, size);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                }
                if (n > 0) {
                    z = true;
                }
                end(z);
                this.threads.remove(ti);
                return this;
            } catch (Throwable th) {
                this.threads.remove(ti);
                throw th;
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:9:0x0022=Splitter:B:9:0x0022, B:16:0x0032=Splitter:B:16:0x0032} */
    public void force(boolean metaData) throws IOException {
        int n;
        int ti = this.threads.add();
        boolean z = false;
        try {
            begin();
            do {
                n = nd.force(this.fdObj, metaData);
                if (n != -3) {
                    break;
                }
            } while (isOpen());
            if (n >= 0) {
                z = true;
            }
            end(z);
            this.threads.remove(ti);
        } catch (Throwable th) {
            this.threads.remove(ti);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public <A> Future<FileLock> implLock(long position, long size, boolean shared, A attachment, CompletionHandler<FileLock, ? super A> handler) {
        CompletionHandler<FileLock, ? super A> completionHandler = handler;
        if (shared && !this.reading) {
            throw new NonReadableChannelException();
        } else if (shared || this.writing) {
            FileLockImpl fli = addToFileLockTable(position, size, shared);
            PendingFuture<FileLock, A> pendingFuture = null;
            if (fli == null) {
                Throwable exc = new ClosedChannelException();
                if (completionHandler == null) {
                    return CompletedFuture.withFailure(exc);
                }
                Invoker.invokeIndirectly(completionHandler, attachment, null, exc, (Executor) this.executor);
                return null;
            }
            A a = attachment;
            if (completionHandler == null) {
                pendingFuture = new PendingFuture<>(this);
            }
            PendingFuture<FileLock, A> result = pendingFuture;
            final long j = position;
            final long j2 = size;
            final boolean z = shared;
            final FileLockImpl fileLockImpl = fli;
            final CompletionHandler<FileLock, ? super A> completionHandler2 = completionHandler;
            final PendingFuture<FileLock, A> pendingFuture2 = result;
            final A a2 = attachment;
            AnonymousClass1 r1 = new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:15:0x0044, code lost:
                    r2 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
                    r11.this$0.end();
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:31:0x0088, code lost:
                    throw r2;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:32:0x0089, code lost:
                    r2 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:33:0x008a, code lost:
                    sun.nio.ch.SimpleAsynchronousFileChannelImpl.access$000(r11.this$0).remove(r1);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:34:0x0093, code lost:
                    throw r2;
                 */
                /* JADX WARNING: Exception block dominator not found, dom blocks: [B:10:0x0038, B:17:0x0047] */
                public void run() {
                    int n;
                    Throwable exc = null;
                    int ti = SimpleAsynchronousFileChannelImpl.this.threads.add();
                    try {
                        SimpleAsynchronousFileChannelImpl.this.begin();
                        do {
                            n = SimpleAsynchronousFileChannelImpl.nd.lock(SimpleAsynchronousFileChannelImpl.this.fdObj, true, j, j2, z);
                            if (n != 2) {
                                break;
                            }
                        } while (SimpleAsynchronousFileChannelImpl.this.isOpen());
                        if (n != 0 || !SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                            throw new AsynchronousCloseException();
                        }
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                        if (completionHandler2 == null) {
                            pendingFuture2.setResult(fileLockImpl, exc);
                        } else {
                            Invoker.invokeUnchecked(completionHandler2, a2, fileLockImpl, exc);
                        }
                    } catch (IOException e) {
                        x = e;
                        SimpleAsynchronousFileChannelImpl.this.removeFromFileLockTable(fileLockImpl);
                        if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                            x = new AsynchronousCloseException();
                        }
                        exc = x;
                        SimpleAsynchronousFileChannelImpl.this.end();
                    }
                }
            };
            boolean executed = false;
            try {
                this.executor.execute(r1);
                executed = true;
                return result;
            } finally {
                if (!executed) {
                    removeFromFileLockTable(fli);
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        int n;
        if (shared && !this.reading) {
            throw new NonReadableChannelException();
        } else if (shared || this.writing) {
            FileLockImpl fli = addToFileLockTable(position, size, shared);
            if (fli != null) {
                int ti = this.threads.add();
                boolean gotLock = false;
                try {
                    begin();
                    do {
                        n = nd.lock(this.fdObj, false, position, size, shared);
                        if (n != 2) {
                            break;
                        }
                    } while (isOpen());
                    if (n == 0 && isOpen()) {
                        gotLock = true;
                        return fli;
                    } else if (n == -1) {
                        if (!gotLock) {
                            removeFromFileLockTable(fli);
                        }
                        end();
                        this.threads.remove(ti);
                        return null;
                    } else if (n == 2) {
                        throw new AsynchronousCloseException();
                    } else {
                        throw new AssertionError();
                    }
                } finally {
                    if (!gotLock) {
                        removeFromFileLockTable(fli);
                    }
                    end();
                    this.threads.remove(ti);
                }
            } else {
                throw new ClosedChannelException();
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* access modifiers changed from: protected */
    public void implRelease(FileLockImpl fli) throws IOException {
        nd.release(this.fdObj, fli.position(), fli.size());
    }

    /* access modifiers changed from: package-private */
    public <A> Future<Integer> implRead(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (!this.reading) {
            throw new NonReadableChannelException();
        } else if (!dst.isReadOnly()) {
            PendingFuture<Integer, A> pendingFuture = null;
            if (!isOpen() || dst.remaining() == 0) {
                Throwable exc = isOpen() ? null : new ClosedChannelException();
                if (handler == null) {
                    return CompletedFuture.withResult(0, exc);
                }
                Invoker.invokeIndirectly(handler, attachment, 0, exc, (Executor) this.executor);
                return null;
            }
            if (handler == null) {
                pendingFuture = new PendingFuture<>(this);
            }
            PendingFuture<Integer, A> result = pendingFuture;
            final ByteBuffer byteBuffer = dst;
            final long j = position;
            final CompletionHandler<Integer, ? super A> completionHandler = handler;
            final PendingFuture<Integer, A> pendingFuture2 = result;
            final A a = attachment;
            AnonymousClass2 r2 = new Runnable() {
                public void run() {
                    int n = 0;
                    Throwable exc = null;
                    int ti = SimpleAsynchronousFileChannelImpl.this.threads.add();
                    try {
                        SimpleAsynchronousFileChannelImpl.this.begin();
                        do {
                            n = IOUtil.read(SimpleAsynchronousFileChannelImpl.this.fdObj, byteBuffer, j, SimpleAsynchronousFileChannelImpl.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (SimpleAsynchronousFileChannelImpl.this.isOpen());
                        if (n < 0) {
                            if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                                throw new AsynchronousCloseException();
                            }
                        }
                    } catch (IOException e) {
                        x = e;
                        if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                            x = new AsynchronousCloseException();
                        }
                        exc = x;
                    } catch (Throwable th) {
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                        throw th;
                    }
                    SimpleAsynchronousFileChannelImpl.this.end();
                    SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                    if (completionHandler == null) {
                        pendingFuture2.setResult(Integer.valueOf(n), exc);
                    } else {
                        Invoker.invokeUnchecked(completionHandler, a, Integer.valueOf(n), exc);
                    }
                }
            };
            this.executor.execute(r2);
            return result;
        } else {
            throw new IllegalArgumentException("Read-only buffer");
        }
    }

    /* access modifiers changed from: package-private */
    public <A> Future<Integer> implWrite(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.writing) {
            PendingFuture<Integer, A> pendingFuture = null;
            if (!isOpen() || src.remaining() == 0) {
                Throwable exc = isOpen() ? null : new ClosedChannelException();
                if (handler == null) {
                    return CompletedFuture.withResult(0, exc);
                }
                Invoker.invokeIndirectly(handler, attachment, 0, exc, (Executor) this.executor);
                return null;
            }
            if (handler == null) {
                pendingFuture = new PendingFuture<>(this);
            }
            PendingFuture<Integer, A> result = pendingFuture;
            final ByteBuffer byteBuffer = src;
            final long j = position;
            final CompletionHandler<Integer, ? super A> completionHandler = handler;
            final PendingFuture<Integer, A> pendingFuture2 = result;
            final A a = attachment;
            AnonymousClass3 r2 = new Runnable() {
                public void run() {
                    int n = 0;
                    Throwable exc = null;
                    int ti = SimpleAsynchronousFileChannelImpl.this.threads.add();
                    try {
                        SimpleAsynchronousFileChannelImpl.this.begin();
                        do {
                            n = IOUtil.write(SimpleAsynchronousFileChannelImpl.this.fdObj, byteBuffer, j, SimpleAsynchronousFileChannelImpl.nd);
                            if (n != -3) {
                                break;
                            }
                        } while (SimpleAsynchronousFileChannelImpl.this.isOpen());
                        if (n < 0) {
                            if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                                throw new AsynchronousCloseException();
                            }
                        }
                    } catch (IOException e) {
                        x = e;
                        if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                            x = new AsynchronousCloseException();
                        }
                        exc = x;
                    } catch (Throwable th) {
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                        throw th;
                    }
                    SimpleAsynchronousFileChannelImpl.this.end();
                    SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                    if (completionHandler == null) {
                        pendingFuture2.setResult(Integer.valueOf(n), exc);
                    } else {
                        Invoker.invokeUnchecked(completionHandler, a, Integer.valueOf(n), exc);
                    }
                }
            };
            this.executor.execute(r2);
            return result;
        } else {
            throw new NonWritableChannelException();
        }
    }
}
