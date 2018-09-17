package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.net.NetHooks;

class ServerSocketChannelImpl extends ServerSocketChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ST_INUSE = 0;
    private static final int ST_KILLED = 1;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd;
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
        static final Set<SocketOption<?>> defaultOptions = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.ServerSocketChannelImpl.DefaultOptionsHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.ServerSocketChannelImpl.DefaultOptionsHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.ServerSocketChannelImpl.DefaultOptionsHolder.<clinit>():void");
        }

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet(2);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            return Collections.unmodifiableSet(set);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.ServerSocketChannelImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.ServerSocketChannelImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.ServerSocketChannelImpl.<clinit>():void");
    }

    private native int accept0(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor2, InetSocketAddress[] inetSocketAddressArr) throws IOException;

    private static native void initIDs();

    public boolean translateReadyOps(int r1, int r2, sun.nio.ch.SelectionKeyImpl r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.ServerSocketChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.ServerSocketChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean");
    }

    ServerSocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.thread = 0;
        this.lock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        this.fd = Net.serverSocket(true);
        this.fdVal = IOUtil.fdVal(this.fd);
        this.state = ST_INUSE;
    }

    ServerSocketChannelImpl(SelectorProvider sp, FileDescriptor fd, boolean bound) throws IOException {
        super(sp);
        this.thread = 0;
        this.lock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_INUSE;
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

    public /* bridge */ /* synthetic */ NetworkChannel setOption(SocketOption name, Object value) throws IOException {
        return setOption(name, value);
    }

    public <T> ServerSocketChannel m103setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            synchronized (this.stateLock) {
                if (isOpen()) {
                    if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                        this.isReuseAddress = ((Boolean) value).booleanValue();
                    } else {
                        Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                    }
                } else {
                    throw new ClosedChannelException();
                }
            }
            return this;
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
                    isa = new InetSocketAddress(ST_INUSE);
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
                if (backlog < ST_KILLED) {
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

    public SocketChannel accept() throws IOException {
        SocketChannel sc;
        boolean z = true;
        boolean z2 = -assertionsDisabled;
        synchronized (this.lock) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            } else if (isBound()) {
                FileDescriptor newfd = new FileDescriptor();
                InetSocketAddress[] isaa = new InetSocketAddress[ST_KILLED];
                try {
                    begin();
                    if (isOpen()) {
                        int n;
                        this.thread = NativeThread.current();
                        do {
                            n = accept0(this.fd, newfd, isaa);
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
                        } else if (n < ST_KILLED) {
                            return null;
                        } else {
                            IOUtil.configureBlocking(newfd, true);
                            InetSocketAddress isa = isaa[ST_INUSE];
                            sc = new SocketChannelImpl(provider(), newfd, isa);
                            SecurityManager sm = System.getSecurityManager();
                            if (sm != null) {
                                sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                            }
                            return sc;
                        }
                    }
                    this.thread = 0;
                    end(-assertionsDisabled);
                    if (-assertionsDisabled || IOStatus.check((int) ST_INUSE)) {
                        return null;
                    }
                    throw new AssertionError();
                } catch (SecurityException x) {
                    sc.close();
                    throw x;
                } catch (Throwable th) {
                    this.thread = 0;
                    if (ST_INUSE <= null) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (!-assertionsDisabled && !IOStatus.check((int) ST_INUSE)) {
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
            if (this.state != ST_KILLED) {
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
        Object obj = null;
        synchronized (this.stateLock) {
            if (this.state == ST_KILLED) {
            } else if (this.state == ST_UNINITIALIZED) {
                this.state = ST_KILLED;
            } else {
                if (!-assertionsDisabled) {
                    if (!(isOpen() || isRegistered())) {
                        obj = ST_KILLED;
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

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, ST_INUSE, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = ST_INUSE;
        if ((ops & 16) != 0) {
            newOps = ST_KILLED;
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
}
