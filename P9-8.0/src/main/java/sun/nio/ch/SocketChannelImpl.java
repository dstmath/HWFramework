package sun.nio.ch;

import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jdk.net.ExtendedSocketOptions;
import sun.net.ExtendedOptionsImpl;
import sun.net.NetHooks;

class SocketChannelImpl extends SocketChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = (SocketChannelImpl.class.desiredAssertionStatus() ^ 1);
    private static final int ST_CONNECTED = 2;
    private static final int ST_KILLED = 4;
    private static final int ST_KILLPENDING = 3;
    private static final int ST_PENDING = 1;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd = new SocketDispatcher();
    private final FileDescriptor fd;
    private final int fdVal;
    private final CloseGuard guard;
    private boolean isInputOpen;
    private boolean isOutputOpen;
    private boolean isReuseAddress;
    private InetSocketAddress localAddress;
    private final Object readLock;
    private volatile long readerThread;
    private boolean readyToConnect;
    private InetSocketAddress remoteAddress;
    private Socket socket;
    private int state;
    private final Object stateLock;
    private final Object writeLock;
    private volatile long writerThread;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet(8);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_SNDBUF);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_RCVBUF);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_KEEPALIVE);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_REUSEADDR);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_LINGER);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.TCP_NODELAY);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.IP_TOS);
            set.-java_util_stream_DistinctOps$1-mthref-1(ExtendedSocketOption.SO_OOBINLINE);
            if (ExtendedOptionsImpl.flowSupported()) {
                set.-java_util_stream_DistinctOps$1-mthref-1(ExtendedSocketOptions.SO_FLOW_SLA);
            }
            return Collections.unmodifiableSet(set);
        }
    }

    private static native int checkConnect(FileDescriptor fileDescriptor, boolean z, boolean z2) throws IOException;

    private static native int sendOutOfBandData(FileDescriptor fileDescriptor, byte b) throws IOException;

    SocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = -assertionsDisabled;
        this.guard = CloseGuard.get();
        this.fd = Net.socket(true);
        this.fdVal = IOUtil.fdVal(this.fd);
        this.state = 0;
        if (this.fd != null && this.fd.valid()) {
            this.guard.open("close");
        }
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd, boolean bound) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = -assertionsDisabled;
        this.guard = CloseGuard.get();
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 0;
        if (fd != null && fd.valid()) {
            this.guard.open("close");
        }
        if (bound) {
            this.localAddress = Net.localAddress(fd);
        }
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd, InetSocketAddress remote) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = -assertionsDisabled;
        this.guard = CloseGuard.get();
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 2;
        this.localAddress = Net.localAddress(fd);
        this.remoteAddress = remote;
        if (fd != null && fd.valid()) {
            this.guard.open("close");
        }
    }

    public Socket socket() {
        Socket socket;
        synchronized (this.stateLock) {
            if (this.socket == null) {
                this.socket = SocketAdaptor.create(this);
            }
            socket = this.socket;
        }
        return socket;
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

    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
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
                    return this;
                } else {
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
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
                T valueOf;
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    valueOf = Boolean.valueOf(this.isReuseAddress);
                    return valueOf;
                } else if (name == StandardSocketOptions.IP_TOS) {
                    valueOf = Net.getSocketOption(this.fd, Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET, name);
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

    private boolean ensureReadOpen() throws ClosedChannelException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!isConnected()) {
                throw new NotYetConnectedException();
            } else if (this.isInputOpen) {
                return true;
            } else {
                return -assertionsDisabled;
            }
        }
    }

    private void ensureWriteOpen() throws ClosedChannelException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!this.isOutputOpen) {
                throw new ClosedChannelException();
            } else if (isConnected()) {
            } else {
                throw new NotYetConnectedException();
            }
        }
    }

    private void readerCleanup() throws IOException {
        synchronized (this.stateLock) {
            this.readerThread = 0;
            if (this.state == 3) {
                kill();
            }
        }
    }

    private void writerCleanup() throws IOException {
        synchronized (this.stateLock) {
            this.writerThread = 0;
            if (this.state == 3) {
                kill();
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:?, code:
            readerCleanup();
            end(-assertionsDisabled);
            r1 = r10.stateLock;
     */
    /* JADX WARNING: Missing block: B:22:0x002e, code:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:26:0x0033, code:
            if ((r10.isInputOpen ^ 1) == 0) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:30:0x0037, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:34:0x003b, code:
            if (-assertionsDisabled != false) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:36:0x0041, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:38:0x0048, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:47:0x0050, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:52:0x0058, code:
            r0 = sun.nio.ch.IOUtil.read(r10.fd, r11, -1, nd);
     */
    /* JADX WARNING: Missing block: B:53:0x0063, code:
            if (r0 != -3) goto L_0x006b;
     */
    /* JADX WARNING: Missing block: B:55:0x0069, code:
            if (isOpen() != false) goto L_0x0058;
     */
    /* JADX WARNING: Missing block: B:56:0x006b, code:
            r1 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:59:0x0072, code:
            if (r0 > 0) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:60:0x0074, code:
            if (r0 != -2) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:61:0x0076, code:
            end(r2);
            r2 = r10.stateLock;
     */
    /* JADX WARNING: Missing block: B:62:0x007b, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:63:0x007c, code:
            if (r0 > 0) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:67:0x0082, code:
            if ((r10.isInputOpen ^ 1) == 0) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:69:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:71:0x0086, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:92:0x00a3, code:
            r2 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:94:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:96:0x00a8, code:
            if (-assertionsDisabled != false) goto L_0x00b9;
     */
    /* JADX WARNING: Missing block: B:98:0x00ae, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00b9;
     */
    /* JADX WARNING: Missing block: B:100:0x00b5, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:105:0x00ba, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(ByteBuffer buf) throws IOException {
        boolean z = true;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.readLock) {
            if (ensureReadOpen()) {
                try {
                    begin();
                    synchronized (this.stateLock) {
                        if (isOpen()) {
                            this.readerThread = NativeThread.current();
                        }
                    }
                } catch (Throwable th) {
                    readerCleanup();
                    if (null <= null && 0 != -2) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    synchronized (this.stateLock) {
                        if (null <= null) {
                            if ((this.isInputOpen ^ 1) != 0) {
                                return -1;
                            }
                        }
                        if (!-assertionsDisabled && !IOStatus.check(0)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                }
            } else {
                return -1;
            }
        }
    }

    /* JADX WARNING: Missing block: B:24:?, code:
            readerCleanup();
            end(-assertionsDisabled);
            r2 = r10.stateLock;
     */
    /* JADX WARNING: Missing block: B:25:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:29:0x0038, code:
            if ((r10.isInputOpen ^ 1) == 0) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:34:0x003e, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:38:0x0042, code:
            if (-assertionsDisabled != false) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:40:0x0048, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:42:0x004f, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:52:0x0059, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:57:0x0061, code:
            r0 = sun.nio.ch.IOUtil.read(r10.fd, r11, r12, r13, nd);
     */
    /* JADX WARNING: Missing block: B:58:0x006d, code:
            if (r0 != -3) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:60:0x0073, code:
            if (isOpen() != false) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:61:0x0075, code:
            r6 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:63:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:64:0x0080, code:
            if (r0 > 0) goto L_0x0088;
     */
    /* JADX WARNING: Missing block: B:66:0x0086, code:
            if (r0 != -2) goto L_0x00cb;
     */
    /* JADX WARNING: Missing block: B:67:0x0088, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:68:0x0089, code:
            end(r2);
            r2 = r10.stateLock;
     */
    /* JADX WARNING: Missing block: B:69:0x008e, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:71:0x0093, code:
            if (r0 > 0) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:75:0x0099, code:
            if ((r10.isInputOpen ^ 1) == 0) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:77:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:80:0x009f, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:105:0x00cb, code:
            r2 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:107:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:109:0x00d0, code:
            if (-assertionsDisabled != false) goto L_0x00e1;
     */
    /* JADX WARNING: Missing block: B:111:0x00d6, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00e1;
     */
    /* JADX WARNING: Missing block: B:113:0x00dd, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:118:0x00e2, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            if (ensureReadOpen()) {
                try {
                    begin();
                    synchronized (this.stateLock) {
                        if (isOpen()) {
                            this.readerThread = NativeThread.current();
                        }
                    }
                } catch (Throwable th) {
                    readerCleanup();
                    boolean z = (0 > 0 || 0 == -2) ? true : -assertionsDisabled;
                    end(z);
                    synchronized (this.stateLock) {
                        if (0 <= 0) {
                            if ((this.isInputOpen ^ 1) != 0) {
                                return -1;
                            }
                        }
                        if (!-assertionsDisabled && !IOStatus.check(0)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                }
            } else {
                return -1;
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:?, code:
            writerCleanup();
            end(-assertionsDisabled);
            r2 = r9.stateLock;
     */
    /* JADX WARNING: Missing block: B:19:0x0028, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:22:0x002d, code:
            if ((r9.isOutputOpen ^ 1) == 0) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:24:0x0034, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:35:0x003e, code:
            if (-assertionsDisabled != false) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:37:0x0044, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:39:0x004b, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:41:0x004d, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:46:0x0055, code:
            r0 = sun.nio.ch.IOUtil.write(r9.fd, r10, -1, nd);
     */
    /* JADX WARNING: Missing block: B:47:0x0060, code:
            if (r0 != -3) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:49:0x0066, code:
            if (isOpen() != false) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:50:0x0068, code:
            r1 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            writerCleanup();
     */
    /* JADX WARNING: Missing block: B:53:0x006f, code:
            if (r0 > 0) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:54:0x0071, code:
            if (r0 != -2) goto L_0x00ac;
     */
    /* JADX WARNING: Missing block: B:55:0x0073, code:
            end(r2);
            r2 = r9.stateLock;
     */
    /* JADX WARNING: Missing block: B:56:0x0078, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:57:0x0079, code:
            if (r0 > 0) goto L_0x00ae;
     */
    /* JADX WARNING: Missing block: B:60:0x007f, code:
            if ((r9.isOutputOpen ^ 1) == 0) goto L_0x00ae;
     */
    /* JADX WARNING: Missing block: B:62:0x0086, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:88:0x00ac, code:
            r2 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:89:0x00ae, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:91:0x00b1, code:
            if (-assertionsDisabled != false) goto L_0x00bf;
     */
    /* JADX WARNING: Missing block: B:93:0x00b7, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00bf;
     */
    /* JADX WARNING: Missing block: B:95:0x00be, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:97:0x00c0, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int write(ByteBuffer buf) throws IOException {
        boolean z = true;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                if (null <= null && 0 != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                synchronized (this.stateLock) {
                    if (null <= null) {
                        if ((this.isOutputOpen ^ 1) != 0) {
                            AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                        }
                    }
                    if (!-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:?, code:
            writerCleanup();
            end(-assertionsDisabled);
            r3 = r10.stateLock;
     */
    /* JADX WARNING: Missing block: B:21:0x002c, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:24:0x0031, code:
            if ((r10.isOutputOpen ^ 1) == 0) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:26:0x0038, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:37:0x0042, code:
            if (-assertionsDisabled != false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:39:0x0048, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:41:0x004f, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:44:0x0053, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:49:0x005b, code:
            r0 = sun.nio.ch.IOUtil.write(r10.fd, r11, r12, r13, nd);
     */
    /* JADX WARNING: Missing block: B:50:0x0067, code:
            if (r0 != -3) goto L_0x006f;
     */
    /* JADX WARNING: Missing block: B:52:0x006d, code:
            if (isOpen() != false) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:53:0x006f, code:
            r6 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:55:?, code:
            writerCleanup();
     */
    /* JADX WARNING: Missing block: B:56:0x007a, code:
            if (r0 > 0) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:58:0x0080, code:
            if (r0 != -2) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:59:0x0082, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:60:0x0083, code:
            end(r2);
            r3 = r10.stateLock;
     */
    /* JADX WARNING: Missing block: B:61:0x0088, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:63:0x008d, code:
            if (r0 > 0) goto L_0x00cf;
     */
    /* JADX WARNING: Missing block: B:66:0x0093, code:
            if ((r10.isOutputOpen ^ 1) == 0) goto L_0x00cf;
     */
    /* JADX WARNING: Missing block: B:68:0x009a, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:97:0x00cd, code:
            r2 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:98:0x00cf, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:100:0x00d2, code:
            if (-assertionsDisabled != false) goto L_0x00e0;
     */
    /* JADX WARNING: Missing block: B:102:0x00d8, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00e0;
     */
    /* JADX WARNING: Missing block: B:104:0x00df, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:106:0x00e1, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                boolean z = (0 > 0 || 0 == -2) ? true : -assertionsDisabled;
                end(z);
                synchronized (this.stateLock) {
                    if (0 <= 0) {
                        if ((this.isOutputOpen ^ 1) != 0) {
                            AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                        }
                    }
                    if (!-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:?, code:
            writerCleanup();
            end(-assertionsDisabled);
            r2 = r9.stateLock;
     */
    /* JADX WARNING: Missing block: B:15:0x0020, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:18:0x0025, code:
            if ((r9.isOutputOpen ^ 1) == 0) goto L_0x0033;
     */
    /* JADX WARNING: Missing block: B:20:0x002c, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:31:0x0036, code:
            if (-assertionsDisabled != false) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:33:0x003c, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:35:0x0043, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:37:0x0045, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:42:0x004d, code:
            r0 = sendOutOfBandData(r9.fd, r10);
     */
    /* JADX WARNING: Missing block: B:43:0x0054, code:
            if (r0 != -3) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:45:0x005a, code:
            if (isOpen() != false) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:46:0x005c, code:
            r1 = sun.nio.ch.IOStatus.normalize(r0);
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            writerCleanup();
     */
    /* JADX WARNING: Missing block: B:49:0x0063, code:
            if (r0 > 0) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:50:0x0065, code:
            if (r0 != -2) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:51:0x0067, code:
            end(r2);
            r2 = r9.stateLock;
     */
    /* JADX WARNING: Missing block: B:52:0x006c, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:53:0x006d, code:
            if (r0 > 0) goto L_0x00a2;
     */
    /* JADX WARNING: Missing block: B:56:0x0073, code:
            if ((r9.isOutputOpen ^ 1) == 0) goto L_0x00a2;
     */
    /* JADX WARNING: Missing block: B:58:0x007a, code:
            throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Missing block: B:84:0x00a0, code:
            r2 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:85:0x00a2, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:87:0x00a5, code:
            if (-assertionsDisabled != false) goto L_0x00b3;
     */
    /* JADX WARNING: Missing block: B:89:0x00ab, code:
            if (sun.nio.ch.IOStatus.check(r0) != false) goto L_0x00b3;
     */
    /* JADX WARNING: Missing block: B:91:0x00b2, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:93:0x00b4, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int sendOutOfBandData(byte b) throws IOException {
        boolean z = true;
        synchronized (this.writeLock) {
            ensureWriteOpen();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                if (null <= null && 0 != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                synchronized (this.stateLock) {
                    if (null <= null) {
                        if ((this.isOutputOpen ^ 1) != 0) {
                            AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                        }
                    }
                    if (!-assertionsDisabled && !IOStatus.check(0)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public InetSocketAddress localAddress() {
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            inetSocketAddress = this.localAddress;
        }
        return inetSocketAddress;
    }

    public SocketAddress remoteAddress() {
        SocketAddress socketAddress;
        synchronized (this.stateLock) {
            socketAddress = this.remoteAddress;
        }
        return socketAddress;
    }

    public SocketChannel bind(SocketAddress local) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (!isOpen()) {
                        throw new ClosedChannelException();
                    } else if (this.state == 1) {
                        throw new ConnectionPendingException();
                    } else if (this.localAddress != null) {
                        throw new AlreadyBoundException();
                    } else {
                        InetSocketAddress isa = local == null ? new InetSocketAddress(0) : Net.checkAddress(local);
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkListen(isa.getPort());
                        }
                        NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                        Net.bind(this.fd, isa.getAddress(), isa.getPort());
                        this.localAddress = Net.localAddress(this.fd);
                    }
                }
            }
        }
        return this;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.state == 2 ? true : -assertionsDisabled;
        }
        return z;
    }

    public boolean isConnectionPending() {
        boolean z = true;
        synchronized (this.stateLock) {
            if (this.state != 1) {
                z = -assertionsDisabled;
            }
        }
        return z;
    }

    void ensureOpenAndUnconnected() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.state == 2) {
                throw new AlreadyConnectedException();
            } else if (this.state == 1) {
                throw new ConnectionPendingException();
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:?, code:
            readerCleanup();
            end(-assertionsDisabled);
     */
    /* JADX WARNING: Missing block: B:22:0x003f, code:
            if (-assertionsDisabled != false) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:24:0x0045, code:
            if (sun.nio.ch.IOStatus.check(0) != false) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:26:0x004c, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:27:0x004d, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            close();
     */
    /* JADX WARNING: Missing block: B:30:0x0051, code:
            throw r5;
     */
    /* JADX WARNING: Missing block: B:48:0x005f, code:
            return -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:56:0x0078, code:
            r0 = r1.getAddress();
     */
    /* JADX WARNING: Missing block: B:57:0x0080, code:
            if (r0.isAnyLocalAddress() == false) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:58:0x0082, code:
            r0 = java.net.InetAddress.getLocalHost();
     */
    /* JADX WARNING: Missing block: B:59:0x0086, code:
            r3 = sun.nio.ch.Net.connect(r14.fd, r0, r1.getPort());
     */
    /* JADX WARNING: Missing block: B:60:0x0091, code:
            if (r3 != -3) goto L_0x0099;
     */
    /* JADX WARNING: Missing block: B:62:0x0097, code:
            if (isOpen() != false) goto L_0x0078;
     */
    /* JADX WARNING: Missing block: B:64:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:65:0x009c, code:
            if (r3 > 0) goto L_0x00a1;
     */
    /* JADX WARNING: Missing block: B:67:0x009f, code:
            if (r3 != -2) goto L_0x00d5;
     */
    /* JADX WARNING: Missing block: B:68:0x00a1, code:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:69:0x00a2, code:
            end(r6);
     */
    /* JADX WARNING: Missing block: B:70:0x00a7, code:
            if (-assertionsDisabled != false) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:72:0x00ad, code:
            if (sun.nio.ch.IOStatus.check(r3) != false) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:74:0x00b4, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:92:0x00d5, code:
            r6 = -assertionsDisabled;
     */
    /* JADX WARNING: Missing block: B:96:?, code:
            r7 = r14.stateLock;
     */
    /* JADX WARNING: Missing block: B:97:0x00dc, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:99:?, code:
            r14.remoteAddress = r1;
     */
    /* JADX WARNING: Missing block: B:100:0x00df, code:
            if (r3 <= 0) goto L_0x00f8;
     */
    /* JADX WARNING: Missing block: B:101:0x00e1, code:
            r14.state = 2;
     */
    /* JADX WARNING: Missing block: B:102:0x00e8, code:
            if (isOpen() == false) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:103:0x00ea, code:
            r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Missing block: B:105:?, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:112:0x00f7, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:115:0x00fc, code:
            if (isBlocking() != false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:116:0x00fe, code:
            r14.state = 1;
     */
    /* JADX WARNING: Missing block: B:117:0x0105, code:
            if (isOpen() == false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:118:0x0107, code:
            r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Missing block: B:120:?, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:127:0x0114, code:
            return -assertionsDisabled;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean connect(SocketAddress sa) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                ensureOpenAndUnconnected();
                InetSocketAddress isa = Net.checkAddress(sa);
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
                }
                synchronized (blockingLock()) {
                    try {
                        begin();
                        synchronized (this.stateLock) {
                            if (isOpen()) {
                                if (this.localAddress == null) {
                                    NetHooks.beforeTcpConnect(this.fd, isa.getAddress(), isa.getPort());
                                }
                                this.readerThread = NativeThread.current();
                            }
                        }
                    } catch (Throwable th) {
                        readerCleanup();
                        boolean z = (null > null || 0 == -2) ? true : -assertionsDisabled;
                        end(z);
                        if (!-assertionsDisabled && !IOStatus.check(0)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                }
            }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.nio.ch.SocketChannelImpl.finishConnect():boolean, dom blocks: [B:55:0x004a, B:110:0x00b4]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public boolean finishConnect() throws java.io.IOException {
        /*
        r14 = this;
        r13 = 3;
        r12 = -2;
        r3 = 1;
        r4 = 0;
        r5 = r14.readLock;
        monitor-enter(r5);
        r6 = r14.writeLock;	 Catch:{ all -> 0x001f }
        monitor-enter(r6);	 Catch:{ all -> 0x001f }
        r7 = r14.stateLock;	 Catch:{ all -> 0x001c }
        monitor-enter(r7);	 Catch:{ all -> 0x001c }
        r2 = r14.isOpen();	 Catch:{ all -> 0x0019 }
        if (r2 != 0) goto L_0x0022;	 Catch:{ all -> 0x0019 }
    L_0x0013:
        r2 = new java.nio.channels.ClosedChannelException;	 Catch:{ all -> 0x0019 }
        r2.<init>();	 Catch:{ all -> 0x0019 }
        throw r2;	 Catch:{ all -> 0x0019 }
    L_0x0019:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x001c }
        throw r2;	 Catch:{ all -> 0x001c }
    L_0x001c:
        r2 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        throw r2;	 Catch:{ all -> 0x001f }
    L_0x001f:
        r2 = move-exception;
        monitor-exit(r5);
        throw r2;
    L_0x0022:
        r2 = r14.state;	 Catch:{ all -> 0x0019 }
        r8 = 2;
        if (r2 != r8) goto L_0x002b;
    L_0x0027:
        monitor-exit(r7);	 Catch:{ all -> 0x001c }
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r3;
    L_0x002b:
        r2 = r14.state;	 Catch:{ all -> 0x0019 }
        if (r2 == r3) goto L_0x0035;	 Catch:{ all -> 0x0019 }
    L_0x002f:
        r2 = new java.nio.channels.NoConnectionPendingException;	 Catch:{ all -> 0x0019 }
        r2.<init>();	 Catch:{ all -> 0x0019 }
        throw r2;	 Catch:{ all -> 0x0019 }
    L_0x0035:
        monitor-exit(r7);	 Catch:{ all -> 0x001c }
        r0 = 0;
        r14.begin();	 Catch:{  }
        r7 = r14.blockingLock();	 Catch:{  }
        monitor-enter(r7);	 Catch:{  }
        r8 = r14.stateLock;	 Catch:{ all -> 0x00d0 }
        monitor-enter(r8);	 Catch:{ all -> 0x00d0 }
        r2 = r14.isOpen();	 Catch:{  }
        if (r2 != 0) goto L_0x007e;
    L_0x0048:
        monitor-exit(r8);	 Catch:{ all -> 0x00d0 }
        monitor-exit(r7);	 Catch:{  }
        r9 = r14.stateLock;	 Catch:{ IOException -> 0x0071 }
        monitor-enter(r9);	 Catch:{ IOException -> 0x0071 }
        r10 = 0;
        r14.readerThread = r10;	 Catch:{ all -> 0x0076 }
        r2 = r14.state;	 Catch:{ all -> 0x0076 }
        if (r2 != r13) goto L_0x0059;	 Catch:{ all -> 0x0076 }
    L_0x0055:
        r14.kill();	 Catch:{ all -> 0x0076 }
        r0 = 0;
    L_0x0059:
        monitor-exit(r9);	 Catch:{ all -> 0x00cd }
        if (r4 < 0) goto L_0x005e;
    L_0x005c:
        if (r12 != 0) goto L_0x0079;
    L_0x005e:
        r14.end(r3);	 Catch:{ IOException -> 0x0071 }
        r2 = -assertionsDisabled;	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x007b;	 Catch:{ IOException -> 0x0071 }
    L_0x0065:
        r2 = sun.nio.ch.IOStatus.check(r0);	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x007b;	 Catch:{ IOException -> 0x0071 }
    L_0x006b:
        r2 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x0071 }
        r2.<init>();	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0071:
        r1 = move-exception;
        r14.close();	 Catch:{ all -> 0x001c }
        throw r1;	 Catch:{ all -> 0x001c }
    L_0x0076:
        r2 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x00cd }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0079:
        r3 = r4;
        goto L_0x005e;
    L_0x007b:
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r4;
    L_0x007e:
        r10 = sun.nio.ch.NativeThread.current();	 Catch:{  }
        r14.readerThread = r10;	 Catch:{  }
        monitor-exit(r8);	 Catch:{ all -> 0x00d0 }
        r2 = dalvik.system.BlockGuard.getThreadPolicy();	 Catch:{ all -> 0x00d0 }
        r2.onNetwork();	 Catch:{ all -> 0x00d0 }
        r2 = r14.isBlocking();	 Catch:{ all -> 0x00d0 }
        if (r2 != 0) goto L_0x00fb;	 Catch:{ all -> 0x00d0 }
    L_0x0092:
        r2 = r14.fd;	 Catch:{ all -> 0x00d0 }
        r8 = r14.readyToConnect;	 Catch:{ all -> 0x00d0 }
        r9 = 0;	 Catch:{ all -> 0x00d0 }
        r0 = checkConnect(r2, r9, r8);	 Catch:{ all -> 0x00d0 }
        r2 = -3;	 Catch:{ all -> 0x00d0 }
        if (r0 != r2) goto L_0x00a4;	 Catch:{ all -> 0x00d0 }
    L_0x009e:
        r2 = r14.isOpen();	 Catch:{ all -> 0x00d0 }
        if (r2 != 0) goto L_0x0092;
    L_0x00a4:
        monitor-exit(r7);	 Catch:{  }
        r7 = r14.stateLock;	 Catch:{ IOException -> 0x0071 }
        monitor-enter(r7);	 Catch:{ IOException -> 0x0071 }
        r8 = 0;
        r14.readerThread = r8;	 Catch:{ all -> 0x0110 }
        r2 = r14.state;	 Catch:{ all -> 0x0110 }
        if (r2 != r13) goto L_0x00b4;	 Catch:{ all -> 0x0110 }
    L_0x00b0:
        r14.kill();	 Catch:{ all -> 0x0110 }
        r0 = 0;
    L_0x00b4:
        monitor-exit(r7);	 Catch:{ all -> 0x00d3 }
        if (r0 > 0) goto L_0x00b9;
    L_0x00b7:
        if (r0 != r12) goto L_0x0113;
    L_0x00b9:
        r2 = r3;
    L_0x00ba:
        r14.end(r2);	 Catch:{ IOException -> 0x0071 }
        r2 = -assertionsDisabled;	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x011b;	 Catch:{ IOException -> 0x0071 }
    L_0x00c1:
        r2 = sun.nio.ch.IOStatus.check(r0);	 Catch:{ IOException -> 0x0071 }
        if (r2 != 0) goto L_0x011b;	 Catch:{ IOException -> 0x0071 }
    L_0x00c7:
        r2 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x0071 }
        r2.<init>();	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x00cd:
        r2 = move-exception;
        monitor-exit(r8);	 Catch:{ all -> 0x00d0 }
        throw r2;	 Catch:{ all -> 0x00d0 }
    L_0x00d0:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{  }
        throw r2;	 Catch:{  }
    L_0x00d3:
        r2 = move-exception;
        r7 = r14.stateLock;	 Catch:{ IOException -> 0x0071 }
        monitor-enter(r7);	 Catch:{ IOException -> 0x0071 }
        r8 = 0;
        r14.readerThread = r8;	 Catch:{ all -> 0x0115 }
        r8 = r14.state;	 Catch:{ all -> 0x0115 }
        if (r8 != r13) goto L_0x00e3;	 Catch:{ all -> 0x0115 }
    L_0x00df:
        r14.kill();	 Catch:{ all -> 0x0115 }
        r0 = 0;
    L_0x00e3:
        monitor-exit(r7);	 Catch:{ IOException -> 0x0071 }
        if (r0 > 0) goto L_0x00e8;	 Catch:{ IOException -> 0x0071 }
    L_0x00e6:
        if (r0 != r12) goto L_0x0118;	 Catch:{ IOException -> 0x0071 }
    L_0x00e8:
        r14.end(r3);	 Catch:{ IOException -> 0x0071 }
        r3 = -assertionsDisabled;	 Catch:{ IOException -> 0x0071 }
        if (r3 != 0) goto L_0x011a;	 Catch:{ IOException -> 0x0071 }
    L_0x00ef:
        r3 = sun.nio.ch.IOStatus.check(r0);	 Catch:{ IOException -> 0x0071 }
        if (r3 != 0) goto L_0x011a;	 Catch:{ IOException -> 0x0071 }
    L_0x00f5:
        r2 = new java.lang.AssertionError;	 Catch:{ IOException -> 0x0071 }
        r2.<init>();	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x00fb:
        r2 = r14.fd;	 Catch:{ all -> 0x00d0 }
        r8 = r14.readyToConnect;	 Catch:{ all -> 0x00d0 }
        r9 = 1;	 Catch:{ all -> 0x00d0 }
        r0 = checkConnect(r2, r9, r8);	 Catch:{ all -> 0x00d0 }
        if (r0 == 0) goto L_0x00fb;	 Catch:{ all -> 0x00d0 }
    L_0x0106:
        r2 = -3;	 Catch:{ all -> 0x00d0 }
        if (r0 != r2) goto L_0x00a4;	 Catch:{ all -> 0x00d0 }
    L_0x0109:
        r2 = r14.isOpen();	 Catch:{ all -> 0x00d0 }
        if (r2 == 0) goto L_0x00a4;
    L_0x010f:
        goto L_0x00fb;
    L_0x0110:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x00d3 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0113:
        r2 = r4;	 Catch:{ IOException -> 0x0071 }
        goto L_0x00ba;	 Catch:{ IOException -> 0x0071 }
    L_0x0115:
        r2 = move-exception;	 Catch:{ IOException -> 0x0071 }
        monitor-exit(r7);	 Catch:{ IOException -> 0x0071 }
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x0118:
        r3 = r4;	 Catch:{ IOException -> 0x0071 }
        goto L_0x00e8;	 Catch:{ IOException -> 0x0071 }
    L_0x011a:
        throw r2;	 Catch:{ IOException -> 0x0071 }
    L_0x011b:
        if (r0 <= 0) goto L_0x0138;
    L_0x011d:
        r4 = r14.stateLock;	 Catch:{ all -> 0x001c }
        monitor-enter(r4);	 Catch:{ all -> 0x001c }
        r2 = 2;
        r14.state = r2;	 Catch:{ all -> 0x0135 }
        r2 = r14.isOpen();	 Catch:{ all -> 0x0135 }
        if (r2 == 0) goto L_0x0131;	 Catch:{ all -> 0x0135 }
    L_0x0129:
        r2 = r14.fd;	 Catch:{ all -> 0x0135 }
        r2 = sun.nio.ch.Net.localAddress(r2);	 Catch:{ all -> 0x0135 }
        r14.localAddress = r2;	 Catch:{ all -> 0x0135 }
    L_0x0131:
        monitor-exit(r4);	 Catch:{ all -> 0x001c }
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r3;
    L_0x0135:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x001c }
        throw r2;	 Catch:{ all -> 0x001c }
    L_0x0138:
        monitor-exit(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r5);
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.finishConnect():boolean");
    }

    public SocketChannel shutdownInput() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isConnected()) {
                if (this.isInputOpen) {
                    Net.shutdown(this.fd, 0);
                    if (this.readerThread != 0) {
                        NativeThread.signal(this.readerThread);
                    }
                    this.isInputOpen = -assertionsDisabled;
                }
            } else {
                throw new NotYetConnectedException();
            }
        }
        return this;
    }

    public SocketChannel shutdownOutput() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isConnected()) {
                if (this.isOutputOpen) {
                    Net.shutdown(this.fd, 1);
                    if (this.writerThread != 0) {
                        NativeThread.signal(this.writerThread);
                    }
                    this.isOutputOpen = -assertionsDisabled;
                }
            } else {
                throw new NotYetConnectedException();
            }
        }
        return this;
    }

    public boolean isInputOpen() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.isInputOpen;
        }
        return z;
    }

    public boolean isOutputOpen() {
        boolean z;
        synchronized (this.stateLock) {
            z = this.isOutputOpen;
        }
        return z;
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            this.isInputOpen = -assertionsDisabled;
            this.isOutputOpen = -assertionsDisabled;
            if (this.state != 4) {
                this.guard.close();
                nd.preClose(this.fd);
            }
            if (this.readerThread != 0) {
                NativeThread.signal(this.readerThread);
            }
            if (this.writerThread != 0) {
                NativeThread.signal(this.writerThread);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    /* JADX WARNING: Missing block: B:32:0x0046, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void kill() throws IOException {
        synchronized (this.stateLock) {
            if (this.state == 4) {
            } else if (this.state == -1) {
                this.state = 4;
            } else if (!-assertionsDisabled && (isOpen() || isRegistered())) {
                throw new AssertionError();
            } else if (this.readerThread == 0 && this.writerThread == 0) {
                nd.close(this.fd);
                this.state = 4;
            } else {
                this.state = 3;
            }
        }
    }

    protected void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
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
            this.readyToConnect = true;
            if (((~oldOps) & intOps) == 0) {
                z = -assertionsDisabled;
            }
            return z;
        }
        if (!((Net.POLLIN & ops) == 0 || (intOps & 1) == 0 || this.state != 2)) {
            newOps = initialOps | 1;
        }
        if (!((Net.POLLCONN & ops) == 0 || (intOps & 8) == 0 || (this.state != 0 && this.state != 1))) {
            newOps |= 8;
            this.readyToConnect = true;
        }
        if (!((Net.POLLOUT & ops) == 0 || (intOps & 4) == 0 || this.state != 2)) {
            newOps |= 4;
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

    /* JADX WARNING: Missing block: B:26:0x003c, code:
            r0 = sun.nio.ch.Net.poll(r6.fd, r7, r8);
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            readerCleanup();
     */
    /* JADX WARNING: Missing block: B:29:0x0045, code:
            if (r0 <= 0) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:30:0x0047, code:
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:31:0x0048, code:
            end(r1);
     */
    /* JADX WARNING: Missing block: B:33:0x004c, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int poll(int events, long timeout) throws IOException {
        boolean z = -assertionsDisabled;
        if (-assertionsDisabled || (Thread.holdsLock(blockingLock()) && !isBlocking())) {
            synchronized (this.readLock) {
                try {
                    begin();
                    synchronized (this.stateLock) {
                        if (isOpen()) {
                            this.readerThread = NativeThread.current();
                        }
                    }
                } finally {
                    readerCleanup();
                    end(-assertionsDisabled);
                }
            }
        } else {
            throw new AssertionError();
        }
        return 0;
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = 0;
        if ((ops & 1) != 0) {
            newOps = Net.POLLIN | 0;
        }
        if ((ops & 4) != 0) {
            newOps |= Net.POLLOUT;
        }
        if ((ops & 8) != 0) {
            newOps |= Net.POLLCONN;
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
        sb.append(getClass().getSuperclass().getName());
        sb.append('[');
        if (isOpen()) {
            synchronized (this.stateLock) {
                switch (this.state) {
                    case 0:
                        sb.append("unconnected");
                        break;
                    case 1:
                        sb.append("connection-pending");
                        break;
                    case 2:
                        sb.append("connected");
                        if (!this.isInputOpen) {
                            sb.append(" ishut");
                        }
                        if (!this.isOutputOpen) {
                            sb.append(" oshut");
                            break;
                        }
                        break;
                }
                InetSocketAddress addr = localAddress();
                if (addr != null) {
                    sb.append(" local=");
                    sb.append(Net.getRevealedLocalAddressAsString(addr));
                }
                if (remoteAddress() != null) {
                    sb.append(" remote=");
                    sb.append(remoteAddress().toString());
                }
            }
        } else {
            sb.append("closed");
        }
        sb.append(']');
        return sb.toString();
    }
}
