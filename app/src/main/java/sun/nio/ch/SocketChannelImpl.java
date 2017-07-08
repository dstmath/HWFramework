package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
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
import java.nio.channels.NetworkChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.misc.IoTrace;
import sun.net.NetHooks;

class SocketChannelImpl extends SocketChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ST_CONNECTED = 2;
    private static final int ST_KILLED = 4;
    private static final int ST_KILLPENDING = 3;
    private static final int ST_PENDING = 1;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd;
    private final FileDescriptor fd;
    private final int fdVal;
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
        static final Set<SocketOption<?>> defaultOptions = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.DefaultOptionsHolder.<clinit>():void");
        }

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet(8);
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_KEEPALIVE);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.SO_LINGER);
            set.add(StandardSocketOptions.TCP_NODELAY);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(ExtendedSocketOption.SO_OOBINLINE);
            return Collections.unmodifiableSet(set);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SocketChannelImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.SocketChannelImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.<clinit>():void");
    }

    private static native int checkConnect(FileDescriptor fileDescriptor, boolean z, boolean z2) throws IOException;

    private static native int sendOutOfBandData(FileDescriptor fileDescriptor, byte b) throws IOException;

    public boolean translateReadyOps(int r1, int r2, sun.nio.ch.SelectionKeyImpl r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SocketChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean");
    }

    SocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = -assertionsDisabled;
        this.fd = Net.socket(true);
        this.fdVal = IOUtil.fdVal(this.fd);
        this.state = ST_UNCONNECTED;
    }

    SocketChannelImpl(SelectorProvider sp, FileDescriptor fd, boolean bound) throws IOException {
        super(sp);
        this.readerThread = 0;
        this.writerThread = 0;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = -assertionsDisabled;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_UNCONNECTED;
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
        this.state = ST_UNINITIALIZED;
        this.isInputOpen = true;
        this.isOutputOpen = true;
        this.readyToConnect = -assertionsDisabled;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_CONNECTED;
        this.localAddress = Net.localAddress(fd);
        this.remoteAddress = remote;
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

    public /* bridge */ /* synthetic */ NetworkChannel setOption(SocketOption name, Object value) throws IOException {
        return setOption(name, value);
    }

    public <T> SocketChannel m105setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.IP_TOS) {
                    if (!Net.isIPv6Available()) {
                        Net.setSocketOption(this.fd, StandardProtocolFamily.INET, name, value);
                    }
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
                if (!isOpen()) {
                    throw new ClosedChannelException();
                } else if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    r0 = Boolean.valueOf(this.isReuseAddress);
                    return r0;
                } else if (name == StandardSocketOptions.IP_TOS) {
                    if (Net.isIPv6Available()) {
                        r0 = Integer.valueOf((int) ST_UNCONNECTED);
                    } else {
                        r0 = Net.getSocketOption(this.fd, StandardProtocolFamily.INET, name);
                    }
                    return r0;
                } else {
                    r0 = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                    return r0;
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
            if (this.state == ST_KILLPENDING) {
                kill();
            }
        }
    }

    private void writerCleanup() throws IOException {
        synchronized (this.stateLock) {
            this.writerThread = 0;
            if (this.state == ST_KILLPENDING) {
                kill();
            }
        }
    }

    public int read(ByteBuffer buf) throws IOException {
        int i;
        InetAddress address;
        int i2;
        boolean z = true;
        boolean z2 = -assertionsDisabled;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.readLock) {
            if (ensureReadOpen()) {
                Object traceContext = null;
                if (isBlocking()) {
                    traceContext = IoTrace.socketReadBegin();
                }
                int n = ST_UNCONNECTED;
                int normalize;
                try {
                    begin();
                    i = this.stateLock;
                    synchronized (i) {
                        if (isOpen()) {
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
                            normalize = IOStatus.normalize(n);
                            readerCleanup();
                            if (isBlocking()) {
                                address = this.remoteAddress.getAddress();
                                i = this.remoteAddress.getPort();
                                if (n <= 0) {
                                    i2 = ST_UNCONNECTED;
                                }
                                IoTrace.socketReadEnd(traceContext, address, i, ST_UNCONNECTED, (long) i2);
                            }
                            if (n <= 0 && n != -2) {
                                z = -assertionsDisabled;
                            }
                            end(z);
                            synchronized (this.stateLock) {
                                if (n <= 0) {
                                    if (!this.isInputOpen) {
                                        return ST_UNINITIALIZED;
                                    }
                                }
                                if (-assertionsDisabled || IOStatus.check(n)) {
                                    return normalize;
                                }
                                throw new AssertionError();
                            }
                        }
                        readerCleanup();
                        if (isBlocking()) {
                            IoTrace.socketReadEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), ST_UNCONNECTED, (long) ST_UNCONNECTED);
                        }
                        end(-assertionsDisabled);
                        synchronized (this.stateLock) {
                            if (this.isInputOpen) {
                                if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                                    return ST_UNCONNECTED;
                                }
                                throw new AssertionError();
                            }
                            return ST_UNINITIALIZED;
                        }
                    }
                } finally {
                    address = address;
                    normalize = address;
                    address = n;
                    readerCleanup();
                    if (isBlocking()) {
                        address = this.remoteAddress.getAddress();
                        i = this.remoteAddress.getPort();
                        if (n > 0) {
                            i2 = n;
                        } else {
                            i2 = ST_UNCONNECTED;
                        }
                        IoTrace.socketReadEnd(traceContext, address, i, ST_UNCONNECTED, (long) i2);
                    }
                    if (n > 0 || n == -2) {
                        z2 = true;
                    }
                    end(z2);
                    synchronized (this.stateLock) {
                        if (n <= 0) {
                        }
                        if (-assertionsDisabled && !IOStatus.check(n)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                    if (!this.isInputOpen) {
                        return ST_UNINITIALIZED;
                    }
                    if (-assertionsDisabled) {
                    }
                }
            } else {
                return ST_UNINITIALIZED;
            }
        }
    }

    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.readLock) {
            if (ensureReadOpen()) {
                long n = 0;
                Object traceContext = null;
                if (isBlocking()) {
                    traceContext = IoTrace.socketReadBegin();
                }
                InetAddress address;
                boolean z;
                try {
                    begin();
                    int i = this.stateLock;
                    synchronized (i) {
                        if (isOpen()) {
                            long current = NativeThread.current();
                            this.readerThread = current;
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
                            readerCleanup();
                            if (isBlocking()) {
                                address = this.remoteAddress.getAddress();
                                i = this.remoteAddress.getPort();
                                current = 0;
                                if (n <= 0) {
                                    current = 0;
                                }
                                IoTrace.socketReadEnd(traceContext, address, i, ST_UNCONNECTED, current);
                            }
                            z = (n > 0 || n == -2) ? true : -assertionsDisabled;
                            end(z);
                            synchronized (this.stateLock) {
                                if (n <= 0) {
                                    if (!this.isInputOpen) {
                                        return -1;
                                    }
                                }
                                if (-assertionsDisabled || IOStatus.check(n)) {
                                    return normalize;
                                }
                                throw new AssertionError();
                            }
                        }
                        readerCleanup();
                        if (isBlocking()) {
                            IoTrace.socketReadEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), ST_UNCONNECTED, 0);
                        }
                        end(-assertionsDisabled);
                        synchronized (this.stateLock) {
                            if (this.isInputOpen) {
                                if (-assertionsDisabled || IOStatus.check(0)) {
                                    return 0;
                                }
                                throw new AssertionError();
                            }
                            return -1;
                        }
                    }
                } finally {
                    address = address;
                    InetAddress inetAddress = address;
                    address = n;
                    readerCleanup();
                    if (isBlocking()) {
                        IoTrace.socketReadEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), ST_UNCONNECTED, n > 0 ? n : 0);
                    }
                    z = (n > 0 || n == -2) ? true : -assertionsDisabled;
                    end(z);
                    synchronized (this.stateLock) {
                    }
                    if (n <= 0) {
                        if (!this.isInputOpen) {
                            return -1;
                        }
                    }
                    if (!-assertionsDisabled && !IOStatus.check(n)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
            } else {
                return -1;
            }
        }
    }

    public int write(ByteBuffer buf) throws IOException {
        boolean z = true;
        boolean z2 = -assertionsDisabled;
        if (buf == null) {
            throw new NullPointerException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            Object traceContext = IoTrace.socketWriteBegin();
            int normalize;
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        int n;
                        this.writerThread = NativeThread.current();
                        do {
                            n = IOUtil.write(this.fd, buf, -1, nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        normalize = IOStatus.normalize(n);
                        writerCleanup();
                        IoTrace.socketWriteEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), (long) (n > 0 ? n : ST_UNCONNECTED));
                        if (n <= 0 && n != -2) {
                            z = -assertionsDisabled;
                        }
                        end(z);
                        synchronized (this.stateLock) {
                            if (n <= 0) {
                                if (!this.isOutputOpen) {
                                    throw new AsynchronousCloseException();
                                }
                            }
                        }
                        if (-assertionsDisabled || IOStatus.check(n)) {
                            return normalize;
                        }
                        throw new AssertionError();
                    }
                    writerCleanup();
                    IoTrace.socketWriteEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), (long) ST_UNCONNECTED);
                    end(-assertionsDisabled);
                    synchronized (this.stateLock) {
                        if (this.isOutputOpen) {
                        } else {
                            throw new AsynchronousCloseException();
                        }
                    }
                    if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                        return ST_UNCONNECTED;
                    }
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                writerCleanup();
                InetAddress address = this.remoteAddress.getAddress();
                int port = this.remoteAddress.getPort();
                if (ST_UNCONNECTED > null) {
                    normalize = ST_UNCONNECTED;
                } else {
                    normalize = ST_UNCONNECTED;
                }
                IoTrace.socketWriteEnd(traceContext, address, port, (long) normalize);
                if (ST_UNCONNECTED > null || ST_UNCONNECTED == -2) {
                    z2 = true;
                }
                end(z2);
                synchronized (this.stateLock) {
                    if (ST_UNCONNECTED <= null) {
                    }
                    if (-assertionsDisabled && !IOStatus.check((int) ST_UNCONNECTED)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
                if (!this.isOutputOpen) {
                    AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                }
                if (-assertionsDisabled) {
                }
            }
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (this.writeLock) {
            ensureWriteOpen();
            Object traceContext = IoTrace.socketWriteBegin();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        long n;
                        this.writerThread = NativeThread.current();
                        do {
                            n = IOUtil.write(this.fd, srcs, offset, length, nd);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        long normalize = IOStatus.normalize(n);
                        writerCleanup();
                        IoTrace.socketWriteEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), n > 0 ? n : 0);
                        boolean z = (n > 0 || n == -2) ? true : -assertionsDisabled;
                        end(z);
                        synchronized (this.stateLock) {
                            if (n <= 0) {
                                if (!this.isOutputOpen) {
                                    throw new AsynchronousCloseException();
                                }
                            }
                        }
                        if (-assertionsDisabled || IOStatus.check(n)) {
                            return normalize;
                        }
                        throw new AssertionError();
                    }
                    writerCleanup();
                    IoTrace.socketWriteEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), 0);
                    end(-assertionsDisabled);
                    synchronized (this.stateLock) {
                        if (this.isOutputOpen) {
                        } else {
                            throw new AsynchronousCloseException();
                        }
                    }
                    if (-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                writerCleanup();
                IoTrace.socketWriteEnd(traceContext, this.remoteAddress.getAddress(), this.remoteAddress.getPort(), 0 > 0 ? 0 : 0);
                boolean z2 = (0 > 0 || 0 == -2) ? true : -assertionsDisabled;
                end(z2);
                synchronized (this.stateLock) {
                }
                if (0 <= 0) {
                    if (!this.isOutputOpen) {
                        AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                    }
                }
                if (!-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    int sendOutOfBandData(byte b) throws IOException {
        boolean z = true;
        synchronized (this.writeLock) {
            ensureWriteOpen();
            try {
                begin();
                synchronized (this.stateLock) {
                    if (isOpen()) {
                        int n;
                        this.writerThread = NativeThread.current();
                        do {
                            n = sendOutOfBandData(this.fd, b);
                            if (n != -3) {
                                break;
                            }
                        } while (isOpen());
                        int normalize = IOStatus.normalize(n);
                        writerCleanup();
                        if (n <= 0 && n != -2) {
                            z = -assertionsDisabled;
                        }
                        end(z);
                        synchronized (this.stateLock) {
                            if (n <= 0) {
                                if (!this.isOutputOpen) {
                                    throw new AsynchronousCloseException();
                                }
                            }
                        }
                        if (-assertionsDisabled || IOStatus.check(n)) {
                            return normalize;
                        }
                        throw new AssertionError();
                    }
                    writerCleanup();
                    end(-assertionsDisabled);
                    synchronized (this.stateLock) {
                        if (this.isOutputOpen) {
                        } else {
                            throw new AsynchronousCloseException();
                        }
                    }
                    if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                        return ST_UNCONNECTED;
                    }
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                writerCleanup();
                if (ST_UNCONNECTED <= null && ST_UNCONNECTED != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                synchronized (this.stateLock) {
                    if (ST_UNCONNECTED <= null) {
                    }
                    if (-assertionsDisabled && !IOStatus.check((int) ST_UNCONNECTED)) {
                        AssertionError assertionError = new AssertionError();
                    }
                }
                if (!this.isOutputOpen) {
                    AsynchronousCloseException asynchronousCloseException = new AsynchronousCloseException();
                }
                if (-assertionsDisabled) {
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

    public /* bridge */ /* synthetic */ NetworkChannel bind(SocketAddress local) throws IOException {
        return bind(local);
    }

    public SocketChannel m104bind(SocketAddress local) throws IOException {
        synchronized (this.readLock) {
            synchronized (this.writeLock) {
                synchronized (this.stateLock) {
                    if (!isOpen()) {
                        throw new ClosedChannelException();
                    } else if (this.state == ST_PENDING) {
                        throw new ConnectionPendingException();
                    } else if (this.localAddress != null) {
                        throw new AlreadyBoundException();
                    } else {
                        InetSocketAddress isa = local == null ? new InetSocketAddress(ST_UNCONNECTED) : Net.checkAddress(local);
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
            z = this.state == ST_CONNECTED ? true : -assertionsDisabled;
        }
        return z;
    }

    public boolean isConnectionPending() {
        boolean z = true;
        synchronized (this.stateLock) {
            if (this.state != ST_PENDING) {
                z = -assertionsDisabled;
            }
        }
        return z;
    }

    void ensureOpenAndUnconnected() throws IOException {
        synchronized (this.stateLock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (this.state == ST_CONNECTED) {
                throw new AlreadyConnectedException();
            } else if (this.state == ST_PENDING) {
                throw new ConnectionPendingException();
            }
        }
    }

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
                                int n;
                                if (this.localAddress == null) {
                                    NetHooks.beforeTcpConnect(this.fd, isa.getAddress(), isa.getPort());
                                }
                                this.readerThread = NativeThread.current();
                                do {
                                    InetAddress ia = isa.getAddress();
                                    if (ia.isAnyLocalAddress()) {
                                        ia = InetAddress.getLocalHost();
                                    }
                                    n = Net.connect(this.fd, ia, isa.getPort());
                                    if (n == -3) {
                                    }
                                    break;
                                } while (isOpen());
                                break;
                                readerCleanup();
                                boolean z = (n > 0 || n == -2) ? true : -assertionsDisabled;
                                end(z);
                                if (-assertionsDisabled || IOStatus.check(n)) {
                                    synchronized (this.stateLock) {
                                        this.remoteAddress = isa;
                                        if (n > 0) {
                                            this.state = ST_CONNECTED;
                                            if (isOpen()) {
                                                this.localAddress = Net.localAddress(this.fd);
                                            }
                                            return true;
                                        }
                                        if (!isBlocking()) {
                                            this.state = ST_PENDING;
                                            if (isOpen()) {
                                                this.localAddress = Net.localAddress(this.fd);
                                            }
                                        }
                                        return -assertionsDisabled;
                                    }
                                }
                                throw new AssertionError();
                            }
                            try {
                                readerCleanup();
                                end(-assertionsDisabled);
                                if (-assertionsDisabled || IOStatus.check((int) ST_UNCONNECTED)) {
                                    return -assertionsDisabled;
                                }
                                throw new AssertionError();
                            } catch (IOException x) {
                                close();
                                throw x;
                            }
                        }
                    } catch (Throwable th) {
                        readerCleanup();
                        boolean z2 = (ST_UNCONNECTED > null || ST_UNCONNECTED == -2) ? true : -assertionsDisabled;
                        end(z2);
                        if (!-assertionsDisabled && !IOStatus.check((int) ST_UNCONNECTED)) {
                            AssertionError assertionError = new AssertionError();
                        }
                    }
                }
            }
        }
    }

    public boolean finishConnect() throws java.io.IOException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.nio.ch.SocketChannelImpl.finishConnect():boolean. bs: [B:56:0x004a, B:112:0x00b4]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
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
                    Net.shutdown(this.fd, ST_UNCONNECTED);
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
                    Net.shutdown(this.fd, ST_PENDING);
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
            if (this.state != ST_KILLED) {
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

    public void kill() throws IOException {
        Object obj = null;
        synchronized (this.stateLock) {
            if (this.state == ST_KILLED) {
            } else if (this.state == ST_UNINITIALIZED) {
                this.state = ST_KILLED;
            } else {
                if (!-assertionsDisabled) {
                    if (!(isOpen() || isRegistered())) {
                        obj = ST_PENDING;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (this.readerThread == 0 && this.writerThread == 0) {
                    nd.close(this.fd);
                    this.state = ST_KILLED;
                } else {
                    this.state = ST_KILLPENDING;
                }
            }
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
        if ((ops & ST_PENDING) != 0) {
            newOps = ST_PENDING;
        }
        if ((ops & ST_KILLED) != 0) {
            newOps |= ST_KILLED;
        }
        if ((ops & 8) != 0) {
            newOps |= ST_KILLED;
        }
        sk.selector.putEventOps(sk, newOps);
    }

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSuperclass().getName());
        sb.append('[');
        if (isOpen()) {
            synchronized (this.stateLock) {
                switch (this.state) {
                    case ST_UNCONNECTED /*0*/:
                        sb.append("unconnected");
                        break;
                    case ST_PENDING /*1*/:
                        sb.append("connection-pending");
                        break;
                    case ST_CONNECTED /*2*/:
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
            }
        }
        sb.append("closed");
        sb.append(']');
        return sb.toString();
    }
}
