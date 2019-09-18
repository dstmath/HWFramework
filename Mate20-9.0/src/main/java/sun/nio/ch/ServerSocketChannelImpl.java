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
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
            HashSet<SocketOption<?>> set = new HashSet<>(2);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.IP_TOS);
            return Collections.unmodifiableSet(set);
        }
    }

    private native int accept0(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2, InetSocketAddress[] inetSocketAddressArr) throws IOException;

    private static native void initIDs();

    static {
        initIDs();
    }

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

    ServerSocketChannelImpl(SelectorProvider sp, FileDescriptor fd2, boolean bound) throws IOException {
        super(sp);
        this.thread = 0;
        this.lock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.fd = fd2;
        this.fdVal = IOUtil.fdVal(fd2);
        this.state = 0;
        if (bound) {
            this.localAddress = Net.localAddress(fd2);
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
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.localAddress == null) {
                inetSocketAddress = this.localAddress;
            } else {
                inetSocketAddress = Net.getRevealedLocalAddress(Net.asInetSocketAddress(this.localAddress));
            }
        }
        return inetSocketAddress;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0047, code lost:
        return r3;
     */
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
                } else if (name != StandardSocketOptions.SO_REUSEADDR || !Net.useExclusiveBind()) {
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                } else {
                    this.isReuseAddress = ((Boolean) value).booleanValue();
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
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name != StandardSocketOptions.SO_REUSEADDR || !Net.useExclusiveBind()) {
                    T socketOption = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return socketOption;
                } else {
                    T valueOf = Boolean.valueOf(this.isReuseAddress);
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
            z = this.localAddress != null ? true : $assertionsDisabled;
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
        InetSocketAddress isa;
        synchronized (this.lock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!isBound()) {
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
                Net.listen(this.fd, backlog < 1 ? 50 : backlog);
                synchronized (this.stateLock) {
                    this.localAddress = Net.localAddress(this.fd);
                }
            } else {
                throw new AlreadyBoundException();
            }
        }
        return this;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0083, code lost:
        return r1;
     */
    public SocketChannel accept() throws IOException {
        int n;
        SocketChannel sc;
        synchronized (this.lock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isBound()) {
                FileDescriptor newfd = new FileDescriptor();
                boolean z = true;
                InetSocketAddress[] isaa = new InetSocketAddress[1];
                try {
                    begin();
                    if (!isOpen()) {
                        this.thread = 0;
                        if (0 <= 0) {
                            z = false;
                        }
                        end(z);
                        return null;
                    }
                    this.thread = NativeThread.current();
                    do {
                        n = accept(this.fd, newfd, isaa);
                        if (n == -3) {
                        }
                        break;
                    } while (isOpen());
                    break;
                    this.thread = 0;
                    end(n > 0);
                    if (n < 1) {
                        return null;
                    }
                    IOUtil.configureBlocking(newfd, true);
                    InetSocketAddress isa = isaa[0];
                    sc = new SocketChannelImpl(provider(), newfd, isa);
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                    }
                } catch (SecurityException x) {
                    sc.close();
                    throw x;
                } catch (Throwable th) {
                    this.thread = 0;
                    if (0 <= 0) {
                        z = false;
                    }
                    end(z);
                    throw th;
                }
            } else {
                throw new NotYetBoundException();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    /* access modifiers changed from: protected */
    public void implCloseSelectableChannel() throws IOException {
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
            if (this.state != 1) {
                if (this.state == -1) {
                    this.state = 1;
                    return;
                }
                nd.close(this.fd);
                this.state = 1;
            }
        }
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        short s = Net.POLLNVAL & ops;
        boolean z = $assertionsDisabled;
        if (s != 0) {
            return $assertionsDisabled;
        }
        if (((Net.POLLERR | Net.POLLHUP) & ops) != 0) {
            int newOps2 = intOps;
            sk.nioReadyOps(newOps2);
            if (((~oldOps) & newOps2) != 0) {
                z = true;
            }
            return z;
        }
        if (!((Net.POLLIN & ops) == 0 || (intOps & 16) == 0)) {
            newOps |= 16;
        }
        sk.nioReadyOps(newOps);
        if (((~oldOps) & newOps) != 0) {
            z = true;
        }
        return z;
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r9.thread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        if (0 <= 0) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001b, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001c, code lost:
        end(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0020, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002e, code lost:
        r2 = sun.nio.ch.Net.poll(r9.fd, r10, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r9.thread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0031, code lost:
        if (r2 <= 0) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0033, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0035, code lost:
        end(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x003a, code lost:
        return r2;
     */
    public int poll(int events, long timeout) throws IOException {
        synchronized (this.lock) {
            boolean z = $assertionsDisabled;
            boolean z2 = true;
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.thread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                this.thread = 0;
                if (0 > 0) {
                    z = true;
                }
                end(z);
                throw th;
            }
        }
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = 0;
        if ((ops & 16) != 0) {
            newOps = 0 | Net.POLLIN;
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
        if (!isOpen()) {
            sb.append("closed");
        } else {
            synchronized (this.stateLock) {
                InetSocketAddress addr = localAddress();
                if (addr == null) {
                    sb.append("unbound");
                } else {
                    sb.append(Net.getRevealedLocalAddressAsString(addr));
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private int accept(FileDescriptor ssfd, FileDescriptor newfd, InetSocketAddress[] isaa) throws IOException {
        return accept0(ssfd, newfd, isaa);
    }
}
