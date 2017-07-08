package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
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
import java.nio.channels.NetworkChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.net.ResourceManager;

class DatagramChannelImpl extends DatagramChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ST_CONNECTED = 1;
    private static final int ST_KILLED = 2;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd;
    private InetAddress cachedSenderInetAddress;
    private int cachedSenderPort;
    private final ProtocolFamily family;
    final FileDescriptor fd;
    private final int fdVal;
    private boolean isReuseAddress;
    private InetSocketAddress localAddress;
    private final Object readLock;
    private volatile long readerThread;
    private InetSocketAddress remoteAddress;
    private boolean reuseAddressEmulated;
    private SocketAddress sender;
    private DatagramSocket socket;
    private int state;
    private final Object stateLock;
    private final Object writeLock;
    private volatile long writerThread;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.DefaultOptionsHolder.<clinit>():void");
        }

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet(8);
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.SO_BROADCAST);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(StandardSocketOptions.IP_MULTICAST_IF);
            set.add(StandardSocketOptions.IP_MULTICAST_TTL);
            set.add(StandardSocketOptions.IP_MULTICAST_LOOP);
            return Collections.unmodifiableSet(set);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.DatagramChannelImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.DatagramChannelImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.<clinit>():void");
    }

    private static native void disconnect0(FileDescriptor fileDescriptor, boolean z) throws IOException;

    private static native void initIDs();

    private native int receive0(FileDescriptor fileDescriptor, long j, int i, boolean z) throws IOException;

    private native int send0(boolean z, FileDescriptor fileDescriptor, long j, int i, InetAddress inetAddress, int i2) throws IOException;

    public boolean translateReadyOps(int r1, int r2, sun.nio.ch.SelectionKeyImpl r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.DatagramChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean");
    }

    public DatagramChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        ResourceManager.beforeUdpCreate();
        try {
            this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
            this.fd = Net.socket(this.family, -assertionsDisabled);
            this.fdVal = IOUtil.fdVal(this.fd);
            this.state = ST_UNCONNECTED;
        } catch (IOException ioe) {
            ResourceManager.afterUdpClose();
            throw ioe;
        }
    }

    public DatagramChannelImpl(SelectorProvider sp, ProtocolFamily family) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        if (family == StandardProtocolFamily.INET || family == StandardProtocolFamily.INET6) {
            if (family != StandardProtocolFamily.INET6 || Net.isIPv6Available()) {
                this.family = family;
                this.fd = Net.socket(family, -assertionsDisabled);
                this.fdVal = IOUtil.fdVal(this.fd);
                this.state = ST_UNCONNECTED;
                return;
            }
            throw new UnsupportedOperationException("IPv6 not available");
        } else if (family == null) {
            throw new NullPointerException("'family' is null");
        } else {
            throw new UnsupportedOperationException("Protocol family not supported");
        }
    }

    public DatagramChannelImpl(SelectorProvider sp, FileDescriptor fd) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_UNCONNECTED;
        this.localAddress = Net.localAddress(fd);
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
        SocketAddress revealedLocalAddress;
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
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            if (isOpen()) {
                socketAddress = this.remoteAddress;
            } else {
                throw new ClosedChannelException();
            }
        }
        return socketAddress;
    }

    public /* bridge */ /* synthetic */ NetworkChannel m102setOption(SocketOption name, Object value) throws IOException {
        return setOption(name, value);
    }

    public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (name == StandardSocketOptions.IP_TOS) {
                    if (this.family == StandardProtocolFamily.INET) {
                        Net.setSocketOption(this.fd, this.family, name, value);
                    }
                    return this;
                } else if (name == StandardSocketOptions.IP_MULTICAST_TTL || name == StandardSocketOptions.IP_MULTICAST_LOOP) {
                    Net.setSocketOption(this.fd, this.family, name, value);
                    return this;
                } else if (name != StandardSocketOptions.IP_MULTICAST_IF) {
                    if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind() && this.localAddress != null) {
                        this.reuseAddressEmulated = true;
                        this.isReuseAddress = ((Boolean) value).booleanValue();
                    }
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                    return this;
                } else if (value == null) {
                    throw new IllegalArgumentException("Cannot set IP_MULTICAST_IF to 'null'");
                } else {
                    NetworkInterface interf = (NetworkInterface) value;
                    if (this.family == StandardProtocolFamily.INET6) {
                        int index = interf.getIndex();
                        if (index == ST_UNINITIALIZED) {
                            throw new IOException("Network interface cannot be identified");
                        }
                        Net.setInterface6(this.fd, index);
                    } else {
                        Inet4Address target = Net.anyInet4Address(interf);
                        if (target == null) {
                            throw new IOException("Network interface not configured for IPv4");
                        }
                        Net.setInterface4(this.fd, Net.inet4AsInt(target));
                    }
                    return this;
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
                ensureOpen();
                T socketOption;
                if (name == StandardSocketOptions.IP_TOS) {
                    if (this.family == StandardProtocolFamily.INET) {
                        socketOption = Net.getSocketOption(this.fd, this.family, name);
                        return socketOption;
                    }
                    socketOption = Integer.valueOf((int) ST_UNCONNECTED);
                    return socketOption;
                } else if (name == StandardSocketOptions.IP_MULTICAST_TTL || name == StandardSocketOptions.IP_MULTICAST_LOOP) {
                    socketOption = Net.getSocketOption(this.fd, this.family, name);
                    return socketOption;
                } else if (name == StandardSocketOptions.IP_MULTICAST_IF) {
                    NetworkInterface ni;
                    if (this.family == StandardProtocolFamily.INET) {
                        int address = Net.getInterface4(this.fd);
                        if (address == 0) {
                            return null;
                        }
                        ni = NetworkInterface.getByInetAddress(Net.inet4FromInt(address));
                        if (ni == null) {
                            throw new IOException("Unable to map address to interface");
                        }
                        return ni;
                    }
                    int index = Net.getInterface6(this.fd);
                    if (index == 0) {
                        return null;
                    }
                    ni = NetworkInterface.getByIndex(index);
                    if (ni == null) {
                        throw new IOException("Unable to map index to interface");
                    }
                    return ni;
                } else if (name == StandardSocketOptions.SO_REUSEADDR && this.reuseAddressEmulated) {
                    socketOption = Boolean.valueOf(this.isReuseAddress);
                    return socketOption;
                } else {
                    socketOption = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return socketOption;
                }
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        boolean z = true;
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else if (dst == null) {
            throw new NullPointerException();
        } else if (this.localAddress == null) {
            return null;
        } else {
            synchronized (this.readLock) {
                ensureOpen();
                int n = ST_UNCONNECTED;
                ByteBuffer byteBuffer = null;
                begin();
                if (isOpen()) {
                    SecurityManager security = System.getSecurityManager();
                    this.readerThread = NativeThread.current();
                    if (isConnected() || security == null) {
                        do {
                            n = receive(this.fd, dst);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        if (n == -2) {
                            this.readerThread = 0;
                            if (n <= 0 && n != -2) {
                                z = -assertionsDisabled;
                            }
                            end(z);
                            if (-assertionsDisabled || IOStatus.check(n)) {
                                return null;
                            }
                            throw new AssertionError();
                        }
                    }
                    byteBuffer = Util.getTemporaryDirectBuffer(dst.remaining());
                    while (true) {
                        n = receive(this.fd, byteBuffer);
                        if (n != -3 || !isOpen()) {
                            if (n == -2) {
                                break;
                            }
                            InetSocketAddress isa = this.sender;
                            try {
                                security.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                                break;
                            } catch (SecurityException e) {
                                byteBuffer.clear();
                            } catch (Throwable th) {
                                if (byteBuffer != null) {
                                    Util.releaseTemporaryDirectBuffer(byteBuffer);
                                }
                                this.readerThread = 0;
                                if (n <= 0 && n != -2) {
                                    z = -assertionsDisabled;
                                }
                                end(z);
                                if (!-assertionsDisabled && !IOStatus.check(n)) {
                                    AssertionError assertionError = new AssertionError();
                                }
                            }
                        }
                    }
                    if (byteBuffer != null) {
                        Util.releaseTemporaryDirectBuffer(byteBuffer);
                    }
                    this.readerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(n)) {
                        return null;
                    }
                    throw new AssertionError();
                    SocketAddress socketAddress = this.sender;
                    if (byteBuffer != null) {
                        Util.releaseTemporaryDirectBuffer(byteBuffer);
                    }
                    this.readerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(n)) {
                        return socketAddress;
                    }
                    throw new AssertionError();
                }
                this.readerThread = 0;
                end(-assertionsDisabled);
                if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                    return null;
                }
                throw new AssertionError();
            }
        }
    }

    private int receive(FileDescriptor fd, ByteBuffer dst) throws IOException {
        int i = ST_UNCONNECTED;
        int pos = dst.position();
        int lim = dst.limit();
        if (!-assertionsDisabled) {
            if (pos <= lim) {
                i = ST_CONNECTED;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        int rem = pos <= lim ? lim - pos : ST_UNCONNECTED;
        if ((dst instanceof DirectBuffer) && rem > 0) {
            return receiveIntoNativeBuffer(fd, dst, rem, pos);
        }
        int newSize = Math.max(rem, (int) ST_CONNECTED);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(newSize);
        try {
            BlockGuard.getThreadPolicy().onNetwork();
            int n = receiveIntoNativeBuffer(fd, bb, newSize, ST_UNCONNECTED);
            bb.flip();
            if (n > 0 && rem > 0) {
                dst.put(bb);
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return n;
        } catch (Throwable th) {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int receiveIntoNativeBuffer(FileDescriptor fd, ByteBuffer bb, int rem, int pos) throws IOException {
        FileDescriptor fileDescriptor = fd;
        int i = rem;
        int n = receive0(fileDescriptor, ((long) pos) + ((DirectBuffer) bb).address(), i, isConnected());
        if (n > 0) {
            bb.position(pos + n);
        }
        return n;
    }

    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        boolean z = true;
        if (src == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            ensureOpen();
            InetSocketAddress isa = Net.checkAddress(target);
            InetAddress ia = isa.getAddress();
            if (ia == null) {
                throw new IOException("Target address not resolved");
            }
            synchronized (this.stateLock) {
                int write;
                if (isConnected()) {
                    if (target.equals(this.remoteAddress)) {
                        write = write(src);
                        return write;
                    }
                    throw new IllegalArgumentException("Connected address not equal to target address");
                } else if (target == null) {
                    throw new NullPointerException();
                } else {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        if (ia.isMulticastAddress()) {
                            sm.checkMulticast(ia);
                        } else {
                            sm.checkConnect(ia.getHostAddress(), isa.getPort());
                        }
                    }
                    int n = ST_UNCONNECTED;
                    try {
                        begin();
                        long isOpen = isOpen();
                        if (isOpen == null) {
                            this.writerThread = isOpen;
                            end(-assertionsDisabled);
                            if (-assertionsDisabled || IOStatus.check(n)) {
                                return ST_UNCONNECTED;
                            }
                            throw new AssertionError();
                        }
                        this.writerThread = NativeThread.current();
                        BlockGuard.getThreadPolicy().onNetwork();
                        while (true) {
                            n = send(this.fd, src, isa);
                            if (n == -3) {
                                if (!isOpen()) {
                                    break;
                                }
                            }
                            break;
                        }
                        synchronized (this.stateLock) {
                            if (isOpen() && this.localAddress == null) {
                                this.localAddress = Net.localAddress(this.fd);
                            }
                        }
                        write = IOStatus.normalize(n);
                        this.writerThread = 0;
                        if (n <= 0 && n != -2) {
                            z = -assertionsDisabled;
                        }
                        end(z);
                        if (-assertionsDisabled || IOStatus.check(n)) {
                            return write;
                        }
                        throw new AssertionError();
                    } finally {
                        this.writerThread = 0;
                        if (n <= 0 && n != -2) {
                            z = -assertionsDisabled;
                        }
                        end(z);
                        if (!-assertionsDisabled && !IOStatus.check(n)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                }
            }
        }
    }

    private int send(FileDescriptor fd, ByteBuffer src, InetSocketAddress target) throws IOException {
        int rem = ST_UNCONNECTED;
        if (src instanceof DirectBuffer) {
            return sendFromNativeBuffer(fd, src, target);
        }
        int pos = src.position();
        int lim = src.limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? ST_CONNECTED : ST_UNCONNECTED) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        }
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = sendFromNativeBuffer(fd, bb, target);
            if (n > 0) {
                src.position(pos + n);
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return n;
        } catch (Throwable th) {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int sendFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, InetSocketAddress target) throws IOException {
        int written;
        int pos = bb.position();
        int lim = bb.limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? ST_CONNECTED : null) == null) {
                throw new AssertionError();
            }
        }
        int rem = pos <= lim ? lim - pos : ST_UNCONNECTED;
        try {
            written = send0(this.family != StandardProtocolFamily.INET ? true : -assertionsDisabled, fd, ((DirectBuffer) bb).address() + ((long) pos), rem, target.getAddress(), target.getPort());
        } catch (PortUnreachableException pue) {
            if (isConnected()) {
                throw pue;
            }
            written = rem;
        }
        if (written > 0) {
            bb.position(pos + written);
        }
        return written;
    }

    public int read(ByteBuffer buf) throws IOException {
        boolean z = true;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.readLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                if (isOpen()) {
                    int n;
                    this.readerThread = NativeThread.current();
                    while (true) {
                        n = IOUtil.read(this.fd, buf, -1, nd);
                        if (n == -3) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    }
                    int normalize = IOStatus.normalize(n);
                    this.readerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(n)) {
                        return normalize;
                    }
                    throw new AssertionError();
                }
                this.readerThread = 0;
                end(-assertionsDisabled);
                if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                    return ST_UNCONNECTED;
                }
                throw new AssertionError();
            } finally {
                this.readerThread = 0;
                if (ST_UNCONNECTED <= null && ST_UNCONNECTED != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check((int) ST_UNCONNECTED)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                long isOpen = isOpen();
                if (isOpen == null) {
                    this.readerThread = isOpen;
                    end(-assertionsDisabled);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                }
                long n;
                this.readerThread = NativeThread.current();
                while (true) {
                    n = IOUtil.read(this.fd, dsts, offset, length, nd);
                    if (n == -3) {
                        if (!isOpen()) {
                            break;
                        }
                    }
                    break;
                }
                long normalize = IOStatus.normalize(n);
                this.readerThread = 0;
                boolean z = (n > 0 || n == -2) ? true : -assertionsDisabled;
                end(z);
                if (-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            } finally {
                this.readerThread = 0;
                boolean z2 = (0 > 0 || 0 == -2) ? true : -assertionsDisabled;
                end(z2);
                if (!-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    public int write(ByteBuffer buf) throws IOException {
        boolean z = true;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                if (isOpen()) {
                    int n;
                    this.writerThread = NativeThread.current();
                    while (true) {
                        n = IOUtil.write(this.fd, buf, -1, nd);
                        if (n == -3) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    }
                    int normalize = IOStatus.normalize(n);
                    this.writerThread = 0;
                    if (n <= 0 && n != -2) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(n)) {
                        return normalize;
                    }
                    throw new AssertionError();
                }
                this.writerThread = 0;
                end(-assertionsDisabled);
                if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                    return ST_UNCONNECTED;
                }
                throw new AssertionError();
            } finally {
                this.writerThread = 0;
                if (ST_UNCONNECTED <= null && ST_UNCONNECTED != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check((int) ST_UNCONNECTED)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            synchronized (this.stateLock) {
                ensureOpen();
                if (isConnected()) {
                } else {
                    throw new NotYetConnectedException();
                }
            }
            try {
                begin();
                long isOpen = isOpen();
                if (isOpen == null) {
                    this.writerThread = isOpen;
                    end(-assertionsDisabled);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                }
                long n;
                this.writerThread = NativeThread.current();
                while (true) {
                    n = IOUtil.write(this.fd, srcs, offset, length, nd);
                    if (n == -3) {
                        if (!isOpen()) {
                            break;
                        }
                    }
                    break;
                }
                long normalize = IOStatus.normalize(n);
                this.writerThread = 0;
                boolean z = (n > 0 || n == -2) ? true : -assertionsDisabled;
                end(z);
                if (-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            } finally {
                this.writerThread = 0;
                boolean z2 = (0 > 0 || 0 == -2) ? true : -assertionsDisabled;
                end(z2);
                if (!-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public SocketAddress localAddress() {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            socketAddress = this.localAddress;
        }
        return socketAddress;
    }

    public SocketAddress remoteAddress() {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            socketAddress = this.remoteAddress;
        }
        return socketAddress;
    }

    public /* bridge */ /* synthetic */ NetworkChannel m101bind(SocketAddress local) throws IOException {
        return bind(local);
    }

    public DatagramChannel bind(SocketAddress local) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    ensureOpen();
                    if (this.localAddress != null) {
                        throw new AlreadyBoundException();
                    }
                    InetSocketAddress isa;
                    if (local == null) {
                        isa = this.family == StandardProtocolFamily.INET ? new InetSocketAddress(InetAddress.getByName("0.0.0.0"), (int) ST_UNCONNECTED) : new InetSocketAddress(ST_UNCONNECTED);
                    } else {
                        isa = Net.checkAddress(local);
                        if (this.family == StandardProtocolFamily.INET && !(isa.getAddress() instanceof Inet4Address)) {
                            throw new UnsupportedAddressTypeException();
                        }
                    }
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkListen(isa.getPort());
                    }
                    Net.bind(this.family, this.fd, isa.getAddress(), isa.getPort());
                    this.localAddress = Net.localAddress(this.fd);
                }
            }
        }
        return this;
    }

    public boolean isConnected() {
        boolean z = true;
        synchronized (this.stateLock) {
            if (this.state != ST_CONNECTED) {
                z = -assertionsDisabled;
            }
        }
        return z;
    }

    void ensureOpenAndUnconnected() throws IOException {
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
                    if (Net.connect(this.family, this.fd, isa.getAddress(), isa.getPort()) <= 0) {
                        throw new Error();
                    }
                    this.state = ST_CONNECTED;
                    this.remoteAddress = isa;
                    this.sender = isa;
                    this.cachedSenderInetAddress = isa.getAddress();
                    this.cachedSenderPort = isa.getPort();
                    this.localAddress = Net.localAddress(this.fd);
                }
            }
        }
        return this;
    }

    public DatagramChannel disconnect() throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (isConnected() && isOpen()) {
                        InetSocketAddress isa = this.remoteAddress;
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                        }
                        disconnect0(this.fd, this.family == StandardProtocolFamily.INET6 ? true : -assertionsDisabled);
                        this.remoteAddress = null;
                        this.state = ST_UNCONNECTED;
                        this.localAddress = Net.localAddress(this.fd);
                        return this;
                    }
                    return this;
                }
            }
        }
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != ST_KILLED) {
                nd.preClose(this.fd);
            }
            ResourceManager.afterUdpClose();
            long th = this.readerThread;
            if (th != 0) {
                NativeThread.signal(th);
            }
            th = this.writerThread;
            if (th != 0) {
                NativeThread.signal(th);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    public void kill() throws IOException {
        Object obj = null;
        synchronized (this.stateLock) {
            if (this.state == ST_KILLED) {
            } else if (this.state == ST_UNINITIALIZED) {
                this.state = ST_KILLED;
            } else {
                if (!-assertionsDisabled) {
                    if (!(isOpen() || isRegistered())) {
                        obj = ST_CONNECTED;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                nd.close(this.fd);
                this.state = ST_KILLED;
            }
        }
    }

    protected void finalize() throws IOException {
        if (this.fd != null) {
            close();
        }
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, ST_UNCONNECTED, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = ST_UNCONNECTED;
        if ((ops & ST_CONNECTED) != 0) {
            newOps = ST_CONNECTED;
        }
        if ((ops & 4) != 0) {
            newOps |= 4;
        }
        if ((ops & 8) != 0) {
            newOps |= ST_CONNECTED;
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
