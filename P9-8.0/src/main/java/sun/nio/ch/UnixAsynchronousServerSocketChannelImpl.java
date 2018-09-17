package sun.nio.ch;

import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NotYetBoundException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class UnixAsynchronousServerSocketChannelImpl extends AsynchronousServerSocketChannelImpl implements PollableChannel {
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

    UnixAsynchronousServerSocketChannelImpl(Port port) throws IOException {
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

    /* JADX WARNING: Missing block: B:11:0x0028, code:
            r3 = new java.nio.channels.AsynchronousCloseException();
            r3.setStackTrace(new java.lang.StackTraceElement[0]);
     */
    /* JADX WARNING: Missing block: B:12:0x0032, code:
            if (r2 != null) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:13:0x0034, code:
            r1.setFailure(r3);
     */
    /* JADX WARNING: Missing block: B:14:0x0037, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x003b, code:
            sun.nio.ch.Invoker.invokeIndirectly((java.nio.channels.AsynchronousChannel) r8, r2, r0, null, r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void implClose() throws IOException {
        this.guard.close();
        this.port.unregister(this.fdVal);
        nd.close(this.fd);
        synchronized (this.updateLock) {
            if (this.acceptPending) {
                this.acceptPending = false;
                CompletionHandler handler = this.acceptHandler;
                Object att = this.acceptAttachment;
                PendingFuture<AsynchronousSocketChannel, Object> future = this.acceptFuture;
            }
        }
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

    public AsynchronousChannelGroupImpl group() {
        return this.port;
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x008e  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0061  */
    /* JADX WARNING: Missing block: B:11:0x000d, code:
            r8 = new java.io.FileDescriptor();
            r6 = new java.net.InetSocketAddress[1];
            r2 = null;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            begin();
     */
    /* JADX WARNING: Missing block: B:14:0x0020, code:
            if (accept(r14.fd, r8, r6) != -2) goto L_0x0070;
     */
    /* JADX WARNING: Missing block: B:15:0x0022, code:
            r11 = r14.updateLock;
     */
    /* JADX WARNING: Missing block: B:16:0x0024, code:
            monitor-enter(r11);
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            r14.acceptPending = true;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:22:0x0029, code:
            r14.port.startPoll(r14.fdVal, sun.nio.ch.Net.POLLIN);
     */
    /* JADX WARNING: Missing block: B:23:0x0032, code:
            end();
     */
    /* JADX WARNING: Missing block: B:24:0x0035, code:
            return;
     */
    /* JADX WARNING: Missing block: B:32:0x003c, code:
            r9 = th;
     */
    /* JADX WARNING: Missing block: B:35:0x003f, code:
            if ((r9 instanceof java.nio.channels.ClosedChannelException) != false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:36:0x0041, code:
            r9 = new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:37:0x0046, code:
            r2 = r9;
            end();
     */
    /* JADX WARNING: Missing block: B:52:0x0070, code:
            end();
     */
    /* JADX WARNING: Missing block: B:54:0x0075, code:
            end();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onEvent(int events, boolean mayInvokeDirect) {
        synchronized (this.updateLock) {
            if (this.acceptPending) {
                this.acceptPending = false;
            } else {
                return;
            }
        }
        CompletionHandler<AsynchronousSocketChannel, Object> handler;
        Object att;
        PendingFuture<AsynchronousSocketChannel, Object> future;
        AsynchronousSocketChannel child;
        Throwable exc;
        handler = this.acceptHandler;
        att = this.acceptAttachment;
        future = this.acceptFuture;
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
        child = null;
        if (exc == null) {
            try {
                child = finishAccept(newfd, isaa[0], this.acceptAcc);
            } catch (Throwable th) {
                Throwable x = th;
                if (!((x instanceof IOException) || ((x instanceof SecurityException) ^ 1) == 0)) {
                    x = new IOException(x);
                }
                exc = x;
            }
        }
        handler = this.acceptHandler;
        att = this.acceptAttachment;
        future = this.acceptFuture;
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

    Future<AsynchronousSocketChannel> implAccept(Object att, CompletionHandler<AsynchronousSocketChannel, Object> handler) {
        Throwable th;
        Throwable x;
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
                    Future<AsynchronousSocketChannel> future = null;
                    synchronized (this.updateLock) {
                        AccessControlContext accessControlContext;
                        if (handler == null) {
                            PendingFuture<AsynchronousSocketChannel, Object> result;
                            try {
                                this.acceptHandler = null;
                                result = new PendingFuture(this);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                            try {
                                this.acceptFuture = result;
                                future = result;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                        this.acceptHandler = handler;
                        this.acceptAttachment = att;
                        if (System.getSecurityManager() == null) {
                            accessControlContext = null;
                        } else {
                            accessControlContext = AccessController.getContext();
                        }
                        this.acceptAcc = accessControlContext;
                        this.acceptPending = true;
                        this.port.startPoll(this.fdVal, Net.POLLIN);
                        end();
                        return future;
                    }
                }
                end();
                Object child = null;
                if (exc == null) {
                    try {
                        child = finishAccept(newfd, isaa[0], null);
                    } catch (Throwable x2) {
                        exc = x2;
                    }
                }
                enableAccept();
                if (handler == null) {
                    return CompletedFuture.withResult(child, exc);
                }
                Invoker.invokeIndirectly((AsynchronousChannel) this, (CompletionHandler) handler, att, child, exc);
                return null;
            } catch (Throwable th4) {
                end();
                throw th4;
            }
        } else {
            throw new AcceptPendingException();
        }
    }

    private int accept(FileDescriptor ssfd, FileDescriptor newfd, InetSocketAddress[] isaa) throws IOException {
        return accept0(ssfd, newfd, isaa);
    }
}
