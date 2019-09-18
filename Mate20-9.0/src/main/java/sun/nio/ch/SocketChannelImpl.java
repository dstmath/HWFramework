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
import java.nio.channels.NoConnectionPendingException;
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
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
            HashSet<SocketOption<?>> set = new HashSet<>(8);
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_KEEPALIVE);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.SO_LINGER);
            set.add(StandardSocketOptions.TCP_NODELAY);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(ExtendedSocketOption.SO_OOBINLINE);
            if (ExtendedOptionsImpl.flowSupported()) {
                set.add(ExtendedSocketOptions.SO_FLOW_SLA);
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
        this.readyToConnect = $assertionsDisabled;
        this.guard = CloseGuard.get();
        this.fd = Net.socket(true);
        this.fdVal = IOUtil.fdVal(this.fd);
        this.state = 0;
        if (this.fd != null && this.fd.valid()) {
            this.guard.open("close");
        }
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd2, boolean bound) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = $assertionsDisabled;
        this.guard = CloseGuard.get();
        this.fd = fd2;
        this.fdVal = IOUtil.fdVal(fd2);
        this.state = 0;
        if (fd2 != null && fd2.valid()) {
            this.guard.open("close");
        }
        if (bound) {
            this.localAddress = Net.localAddress(fd2);
        }
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd2, InetSocketAddress remote) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = -1;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = $assertionsDisabled;
        this.guard = CloseGuard.get();
        this.fd = fd2;
        this.fdVal = IOUtil.fdVal(fd2);
        this.state = 2;
        this.localAddress = Net.localAddress(fd2);
        this.remoteAddress = remote;
        if (fd2 != null && fd2.valid()) {
            this.guard.open("close");
        }
    }

    public Socket socket() {
        Socket socket2;
        synchronized (this.stateLock) {
            if (this.socket == null) {
                this.socket = SocketAdaptor.create(this);
            }
            socket2 = this.socket;
        }
        return socket2;
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
                } else if (name != StandardSocketOptions.SO_REUSEADDR || !Net.useExclusiveBind()) {
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                    return this;
                } else {
                    this.isReuseAddress = ((Boolean) value).booleanValue();
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
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    T valueOf = Boolean.valueOf(this.isReuseAddress);
                    return valueOf;
                } else if (name == StandardSocketOptions.IP_TOS) {
                    T socketOption = Net.getSocketOption(this.fd, Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET, name);
                    return socketOption;
                } else {
                    T socketOption2 = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return socketOption2;
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
            } else if (!this.isInputOpen) {
                return $assertionsDisabled;
            } else {
                return true;
            }
        }
    }

    private void ensureWriteOpen() throws ClosedChannelException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!this.isOutputOpen) {
                throw new ClosedChannelException();
            } else if (!isConnected()) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        if (0 > 0) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0024, code lost:
        if (0 != -2) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0027, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0029, code lost:
        end(r4);
        r4 = r10.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002e, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x002f, code lost:
        if (0 > 0) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0033, code lost:
        if (r10.isInputOpen != false) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0035, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0037, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x003c, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        r3 = sun.nio.ch.IOUtil.read(r10.fd, r11, -1, nd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0052, code lost:
        if (r3 != -3) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0058, code lost:
        if (isOpen() == false) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x005b, code lost:
        r6 = sun.nio.ch.IOStatus.normalize(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0062, code lost:
        if (r3 > 0) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0064, code lost:
        if (r3 != -2) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0068, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0069, code lost:
        end(r1);
        r1 = r10.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x006e, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x006f, code lost:
        if (r3 > 0) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0073, code lost:
        if (r10.isInputOpen != false) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0075, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0077, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0078, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x007c, code lost:
        return r6;
     */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0093  */
    public int read(ByteBuffer buf) throws IOException {
        if (buf != null) {
            synchronized (this.readLock) {
                if (!ensureReadOpen()) {
                    return -1;
                }
                boolean z = $assertionsDisabled;
                int n = 0;
                boolean z2 = true;
                try {
                    begin();
                    synchronized (this.stateLock) {
                        if (isOpen()) {
                            this.readerThread = NativeThread.current();
                        }
                    }
                } catch (Throwable th) {
                    readerCleanup();
                    if (n <= 0) {
                        if (n != -2) {
                            end(z);
                            synchronized (this.stateLock) {
                                if (n <= 0) {
                                    try {
                                        if (!this.isInputOpen) {
                                            return -1;
                                        }
                                    } catch (Throwable th2) {
                                        while (true) {
                                            th = th2;
                                        }
                                        throw th;
                                    }
                                }
                                throw th;
                            }
                        }
                    }
                    z = true;
                    end(z);
                    synchronized (this.stateLock) {
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 17 */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0035, code lost:
        if (0 > 0) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0039, code lost:
        if (0 != -2) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003d, code lost:
        r13 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003e, code lost:
        end(r13);
        r12 = r1.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0043, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0046, code lost:
        if (0 > 0) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x004a, code lost:
        if (r1.isInputOpen != false) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x004c, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x004e, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x004f, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0053, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        r10 = sun.nio.ch.IOUtil.read(r1.fd, r2, r3, r4, nd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x006a, code lost:
        if (r10 != -3) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0070, code lost:
        if (isOpen() == false) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0073, code lost:
        r6 = sun.nio.ch.IOStatus.normalize(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x007c, code lost:
        if (r10 > 0) goto L_0x0084;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0080, code lost:
        if (r10 != -2) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0084, code lost:
        r13 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0085, code lost:
        end(r13);
        r12 = r1.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x008a, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x008d, code lost:
        if (r10 > 0) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0091, code lost:
        if (r1.isInputOpen != false) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0093, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0098, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x009c, code lost:
        return r6;
     */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x00b7  */
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        ByteBuffer[] byteBufferArr = dsts;
        int i = offset;
        int i2 = length;
        if (i < 0 || i2 < 0 || i > byteBufferArr.length - i2) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            if (!ensureReadOpen()) {
                return -1;
            }
            long n = 0;
            boolean z = $assertionsDisabled;
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.readerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                readerCleanup();
                if (n <= 0) {
                    if (n != -2) {
                        end(z);
                        synchronized (this.stateLock) {
                            if (n <= 0) {
                                try {
                                    if (!this.isInputOpen) {
                                        return -1;
                                    }
                                } catch (Throwable th2) {
                                    while (true) {
                                        th = th2;
                                    }
                                    throw th;
                                }
                            }
                            throw th;
                        }
                    }
                }
                z = true;
                end(z);
                synchronized (this.stateLock) {
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        writerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001c, code lost:
        if (0 > 0) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001e, code lost:
        if (0 != -2) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0021, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0023, code lost:
        end(r3);
        r3 = r9.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0028, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0029, code lost:
        if (0 > 0) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x002d, code lost:
        if (r9.isOutputOpen == false) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0035, code lost:
        throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0036, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0038, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x003a, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r2 = sun.nio.ch.IOUtil.write(r9.fd, r10, -1, nd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0050, code lost:
        if (r2 != -3) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0056, code lost:
        if (isOpen() == false) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0059, code lost:
        r5 = sun.nio.ch.IOStatus.normalize(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        writerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0060, code lost:
        if (r2 > 0) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0062, code lost:
        if (r2 != -2) goto L_0x0065;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0066, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0067, code lost:
        end(r1);
        r1 = r9.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x006c, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x006d, code lost:
        if (r2 > 0) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0071, code lost:
        if (r9.isOutputOpen == false) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0079, code lost:
        throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x007a, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x007c, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x007e, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x00a2, code lost:
        r3 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:22:0x002b, B:90:0x0097] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0095  */
    public int write(ByteBuffer buf) throws IOException {
        if (buf != null) {
            synchronized (this.writeLock) {
                ensureWriteOpen();
                boolean z = $assertionsDisabled;
                int n = 0;
                boolean z2 = true;
                try {
                    begin();
                    synchronized (this.stateLock) {
                        if (isOpen()) {
                            this.writerThread = NativeThread.current();
                        }
                    }
                } catch (Throwable th) {
                    writerCleanup();
                    if (n <= 0) {
                        if (n != -2) {
                            end(z);
                            synchronized (this.stateLock) {
                                if (n <= 0) {
                                    if (!this.isOutputOpen) {
                                        throw new AsynchronousCloseException();
                                    }
                                }
                                throw th;
                            }
                        }
                    }
                    z = true;
                    end(z);
                    synchronized (this.stateLock) {
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        writerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0027, code lost:
        if (0 > 0) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002b, code lost:
        if (0 != -2) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002e, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0030, code lost:
        end(r5);
        r5 = r12.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0035, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0038, code lost:
        if (0 > 0) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x003c, code lost:
        if (r12.isOutputOpen == false) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0044, code lost:
        throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0045, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0047, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0049, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        r3 = sun.nio.ch.IOUtil.write(r12.fd, r13, r14, r15, nd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0060, code lost:
        if (r3 != -3) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0066, code lost:
        if (isOpen() == false) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0069, code lost:
        r9 = sun.nio.ch.IOStatus.normalize(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        writerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0072, code lost:
        if (r3 > 0) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0076, code lost:
        if (r3 != -2) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0079, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x007b, code lost:
        end(r5);
        r5 = r12.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0080, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0083, code lost:
        if (r3 > 0) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0087, code lost:
        if (r12.isOutputOpen == false) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x008f, code lost:
        throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0090, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0092, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0094, code lost:
        return r9;
     */
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            long n = 0;
            boolean z = true;
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                if (n <= 0) {
                    if (n != -2) {
                        z = false;
                    }
                }
                end(z);
                synchronized (this.stateLock) {
                    if (n <= 0) {
                        try {
                            if (!this.isOutputOpen) {
                                throw new AsynchronousCloseException();
                            }
                        } catch (Throwable th2) {
                            while (true) {
                                th = th2;
                            }
                            throw th;
                        }
                    }
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        writerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001a, code lost:
        if (0 > 0) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001c, code lost:
        if (0 != -2) goto L_0x001f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001f, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0021, code lost:
        end(r3);
        r3 = r8.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0026, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0027, code lost:
        if (0 > 0) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002b, code lost:
        if (r8.isOutputOpen == false) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0033, code lost:
        throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0034, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0036, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0038, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r2 = sendOutOfBandData(r8.fd, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x004a, code lost:
        if (r2 != -3) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0050, code lost:
        if (isOpen() == false) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0053, code lost:
        r5 = sun.nio.ch.IOStatus.normalize(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        writerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x005a, code lost:
        if (r2 > 0) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x005c, code lost:
        if (r2 != -2) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0060, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0061, code lost:
        end(r1);
        r1 = r8.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0066, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0067, code lost:
        if (r2 > 0) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x006b, code lost:
        if (r8.isOutputOpen == false) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0073, code lost:
        throw new java.nio.channels.AsynchronousCloseException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0074, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0076, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0078, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x009c, code lost:
        r3 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:21:0x0029, B:89:0x0091] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x008f  */
    public int sendOutOfBandData(byte b) throws IOException {
        synchronized (this.writeLock) {
            ensureWriteOpen();
            boolean z = $assertionsDisabled;
            int n = 0;
            boolean z2 = true;
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        this.writerThread = NativeThread.current();
                    }
                }
            } catch (Throwable th) {
                writerCleanup();
                if (n <= 0) {
                    if (n != -2) {
                        end(z);
                        synchronized (this.stateLock) {
                            if (n <= 0) {
                                if (!this.isOutputOpen) {
                                    throw new AsynchronousCloseException();
                                }
                            }
                            throw th;
                        }
                    }
                }
                z = true;
                end(z);
                synchronized (this.stateLock) {
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void implConfigureBlocking(boolean block) throws IOException {
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
        InetSocketAddress inetSocketAddress;
        synchronized (this.stateLock) {
            inetSocketAddress = this.remoteAddress;
        }
        return inetSocketAddress;
    }

    public SocketChannel bind(SocketAddress local) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (!isOpen()) {
                        throw new ClosedChannelException();
                    } else if (this.state == 1) {
                        throw new ConnectionPendingException();
                    } else if (this.localAddress == null) {
                        InetSocketAddress isa = local == null ? new InetSocketAddress(0) : Net.checkAddress(local);
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkListen(isa.getPort());
                        }
                        NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                        Net.bind(this.fd, isa.getAddress(), isa.getPort());
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
            z = this.state == 2 ? true : $assertionsDisabled;
        }
        return z;
    }

    public boolean isConnectionPending() {
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
            } else if (this.state == 2) {
                throw new AlreadyConnectedException();
            } else if (this.state == 1) {
                throw new ConnectionPendingException();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:107:0x00e5, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:?, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x00e9, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003c, code lost:
        if (0 > 0) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
        if (0 != -2) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0041, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0043, code lost:
        end(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0049, code lost:
        return $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r10 = r3.getAddress();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006a, code lost:
        if (r10.isAnyLocalAddress() == false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x006c, code lost:
        r10 = java.net.InetAddress.getLocalHost();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0071, code lost:
        r7 = sun.nio.ch.Net.connect(r14.fd, r10, r3.getPort());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x007d, code lost:
        if (r7 != -3) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0083, code lost:
        if (isOpen() == false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0089, code lost:
        if (r7 > 0) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x008b, code lost:
        if (r7 != -2) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x008e, code lost:
        r8 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0090, code lost:
        r8 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0091, code lost:
        end(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        r8 = r14.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0098, code lost:
        monitor-enter(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        r14.remoteAddress = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x009b, code lost:
        if (r7 <= 0) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x009d, code lost:
        r14.state = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00a4, code lost:
        if (isOpen() == false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00a6, code lost:
        r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ae, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00b2, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00b7, code lost:
        if (isBlocking() != false) goto L_0x00c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00b9, code lost:
        r14.state = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00bf, code lost:
        if (isOpen() == false) goto L_0x00c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00c1, code lost:
        r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x00c9, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x00cd, code lost:
        return $assertionsDisabled;
     */
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
                    boolean z = $assertionsDisabled;
                    int n = 0;
                    boolean z2 = true;
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
                        if (n <= 0) {
                            if (n != -2) {
                                end(z);
                                throw th;
                            }
                        }
                        z = true;
                        end(z);
                        throw th;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x00c2, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x00c5, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x00cb, code lost:
        return $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x00cc, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x00d5, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x00d8, code lost:
        monitor-enter(r14.stateLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:?, code lost:
        r14.readerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x00dd, code lost:
        if (r14.state == 3) goto L_0x00df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x00df, code lost:
        kill();
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x00ea, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:?, code lost:
        end(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x00ee, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x00ef, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x00f3, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:?, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x00f7, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x001e, code lost:
        r2 = $assertionsDisabled;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        begin();
        r10 = blockingLock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x002b, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r11 = r14.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x002e, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0033, code lost:
        if (isOpen() != false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0035, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r4 = r14.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0039, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r14.readerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x003e, code lost:
        if (r14.state != 3) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0040, code lost:
        kill();
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0044, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0045, code lost:
        if (r3 > 0) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0047, code lost:
        if (r3 != -2) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x004a, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        end(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0051, code lost:
        return $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0052, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r14.readerThread = sun.nio.ch.NativeThread.current();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x005b, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        dalvik.system.BlockGuard.getThreadPolicy().onNetwork();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0068, code lost:
        if (isBlocking() != false) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x006a, code lost:
        r3 = checkConnect(r14.fd, $assertionsDisabled, r14.readyToConnect);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0073, code lost:
        if (r3 != -3) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0079, code lost:
        if (isOpen() == false) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x007c, code lost:
        r3 = checkConnect(r14.fd, true, r14.readyToConnect);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0085, code lost:
        if (r3 != 0) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0088, code lost:
        if (r3 != -3) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x008e, code lost:
        if (isOpen() == false) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0091, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        r10 = r14.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0094, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
        r14.readerThread = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0099, code lost:
        if (r14.state != 3) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x009b, code lost:
        kill();
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x009f, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x00a0, code lost:
        if (r3 > 0) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x00a2, code lost:
        if (r3 != -2) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x00a5, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x00a7, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        end(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x00ad, code lost:
        if (r3 <= 0) goto L_0x00c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:?, code lost:
        r2 = r14.stateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x00b1, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
        r14.state = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x00b8, code lost:
        if (isOpen() == false) goto L_0x00c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x00ba, code lost:
        r14.localAddress = sun.nio.ch.Net.localAddress(r14.fd);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:36:0x0037, B:131:0x00d9] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:57:0x0054=Splitter:B:57:0x0054, B:90:0x00a8=Splitter:B:90:0x00a8, B:139:0x00eb=Splitter:B:139:0x00eb, B:47:0x004c=Splitter:B:47:0x004c} */
    public boolean finishConnect() throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        boolean z = true;
                        if (this.state == 2) {
                            return true;
                        }
                        if (this.state != 1) {
                            throw new NoConnectionPendingException();
                        }
                    } else {
                        throw new ClosedChannelException();
                    }
                }
            }
        }
    }

    public SocketChannel shutdownInput() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!isConnected()) {
                throw new NotYetConnectedException();
            } else if (this.isInputOpen) {
                Net.shutdown(this.fd, 0);
                if (this.readerThread != 0) {
                    NativeThread.signal(this.readerThread);
                }
                this.isInputOpen = $assertionsDisabled;
            }
        }
        return this;
    }

    public SocketChannel shutdownOutput() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (!isConnected()) {
                throw new NotYetConnectedException();
            } else if (this.isOutputOpen) {
                Net.shutdown(this.fd, 1);
                if (this.writerThread != 0) {
                    NativeThread.signal(this.writerThread);
                }
                this.isOutputOpen = $assertionsDisabled;
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

    /* access modifiers changed from: protected */
    public void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            this.isInputOpen = $assertionsDisabled;
            this.isOutputOpen = $assertionsDisabled;
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

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0030, code lost:
        return;
     */
    public void kill() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != 4) {
                if (this.state == -1) {
                    this.state = 4;
                } else if (this.readerThread == 0 && this.writerThread == 0) {
                    nd.close(this.fd);
                    this.state = 4;
                } else {
                    this.state = 3;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
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
            this.readyToConnect = true;
            if (((~oldOps) & newOps2) != 0) {
                z = true;
            }
            return z;
        }
        if (!((Net.POLLIN & ops) == 0 || (intOps & 1) == 0 || this.state != 2)) {
            newOps |= 1;
        }
        if (!((Net.POLLCONN & ops) == 0 || (intOps & 8) == 0 || (this.state != 0 && this.state != 1))) {
            newOps |= 8;
            this.readyToConnect = true;
        }
        if (!((Net.POLLOUT & ops) == 0 || (intOps & 4) == 0 || this.state != 2)) {
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
        readerCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0017, code lost:
        if (0 <= 0) goto L_0x001a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001a, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001b, code lost:
        end(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001f, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002d, code lost:
        r2 = sun.nio.ch.Net.poll(r7.fd, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        readerCleanup();
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
                readerCleanup();
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
        if (!isOpen()) {
            sb.append("closed");
        } else {
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
        }
        sb.append(']');
        return sb.toString();
    }
}
