package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.net.NetHooks;

class ServerSocketChannelImpl extends ServerSocketChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = (ServerSocketChannelImpl.class.desiredAssertionStatus() ^ 1);
    private static final int ST_INUSE = 0;
    private static final int ST_KILLED = 1;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd = new SocketDispatcher();
    private final FileDescriptor fd;
    private int fdVal;
    private boolean isReuseAddress;
    private InetSocketAddress localAddress;
    private final Object lock;
    ServerSocket socket;
    private int state;
    private final Object stateLock;
    private volatile long thread;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet(2);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_RCVBUF);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_REUSEADDR);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.IP_TOS);
            return Collections.unmodifiableSet(set);
        }
    }

    private native int accept0(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2, InetSocketAddress[] inetSocketAddressArr) throws IOException;

    private static native void initIDs();

    ServerSocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.thread = 0;
        this.lock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.fd = Net.serverSocket(true);
        this.fdVal = IOUtil.fdVal(this.fd);
        this.state = 0;
    }

    ServerSocketChannelImpl(SelectorProvider sp, FileDescriptor fd, boolean bound) throws IOException {
        super(sp);
        this.thread = 0;
        this.lock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 0;
        if (bound) {
            this.localAddress = Net.localAddress(fd);
        }
    }

    public ServerSocket socket() {
        ServerSocket serverSocket;
        synchronized (this.stateLock) {
            if (this.socket == null) {
                this.socket = ServerSocketAdaptor.create(this);
            }
            serverSocket = this.socket;
        }
        return serverSocket;
    }

    public SocketAddress getLocalAddress() throws IOException {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            if (isOpen()) {
                if (this.localAddress == null) {
                    socketAddress = this.localAddress;
                } else {
                    socketAddress = Net.getRevealedLocalAddress(Net.asInetSocketAddress(this.localAddress));
                }
            } else {
                throw new ClosedChannelException();
            }
        }
        return socketAddress;
    }

    /* JADX WARNING: Missing block: B:34:0x006e, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.IP_TOS) {
                    Net.setSocketOption(this.fd, Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET, name, value);
                    return this;
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    this.isReuseAddress = ((Boolean) value).booleanValue();
                } else {
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                }
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                T valueOf;
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    valueOf = Boolean.valueOf(this.isReuseAddress);
                    return valueOf;
                } else {
                    valueOf = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return valueOf;
                }
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    public boolean isBound() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.localAddress != null ? true : -assertionsDisabled;
        }
        return z;
    }

    public InetSocketAddress localAddress() {
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            inetSocketAddress = this.localAddress;
        }
        return inetSocketAddress;
    }

    public ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        synchronized (this.lock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isBound()) {
                throw new AlreadyBoundException();
            } else {
                InetSocketAddress isa;
                if (local == null) {
                    isa = new InetSocketAddress(0);
                } else {
                    isa = Net.checkAddress(local);
                }
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkListen(isa.getPort());
                }
                NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                Net.bind(this.fd, isa.getAddress(), isa.getPort());
                FileDescriptor fileDescriptor = this.fd;
                if (backlog < 1) {
                    backlog = 50;
                }
                Net.listen(fileDescriptor, backlog);
                synchronized (this.stateLock) {
                    this.localAddress = Net.localAddress(this.fd);
                }
            }
        }
        return this;
    }

    /* JADX WARNING: Missing block: B:28:0x004d, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:67:0x00c4, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SocketChannel accept() throws IOException {
        SocketChannel sc;
        boolean z = true;
        boolean z2 = -assertionsDisabled;
        synchronized (this.lock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isBound()) {
                FileDescriptor newfd = new FileDescriptor();
                InetSocketAddress[] isaa = new InetSocketAddress[1];
                try {
                    begin();
                    if (isOpen()) {
                        int n;
                        this.thread = NativeThread.current();
                        do {
                            n = accept(this.fd, newfd, isaa);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        this.thread = 0;
                        if (n > 0) {
                            z2 = true;
                        }
                        end(z2);
                        if (!-assertionsDisabled && !IOStatus.check(n)) {
                            throw new AssertionError();
                        } else if (n < 1) {
                            return null;
                        } else {
                            IOUtil.configureBlocking(newfd, true);
                            InetSocketAddress isa = isaa[0];
                            sc = new SocketChannelImpl(provider(), newfd, isa);
                            SecurityManager sm = System.getSecurityManager();
                            if (sm != null) {
                                sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                            }
                        }
                    } else {
                        this.thread = 0;
                        end(-assertionsDisabled);
                        if (-assertionsDisabled || IOStatus.check(0)) {
                        } else {
                            throw new AssertionError();
                        }
                    }
                } catch (SecurityException x) {
                    sc.close();
                    throw x;
                } catch (Throwable th) {
                    this.thread = 0;
                    if (null <= null) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (!-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            } else {
                throw new NotYetBoundException();
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != 1) {
                nd.preClose(this.fd);
            }
            long th = this.thread;
            if (th != 0) {
                NativeThread.signal(th);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    public void kill() throws IOException {
        synchronized (this.stateLock) {
            if (this.state == 1) {
            } else if (this.state == -1) {
                this.state = 1;
            } else if (-assertionsDisabled || !(isOpen() || isRegistered())) {
                nd.close(this.fd);
                this.state = 1;
            } else {
                throw new AssertionError();
            }
        }
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        boolean z = true;
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((Net.POLLNVAL & ops) != 0) {
            return -assertionsDisabled;
        }
        if (((Net.POLLERR | Net.POLLHUP) & ops) != 0) {
            newOps = intOps;
            sk.nioReadyOps(intOps);
            if (((~oldOps) & intOps) == 0) {
                z = -assertionsDisabled;
            }
            return z;
        }
        if (!((Net.POLLIN & ops) == 0 || (intOps & 16) == 0)) {
            newOps = initialOps | 16;
        }
        sk.nioReadyOps(newOps);
        if (((~oldOps) & newOps) == 0) {
            z = -assertionsDisabled;
        }
        return z;
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    /* JADX WARNING: Missing block: B:20:?, code:
            r6.thread = r4;
            end(-assertionsDisabled);
     */
    /* JADX WARNING: Missing block: B:22:0x0035, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:27:0x003d, code:
            r0 = sun.nio.ch.Net.poll(r6.fd, r7, r8);
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r6.thread = 0;
     */
    /* JADX WARNING: Missing block: B:31:0x0047, code:
            if (r0 <= 0) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:32:0x0049, code:
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:33:0x004a, code:
            end(r1);
     */
    /* JADX WARNING: Missing block: B:35:0x004e, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int poll(int events, long timeout) throws IOException {
        boolean z = -assertionsDisabled;
        if (-assertionsDisabled || (Thread.holdsLock(blockingLock()) && !isBlocking())) {
            synchronized (this.lock) {
                long isOpen;
                try {
                    begin();
                    synchronized (this.stateLock) {
                        isOpen = isOpen();
                        if (isOpen != null) {
                            this.thread = NativeThread.current();
                        }
                    }
                } finally {
                    isOpen = 0;
                    this.thread = 0;
                    end(-assertionsDisabled);
                }
            }
        }
        throw new AssertionError();
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = 0;
        if ((ops & 16) != 0) {
            newOps = Net.POLLIN | 0;
        }
        sk.selector.putEventOps(sk, newOps);
    }

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append('[');
        if (isOpen()) {
            synchronized (this.stateLock) {
                InetSocketAddress addr = localAddress();
                if (addr == null) {
                    sb.append("unbound");
                } else {
                    sb.append(Net.getRevealedLocalAddressAsString(addr));
                }
            }
        } else {
            sb.append("closed");
        }
        sb.append(']');
        return sb.toString();
    }

    private int accept(FileDescriptor ssfd, FileDescriptor newfd, InetSocketAddress[] isaa) throws IOException {
        return accept0(ssfd, newfd, isaa);
    }

    static {
        initIDs();
    }
}
