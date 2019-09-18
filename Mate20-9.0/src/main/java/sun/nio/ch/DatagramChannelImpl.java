package sun.nio.ch;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jdk.net.ExtendedSocketOptions;
import sun.net.ExtendedOptionsImpl;
import sun.net.ResourceManager;
import sun.nio.ch.MembershipKeyImpl;

class DatagramChannelImpl extends DatagramChannel implements SelChImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ST_CONNECTED = 1;
    private static final int ST_KILLED = 2;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd = new DatagramDispatcher();
    private InetAddress cachedSenderInetAddress;
    private int cachedSenderPort;
    private final ProtocolFamily family;
    final FileDescriptor fd;
    private final int fdVal;
    private final CloseGuard guard = CloseGuard.get();
    private boolean isReuseAddress;
    private InetSocketAddress localAddress;
    private final Object readLock = new Object();
    private volatile long readerThread = 0;
    private MembershipRegistry registry;
    private InetSocketAddress remoteAddress;
    private boolean reuseAddressEmulated;
    private SocketAddress sender;
    private DatagramSocket socket;
    private int state = -1;
    private final Object stateLock = new Object();
    private final Object writeLock = new Object();
    private volatile long writerThread = 0;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet<>(8);
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.SO_BROADCAST);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(StandardSocketOptions.IP_MULTICAST_IF);
            set.add(StandardSocketOptions.IP_MULTICAST_TTL);
            set.add(StandardSocketOptions.IP_MULTICAST_LOOP);
            if (ExtendedOptionsImpl.flowSupported()) {
                set.add(ExtendedSocketOptions.SO_FLOW_SLA);
            }
            return Collections.unmodifiableSet(set);
        }
    }

    private static native void disconnect0(FileDescriptor fileDescriptor, boolean z) throws IOException;

    private static native void initIDs();

    private native int receive0(FileDescriptor fileDescriptor, long j, int i, boolean z) throws IOException;

    private native int send0(boolean z, FileDescriptor fileDescriptor, long j, int i, InetAddress inetAddress, int i2) throws IOException;

    static {
        initIDs();
    }

    public DatagramChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        ResourceManager.beforeUdpCreate();
        try {
            this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
            this.fd = Net.socket(this.family, $assertionsDisabled);
            this.fdVal = IOUtil.fdVal(this.fd);
            this.state = 0;
            if (this.fd != null && this.fd.valid()) {
                this.guard.open("close");
            }
        } catch (IOException ioe) {
            ResourceManager.afterUdpClose();
            throw ioe;
        }
    }

    public DatagramChannelImpl(SelectorProvider sp, ProtocolFamily family2) throws IOException {
        super(sp);
        if (family2 == StandardProtocolFamily.INET || family2 == StandardProtocolFamily.INET6) {
            if (family2 != StandardProtocolFamily.INET6 || Net.isIPv6Available()) {
                this.family = family2;
                this.fd = Net.socket(family2, $assertionsDisabled);
                this.fdVal = IOUtil.fdVal(this.fd);
                this.state = 0;
                if (this.fd != null && this.fd.valid()) {
                    this.guard.open("close");
                    return;
                }
                return;
            }
            throw new UnsupportedOperationException("IPv6 not available");
        } else if (family2 == null) {
            throw new NullPointerException("'family' is null");
        } else {
            throw new UnsupportedOperationException("Protocol family not supported");
        }
    }

    public DatagramChannelImpl(SelectorProvider sp, FileDescriptor fd2) throws IOException {
        super(sp);
        this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
        this.fd = fd2;
        this.fdVal = IOUtil.fdVal(fd2);
        this.state = 0;
        this.localAddress = Net.localAddress(fd2);
        if (fd2 != null && fd2.valid()) {
            this.guard.open("close");
        }
    }

    public DatagramSocket socket() {
        DatagramSocket datagramSocket;
        synchronized (this.stateLock) {
            if (this.socket == null) {
                this.socket = DatagramSocketAdaptor.create(this);
            }
            datagramSocket = this.socket;
        }
        return datagramSocket;
    }

    public SocketAddress getLocalAddress() throws IOException {
        InetSocketAddress revealedLocalAddress;
        synchronized (this.stateLock) {
            if (isOpen()) {
                revealedLocalAddress = Net.getRevealedLocalAddress(this.localAddress);
            } else {
                throw new ClosedChannelException();
            }
        }
        return revealedLocalAddress;
    }

    public SocketAddress getRemoteAddress() throws IOException {
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            if (isOpen()) {
                inetSocketAddress = this.remoteAddress;
            } else {
                throw new ClosedChannelException();
            }
        }
        return inetSocketAddress;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0053, code lost:
        return r5;
     */
    public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (!(name == StandardSocketOptions.IP_TOS || name == StandardSocketOptions.IP_MULTICAST_TTL)) {
                    if (name != StandardSocketOptions.IP_MULTICAST_LOOP) {
                        if (name != StandardSocketOptions.IP_MULTICAST_IF) {
                            if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind() && this.localAddress != null) {
                                this.reuseAddressEmulated = true;
                                this.isReuseAddress = ((Boolean) value).booleanValue();
                            }
                            Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                            return this;
                        } else if (value != null) {
                            NetworkInterface interf = (NetworkInterface) value;
                            if (this.family == StandardProtocolFamily.INET6) {
                                int index = interf.getIndex();
                                if (index != -1) {
                                    Net.setInterface6(this.fd, index);
                                } else {
                                    throw new IOException("Network interface cannot be identified");
                                }
                            } else {
                                Inet4Address target = Net.anyInet4Address(interf);
                                if (target != null) {
                                    Net.setInterface4(this.fd, Net.inet4AsInt(target));
                                } else {
                                    throw new IOException("Network interface not configured for IPv4");
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Cannot set IP_MULTICAST_IF to 'null'");
                        }
                    }
                }
                Net.setSocketOption(this.fd, this.family, name, value);
                return this;
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
                ensureOpen();
                if (!(name == StandardSocketOptions.IP_TOS || name == StandardSocketOptions.IP_MULTICAST_TTL)) {
                    if (name != StandardSocketOptions.IP_MULTICAST_LOOP) {
                        if (name == StandardSocketOptions.IP_MULTICAST_IF) {
                            if (this.family == StandardProtocolFamily.INET) {
                                int address = Net.getInterface4(this.fd);
                                if (address == 0) {
                                    return null;
                                }
                                NetworkInterface ni = NetworkInterface.getByInetAddress(Net.inet4FromInt(address));
                                if (ni != null) {
                                    return ni;
                                }
                                throw new IOException("Unable to map address to interface");
                            }
                            int index = Net.getInterface6(this.fd);
                            if (index == 0) {
                                return null;
                            }
                            NetworkInterface ni2 = NetworkInterface.getByIndex(index);
                            if (ni2 != null) {
                                return ni2;
                            }
                            throw new IOException("Unable to map index to interface");
                        } else if (name != StandardSocketOptions.SO_REUSEADDR || !this.reuseAddressEmulated) {
                            T socketOption = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                            return socketOption;
                        } else {
                            T valueOf = Boolean.valueOf(this.isReuseAddress);
                            return valueOf;
                        }
                    }
                }
                T socketOption2 = Net.getSocketOption(this.fd, this.family, name);
                return socketOption2;
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0067, code lost:
        if (r3 == null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        sun.nio.ch.Util.releaseTemporaryDirectBuffer(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006c, code lost:
        r14.readerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x006e, code lost:
        if (r2 > 0) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0070, code lost:
        if (r2 != -2) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0073, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0075, code lost:
        end(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0079, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r3.flip();
        r15.put(r3);
     */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00c5 A[SYNTHETIC, Splitter:B:74:0x00c5] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x00cc  */
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else if (dst == null) {
            throw new NullPointerException();
        } else if (this.localAddress == null) {
            return null;
        } else {
            synchronized (this.readLock) {
                ensureOpen();
                int n = 0;
                ByteBuffer bb = null;
                boolean z = true;
                try {
                    begin();
                    if (!isOpen()) {
                        if (0 != 0) {
                            Util.releaseTemporaryDirectBuffer(null);
                        }
                        this.readerThread = 0;
                        if (0 <= 0) {
                            if (0 != -2) {
                                z = false;
                            }
                        }
                        end(z);
                        return null;
                    }
                    SecurityManager security = System.getSecurityManager();
                    this.readerThread = NativeThread.current();
                    if (!isConnected()) {
                        if (security != null) {
                            bb = Util.getTemporaryDirectBuffer(dst.remaining());
                            while (true) {
                                n = receive(this.fd, bb);
                                if (n != -3 || !isOpen()) {
                                    if (n != -2) {
                                        InetSocketAddress isa = (InetSocketAddress) this.sender;
                                        security.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                                        break;
                                    }
                                    break;
                                }
                            }
                            SocketAddress socketAddress = this.sender;
                            if (bb != null) {
                                Util.releaseTemporaryDirectBuffer(bb);
                            }
                            this.readerThread = 0;
                            if (n <= 0) {
                                if (n != -2) {
                                    z = false;
                                }
                            }
                            end(z);
                            return socketAddress;
                        }
                    }
                    do {
                        n = receive(this.fd, dst);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    if (n == -2) {
                        if (0 != 0) {
                            Util.releaseTemporaryDirectBuffer(null);
                        }
                        this.readerThread = 0;
                        if (n <= 0) {
                            if (n != -2) {
                                z = false;
                            }
                        }
                        end(z);
                        return null;
                    }
                    SocketAddress socketAddress2 = this.sender;
                    if (bb != null) {
                    }
                    this.readerThread = 0;
                    if (n <= 0) {
                    }
                    end(z);
                    return socketAddress2;
                } catch (SecurityException e) {
                    bb.clear();
                } catch (Throwable th) {
                    if (bb != null) {
                        Util.releaseTemporaryDirectBuffer(bb);
                    }
                    this.readerThread = 0;
                    if (n <= 0) {
                        if (n != -2) {
                            z = false;
                        }
                    }
                    end(z);
                    throw th;
                }
            }
        }
    }

    private int receive(FileDescriptor fd2, ByteBuffer dst) throws IOException {
        int pos = dst.position();
        int lim = dst.limit();
        int rem = pos <= lim ? lim - pos : 0;
        if ((dst instanceof DirectBuffer) && rem > 0) {
            return receiveIntoNativeBuffer(fd2, dst, rem, pos);
        }
        int newSize = Math.max(rem, 1);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(newSize);
        try {
            BlockGuard.getThreadPolicy().onNetwork();
            int n = receiveIntoNativeBuffer(fd2, bb, newSize, 0);
            bb.flip();
            if (n > 0 && rem > 0) {
                dst.put(bb);
            }
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int receiveIntoNativeBuffer(FileDescriptor fd2, ByteBuffer bb, int rem, int pos) throws IOException {
        int n = receive0(fd2, ((DirectBuffer) bb).address() + ((long) pos), rem, isConnected());
        if (n > 0) {
            bb.position(pos + n);
        }
        return n;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        r3 = $assertionsDisabled;
        r4 = 0;
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        begin();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        if (isOpen() != false) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r11.writerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        if (0 > 0) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        if (0 != -2) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0051, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0053, code lost:
        end(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0057, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r11.writerThread = sun.nio.ch.NativeThread.current();
        dalvik.system.BlockGuard.getThreadPolicy().onNetwork();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0065, code lost:
        r4 = send(r11.fd, r12, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x006d, code lost:
        if (r4 != -3) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0073, code lost:
        if (isOpen() != false) goto L_0x0065;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0075, code lost:
        r9 = r11.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0077, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x007c, code lost:
        if (isOpen() == false) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0080, code lost:
        if (r11.localAddress != null) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0082, code lost:
        r11.localAddress = sun.nio.ch.Net.localAddress(r11.fd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x008a, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r9 = sun.nio.ch.IOStatus.normalize(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        r11.writerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0091, code lost:
        if (r4 > 0) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0093, code lost:
        if (r4 != -2) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0097, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0098, code lost:
        end(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009c, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00a0, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
        r11.writerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00a3, code lost:
        if (r4 <= 0) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00a5, code lost:
        if (r4 == -2) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00a9, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00aa, code lost:
        end(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00ad, code lost:
        throw r9;
     */
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        if (src != null) {
            synchronized (this.writeLock) {
                ensureOpen();
                InetSocketAddress isa = Net.checkAddress(target);
                InetAddress ia = isa.getAddress();
                if (ia != null) {
                    synchronized (this.stateLock) {
                        if (!isConnected()) {
                            if (target != null) {
                                SecurityManager sm = System.getSecurityManager();
                                if (sm != null) {
                                    if (ia.isMulticastAddress()) {
                                        sm.checkMulticast(ia);
                                    } else {
                                        sm.checkConnect(ia.getHostAddress(), isa.getPort());
                                    }
                                }
                            } else {
                                throw new NullPointerException();
                            }
                        } else if (target.equals(this.remoteAddress)) {
                            int write = write(src);
                            return write;
                        } else {
                            throw new IllegalArgumentException("Connected address not equal to target address");
                        }
                    }
                } else {
                    throw new IOException("Target address not resolved");
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    private int send(FileDescriptor fd2, ByteBuffer src, InetSocketAddress target) throws IOException {
        if (src instanceof DirectBuffer) {
            return sendFromNativeBuffer(fd2, src, target);
        }
        int pos = src.position();
        int lim = src.limit();
        ByteBuffer bb = Util.getTemporaryDirectBuffer(pos <= lim ? lim - pos : 0);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = sendFromNativeBuffer(fd2, bb, target);
            if (n > 0) {
                src.position(pos + n);
            }
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int sendFromNativeBuffer(FileDescriptor fd2, ByteBuffer bb, InetSocketAddress target) throws IOException {
        int written;
        int pos = bb.position();
        int lim = bb.limit();
        boolean preferIPv6 = $assertionsDisabled;
        int rem = pos <= lim ? lim - pos : 0;
        if (this.family != StandardProtocolFamily.INET) {
            preferIPv6 = true;
        }
        try {
            written = send0(preferIPv6, fd2, ((DirectBuffer) bb).address() + ((long) pos), rem, target.getAddress(), target.getPort());
        } catch (PortUnreachableException pue) {
            if (!isConnected()) {
                written = rem;
            } else {
                throw pue;
            }
        }
        if (written > 0) {
            bb.position(pos + written);
        }
        return written;
    }

    public int read(ByteBuffer buf) throws IOException {
        if (buf != null) {
            synchronized (this.readLock) {
                synchronized (this.stateLock) {
                    ensureOpen();
                    if (!isConnected()) {
                        throw new NotYetConnectedException();
                    }
                }
                boolean z = $assertionsDisabled;
                int n = z;
                boolean z2 = true;
                try {
                    begin();
                    if (!isOpen()) {
                        this.readerThread = 0;
                        if (n <= 0) {
                            if (n != -2) {
                                z2 = z;
                            }
                        }
                        end(z2);
                        return z ? 1 : 0;
                    }
                    this.readerThread = NativeThread.current();
                    do {
                        n = IOUtil.read(this.fd, buf, -1, nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    int normalize = IOStatus.normalize(n);
                    if (n <= 0) {
                        if (n != -2) {
                            return normalize;
                        }
                    }
                    return normalize;
                } finally {
                    this.readerThread = 0;
                    if (n <= 0) {
                        if (n != -2) {
                            end(z);
                        }
                    }
                    z = z2;
                    end(z);
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        int i;
        int i2;
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (!isConnected()) {
                    throw new NotYetConnectedException();
                }
            }
            long n = 0;
            boolean z = true;
            try {
                begin();
                if (!isOpen()) {
                    this.readerThread = 0;
                    if (n <= 0) {
                        if (n != -2) {
                            z = false;
                        }
                    }
                    end(z);
                    return 0;
                }
                this.readerThread = NativeThread.current();
                do {
                    n = IOUtil.read(this.fd, dsts, offset, length, nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                long normalize = IOStatus.normalize(n);
                if (i <= 0) {
                    if (i2 != 0) {
                        z = false;
                    }
                }
                return normalize;
            } finally {
                this.readerThread = 0;
                if (n <= 0) {
                    if (n != -2) {
                        z = false;
                    }
                }
                end(z);
            }
        }
    }

    public int write(ByteBuffer buf) throws IOException {
        if (buf != null) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    ensureOpen();
                    if (!isConnected()) {
                        throw new NotYetConnectedException();
                    }
                }
                boolean z = $assertionsDisabled;
                int n = z;
                boolean z2 = true;
                try {
                    begin();
                    if (!isOpen()) {
                        this.writerThread = 0;
                        if (n <= 0) {
                            if (n != -2) {
                                z2 = z;
                            }
                        }
                        end(z2);
                        return z ? 1 : 0;
                    }
                    this.writerThread = NativeThread.current();
                    do {
                        n = IOUtil.write(this.fd, buf, -1, nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    int normalize = IOStatus.normalize(n);
                    if (n <= 0) {
                        if (n != -2) {
                            return normalize;
                        }
                    }
                    return normalize;
                } finally {
                    this.writerThread = 0;
                    if (n <= 0) {
                        if (n != -2) {
                            end(z);
                        }
                    }
                    z = z2;
                    end(z);
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        int i;
        int i2;
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (!isConnected()) {
                    throw new NotYetConnectedException();
                }
            }
            long n = 0;
            boolean z = true;
            try {
                begin();
                if (!isOpen()) {
                    this.writerThread = 0;
                    if (n <= 0) {
                        if (n != -2) {
                            z = false;
                        }
                    }
                    end(z);
                    return 0;
                }
                this.writerThread = NativeThread.current();
                do {
                    n = IOUtil.write(this.fd, srcs, offset, length, nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                long normalize = IOStatus.normalize(n);
                if (i <= 0) {
                    if (i2 != 0) {
                        z = false;
                    }
                }
                return normalize;
            } finally {
                this.writerThread = 0;
                if (n <= 0) {
                    if (n != -2) {
                        z = false;
                    }
                }
                end(z);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public SocketAddress localAddress() {
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            inetSocketAddress = this.localAddress;
        }
        return inetSocketAddress;
    }

    public SocketAddress remoteAddress() {
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            inetSocketAddress = this.remoteAddress;
        }
        return inetSocketAddress;
    }

    public DatagramChannel bind(SocketAddress local) throws IOException {
        InetSocketAddress isa;
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    ensureOpen();
                    if (this.localAddress == null) {
                        if (local != null) {
                            isa = Net.checkAddress(local);
                            if (this.family == StandardProtocolFamily.INET) {
                                if (!(isa.getAddress() instanceof Inet4Address)) {
                                    throw new UnsupportedAddressTypeException();
                                }
                            }
                        } else if (this.family == StandardProtocolFamily.INET) {
                            isa = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
                        } else {
                            isa = new InetSocketAddress(0);
                        }
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkListen(isa.getPort());
                        }
                        Net.bind(this.family, this.fd, isa.getAddress(), isa.getPort());
                        this.localAddress = Net.localAddress(this.fd);
                    } else {
                        throw new AlreadyBoundException();
                    }
                }
            }
        }
        return this;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.stateLock) {
            z = true;
            if (this.state != 1) {
                z = $assertionsDisabled;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void ensureOpenAndUnconnected() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.state != 0) {
                throw new IllegalStateException("Connect already invoked");
            }
        }
    }

    public DatagramChannel connect(SocketAddress sa) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    ensureOpenAndUnconnected();
                    InetSocketAddress isa = Net.checkAddress(sa);
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                    }
                    if (Net.connect(this.family, this.fd, isa.getAddress(), isa.getPort()) > 0) {
                        this.state = 1;
                        this.remoteAddress = isa;
                        this.sender = isa;
                        this.cachedSenderInetAddress = isa.getAddress();
                        this.cachedSenderPort = isa.getPort();
                        this.localAddress = Net.localAddress(this.fd);
                        synchronized (blockingLock()) {
                            try {
                                boolean blocking = isBlocking();
                                ByteBuffer tmpBuf = ByteBuffer.allocate(1);
                                if (blocking) {
                                    configureBlocking($assertionsDisabled);
                                }
                                do {
                                    tmpBuf.clear();
                                } while (receive(tmpBuf) != null);
                                if (blocking) {
                                    configureBlocking(true);
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    } else {
                        throw new Error();
                    }
                }
            }
        }
        return this;
    }

    public DatagramChannel disconnect() throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (isConnected()) {
                        if (isOpen()) {
                            InetSocketAddress isa = this.remoteAddress;
                            SecurityManager sm = System.getSecurityManager();
                            if (sm != null) {
                                sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                            }
                            disconnect0(this.fd, this.family == StandardProtocolFamily.INET6);
                            this.remoteAddress = null;
                            this.state = 0;
                            this.localAddress = Net.localAddress(this.fd);
                            return this;
                        }
                    }
                    return this;
                }
            }
        }
    }

    /* JADX WARNING: type inference failed for: r16v0 */
    /* JADX WARNING: type inference failed for: r1v31, types: [sun.nio.ch.MembershipKeyImpl$Type4] */
    /* JADX WARNING: type inference failed for: r1v32, types: [sun.nio.ch.MembershipKeyImpl$Type6] */
    /* JADX WARNING: Multi-variable type inference failed */
    private MembershipKey innerJoin(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException {
        NetworkInterface networkInterface;
        ? r16;
        byte[] bArr;
        InetAddress inetAddress = group;
        InetAddress inetAddress2 = source;
        if (group.isMulticastAddress()) {
            if (inetAddress instanceof Inet4Address) {
                if (this.family == StandardProtocolFamily.INET6 && !Net.canIPv6SocketJoinIPv4Group()) {
                    throw new IllegalArgumentException("IPv6 socket cannot join IPv4 multicast group");
                }
            } else if (!(inetAddress instanceof Inet6Address)) {
                NetworkInterface networkInterface2 = interf;
                throw new IllegalArgumentException("Address type not supported");
            } else if (this.family != StandardProtocolFamily.INET6) {
                NetworkInterface networkInterface3 = interf;
                throw new IllegalArgumentException("Only IPv6 sockets can join IPv6 multicast group");
            }
            if (inetAddress2 != null) {
                if (source.isAnyLocalAddress()) {
                    throw new IllegalArgumentException("Source address is a wildcard address");
                } else if (source.isMulticastAddress()) {
                    throw new IllegalArgumentException("Source address is multicast address");
                } else if (source.getClass() != group.getClass()) {
                    throw new IllegalArgumentException("Source address is different type to group");
                }
            }
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkMulticast(inetAddress);
            }
            synchronized (this.stateLock) {
                try {
                    if (isOpen()) {
                        if (this.registry == null) {
                            this.registry = new MembershipRegistry();
                            networkInterface = interf;
                        } else {
                            networkInterface = interf;
                            MembershipKey key = this.registry.checkMembership(inetAddress, networkInterface, inetAddress2);
                            if (key != null) {
                                return key;
                            }
                        }
                        if (this.family != StandardProtocolFamily.INET6 || (!(inetAddress instanceof Inet6Address) && !Net.canJoin6WithIPv4Group())) {
                            Inet4Address target = Net.anyInet4Address(interf);
                            if (target != null) {
                                int groupAddress = Net.inet4AsInt(group);
                                int targetAddress = Net.inet4AsInt(target);
                                int sourceAddress = inetAddress2 == null ? 0 : Net.inet4AsInt(source);
                                int n = Net.join4(this.fd, groupAddress, targetAddress, sourceAddress);
                                if (n != -2) {
                                    int i = n;
                                    int i2 = targetAddress;
                                    MembershipKeyImpl.Type4 type4 = new MembershipKeyImpl.Type4(this, inetAddress, networkInterface, inetAddress2, groupAddress, targetAddress, sourceAddress);
                                    r16 = type4;
                                } else {
                                    int i3 = n;
                                    int i4 = sourceAddress;
                                    int i5 = targetAddress;
                                    throw new UnsupportedOperationException();
                                }
                            } else {
                                throw new IOException("Network interface not configured for IPv4");
                            }
                        } else {
                            int index = interf.getIndex();
                            if (index != -1) {
                                byte[] groupAddress2 = Net.inet6AsByteArray(group);
                                if (inetAddress2 == null) {
                                    bArr = null;
                                } else {
                                    bArr = Net.inet6AsByteArray(source);
                                }
                                byte[] sourceAddress2 = bArr;
                                int n2 = Net.join6(this.fd, groupAddress2, index, sourceAddress2);
                                if (n2 != -2) {
                                    int i6 = n2;
                                    byte[] bArr2 = sourceAddress2;
                                    MembershipKeyImpl.Type6 type6 = new MembershipKeyImpl.Type6(this, inetAddress, networkInterface, inetAddress2, groupAddress2, index, sourceAddress2);
                                    MembershipKeyImpl.Type6 type62 = type6;
                                    r16 = type6;
                                } else {
                                    int i7 = n2;
                                    byte[] bArr3 = sourceAddress2;
                                    throw new UnsupportedOperationException();
                                }
                            } else {
                                throw new IOException("Network interface cannot be identified");
                            }
                        }
                        MembershipKeyImpl key2 = r16;
                        this.registry.add(key2);
                        return key2;
                    }
                    NetworkInterface networkInterface4 = interf;
                    throw new ClosedChannelException();
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        } else {
            NetworkInterface networkInterface5 = interf;
            throw new IllegalArgumentException("Group not a multicast address");
        }
    }

    public MembershipKey join(InetAddress group, NetworkInterface interf) throws IOException {
        return innerJoin(group, interf, null);
    }

    public MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException {
        if (source != null) {
            return innerJoin(group, interf, source);
        }
        throw new NullPointerException("source address is null");
    }

    /* access modifiers changed from: package-private */
    public void drop(MembershipKeyImpl key) {
        synchronized (this.stateLock) {
            if (key.isValid()) {
                try {
                    if (key instanceof MembershipKeyImpl.Type6) {
                        MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) key;
                        Net.drop6(this.fd, key6.groupAddress(), key6.index(), key6.source());
                    } else {
                        MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) key;
                        Net.drop4(this.fd, key4.groupAddress(), key4.interfaceAddress(), key4.source());
                    }
                    key.invalidate();
                    this.registry.remove(key);
                } catch (IOException ioe) {
                    throw new AssertionError((Object) ioe);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void block(MembershipKeyImpl key, InetAddress source) throws IOException {
        int n;
        synchronized (this.stateLock) {
            if (!key.isValid()) {
                throw new IllegalStateException("key is no longer valid");
            } else if (source.isAnyLocalAddress()) {
                throw new IllegalArgumentException("Source address is a wildcard address");
            } else if (source.isMulticastAddress()) {
                throw new IllegalArgumentException("Source address is multicast address");
            } else if (source.getClass() == key.group().getClass()) {
                if (key instanceof MembershipKeyImpl.Type6) {
                    MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) key;
                    n = Net.block6(this.fd, key6.groupAddress(), key6.index(), Net.inet6AsByteArray(source));
                } else {
                    MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) key;
                    n = Net.block4(this.fd, key4.groupAddress(), key4.interfaceAddress(), Net.inet4AsInt(source));
                }
                if (n == -2) {
                    throw new UnsupportedOperationException();
                }
            } else {
                throw new IllegalArgumentException("Source address is different type to group");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unblock(MembershipKeyImpl key, InetAddress source) {
        synchronized (this.stateLock) {
            if (key.isValid()) {
                try {
                    if (key instanceof MembershipKeyImpl.Type6) {
                        MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) key;
                        Net.unblock6(this.fd, key6.groupAddress(), key6.index(), Net.inet6AsByteArray(source));
                    } else {
                        MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) key;
                        Net.unblock4(this.fd, key4.groupAddress(), key4.interfaceAddress(), Net.inet4AsInt(source));
                    }
                } catch (IOException ioe) {
                    throw new AssertionError((Object) ioe);
                }
            } else {
                throw new IllegalStateException("key is no longer valid");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            this.guard.close();
            if (this.state != 2) {
                nd.preClose(this.fd);
            }
            ResourceManager.afterUdpClose();
            if (this.registry != null) {
                this.registry.invalidateAll();
            }
            long j = this.readerThread;
            long th = j;
            if (j != 0) {
                NativeThread.signal(th);
            }
            long j2 = this.writerThread;
            long th2 = j2;
            if (j2 != 0) {
                NativeThread.signal(th2);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    public void kill() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != 2) {
                if (this.state == -1) {
                    this.state = 2;
                    return;
                }
                nd.close(this.fd);
                this.state = 2;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            if (this.fd != null) {
                close();
            }
        } finally {
            super.finalize();
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
        if (!((Net.POLLIN & ops) == 0 || (intOps & 1) == 0)) {
            newOps |= 1;
        }
        if (!((Net.POLLOUT & ops) == 0 || (intOps & 4) == 0)) {
            newOps |= 4;
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
        r9.readerThread = 0;
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
        r9.readerThread = 0;
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
        synchronized (this.readLock) {
            boolean z = $assertionsDisabled;
            boolean z2 = true;
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.readerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                this.readerThread = 0;
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
        if ((ops & 1) != 0) {
            newOps = 0 | Net.POLLIN;
        }
        if ((ops & 4) != 0) {
            newOps |= Net.POLLOUT;
        }
        if ((ops & 8) != 0) {
            newOps |= Net.POLLIN;
        }
        sk.selector.putEventOps(sk, newOps);
    }

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }
}
