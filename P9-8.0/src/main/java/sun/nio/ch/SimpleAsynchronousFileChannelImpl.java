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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SimpleAsynchronousFileChannelImpl extends AsynchronousFileChannelImpl {
    private static final FileDispatcher nd = new FileDispatcherImpl();
    private final NativeThreadSet threads = new NativeThreadSet(2);

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
            if (this.closed) {
                return;
            }
            this.closed = true;
            invalidateAllLocks();
            this.threads.signalAndWait();
            this.closeLock.writeLock().lock();
            this.closeLock.writeLock().unlock();
            nd.close(this.fdObj);
        }
    }

    public long size() throws IOException {
        boolean z = true;
        int ti = this.threads.add();
        try {
            long n;
            begin();
            do {
                n = nd.size(this.fdObj);
                if (n != -3) {
                    break;
                }
            } while (isOpen());
            if (n < 0) {
                z = false;
            }
            end(z);
            this.threads.remove(ti);
            return n;
        } catch (Throwable th) {
            this.threads.remove(ti);
        }
    }

    public AsynchronousFileChannel truncate(long size) throws IOException {
        boolean z = true;
        if (size < 0) {
            throw new IllegalArgumentException("Negative size");
        } else if (this.writing) {
            int ti = this.threads.add();
            try {
                long n;
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
                if (n <= 0) {
                    z = false;
                }
                end(z);
                this.threads.remove(ti);
                return this;
            } catch (Throwable th) {
                this.threads.remove(ti);
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public void force(boolean metaData) throws IOException {
        boolean z = true;
        int ti = this.threads.add();
        try {
            int n;
            begin();
            do {
                n = nd.force(this.fdObj, metaData);
                if (n != -3) {
                    break;
                }
            } while (isOpen());
            if (n < 0) {
                z = false;
            }
            end(z);
            this.threads.remove(ti);
        } catch (Throwable th) {
            this.threads.remove(ti);
        }
    }

    <A> Future<FileLock> implLock(long position, long size, boolean shared, A attachment, CompletionHandler<FileLock, ? super A> handler) {
        if (shared && (this.reading ^ 1) != 0) {
            throw new NonReadableChannelException();
        } else if (shared || (this.writing ^ 1) == 0) {
            final FileLockImpl fli = addToFileLockTable(position, size, shared);
            if (fli == null) {
                Throwable exc = new ClosedChannelException();
                if (handler == null) {
                    return CompletedFuture.withFailure(exc);
                }
                Invoker.invokeIndirectly((CompletionHandler) handler, (Object) attachment, null, exc, this.executor);
                return null;
            }
            final Future result = handler == null ? new PendingFuture(this) : null;
            final long j = position;
            final long j2 = size;
            final boolean z = shared;
            final CompletionHandler<FileLock, ? super A> completionHandler = handler;
            final A a = attachment;
            boolean executed = false;
            try {
                this.executor.execute(new Runnable() {
                    public void run() {
                        Throwable exc = null;
                        int ti = SimpleAsynchronousFileChannelImpl.this.threads.add();
                        try {
                            int n;
                            SimpleAsynchronousFileChannelImpl.this.begin();
                            do {
                                n = SimpleAsynchronousFileChannelImpl.nd.lock(SimpleAsynchronousFileChannelImpl.this.fdObj, true, j, j2, z);
                                if (n != 2) {
                                    break;
                                }
                            } while (SimpleAsynchronousFileChannelImpl.this.isOpen());
                            if (n == 0 && (SimpleAsynchronousFileChannelImpl.this.isOpen() ^ 1) == 0) {
                                SimpleAsynchronousFileChannelImpl.this.end();
                                SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                                if (completionHandler == null) {
                                    result.setResult(fli, exc);
                                    return;
                                } else {
                                    Invoker.invokeUnchecked(completionHandler, a, fli, exc);
                                    return;
                                }
                            }
                            throw new AsynchronousCloseException();
                        } catch (IOException e) {
                            Throwable x = e;
                            SimpleAsynchronousFileChannelImpl.this.removeFromFileLockTable(fli);
                            if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                                x = new AsynchronousCloseException();
                            }
                            exc = x;
                            SimpleAsynchronousFileChannelImpl.this.end();
                        } catch (Throwable th) {
                            SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                        }
                    }
                });
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
        if (shared && (this.reading ^ 1) != 0) {
            throw new NonReadableChannelException();
        } else if (shared || (this.writing ^ 1) == 0) {
            FileLockImpl fli = addToFileLockTable(position, size, shared);
            if (fli == null) {
                throw new ClosedChannelException();
            }
            int ti = this.threads.add();
            try {
                int n;
                begin();
                do {
                    n = nd.lock(this.fdObj, false, position, size, shared);
                    if (n != 2) {
                        break;
                    }
                } while (isOpen());
                if (n == 0 && isOpen()) {
                    if (!true) {
                        removeFromFileLockTable(fli);
                    }
                    end();
                    this.threads.remove(ti);
                    return fli;
                } else if (n == -1) {
                    if (null == null) {
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
            } catch (Throwable th) {
                if (null == null) {
                    removeFromFileLockTable(fli);
                }
                end();
                this.threads.remove(ti);
                throw th;
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    protected void implRelease(FileLockImpl fli) throws IOException {
        nd.release(this.fdObj, fli.position(), fli.size());
    }

    <A> Future<Integer> implRead(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (!this.reading) {
            throw new NonReadableChannelException();
        } else if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else if (!isOpen() || dst.remaining() == 0) {
            Throwable exc = isOpen() ? null : new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withResult(Integer.valueOf(0), exc);
            }
            Invoker.invokeIndirectly((CompletionHandler) handler, (Object) attachment, Integer.valueOf(0), exc, this.executor);
            return null;
        } else {
            final Future result = handler == null ? new PendingFuture(this) : null;
            final ByteBuffer byteBuffer = dst;
            final long j = position;
            final CompletionHandler<Integer, ? super A> completionHandler = handler;
            final A a = attachment;
            this.executor.execute(new Runnable() {
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
                        if (n >= 0 || (SimpleAsynchronousFileChannelImpl.this.isOpen() ^ 1) == 0) {
                            SimpleAsynchronousFileChannelImpl.this.end();
                            SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                            if (completionHandler == null) {
                                result.setResult(Integer.valueOf(n), exc);
                                return;
                            } else {
                                Invoker.invokeUnchecked(completionHandler, a, Integer.valueOf(n), exc);
                                return;
                            }
                        }
                        throw new AsynchronousCloseException();
                    } catch (IOException e) {
                        Throwable x = e;
                        if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                            x = new AsynchronousCloseException();
                        }
                        exc = x;
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                    } catch (Throwable th) {
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                    }
                }
            });
            return result;
        }
    }

    <A> Future<Integer> implWrite(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (!this.writing) {
            throw new NonWritableChannelException();
        } else if (!isOpen() || src.remaining() == 0) {
            Throwable exc = isOpen() ? null : new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withResult(Integer.valueOf(0), exc);
            }
            Invoker.invokeIndirectly((CompletionHandler) handler, (Object) attachment, Integer.valueOf(0), exc, this.executor);
            return null;
        } else {
            final Future result = handler == null ? new PendingFuture(this) : null;
            final ByteBuffer byteBuffer = src;
            final long j = position;
            final CompletionHandler<Integer, ? super A> completionHandler = handler;
            final A a = attachment;
            this.executor.execute(new Runnable() {
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
                        if (n >= 0 || (SimpleAsynchronousFileChannelImpl.this.isOpen() ^ 1) == 0) {
                            SimpleAsynchronousFileChannelImpl.this.end();
                            SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                            if (completionHandler == null) {
                                result.setResult(Integer.valueOf(n), exc);
                                return;
                            } else {
                                Invoker.invokeUnchecked(completionHandler, a, Integer.valueOf(n), exc);
                                return;
                            }
                        }
                        throw new AsynchronousCloseException();
                    } catch (IOException e) {
                        Throwable x = e;
                        if (!SimpleAsynchronousFileChannelImpl.this.isOpen()) {
                            x = new AsynchronousCloseException();
                        }
                        exc = x;
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                    } catch (Throwable th) {
                        SimpleAsynchronousFileChannelImpl.this.end();
                        SimpleAsynchronousFileChannelImpl.this.threads.remove(ti);
                    }
                }
            });
            return result;
        }
    }
}
