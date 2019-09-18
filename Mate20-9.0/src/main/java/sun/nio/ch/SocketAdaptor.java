package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class SocketAdaptor extends Socket {
    /* access modifiers changed from: private */
    public final SocketChannelImpl sc;
    private InputStream socketInputStream = null;
    /* access modifiers changed from: private */
    public volatile int timeout = 0;

    private class SocketInputStream extends ChannelInputStream {
        private SocketInputStream() {
            super(SocketAdaptor.this.sc);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x005a, code lost:
            return r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a4, code lost:
            return r3;
         */
        public int read(ByteBuffer bb) throws IOException {
            synchronized (SocketAdaptor.this.sc.blockingLock()) {
                if (!SocketAdaptor.this.sc.isBlocking()) {
                    throw new IllegalBlockingModeException();
                } else if (SocketAdaptor.this.timeout == 0) {
                    int read = SocketAdaptor.this.sc.read(bb);
                    return read;
                } else {
                    SocketAdaptor.this.sc.configureBlocking(false);
                    try {
                        int read2 = SocketAdaptor.this.sc.read(bb);
                        int n = read2;
                        if (read2 == 0) {
                            long to = (long) SocketAdaptor.this.timeout;
                            while (SocketAdaptor.this.sc.isOpen()) {
                                long st = System.currentTimeMillis();
                                if (SocketAdaptor.this.sc.poll(Net.POLLIN, to) > 0) {
                                    int read3 = SocketAdaptor.this.sc.read(bb);
                                    int n2 = read3;
                                    if (read3 != 0) {
                                    }
                                }
                                to -= System.currentTimeMillis() - st;
                                if (to <= 0) {
                                    throw new SocketTimeoutException();
                                }
                            }
                            throw new ClosedChannelException();
                        } else if (SocketAdaptor.this.sc.isOpen()) {
                            SocketAdaptor.this.sc.configureBlocking(true);
                        }
                    } finally {
                        if (SocketAdaptor.this.sc.isOpen()) {
                            SocketAdaptor.this.sc.configureBlocking(true);
                        }
                    }
                }
            }
        }
    }

    private SocketAdaptor(SocketChannelImpl sc2) throws SocketException {
        super((SocketImpl) new FileDescriptorHolderSocketImpl(sc2.getFD()));
        this.sc = sc2;
    }

    public static Socket create(SocketChannelImpl sc2) {
        try {
            return new SocketAdaptor(sc2);
        } catch (SocketException e) {
            throw new InternalError("Should not reach here");
        }
    }

    public SocketChannel getChannel() {
        return this.sc;
    }

    public void connect(SocketAddress remote) throws IOException {
        connect(remote, 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x008d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0094, code lost:
        if (r9.sc.isOpen() != false) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0096, code lost:
        r9.sc.configureBlocking(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x009c, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        sun.nio.ch.Net.translateException(r2, true);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:12:0x001d, B:30:0x0040] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:14:0x0020=Splitter:B:14:0x0020, B:26:0x003d=Splitter:B:26:0x003d} */
    public void connect(SocketAddress remote, int timeout2) throws IOException {
        if (remote == null) {
            throw new IllegalArgumentException("connect: The address can't be null");
        } else if (timeout2 >= 0) {
            synchronized (this.sc.blockingLock()) {
                if (!this.sc.isBlocking()) {
                    throw new IllegalBlockingModeException();
                } else if (timeout2 == 0) {
                    try {
                        this.sc.connect(remote);
                    } catch (Exception ex) {
                        Net.translateException(ex);
                    }
                } else {
                    this.sc.configureBlocking(false);
                    if (this.sc.connect(remote)) {
                        if (this.sc.isOpen()) {
                            this.sc.configureBlocking(true);
                        }
                        return;
                    }
                    long to = (long) timeout2;
                    do {
                        if (this.sc.isOpen()) {
                            long st = System.currentTimeMillis();
                            if (this.sc.poll(Net.POLLCONN, to) <= 0 || !this.sc.finishConnect()) {
                                to -= System.currentTimeMillis() - st;
                            } else if (this.sc.isOpen()) {
                                this.sc.configureBlocking(true);
                            }
                        } else {
                            throw new ClosedChannelException();
                        }
                    } while (to > 0);
                    try {
                        this.sc.close();
                    } catch (IOException e) {
                    }
                    throw new SocketTimeoutException();
                }
            }
        } else {
            throw new IllegalArgumentException("connect: timeout can't be negative");
        }
    }

    public void bind(SocketAddress local) throws IOException {
        try {
            this.sc.bind(local);
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public InetAddress getInetAddress() {
        if (!isConnected()) {
            return null;
        }
        SocketAddress remote = this.sc.remoteAddress();
        if (remote == null) {
            return null;
        }
        return ((InetSocketAddress) remote).getAddress();
    }

    public InetAddress getLocalAddress() {
        if (this.sc.isOpen()) {
            InetSocketAddress local = this.sc.localAddress();
            if (local != null) {
                return Net.getRevealedLocalAddress(local).getAddress();
            }
        }
        return new InetSocketAddress(0).getAddress();
    }

    public int getPort() {
        if (!isConnected()) {
            return 0;
        }
        SocketAddress remote = this.sc.remoteAddress();
        if (remote == null) {
            return 0;
        }
        return ((InetSocketAddress) remote).getPort();
    }

    public int getLocalPort() {
        SocketAddress local = this.sc.localAddress();
        if (local == null) {
            return -1;
        }
        return ((InetSocketAddress) local).getPort();
    }

    public InputStream getInputStream() throws IOException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        } else if (!this.sc.isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (this.sc.isInputOpen()) {
            if (this.socketInputStream == null) {
                try {
                    this.socketInputStream = (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            return new SocketInputStream();
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw ((IOException) e.getException());
                }
            }
            return this.socketInputStream;
        } else {
            throw new SocketException("Socket input is shutdown");
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        } else if (!this.sc.isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (this.sc.isOutputOpen()) {
            try {
                return (OutputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<OutputStream>() {
                    public OutputStream run() throws IOException {
                        return Channels.newOutputStream((WritableByteChannel) SocketAdaptor.this.sc);
                    }
                });
            } catch (PrivilegedActionException e) {
                throw ((IOException) e.getException());
            }
        } else {
            throw new SocketException("Socket output is shutdown");
        }
    }

    private void setBooleanOption(SocketOption<Boolean> name, boolean value) throws SocketException {
        try {
            this.sc.setOption((SocketOption) name, (Object) Boolean.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private void setIntOption(SocketOption<Integer> name, int value) throws SocketException {
        try {
            this.sc.setOption((SocketOption) name, (Object) Integer.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [java.net.SocketOption<java.lang.Boolean>, java.net.SocketOption] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean getBooleanOption(SocketOption<Boolean> r3) throws SocketException {
        try {
            return ((Boolean) this.sc.getOption(r3)).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [java.net.SocketOption<java.lang.Integer>, java.net.SocketOption] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private int getIntOption(SocketOption<Integer> r3) throws SocketException {
        try {
            return ((Integer) this.sc.getOption(r3)).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;
        }
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.TCP_NODELAY, on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return getBooleanOption(StandardSocketOptions.TCP_NODELAY);
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (!on) {
            linger = -1;
        }
        setIntOption(StandardSocketOptions.SO_LINGER, linger);
    }

    public int getSoLinger() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_LINGER);
    }

    public void sendUrgentData(int data) throws IOException {
        if (this.sc.sendOutOfBandData((byte) data) == 0) {
            throw new IOException("Socket buffer full");
        }
    }

    public void setOOBInline(boolean on) throws SocketException {
        setBooleanOption(ExtendedSocketOption.SO_OOBINLINE, on);
    }

    public boolean getOOBInline() throws SocketException {
        return getBooleanOption(ExtendedSocketOption.SO_OOBINLINE);
    }

    public void setSoTimeout(int timeout2) throws SocketException {
        if (timeout2 >= 0) {
            this.timeout = timeout2;
            return;
        }
        throw new IllegalArgumentException("timeout can't be negative");
    }

    public int getSoTimeout() throws SocketException {
        return this.timeout;
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

    public void setKeepAlive(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_KEEPALIVE, on);
    }

    public boolean getKeepAlive() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_KEEPALIVE);
    }

    public void setTrafficClass(int tc) throws SocketException {
        setIntOption(StandardSocketOptions.IP_TOS, tc);
    }

    public int getTrafficClass() throws SocketException {
        return getIntOption(StandardSocketOptions.IP_TOS);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_REUSEADDR, on);
    }

    public boolean getReuseAddress() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_REUSEADDR);
    }

    public void close() throws IOException {
        this.sc.close();
    }

    public void shutdownInput() throws IOException {
        try {
            this.sc.shutdownInput();
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public void shutdownOutput() throws IOException {
        try {
            this.sc.shutdownOutput();
        } catch (Exception x) {
            Net.translateException(x);
        }
    }

    public String toString() {
        if (!this.sc.isConnected()) {
            return "Socket[unconnected]";
        }
        return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
    }

    public boolean isConnected() {
        return this.sc.isConnected();
    }

    public boolean isBound() {
        return this.sc.localAddress() != null;
    }

    public boolean isClosed() {
        return !this.sc.isOpen();
    }

    public boolean isInputShutdown() {
        return !this.sc.isInputOpen();
    }

    public boolean isOutputShutdown() {
        return !this.sc.isOutputOpen();
    }

    public FileDescriptor getFileDescriptor$() {
        return this.sc.getFD();
    }
}
