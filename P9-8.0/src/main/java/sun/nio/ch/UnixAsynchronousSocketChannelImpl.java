package sun.nio.ch;

import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ConnectionPendingException;
import java.security.AccessController;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import sun.net.NetHooks;
import sun.security.action.GetPropertyAction;

class UnixAsynchronousSocketChannelImpl extends AsynchronousSocketChannelImpl implements PollableChannel {
    static final /* synthetic */ boolean -assertionsDisabled = (UnixAsynchronousSocketChannelImpl.class.desiredAssertionStatus() ^ 1);
    private static final boolean disableSynchronousRead;
    private static final NativeDispatcher nd = new SocketDispatcher();
    private Object connectAttachment;
    private PendingFuture<Void, Object> connectFuture;
    private CompletionHandler<Void, Object> connectHandler;
    private boolean connectPending;
    private final int fdVal;
    private final CloseGuard guard = CloseGuard.get();
    private boolean isGatheringWrite;
    private boolean isScatteringRead;
    private SocketAddress pendingRemote;
    private final Port port;
    private Object readAttachment;
    private ByteBuffer readBuffer;
    private ByteBuffer[] readBuffers;
    private PendingFuture<Number, Object> readFuture;
    private CompletionHandler<Number, Object> readHandler;
    private boolean readPending;
    private Runnable readTimeoutTask = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x002e, code:
            r9.this$0.enableReading(true);
            r2 = new java.nio.channels.InterruptedByTimeoutException();
     */
        /* JADX WARNING: Missing block: B:11:0x0039, code:
            if (r4 != null) goto L_0x0042;
     */
        /* JADX WARNING: Missing block: B:12:0x003b, code:
            r3.setFailure(r2);
     */
        /* JADX WARNING: Missing block: B:13:0x003e, code:
            return;
     */
        /* JADX WARNING: Missing block: B:17:0x0042, code:
            sun.nio.ch.Invoker.invokeIndirectly(r9.this$0, r4, r0, null, r2);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (UnixAsynchronousSocketChannelImpl.this.updateLock) {
                if (UnixAsynchronousSocketChannelImpl.this.readPending) {
                    UnixAsynchronousSocketChannelImpl.this.readPending = false;
                    CompletionHandler handler = UnixAsynchronousSocketChannelImpl.this.readHandler;
                    Object att = UnixAsynchronousSocketChannelImpl.this.readAttachment;
                    PendingFuture<Number, Object> future = UnixAsynchronousSocketChannelImpl.this.readFuture;
                }
            }
        }
    };
    private Future<?> readTimer;
    private final Object updateLock = new Object();
    private Object writeAttachment;
    private ByteBuffer writeBuffer;
    private ByteBuffer[] writeBuffers;
    private PendingFuture<Number, Object> writeFuture;
    private CompletionHandler<Number, Object> writeHandler;
    private boolean writePending;
    private Runnable writeTimeoutTask = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x002e, code:
            r8.this$0.enableWriting(true);
            r1 = new java.nio.channels.InterruptedByTimeoutException();
     */
        /* JADX WARNING: Missing block: B:11:0x0039, code:
            if (r3 == null) goto L_0x0044;
     */
        /* JADX WARNING: Missing block: B:12:0x003b, code:
            sun.nio.ch.Invoker.invokeIndirectly(r8.this$0, r3, r0, null, r1);
     */
        /* JADX WARNING: Missing block: B:13:0x0040, code:
            return;
     */
        /* JADX WARNING: Missing block: B:17:0x0044, code:
            r2.setFailure(r1);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (UnixAsynchronousSocketChannelImpl.this.updateLock) {
                if (UnixAsynchronousSocketChannelImpl.this.writePending) {
                    UnixAsynchronousSocketChannelImpl.this.writePending = false;
                    CompletionHandler handler = UnixAsynchronousSocketChannelImpl.this.writeHandler;
                    Object att = UnixAsynchronousSocketChannelImpl.this.writeAttachment;
                    PendingFuture<Number, Object> future = UnixAsynchronousSocketChannelImpl.this.writeFuture;
                }
            }
        }
    };
    private Future<?> writeTimer;

    private enum OpType {
        CONNECT,
        READ,
        WRITE
    }

    private static native void checkConnect(int i) throws IOException;

    static {
        String propValue = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.ch.disableSynchronousRead", "false"));
        disableSynchronousRead = propValue.length() == 0 ? true : Boolean.valueOf(propValue).booleanValue();
    }

    UnixAsynchronousSocketChannelImpl(Port port) throws IOException {
        super(port);
        try {
            IOUtil.configureBlocking(this.fd, false);
            this.port = port;
            this.fdVal = IOUtil.fdVal(this.fd);
            port.register(this.fdVal, this);
            this.guard.open("close");
        } catch (IOException x) {
            nd.close(this.fd);
            throw x;
        }
    }

    UnixAsynchronousSocketChannelImpl(Port port, FileDescriptor fd, InetSocketAddress remote) throws IOException {
        super(port, fd, remote);
        this.fdVal = IOUtil.fdVal(fd);
        IOUtil.configureBlocking(fd, false);
        try {
            port.register(this.fdVal, this);
            this.port = port;
            this.guard.open("close");
        } catch (Throwable x) {
            throw new IOException(x);
        }
    }

    public AsynchronousChannelGroupImpl group() {
        return this.port;
    }

    private void updateEvents() {
        if (-assertionsDisabled || Thread.holdsLock(this.updateLock)) {
            int events = 0;
            if (this.readPending) {
                events = Net.POLLIN | 0;
            }
            if (this.connectPending || this.writePending) {
                events |= Net.POLLOUT;
            }
            if (events != 0) {
                this.port.startPoll(this.fdVal, events);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    private void lockAndUpdateEvents() {
        synchronized (this.updateLock) {
            updateEvents();
        }
    }

    private void finish(boolean mayInvokeDirect, boolean readable, boolean writable) {
        boolean finishRead = false;
        boolean finishWrite = false;
        boolean finishConnect = false;
        synchronized (this.updateLock) {
            if (readable) {
                if (this.readPending) {
                    this.readPending = false;
                    finishRead = true;
                }
            }
            if (writable) {
                if (this.writePending) {
                    this.writePending = false;
                    finishWrite = true;
                } else if (this.connectPending) {
                    this.connectPending = false;
                    finishConnect = true;
                }
            }
        }
        if (finishRead) {
            if (finishWrite) {
                finishWrite(false);
            }
            finishRead(mayInvokeDirect);
            return;
        }
        if (finishWrite) {
            finishWrite(mayInvokeDirect);
        }
        if (finishConnect) {
            finishConnect(mayInvokeDirect);
        }
    }

    public void onEvent(int events, boolean mayInvokeDirect) {
        boolean readable = (Net.POLLIN & events) > 0;
        boolean writable = (Net.POLLOUT & events) > 0;
        if (((Net.POLLERR | Net.POLLHUP) & events) > 0) {
            readable = true;
            writable = true;
        }
        finish(mayInvokeDirect, readable, writable);
    }

    void implClose() throws IOException {
        this.guard.close();
        this.port.unregister(this.fdVal);
        nd.close(this.fd);
        finish(false, true, true);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    public void onCancel(PendingFuture<?, ?> task) {
        if (task.getContext() == OpType.CONNECT) {
            killConnect();
        }
        if (task.getContext() == OpType.READ) {
            killReading();
        }
        if (task.getContext() == OpType.WRITE) {
            killWriting();
        }
    }

    private void setConnected() throws IOException {
        synchronized (this.stateLock) {
            this.state = 2;
            this.localAddress = Net.localAddress(this.fd);
            this.remoteAddress = (InetSocketAddress) this.pendingRemote;
        }
    }

    private void finishConnect(boolean mayInvokeDirect) {
        Throwable e = null;
        try {
            begin();
            checkConnect(this.fdVal);
            setConnected();
            end();
        } catch (Throwable th) {
            end();
            throw th;
        }
        if (e != null) {
            try {
                close();
            } catch (Throwable suppressed) {
                e.addSuppressed(suppressed);
            }
        }
        CompletionHandler handler = this.connectHandler;
        Object att = this.connectAttachment;
        PendingFuture<Void, Object> future = this.connectFuture;
        if (handler == null) {
            future.setResult(null, e);
        } else if (mayInvokeDirect) {
            Invoker.invokeUnchecked(handler, att, null, e);
        } else {
            Invoker.invokeIndirectly((AsynchronousChannel) this, handler, att, null, e);
        }
    }

    <A> Future<Void> implConnect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        Throwable th;
        Throwable e;
        if (isOpen()) {
            boolean notifyBeforeTcpConnect;
            InetSocketAddress isa = Net.checkAddress(remote);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
            }
            synchronized (this.stateLock) {
                try {
                    if (this.state == 2) {
                        throw new AlreadyConnectedException();
                    } else if (this.state == 1) {
                        throw new ConnectionPendingException();
                    } else {
                        this.state = 1;
                        this.pendingRemote = remote;
                        notifyBeforeTcpConnect = this.localAddress == null;
                    }
                } catch (Throwable th2) {
                    throw th2;
                }
            }
            e = null;
            try {
                begin();
                if (notifyBeforeTcpConnect) {
                    NetHooks.beforeTcpConnect(this.fd, isa.getAddress(), isa.getPort());
                }
                if (Net.connect(this.fd, isa.getAddress(), isa.getPort()) == -2) {
                    Future<Void> future = null;
                    synchronized (this.updateLock) {
                        if (handler == null) {
                            PendingFuture<Void, A> result;
                            try {
                                result = new PendingFuture(this, OpType.CONNECT);
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                            try {
                                this.connectFuture = result;
                                future = result;
                            } catch (Throwable th4) {
                                th2 = th4;
                                throw th2;
                            }
                        }
                        this.connectHandler = handler;
                        this.connectAttachment = attachment;
                        this.connectPending = true;
                        updateEvents();
                        end();
                        return future;
                    }
                }
                setConnected();
                end();
                if (e != null) {
                    try {
                        close();
                    } catch (Throwable suppressed) {
                        e.addSuppressed(suppressed);
                    }
                }
                if (handler == null) {
                    return CompletedFuture.withResult(null, e);
                }
                Invoker.invoke(this, handler, attachment, null, e);
                return null;
            } catch (Throwable th22) {
                end();
                throw th22;
            }
        }
        e = new ClosedChannelException();
        if (handler == null) {
            return CompletedFuture.withFailure(e);
        }
        Invoker.invoke(this, handler, attachment, null, e);
        return null;
    }

    private void finishRead(boolean mayInvokeDirect) {
        int n = -1;
        Throwable exc = null;
        boolean scattering = this.isScatteringRead;
        CompletionHandler handler = this.readHandler;
        Object att = this.readAttachment;
        PendingFuture<Number, Object> future = this.readFuture;
        Future<?> timeout = this.readTimer;
        try {
            begin();
            if (scattering) {
                n = (int) IOUtil.read(this.fd, this.readBuffers, nd);
            } else {
                n = IOUtil.read(this.fd, this.readBuffer, -1, nd);
            }
            if (n == -2) {
                synchronized (this.updateLock) {
                    this.readPending = true;
                }
                if (!(null instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
                return;
            }
            this.readBuffer = null;
            this.readBuffers = null;
            this.readAttachment = null;
            enableReading();
            if (!(null instanceof AsynchronousCloseException)) {
                lockAndUpdateEvents();
            }
            end();
            if (timeout != null) {
                timeout.cancel(false);
            }
            Object result = exc != null ? null : scattering ? Long.valueOf((long) n) : Integer.valueOf(n);
            if (handler == null) {
                future.setResult(result, exc);
            } else if (mayInvokeDirect) {
                Invoker.invokeUnchecked(handler, att, result, exc);
            } else {
                Invoker.invokeIndirectly((AsynchronousChannel) this, handler, att, result, exc);
            }
        } catch (Throwable th) {
            Throwable x = th;
            try {
                enableReading();
                if (x instanceof ClosedChannelException) {
                    x = new AsynchronousCloseException();
                }
                exc = x;
                if (!(exc instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
            } catch (Throwable th2) {
                if (!(null instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
            }
        }
    }

    /* JADX WARNING: Missing block: B:28:0x007d, code:
            r9 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    <V extends Number, A> Future<V> implRead(boolean isScatteringRead, ByteBuffer dst, ByteBuffer[] dsts, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
        Throwable th;
        GroupAndInvokeCount myGroupAndInvokeCount = null;
        boolean invokeDirect = false;
        boolean attemptRead = false;
        if (!disableSynchronousRead) {
            if (handler == null) {
                attemptRead = true;
            } else {
                myGroupAndInvokeCount = Invoker.getGroupAndInvokeCount();
                invokeDirect = Invoker.mayInvokeDirect(myGroupAndInvokeCount, this.port);
                attemptRead = !invokeDirect ? this.port.isFixedThreadPool() ^ 1 : true;
            }
        }
        int n = -2;
        Throwable th2 = null;
        boolean pending = false;
        try {
            Future<V> result;
            begin();
            if (attemptRead) {
                if (isScatteringRead) {
                    n = (int) IOUtil.read(this.fd, dsts, nd);
                } else {
                    n = IOUtil.read(this.fd, dst, -1, nd);
                }
            }
            if (n == -2) {
                result = null;
                synchronized (this.updateLock) {
                    try {
                        this.isScatteringRead = isScatteringRead;
                        this.readBuffer = dst;
                        this.readBuffers = dsts;
                        if (handler == null) {
                            this.readHandler = null;
                            PendingFuture<V, A> result2 = new PendingFuture(this, OpType.READ);
                            try {
                                this.readFuture = result2;
                                this.readAttachment = null;
                                result = result2;
                            } catch (Throwable th3) {
                                th = th3;
                                Object result3 = result2;
                                throw th;
                            }
                        }
                        this.readHandler = handler;
                        this.readAttachment = attachment;
                        this.readFuture = null;
                        if (timeout > 0) {
                            this.readTimer = this.port.schedule(this.readTimeoutTask, timeout, unit);
                        }
                        this.readPending = true;
                        updateEvents();
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
            } else {
                if (null == null) {
                    enableReading();
                }
                end();
                Object result4 = th2 != null ? null : isScatteringRead ? Long.valueOf((long) n) : Integer.valueOf(n);
                if (handler == null) {
                    return CompletedFuture.withResult(result4, th2);
                }
                if (invokeDirect) {
                    Invoker.invokeDirect(myGroupAndInvokeCount, handler, attachment, result4, th2);
                } else {
                    Invoker.invokeIndirectly((AsynchronousChannel) this, (CompletionHandler) handler, (Object) attachment, result4, th2);
                }
                return null;
            }
            return result;
        } catch (Throwable th5) {
            Throwable x = th5;
            if (x instanceof ClosedChannelException) {
                x = new AsynchronousCloseException();
            }
            th2 = x;
        } finally {
            if (!pending) {
                enableReading();
            }
            end();
        }
    }

    private void finishWrite(boolean mayInvokeDirect) {
        int n = -1;
        Throwable exc = null;
        boolean gathering = this.isGatheringWrite;
        CompletionHandler handler = this.writeHandler;
        Object att = this.writeAttachment;
        PendingFuture<Number, Object> future = this.writeFuture;
        Future<?> timer = this.writeTimer;
        try {
            begin();
            if (gathering) {
                n = (int) IOUtil.write(this.fd, this.writeBuffers, nd);
            } else {
                n = IOUtil.write(this.fd, this.writeBuffer, -1, nd);
            }
            if (n == -2) {
                synchronized (this.updateLock) {
                    this.writePending = true;
                }
                if (!(null instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
                return;
            }
            this.writeBuffer = null;
            this.writeBuffers = null;
            this.writeAttachment = null;
            enableWriting();
            if (!(null instanceof AsynchronousCloseException)) {
                lockAndUpdateEvents();
            }
            end();
            if (timer != null) {
                timer.cancel(false);
            }
            Object result = exc != null ? null : gathering ? Long.valueOf((long) n) : Integer.valueOf(n);
            if (handler == null) {
                future.setResult(result, exc);
            } else if (mayInvokeDirect) {
                Invoker.invokeUnchecked(handler, att, result, exc);
            } else {
                Invoker.invokeIndirectly((AsynchronousChannel) this, handler, att, result, exc);
            }
        } catch (Throwable th) {
            Throwable x = th;
            try {
                enableWriting();
                if (x instanceof ClosedChannelException) {
                    x = new AsynchronousCloseException();
                }
                exc = x;
                if (!(exc instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
            } catch (Throwable th2) {
                if (!(null instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
            }
        }
    }

    /* JADX WARNING: Missing block: B:28:0x008d, code:
            r9 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    <V extends Number, A> Future<V> implWrite(boolean isGatheringWrite, ByteBuffer src, ByteBuffer[] srcs, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
        int attemptWrite;
        Throwable th;
        GroupAndInvokeCount myGroupAndInvokeCount = Invoker.getGroupAndInvokeCount();
        boolean invokeDirect = Invoker.mayInvokeDirect(myGroupAndInvokeCount, this.port);
        if (handler == null || invokeDirect) {
            attemptWrite = 1;
        } else {
            attemptWrite = this.port.isFixedThreadPool() ^ 1;
        }
        int n = -2;
        Throwable th2 = null;
        boolean pending = false;
        try {
            Future<V> result;
            begin();
            if (attemptWrite != 0) {
                if (isGatheringWrite) {
                    n = (int) IOUtil.write(this.fd, srcs, nd);
                } else {
                    n = IOUtil.write(this.fd, src, -1, nd);
                }
            }
            if (n == -2) {
                result = null;
                synchronized (this.updateLock) {
                    try {
                        this.isGatheringWrite = isGatheringWrite;
                        this.writeBuffer = src;
                        this.writeBuffers = srcs;
                        if (handler == null) {
                            this.writeHandler = null;
                            PendingFuture<V, A> result2 = new PendingFuture(this, OpType.WRITE);
                            try {
                                this.writeFuture = result2;
                                this.writeAttachment = null;
                                result = result2;
                            } catch (Throwable th3) {
                                th = th3;
                                Object result3 = result2;
                                throw th;
                            }
                        }
                        this.writeHandler = handler;
                        this.writeAttachment = attachment;
                        this.writeFuture = null;
                        if (timeout > 0) {
                            this.writeTimer = this.port.schedule(this.writeTimeoutTask, timeout, unit);
                        }
                        this.writePending = true;
                        updateEvents();
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
            } else {
                if (null == null) {
                    enableWriting();
                }
                end();
                Object result4 = th2 != null ? null : isGatheringWrite ? Long.valueOf((long) n) : Integer.valueOf(n);
                if (handler == null) {
                    return CompletedFuture.withResult(result4, th2);
                }
                if (invokeDirect) {
                    Invoker.invokeDirect(myGroupAndInvokeCount, handler, attachment, result4, th2);
                } else {
                    Invoker.invokeIndirectly((AsynchronousChannel) this, (CompletionHandler) handler, (Object) attachment, result4, th2);
                }
                return null;
            }
            return result;
        } catch (Throwable th5) {
            Throwable x = th5;
            if (x instanceof ClosedChannelException) {
                x = new AsynchronousCloseException();
            }
            th2 = x;
        } finally {
            if (!pending) {
                enableWriting();
            }
            end();
        }
    }
}
