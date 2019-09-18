package sun.nio.ch;

import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NotYetBoundException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import sun.nio.ch.Port;

class UnixAsynchronousServerSocketChannelImpl extends AsynchronousServerSocketChannelImpl implements Port.PollableChannel {
    private static final NativeDispatcher nd = new SocketDispatcher();
    private AccessControlContext acceptAcc;
    private Object acceptAttachment;
    private PendingFuture<AsynchronousSocketChannel, Object> acceptFuture;
    private CompletionHandler<AsynchronousSocketChannel, Object> acceptHandler;
    private boolean acceptPending;
    private final AtomicBoolean accepting = new AtomicBoolean();
    private final int fdVal;
    private final CloseGuard guard = CloseGuard.get();
    private final Port port;
    private final Object updateLock = new Object();

    private native int accept0(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2, InetSocketAddress[] inetSocketAddressArr) throws IOException;

    private static native void initIDs();

    static {
        initIDs();
    }

    private void enableAccept() {
        this.accepting.set(false);
    }

    UnixAsynchronousServerSocketChannelImpl(Port port2) throws IOException {
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0030, code lost:
        if (r2 != null) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
        r4.setFailure(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0036, code lost:
        sun.nio.ch.Invoker.invokeIndirectly((java.nio.channels.AsynchronousChannel) r5, r2, r3, null, (java.lang.Throwable) r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0026, code lost:
        r0 = new java.nio.channels.AsynchronousCloseException();
        r0.setStackTrace(new java.lang.StackTraceElement[0]);
     */
    public void implClose() throws IOException {
        this.guard.close();
        this.port.unregister(this.fdVal);
        nd.close(this.fd);
        synchronized (this.updateLock) {
            if (this.acceptPending) {
                this.acceptPending = false;
                CompletionHandler<AsynchronousSocketChannel, Object> handler = this.acceptHandler;
                Object att = this.acceptAttachment;
                PendingFuture<AsynchronousSocketChannel, Object> future = this.acceptFuture;
            }
        }
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

    public AsynchronousChannelGroupImpl group() {
        return this.port;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        begin();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
        if (accept(r8.fd, r0, r3) != -2) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        r6 = r8.updateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r8.acceptPending = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0027, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r8.port.startPoll(r8.fdVal, sun.nio.ch.Net.POLLIN);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0031, code lost:
        end();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0034, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0038, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003a, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x003d, code lost:
        if ((r2 instanceof java.nio.channels.ClosedChannelException) != false) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0044, code lost:
        r2 = new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0045, code lost:
        r4 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0087, code lost:
        end();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x008a, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000d, code lost:
        r0 = new java.io.FileDescriptor();
        r3 = new java.net.InetSocketAddress[1];
        r4 = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0083  */
    public void onEvent(int events, boolean mayInvokeDirect) {
        FileDescriptor newfd;
        InetSocketAddress[] isaa;
        Throwable exc;
        CompletionHandler<AsynchronousSocketChannel, Object> handler;
        synchronized (this.updateLock) {
            if (this.acceptPending) {
                this.acceptPending = false;
            } else {
                return;
            }
        }
        end();
        AsynchronousSocketChannel child = null;
        if (exc == null) {
            try {
                child = finishAccept(newfd, isaa[0], this.acceptAcc);
            } catch (Throwable th) {
                x = th;
                if (!(x instanceof IOException) && !(x instanceof SecurityException)) {
                    x = new IOException(x);
                }
                exc = x;
            }
        }
        handler = this.acceptHandler;
        Object att = this.acceptAttachment;
        PendingFuture<AsynchronousSocketChannel, Object> future = this.acceptFuture;
        enableAccept();
        if (handler != null) {
            future.setResult(child, exc);
            if (child != null && future.isCancelled()) {
                try {
                    child.close();
                } catch (IOException e) {
                }
            }
        } else {
            Invoker.invoke(this, handler, att, child, exc);
        }
        handler = this.acceptHandler;
        Object att2 = this.acceptAttachment;
        PendingFuture<AsynchronousSocketChannel, Object> future2 = this.acceptFuture;
        enableAccept();
        if (handler != null) {
        }
    }

    private AsynchronousSocketChannel finishAccept(FileDescriptor newfd, final InetSocketAddress remote, AccessControlContext acc) throws IOException, SecurityException {
        try {
            AsynchronousSocketChannel ch = new UnixAsynchronousSocketChannelImpl(this.port, newfd, remote);
            if (acc != null) {
                try {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            SecurityManager sm = System.getSecurityManager();
                            if (sm != null) {
                                sm.checkAccept(remote.getAddress().getHostAddress(), remote.getPort());
                            }
                            return null;
                        }
                    }, acc);
                } catch (SecurityException x) {
                    ch.close();
                } catch (Throwable suppressed) {
                    x.addSuppressed(suppressed);
                }
            } else {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkAccept(remote.getAddress().getHostAddress(), remote.getPort());
                }
            }
            return ch;
            throw x;
        } catch (IOException x2) {
            nd.close(newfd);
            throw x2;
        }
    }

    /* access modifiers changed from: package-private */
    public Future<AsynchronousSocketChannel> implAccept(Object att, CompletionHandler<AsynchronousSocketChannel, Object> handler) {
        if (!isOpen()) {
            Throwable e = new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withFailure(e);
            }
            Invoker.invoke(this, handler, att, null, e);
            return null;
        } else if (this.localAddress == null) {
            throw new NotYetBoundException();
        } else if (isAcceptKilled()) {
            throw new RuntimeException("Accept not allowed due cancellation");
        } else if (this.accepting.compareAndSet(false, true)) {
            FileDescriptor newfd = new FileDescriptor();
            InetSocketAddress[] isaa = new InetSocketAddress[1];
            Throwable exc = null;
            try {
                begin();
                if (accept(this.fd, newfd, isaa) == -2) {
                    PendingFuture<AsynchronousSocketChannel, Object> result = null;
                    synchronized (this.updateLock) {
                        if (handler == null) {
                            this.acceptHandler = null;
                            result = new PendingFuture<>(this);
                            this.acceptFuture = result;
                        } else {
                            this.acceptHandler = handler;
                            this.acceptAttachment = att;
                        }
                        this.acceptAcc = System.getSecurityManager() == null ? null : AccessController.getContext();
                        this.acceptPending = true;
                    }
                    this.port.startPoll(this.fdVal, Net.POLLIN);
                    end();
                    return result;
                }
            } catch (Throwable th) {
                x = th;
                try {
                    if (x instanceof ClosedChannelException) {
                        x = new AsynchronousCloseException();
                    }
                    exc = x;
                } catch (Throwable th2) {
                    end();
                    throw th2;
                }
            }
            end();
            AsynchronousSocketChannel child = null;
            if (exc == null) {
                try {
                    child = finishAccept(newfd, isaa[0], null);
                } catch (Throwable x) {
                    exc = x;
                }
            }
            enableAccept();
            if (handler == null) {
                return CompletedFuture.withResult(child, exc);
            }
            Invoker.invokeIndirectly((AsynchronousChannel) this, handler, att, child, exc);
            return null;
        } else {
            throw new AcceptPendingException();
        }
    }

    private int accept(FileDescriptor ssfd, FileDescriptor newfd, InetSocketAddress[] isaa) throws IOException {
        return accept0(ssfd, newfd, isaa);
    }
}
