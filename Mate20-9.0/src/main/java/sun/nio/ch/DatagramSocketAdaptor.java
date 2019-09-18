package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;

public class DatagramSocketAdaptor extends DatagramSocket {
    private static final DatagramSocketImpl dummyDatagramSocket = new DatagramSocketImpl() {
        /* access modifiers changed from: protected */
        public void create() throws SocketException {
        }

        /* access modifiers changed from: protected */
        public void bind(int lport, InetAddress laddr) throws SocketException {
        }

        /* access modifiers changed from: protected */
        public void send(DatagramPacket p) throws IOException {
        }

        /* access modifiers changed from: protected */
        public int peek(InetAddress i) throws IOException {
            return 0;
        }

        /* access modifiers changed from: protected */
        public int peekData(DatagramPacket p) throws IOException {
            return 0;
        }

        /* access modifiers changed from: protected */
        public void receive(DatagramPacket p) throws IOException {
        }

        /* access modifiers changed from: protected */
        @Deprecated
        public void setTTL(byte ttl) throws IOException {
        }

        /* access modifiers changed from: protected */
        @Deprecated
        public byte getTTL() throws IOException {
            return 0;
        }

        /* access modifiers changed from: protected */
        public void setTimeToLive(int ttl) throws IOException {
        }

        /* access modifiers changed from: protected */
        public int getTimeToLive() throws IOException {
            return 0;
        }

        /* access modifiers changed from: protected */
        public void join(InetAddress inetaddr) throws IOException {
        }

        /* access modifiers changed from: protected */
        public void leave(InetAddress inetaddr) throws IOException {
        }

        /* access modifiers changed from: protected */
        public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        }

        /* access modifiers changed from: protected */
        public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        }

        /* access modifiers changed from: protected */
        public void close() {
        }

        public Object getOption(int optID) throws SocketException {
            return null;
        }

        public void setOption(int optID, Object value) throws SocketException {
        }
    };
    private final DatagramChannelImpl dc;
    private volatile int timeout = 0;

    private DatagramSocketAdaptor(DatagramChannelImpl dc2) throws IOException {
        super(dummyDatagramSocket);
        this.dc = dc2;
    }

    public static DatagramSocket create(DatagramChannelImpl dc2) {
        try {
            return new DatagramSocketAdaptor(dc2);
        } catch (IOException x) {
            throw new Error((Throwable) x);
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
        if (remote != null) {
            connectInternal(remote);
            return;
        }
        throw new IllegalArgumentException("Address can't be null");
    }

    public void disconnect() {
        try {
            this.dc.disconnect();
        } catch (IOException x) {
            throw new Error((Throwable) x);
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
        this.dc.configureBlocking(false);
        try {
            SocketAddress receive = this.dc.receive(bb);
            SocketAddress sender = receive;
            if (receive != null) {
                return sender;
            }
            long to = (long) this.timeout;
            while (this.dc.isOpen()) {
                long st = System.currentTimeMillis();
                int result = this.dc.poll(Net.POLLIN, to);
                if (result > 0 && (Net.POLLIN & result) != 0) {
                    SocketAddress receive2 = this.dc.receive(bb);
                    SocketAddress sender2 = receive2;
                    if (receive2 != null) {
                        if (this.dc.isOpen()) {
                            this.dc.configureBlocking(true);
                        }
                        return sender2;
                    }
                }
                to -= System.currentTimeMillis() - st;
                if (to <= 0) {
                    throw new SocketTimeoutException();
                }
            }
            throw new ClosedChannelException();
        } finally {
            if (this.dc.isOpen()) {
                this.dc.configureBlocking(true);
            }
        }
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

    public void setSoTimeout(int timeout2) throws SocketException {
        this.timeout = timeout2;
    }

    public int getSoTimeout() throws SocketException {
        return this.timeout;
    }

    private void setBooleanOption(SocketOption<Boolean> name, boolean value) throws SocketException {
        try {
            this.dc.setOption(name, Boolean.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private void setIntOption(SocketOption<Integer> name, int value) throws SocketException {
        try {
            this.dc.setOption(name, Integer.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [java.net.SocketOption<java.lang.Boolean>, java.net.SocketOption] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean getBooleanOption(SocketOption<Boolean> r3) throws SocketException {
        try {
            return ((Boolean) this.dc.getOption(r3)).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [java.net.SocketOption<java.lang.Integer>, java.net.SocketOption] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private int getIntOption(SocketOption<Integer> r3) throws SocketException {
        try {
            return ((Integer) this.dc.getOption(r3)).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;
        }
    }

    public void setSendBufferSize(int size) throws SocketException {
        if (size > 0) {
            setIntOption(StandardSocketOptions.SO_SNDBUF, size);
            return;
        }
        throw new IllegalArgumentException("Invalid send size");
    }

    public int getSendBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_SNDBUF);
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        if (size > 0) {
            setIntOption(StandardSocketOptions.SO_RCVBUF, size);
            return;
        }
        throw new IllegalArgumentException("Invalid receive size");
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
        } catch (IOException x) {
            throw new Error((Throwable) x);
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
