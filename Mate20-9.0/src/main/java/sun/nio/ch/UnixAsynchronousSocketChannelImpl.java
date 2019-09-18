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
import java.nio.channels.ShutdownChannelGroupException;
import java.security.AccessController;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import sun.net.NetHooks;
import sun.nio.ch.Invoker;
import sun.nio.ch.Port;
import sun.security.action.GetPropertyAction;

class UnixAsynchronousSocketChannelImpl extends AsynchronousSocketChannelImpl implements Port.PollableChannel {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
    /* access modifiers changed from: private */
    public Object readAttachment;
    private ByteBuffer readBuffer;
    private ByteBuffer[] readBuffers;
    /* access modifiers changed from: private */
    public PendingFuture<Number, Object> readFuture;
    /* access modifiers changed from: private */
    public CompletionHandler<Number, Object> readHandler;
    /* access modifiers changed from: private */
    public boolean readPending;
    private Runnable readTimeoutTask = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x003b, code lost:
            if (r0 != null) goto L_0x0041;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x003d, code lost:
            r2.setFailure(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0041, code lost:
            sun.nio.ch.Invoker.invokeIndirectly(r6.this$0, r0, r1, null, (java.lang.Throwable) r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0047, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0030, code lost:
            r6.this$0.enableReading(true);
            r3 = new java.nio.channels.InterruptedByTimeoutException();
         */
        public void run() {
            synchronized (UnixAsynchronousSocketChannelImpl.this.updateLock) {
                if (UnixAsynchronousSocketChannelImpl.this.readPending) {
                    boolean unused = UnixAsynchronousSocketChannelImpl.this.readPending = false;
                    CompletionHandler<Number, Object> handler = UnixAsynchronousSocketChannelImpl.this.readHandler;
                    Object att = UnixAsynchronousSocketChannelImpl.this.readAttachment;
                    PendingFuture access$400 = UnixAsynchronousSocketChannelImpl.this.readFuture;
                }
            }
        }
    };
    private Future<?> readTimer;
    /* access modifiers changed from: private */
    public final Object updateLock = new Object();
    /* access modifiers changed from: private */
    public Object writeAttachment;
    private ByteBuffer writeBuffer;
    private ByteBuffer[] writeBuffers;
    /* access modifiers changed from: private */
    public PendingFuture<Number, Object> writeFuture;
    /* access modifiers changed from: private */
    public CompletionHandler<Number, Object> writeHandler;
    /* access modifiers changed from: private */
    public boolean writePending;
    private Runnable writeTimeoutTask = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x003b, code lost:
            if (r0 == null) goto L_0x0044;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x003d, code lost:
            sun.nio.ch.Invoker.invokeIndirectly((java.nio.channels.AsynchronousChannel) r6.this$0, r0, r1, null, (java.lang.Throwable) r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0044, code lost:
            r2.setFailure(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0047, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0030, code lost:
            r6.this$0.enableWriting(true);
            r3 = new java.nio.channels.InterruptedByTimeoutException();
         */
        public void run() {
            synchronized (UnixAsynchronousSocketChannelImpl.this.updateLock) {
                if (UnixAsynchronousSocketChannelImpl.this.writePending) {
                    boolean unused = UnixAsynchronousSocketChannelImpl.this.writePending = false;
                    CompletionHandler<Number, Object> handler = UnixAsynchronousSocketChannelImpl.this.writeHandler;
                    Object att = UnixAsynchronousSocketChannelImpl.this.writeAttachment;
                    PendingFuture access$800 = UnixAsynchronousSocketChannelImpl.this.writeFuture;
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

    UnixAsynchronousSocketChannelImpl(Port port2) throws IOException {
        super(port2);
        try {
            IOUtil.configureBlocking(this.fd, false);
            this.port = port2;
            this.fdVal = IOUtil.fdVal(this.fd);
            port2.register(this.fdVal, this);
            this.guard.open("close");
        } catch (IOException x) {
            nd.close(this.fd);
            throw x;
        }
    }

    UnixAsynchronousSocketChannelImpl(Port port2, FileDescriptor fd, InetSocketAddress remote) throws IOException {
        super(port2, fd, remote);
        this.fdVal = IOUtil.fdVal(fd);
        IOUtil.configureBlocking(fd, false);
        try {
            port2.register(this.fdVal, this);
            this.port = port2;
            this.guard.open("close");
        } catch (ShutdownChannelGroupException x) {
            throw new IOException((Throwable) x);
        }
    }

    public AsynchronousChannelGroupImpl group() {
        return this.port;
    }

    private void updateEvents() {
        int events = 0;
        if (this.readPending) {
            events = 0 | Net.POLLIN;
        }
        if (this.connectPending || this.writePending) {
            events |= Net.POLLOUT;
        }
        if (events != 0) {
            this.port.startPoll(this.fdVal, events);
        }
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
                try {
                    if (this.readPending) {
                        this.readPending = false;
                        finishRead = true;
                    }
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
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
        boolean writable = false;
        boolean readable = (Net.POLLIN & events) > 0;
        if ((Net.POLLOUT & events) > 0) {
            writable = true;
        }
        if (((Net.POLLERR | Net.POLLHUP) & events) > 0) {
            readable = true;
            writable = true;
        }
        finish(mayInvokeDirect, readable, writable);
    }

    /* access modifiers changed from: package-private */
    public void implClose() throws IOException {
        this.guard.close();
        this.port.unregister(this.fdVal);
        nd.close(this.fd);
        finish(false, true, true);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
        } catch (Throwable th) {
            end();
            throw th;
        }
        end();
        if (e != null) {
            try {
                close();
            } catch (Throwable suppressed) {
                e.addSuppressed(suppressed);
            }
        }
        CompletionHandler<Void, Object> handler = this.connectHandler;
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

    /* access modifiers changed from: package-private */
    public <A> Future<Void> implConnect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        boolean notifyBeforeTcpConnect;
        if (!isOpen()) {
            Throwable e = new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withFailure(e);
            }
            Invoker.invoke(this, handler, attachment, null, e);
            return null;
        }
        InetSocketAddress isa = Net.checkAddress(remote);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
        }
        synchronized (this.stateLock) {
            if (this.state == 2) {
                throw new AlreadyConnectedException();
            } else if (this.state != 1) {
                this.state = 1;
                this.pendingRemote = remote;
                notifyBeforeTcpConnect = this.localAddress == null;
            } else {
                throw new ConnectionPendingException();
            }
        }
        Throwable e2 = null;
        try {
            begin();
            if (notifyBeforeTcpConnect) {
                NetHooks.beforeTcpConnect(this.fd, isa.getAddress(), isa.getPort());
            }
            if (Net.connect(this.fd, isa.getAddress(), isa.getPort()) == -2) {
                PendingFuture<Void, A> result = null;
                synchronized (this.updateLock) {
                    if (handler == null) {
                        result = new PendingFuture<>(this, OpType.CONNECT);
                        this.connectFuture = result;
                    } else {
                        this.connectHandler = handler;
                        this.connectAttachment = attachment;
                    }
                    this.connectPending = true;
                    updateEvents();
                }
                end();
                return result;
            }
            setConnected();
            end();
            if (e2 != null) {
                try {
                    close();
                } catch (Throwable suppressed) {
                    e2.addSuppressed(suppressed);
                }
            }
            if (handler == null) {
                return CompletedFuture.withResult(null, e2);
            }
            Invoker.invoke(this, handler, attachment, null, e2);
            return null;
        } catch (Throwable th) {
            x = th;
            try {
                if (x instanceof ClosedChannelException) {
                    x = new AsynchronousCloseException();
                }
                e2 = x;
            } catch (Throwable th2) {
                end();
                throw th2;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        if ((r1 instanceof java.nio.channels.AsynchronousCloseException) == false) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006d, code lost:
        if (r6 == null) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006f, code lost:
        r6.cancel(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0073, code lost:
        if (r1 == null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0076, code lost:
        if (r2 == false) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0078, code lost:
        r7 = java.lang.Long.valueOf((long) r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x007e, code lost:
        r7 = java.lang.Integer.valueOf(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0082, code lost:
        if (r3 != null) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0084, code lost:
        r5.setResult(r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0088, code lost:
        if (r14 == false) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008a, code lost:
        sun.nio.ch.Invoker.invokeUnchecked(r3, r4, r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x008e, code lost:
        sun.nio.ch.Invoker.invokeIndirectly((java.nio.channels.AsynchronousChannel) r13, r3, r4, r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0091, code lost:
        return;
     */
    private void finishRead(boolean mayInvokeDirect) {
        int n = -1;
        Throwable exc = null;
        boolean scattering = this.isScatteringRead;
        CompletionHandler<Number, Object> handler = this.readHandler;
        Object att = this.readAttachment;
        PendingFuture<Number, Object> future = this.readFuture;
        Future<?> timeout = this.readTimer;
        Number result = null;
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
                if (!(exc instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
                return;
            }
            this.readBuffer = null;
            this.readBuffers = null;
            this.readAttachment = null;
            enableReading();
        } catch (Throwable th) {
            x = th;
            try {
                enableReading();
                if (x instanceof ClosedChannelException) {
                    x = new AsynchronousCloseException();
                }
                exc = x;
            } finally {
                if (!(exc instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00c3, code lost:
        if (1 != 0) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00c5, code lost:
        enableReading();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00c8, code lost:
        end();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00cb, code lost:
        return r12;
     */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x00fe A[Catch:{ all -> 0x0131 }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0107  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0121  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x012c  */
    public <V extends Number, A> Future<V> implRead(boolean isScatteringRead2, ByteBuffer dst, ByteBuffer[] dsts, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
        Throwable exc;
        Throwable exc2;
        int n;
        boolean z = isScatteringRead2;
        ByteBuffer byteBuffer = dst;
        ByteBuffer[] byteBufferArr = dsts;
        long j = timeout;
        A a = attachment;
        CompletionHandler<V, ? super A> completionHandler = handler;
        Invoker.GroupAndInvokeCount myGroupAndInvokeCount = null;
        boolean invokeDirect = false;
        boolean attemptRead = false;
        if (!disableSynchronousRead) {
            if (completionHandler == null) {
                attemptRead = true;
            } else {
                myGroupAndInvokeCount = Invoker.getGroupAndInvokeCount();
                invokeDirect = Invoker.mayInvokeDirect(myGroupAndInvokeCount, this.port);
                attemptRead = invokeDirect || !this.port.isFixedThreadPool();
            }
        }
        Invoker.GroupAndInvokeCount myGroupAndInvokeCount2 = myGroupAndInvokeCount;
        int n2 = -2;
        try {
            begin();
            if (!attemptRead) {
                exc2 = null;
            } else if (z) {
                try {
                    n = -2;
                    exc2 = null;
                } catch (Throwable th) {
                    th = th;
                    TimeUnit timeUnit = unit;
                    if (0 == 0) {
                    }
                    end();
                    throw th;
                }
                try {
                    n2 = (int) IOUtil.read(this.fd, byteBufferArr, nd);
                } catch (Throwable th2) {
                    th = th2;
                    TimeUnit timeUnit2 = unit;
                    if (0 == 0) {
                    }
                    end();
                    throw th;
                }
            } else {
                exc2 = null;
                n2 = IOUtil.read(this.fd, byteBuffer, -1, nd);
            }
            if (n2 == -2) {
                PendingFuture<V, A> result = null;
                try {
                    synchronized (this.updateLock) {
                        try {
                            this.isScatteringRead = z;
                            this.readBuffer = byteBuffer;
                            this.readBuffers = byteBufferArr;
                            if (completionHandler == null) {
                                this.readHandler = null;
                                result = new PendingFuture<>(this, OpType.READ);
                                this.readFuture = result;
                                this.readAttachment = null;
                            } else {
                                this.readHandler = completionHandler;
                                this.readAttachment = a;
                                this.readFuture = null;
                            }
                            if (j > 0) {
                                try {
                                    this.readTimer = this.port.schedule(this.readTimeoutTask, j, unit);
                                } catch (Throwable th3) {
                                    th = th3;
                                    try {
                                        throw th;
                                    } catch (Throwable th4) {
                                        x = th4;
                                    }
                                }
                            } else {
                                TimeUnit timeUnit3 = unit;
                            }
                            this.readPending = true;
                            updateEvents();
                        } catch (Throwable th5) {
                            th = th5;
                            TimeUnit timeUnit4 = unit;
                            throw th;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    TimeUnit timeUnit5 = unit;
                    if (0 == 0) {
                    }
                    end();
                    throw th;
                }
            } else {
                TimeUnit timeUnit6 = unit;
                if (0 == 0) {
                    enableReading();
                }
                end();
                exc = exc2;
                Number result2 = exc != null ? null : z ? Long.valueOf((long) n2) : Integer.valueOf(n2);
                if (completionHandler == null) {
                    return CompletedFuture.withResult(result2, exc);
                }
                if (invokeDirect) {
                    Invoker.invokeDirect(myGroupAndInvokeCount2, completionHandler, a, result2, exc);
                } else {
                    Invoker.invokeIndirectly((AsynchronousChannel) this, completionHandler, a, result2, exc);
                }
                return null;
            }
        } catch (Throwable th7) {
            th = th7;
            TimeUnit timeUnit7 = unit;
            if (0 == 0) {
            }
            end();
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        if ((r1 instanceof java.nio.channels.AsynchronousCloseException) == false) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006d, code lost:
        if (r6 == null) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006f, code lost:
        r6.cancel(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0073, code lost:
        if (r1 == null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0076, code lost:
        if (r2 == false) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0078, code lost:
        r7 = java.lang.Long.valueOf((long) r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x007e, code lost:
        r7 = java.lang.Integer.valueOf(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0082, code lost:
        if (r3 != null) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0084, code lost:
        r5.setResult(r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0088, code lost:
        if (r14 == false) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008a, code lost:
        sun.nio.ch.Invoker.invokeUnchecked(r3, r4, r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x008e, code lost:
        sun.nio.ch.Invoker.invokeIndirectly((java.nio.channels.AsynchronousChannel) r13, r3, r4, r7, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0091, code lost:
        return;
     */
    private void finishWrite(boolean mayInvokeDirect) {
        int n = -1;
        Throwable exc = null;
        boolean gathering = this.isGatheringWrite;
        CompletionHandler<Number, Object> handler = this.writeHandler;
        Object att = this.writeAttachment;
        PendingFuture<Number, Object> future = this.writeFuture;
        Future<?> timer = this.writeTimer;
        Number result = null;
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
                if (!(exc instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
                return;
            }
            this.writeBuffer = null;
            this.writeBuffers = null;
            this.writeAttachment = null;
            enableWriting();
        } catch (Throwable th) {
            x = th;
            try {
                enableWriting();
                if (x instanceof ClosedChannelException) {
                    x = new AsynchronousCloseException();
                }
                exc = x;
            } finally {
                if (!(exc instanceof AsynchronousCloseException)) {
                    lockAndUpdateEvents();
                }
                end();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b8, code lost:
        if (1 != 0) goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00ba, code lost:
        enableWriting();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00bd, code lost:
        end();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00c0, code lost:
        return r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00d4, code lost:
        if (0 == 0) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00d6, code lost:
        enableWriting();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00d9, code lost:
        end();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x00f7, code lost:
        if (0 == 0) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x00fa, code lost:
        if (r14 == null) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x00fc, code lost:
        r15 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x00fe, code lost:
        if (r2 == false) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0100, code lost:
        r15 = java.lang.Long.valueOf((long) r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0106, code lost:
        r15 = java.lang.Integer.valueOf(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x010a, code lost:
        r0 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x010b, code lost:
        if (r8 == null) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x010d, code lost:
        if (r10 == false) goto L_0x0113;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x010f, code lost:
        sun.nio.ch.Invoker.invokeDirect(r9, r8, r7, r0, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0113, code lost:
        sun.nio.ch.Invoker.invokeIndirectly((java.nio.channels.AsynchronousChannel) r1, r8, r7, r0, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0117, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x011c, code lost:
        return sun.nio.ch.CompletedFuture.withResult(r0, r14);
     */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00f0 A[Catch:{ all -> 0x011d }] */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0122  */
    public <V extends Number, A> Future<V> implWrite(boolean isGatheringWrite2, ByteBuffer src, ByteBuffer[] srcs, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
        boolean z = isGatheringWrite2;
        ByteBuffer byteBuffer = src;
        ByteBuffer[] byteBufferArr = srcs;
        long j = timeout;
        A a = attachment;
        CompletionHandler<V, ? super A> completionHandler = handler;
        Invoker.GroupAndInvokeCount myGroupAndInvokeCount = Invoker.getGroupAndInvokeCount();
        boolean invokeDirect = Invoker.mayInvokeDirect(myGroupAndInvokeCount, this.port);
        boolean attemptWrite = completionHandler == null || invokeDirect || !this.port.isFixedThreadPool();
        int n = -2;
        Throwable exc = null;
        try {
            begin();
            if (!attemptWrite) {
            } else if (z) {
                try {
                    boolean z2 = attemptWrite;
                    int n2 = -2;
                    try {
                        n = (int) IOUtil.write(this.fd, byteBufferArr, nd);
                    } catch (Throwable th) {
                        th = th;
                        TimeUnit timeUnit = unit;
                        if (0 == 0) {
                            enableWriting();
                        }
                        end();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    boolean z3 = attemptWrite;
                    TimeUnit timeUnit2 = unit;
                    if (0 == 0) {
                    }
                    end();
                    throw th;
                }
            } else {
                boolean z4 = attemptWrite;
                n = IOUtil.write(this.fd, byteBuffer, -1, nd);
            }
            if (n == -2) {
                PendingFuture<V, A> result = null;
                try {
                    synchronized (this.updateLock) {
                        try {
                            this.isGatheringWrite = z;
                            this.writeBuffer = byteBuffer;
                            this.writeBuffers = byteBufferArr;
                            if (completionHandler == null) {
                                this.writeHandler = null;
                                result = new PendingFuture<>(this, OpType.WRITE);
                                this.writeFuture = result;
                                this.writeAttachment = null;
                            } else {
                                this.writeHandler = completionHandler;
                                this.writeAttachment = a;
                                this.writeFuture = null;
                            }
                            if (j > 0) {
                                try {
                                    this.writeTimer = this.port.schedule(this.writeTimeoutTask, j, unit);
                                } catch (Throwable th3) {
                                    th = th3;
                                    try {
                                        throw th;
                                    } catch (Throwable th4) {
                                        x = th4;
                                    }
                                }
                            } else {
                                TimeUnit timeUnit3 = unit;
                            }
                            this.writePending = true;
                            updateEvents();
                        } catch (Throwable th5) {
                            th = th5;
                            TimeUnit timeUnit4 = unit;
                            throw th;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    TimeUnit timeUnit5 = unit;
                    if (0 == 0) {
                    }
                    end();
                    throw th;
                }
            } else {
                TimeUnit timeUnit6 = unit;
            }
        } catch (Throwable th7) {
            th = th7;
            TimeUnit timeUnit7 = unit;
            boolean z5 = attemptWrite;
            if (0 == 0) {
            }
            end();
            throw th;
        }
    }
}
