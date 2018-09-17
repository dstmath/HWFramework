package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketAdaptor extends ServerSocket {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private final ServerSocketChannelImpl ssc;
    private volatile int timeout;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.ServerSocketAdaptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.ServerSocketAdaptor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.ServerSocketAdaptor.<clinit>():void");
    }

    public static ServerSocket create(ServerSocketChannelImpl ssc) {
        try {
            return new ServerSocketAdaptor(ssc);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    private ServerSocketAdaptor(ServerSocketChannelImpl ssc) throws IOException {
        this.timeout = 0;
        this.ssc = ssc;
    }

    public void bind(SocketAddress local) throws IOException {
        bind(local, 50);
    }

    public void bind(SocketAddress local, int backlog) throws IOException {
        if (local == null) {
            local = new InetSocketAddress(0);
        }
        try {
            this.ssc.bind(local, backlog);
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public InetAddress getInetAddress() {
        if (this.ssc.isBound()) {
            return Net.getRevealedLocalAddress(this.ssc.localAddress()).getAddress();
        }
        return null;
    }

    public int getLocalPort() {
        if (this.ssc.isBound()) {
            return Net.asInetSocketAddress(this.ssc.localAddress()).getPort();
        }
        return -1;
    }

    public Socket accept() throws IOException {
        synchronized (this.ssc.blockingLock()) {
            if (this.ssc.isBound()) {
                SelectionKey selectionKey;
                Selector selector;
                try {
                    SocketChannel sc;
                    Socket socket;
                    if (this.timeout == 0) {
                        sc = this.ssc.accept();
                        if (sc != null || this.ssc.isBlocking()) {
                            socket = sc.socket();
                            return socket;
                        }
                        throw new IllegalBlockingModeException();
                    }
                    selectionKey = null;
                    selector = null;
                    this.ssc.configureBlocking(false);
                    sc = this.ssc.accept();
                    if (sc != null) {
                        socket = sc.socket();
                        if (this.ssc.isOpen()) {
                            this.ssc.configureBlocking(true);
                        }
                        return socket;
                    }
                    selector = Util.getTemporarySelector(this.ssc);
                    selectionKey = this.ssc.register(selector, 16);
                    long to = (long) this.timeout;
                    while (this.ssc.isOpen()) {
                        long st = System.currentTimeMillis();
                        if (selector.select(to) > 0 && selectionKey.isAcceptable()) {
                            sc = this.ssc.accept();
                            if (sc != null) {
                                socket = sc.socket();
                                if (selectionKey != null) {
                                    selectionKey.cancel();
                                }
                                if (this.ssc.isOpen()) {
                                    this.ssc.configureBlocking(true);
                                }
                                if (selector != null) {
                                    Util.releaseTemporarySelector(selector);
                                }
                                return socket;
                            }
                        }
                        selector.selectedKeys().remove(selectionKey);
                        to -= System.currentTimeMillis() - st;
                        if (to <= 0) {
                            throw new SocketTimeoutException();
                        }
                    }
                    throw new ClosedChannelException();
                } catch (Exception x) {
                    Net.translateException(x);
                    if (-assertionsDisabled) {
                        return null;
                    }
                    throw new AssertionError();
                } catch (Throwable th) {
                    if (selectionKey != null) {
                        selectionKey.cancel();
                    }
                    if (this.ssc.isOpen()) {
                        this.ssc.configureBlocking(true);
                    }
                    if (selector != null) {
                        Util.releaseTemporarySelector(selector);
                    }
                }
            }
            throw new IllegalBlockingModeException();
        }
    }

    public void close() throws IOException {
        this.ssc.close();
    }

    public ServerSocketChannel getChannel() {
        return this.ssc;
    }

    public boolean isBound() {
        return this.ssc.isBound();
    }

    public boolean isClosed() {
        return !this.ssc.isOpen();
    }

    public void setSoTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
    }

    public int getSoTimeout() throws SocketException {
        return this.timeout;
    }

    public void setReuseAddress(boolean on) throws SocketException {
        try {
            this.ssc.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    public boolean getReuseAddress() throws SocketException {
        try {
            return ((Boolean) this.ssc.getOption(StandardSocketOptions.SO_REUSEADDR)).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    public String toString() {
        if (isBound()) {
            return "ServerSocket[addr=" + getInetAddress() + ",localport=" + getLocalPort() + "]";
        }
        return "ServerSocket[unbound]";
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("size cannot be 0 or negative");
        }
        try {
            this.ssc.setOption(StandardSocketOptions.SO_RCVBUF, Integer.valueOf(size));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    public int getReceiveBufferSize() throws SocketException {
        try {
            return ((Integer) this.ssc.getOption(StandardSocketOptions.SO_RCVBUF)).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;
        }
    }
}
