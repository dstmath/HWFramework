package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class DatagramSocketAdaptor extends DatagramSocket {
    private static final DatagramSocketImpl dummyDatagramSocket = null;
    private final DatagramChannelImpl dc;
    private volatile int timeout;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.DatagramSocketAdaptor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.DatagramSocketAdaptor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.DatagramSocketAdaptor.<clinit>():void");
    }

    private DatagramSocketAdaptor(DatagramChannelImpl dc) throws IOException {
        super(dummyDatagramSocket);
        this.timeout = 0;
        this.dc = dc;
    }

    public static DatagramSocket create(DatagramChannelImpl dc) {
        try {
            return new DatagramSocketAdaptor(dc);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    private void connectInternal(SocketAddress remote) throws SocketException {
        int port = Net.asInetSocketAddress(remote).getPort();
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("connect: " + port);
        } else if (remote == null) {
            throw new IllegalArgumentException("connect: null address");
        } else if (!isClosed()) {
            try {
                this.dc.connect(remote);
            } catch (Exception x) {
                Net.translateToSocketException(x);
            }
        }
    }

    public void bind(SocketAddress local) throws SocketException {
        if (local == null) {
            try {
                local = new InetSocketAddress(0);
            } catch (Exception x) {
                Net.translateToSocketException(x);
                return;
            }
        }
        this.dc.bind(local);
    }

    public void connect(InetAddress address, int port) {
        try {
            connectInternal(new InetSocketAddress(address, port));
        } catch (SocketException e) {
        }
    }

    public void connect(SocketAddress remote) throws SocketException {
        if (remote == null) {
            throw new IllegalArgumentException("Address can't be null");
        }
        connectInternal(remote);
    }

    public void disconnect() {
        try {
            this.dc.disconnect();
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    public boolean isBound() {
        return this.dc.localAddress() != null;
    }

    public boolean isConnected() {
        return this.dc.remoteAddress() != null;
    }

    public InetAddress getInetAddress() {
        if (isConnected()) {
            return Net.asInetSocketAddress(this.dc.remoteAddress()).getAddress();
        }
        return null;
    }

    public int getPort() {
        if (isConnected()) {
            return Net.asInetSocketAddress(this.dc.remoteAddress()).getPort();
        }
        return -1;
    }

    public void send(DatagramPacket p) throws IOException {
        synchronized (this.dc.blockingLock()) {
            if (this.dc.isBlocking()) {
                try {
                    synchronized (p) {
                        ByteBuffer bb = ByteBuffer.wrap(p.getData(), p.getOffset(), p.getLength());
                        if (!this.dc.isConnected()) {
                            this.dc.send(bb, p.getSocketAddress());
                        } else if (p.getAddress() == null) {
                            InetSocketAddress isa = (InetSocketAddress) this.dc.remoteAddress();
                            p.setPort(isa.getPort());
                            p.setAddress(isa.getAddress());
                            this.dc.write(bb);
                        } else {
                            this.dc.send(bb, p.getSocketAddress());
                        }
                    }
                } catch (IOException x) {
                    Net.translateException(x);
                }
            } else {
                throw new IllegalBlockingModeException();
            }
        }
    }

    private SocketAddress receive(ByteBuffer bb) throws IOException {
        if (this.timeout == 0) {
            return this.dc.receive(bb);
        }
        SelectionKey selectionKey = null;
        Selector selector = null;
        this.dc.configureBlocking(false);
        SocketAddress sender = this.dc.receive(bb);
        if (sender != null) {
            if (this.dc.isOpen()) {
                this.dc.configureBlocking(true);
            }
            return sender;
        }
        selector = Util.getTemporarySelector(this.dc);
        selectionKey = this.dc.register(selector, 1);
        long to = (long) this.timeout;
        while (this.dc.isOpen()) {
            try {
                long st = System.currentTimeMillis();
                if (selector.select(to) > 0 && selectionKey.isReadable()) {
                    sender = this.dc.receive(bb);
                    if (sender != null) {
                        return sender;
                    }
                }
                selector.selectedKeys().remove(selectionKey);
                to -= System.currentTimeMillis() - st;
                if (to <= 0) {
                    throw new SocketTimeoutException();
                }
            } finally {
                if (selectionKey != null) {
                    selectionKey.cancel();
                }
                if (this.dc.isOpen()) {
                    this.dc.configureBlocking(true);
                }
                if (selector != null) {
                    Util.releaseTemporarySelector(selector);
                }
            }
        }
        throw new ClosedChannelException();
    }

    public void receive(DatagramPacket p) throws IOException {
        synchronized (this.dc.blockingLock()) {
            if (this.dc.isBlocking()) {
                try {
                    synchronized (p) {
                        ByteBuffer bb = ByteBuffer.wrap(p.getData(), p.getOffset(), p.getLength());
                        p.setSocketAddress(receive(bb));
                        p.setLength(bb.position() - p.getOffset());
                    }
                } catch (IOException x) {
                    Net.translateException(x);
                }
            } else {
                throw new IllegalBlockingModeException();
            }
        }
    }

    public InetAddress getLocalAddress() {
        if (isClosed()) {
            return null;
        }
        SocketAddress local = this.dc.localAddress();
        if (local == null) {
            local = new InetSocketAddress(0);
        }
        InetAddress result = ((InetSocketAddress) local).getAddress();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkConnect(result.getHostAddress(), -1);
            } catch (SecurityException e) {
                return new InetSocketAddress(0).getAddress();
            }
        }
        return result;
    }

    public int getLocalPort() {
        if (isClosed()) {
            return -1;
        }
        try {
            SocketAddress local = this.dc.getLocalAddress();
            if (local != null) {
                return ((InetSocketAddress) local).getPort();
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public void setSoTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
    }

    public int getSoTimeout() throws SocketException {
        return this.timeout;
    }

    private void setBooleanOption(SocketOption<Boolean> name, boolean value) throws SocketException {
        try {
            this.dc.setOption((SocketOption) name, Boolean.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private void setIntOption(SocketOption<Integer> name, int value) throws SocketException {
        try {
            this.dc.setOption((SocketOption) name, Integer.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private boolean getBooleanOption(SocketOption<Boolean> name) throws SocketException {
        try {
            return ((Boolean) this.dc.getOption(name)).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    private int getIntOption(SocketOption<Integer> name) throws SocketException {
        try {
            return ((Integer) this.dc.getOption(name)).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;
        }
    }

    public void setSendBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid send size");
        }
        setIntOption(StandardSocketOptions.SO_SNDBUF, size);
    }

    public int getSendBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_SNDBUF);
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid receive size");
        }
        setIntOption(StandardSocketOptions.SO_RCVBUF, size);
    }

    public int getReceiveBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_RCVBUF);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_REUSEADDR, on);
    }

    public boolean getReuseAddress() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_REUSEADDR);
    }

    public void setBroadcast(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_BROADCAST, on);
    }

    public boolean getBroadcast() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_BROADCAST);
    }

    public void setTrafficClass(int tc) throws SocketException {
        setIntOption(StandardSocketOptions.IP_TOS, tc);
    }

    public int getTrafficClass() throws SocketException {
        return getIntOption(StandardSocketOptions.IP_TOS);
    }

    public void close() {
        try {
            this.dc.close();
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    public boolean isClosed() {
        return !this.dc.isOpen();
    }

    public DatagramChannel getChannel() {
        return this.dc;
    }

    public final FileDescriptor getFileDescriptor$() {
        return this.dc.fd;
    }
}
