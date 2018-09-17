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
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class SocketAdaptor extends Socket {
    private final SocketChannelImpl sc;
    private InputStream socketInputStream = null;
    private volatile int timeout = 0;

    private class SocketInputStream extends ChannelInputStream {
        /* synthetic */ SocketInputStream(SocketAdaptor this$0, SocketInputStream -this1) {
            this();
        }

        private SocketInputStream() {
            super(SocketAdaptor.this.sc);
        }

        /* JADX WARNING: Missing block: B:20:0x0061, code:
            return r0;
     */
        /* JADX WARNING: Missing block: B:43:0x00c8, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected int read(ByteBuffer bb) throws IOException {
            synchronized (SocketAdaptor.this.sc.blockingLock()) {
                if (!SocketAdaptor.this.sc.isBlocking()) {
                    throw new IllegalBlockingModeException();
                } else if (SocketAdaptor.this.timeout == 0) {
                    int read = SocketAdaptor.this.sc.read(bb);
                    return read;
                } else {
                    SocketAdaptor.this.sc.configureBlocking(false);
                    try {
                        int n = SocketAdaptor.this.sc.read(bb);
                        if (n == 0) {
                            long to = (long) SocketAdaptor.this.timeout;
                            while (SocketAdaptor.this.sc.isOpen()) {
                                long st = System.currentTimeMillis();
                                if (SocketAdaptor.this.sc.poll(Net.POLLIN, to) > 0) {
                                    n = SocketAdaptor.this.sc.read(bb);
                                    if (n != 0) {
                                        if (SocketAdaptor.this.sc.isOpen()) {
                                            SocketAdaptor.this.sc.configureBlocking(true);
                                        }
                                    }
                                }
                                to -= System.currentTimeMillis() - st;
                                if (to <= 0) {
                                    throw new SocketTimeoutException();
                                }
                            }
                            throw new ClosedChannelException();
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

    private SocketAdaptor(SocketChannelImpl sc) throws SocketException {
        super(new FileDescriptorHolderSocketImpl(sc.getFD()));
        this.sc = sc;
    }

    public static Socket create(SocketChannelImpl sc) {
        try {
            return new SocketAdaptor(sc);
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

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.nio.ch.SocketAdaptor.connect(java.net.SocketAddress, int):void, dom blocks: [B:22:0x0038, B:42:0x0062]
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
    public void connect(java.net.SocketAddress r13, int r14) throws java.io.IOException {
        /*
        r12 = this;
        if (r13 != 0) goto L_0x000b;
    L_0x0002:
        r8 = new java.lang.IllegalArgumentException;
        r9 = "connect: The address can't be null";
        r8.<init>(r9);
        throw r8;
    L_0x000b:
        if (r14 >= 0) goto L_0x0016;
    L_0x000d:
        r8 = new java.lang.IllegalArgumentException;
        r9 = "connect: timeout can't be negative";
        r8.<init>(r9);
        throw r8;
    L_0x0016:
        r8 = r12.sc;
        r9 = r8.blockingLock();
        monitor-enter(r9);
        r8 = r12.sc;	 Catch:{ all -> 0x002b }
        r8 = r8.isBlocking();	 Catch:{ all -> 0x002b }
        if (r8 != 0) goto L_0x002e;	 Catch:{ all -> 0x002b }
    L_0x0025:
        r8 = new java.nio.channels.IllegalBlockingModeException;	 Catch:{ all -> 0x002b }
        r8.<init>();	 Catch:{ all -> 0x002b }
        throw r8;	 Catch:{ all -> 0x002b }
    L_0x002b:
        r8 = move-exception;
        monitor-exit(r9);
        throw r8;
    L_0x002e:
        if (r14 != 0) goto L_0x0043;
    L_0x0030:
        r8 = r12.sc;	 Catch:{ Exception -> 0x0037 }
        r8.connect(r13);	 Catch:{ Exception -> 0x0037 }
    L_0x0035:
        monitor-exit(r9);
        return;
    L_0x0037:
        r0 = move-exception;
        sun.nio.ch.Net.translateException(r0);	 Catch:{ Exception -> 0x003c }
        goto L_0x0035;
    L_0x003c:
        r7 = move-exception;
        r8 = 1;
        sun.nio.ch.Net.translateException(r7, r8);	 Catch:{ all -> 0x002b }
    L_0x0041:
        monitor-exit(r9);
        return;
    L_0x0043:
        r8 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r10 = 0;	 Catch:{ Exception -> 0x003c }
        r8.configureBlocking(r10);	 Catch:{ Exception -> 0x003c }
        r8 = r12.sc;	 Catch:{ all -> 0x0070 }
        r8 = r8.connect(r13);	 Catch:{ all -> 0x0070 }
        if (r8 == 0) goto L_0x0061;
    L_0x0051:
        r8 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r8 = r8.isOpen();	 Catch:{ Exception -> 0x003c }
        if (r8 == 0) goto L_0x005f;	 Catch:{ Exception -> 0x003c }
    L_0x0059:
        r8 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r10 = 1;	 Catch:{ Exception -> 0x003c }
        r8.configureBlocking(r10);	 Catch:{ Exception -> 0x003c }
    L_0x005f:
        monitor-exit(r9);
        return;
    L_0x0061:
        r4 = (long) r14;
    L_0x0062:
        r8 = r12.sc;	 Catch:{ all -> 0x0070 }
        r8 = r8.isOpen();	 Catch:{ all -> 0x0070 }
        if (r8 != 0) goto L_0x0080;	 Catch:{ all -> 0x0070 }
    L_0x006a:
        r8 = new java.nio.channels.ClosedChannelException;	 Catch:{ all -> 0x0070 }
        r8.<init>();	 Catch:{ all -> 0x0070 }
        throw r8;	 Catch:{ all -> 0x0070 }
    L_0x0070:
        r8 = move-exception;
        r10 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r10 = r10.isOpen();	 Catch:{ Exception -> 0x003c }
        if (r10 == 0) goto L_0x007f;	 Catch:{ Exception -> 0x003c }
    L_0x0079:
        r10 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r11 = 1;	 Catch:{ Exception -> 0x003c }
        r10.configureBlocking(r11);	 Catch:{ Exception -> 0x003c }
    L_0x007f:
        throw r8;	 Catch:{ Exception -> 0x003c }
    L_0x0080:
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0070 }
        r8 = r12.sc;	 Catch:{ all -> 0x0070 }
        r10 = sun.nio.ch.Net.POLLCONN;	 Catch:{ all -> 0x0070 }
        r1 = r8.poll(r10, r4);	 Catch:{ all -> 0x0070 }
        if (r1 <= 0) goto L_0x00a5;	 Catch:{ all -> 0x0070 }
    L_0x008e:
        r8 = r12.sc;	 Catch:{ all -> 0x0070 }
        r8 = r8.finishConnect();	 Catch:{ all -> 0x0070 }
        if (r8 == 0) goto L_0x00a5;
    L_0x0096:
        r8 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r8 = r8.isOpen();	 Catch:{ Exception -> 0x003c }
        if (r8 == 0) goto L_0x0041;	 Catch:{ Exception -> 0x003c }
    L_0x009e:
        r8 = r12.sc;	 Catch:{ Exception -> 0x003c }
        r10 = 1;	 Catch:{ Exception -> 0x003c }
        r8.configureBlocking(r10);	 Catch:{ Exception -> 0x003c }
        goto L_0x0041;
    L_0x00a5:
        r10 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0070 }
        r10 = r10 - r2;
        r4 = r4 - r10;
        r10 = 0;
        r8 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r8 > 0) goto L_0x0062;
    L_0x00b1:
        r8 = r12.sc;	 Catch:{ IOException -> 0x00bc }
        r8.close();	 Catch:{ IOException -> 0x00bc }
    L_0x00b6:
        r8 = new java.net.SocketTimeoutException;	 Catch:{ all -> 0x0070 }
        r8.<init>();	 Catch:{ all -> 0x0070 }
        throw r8;	 Catch:{ all -> 0x0070 }
    L_0x00bc:
        r6 = move-exception;
        goto L_0x00b6;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SocketAdaptor.connect(java.net.SocketAddress, int):void");
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
                            return new SocketInputStream(SocketAdaptor.this, null);
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
                        return Channels.newOutputStream(SocketAdaptor.this.sc);
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
            this.sc.setOption((SocketOption) name, Boolean.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private void setIntOption(SocketOption<Integer> name, int value) throws SocketException {
        try {
            this.sc.setOption((SocketOption) name, Integer.valueOf(value));
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private boolean getBooleanOption(SocketOption<Boolean> name) throws SocketException {
        try {
            return ((Boolean) this.sc.getOption(name)).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    private int getIntOption(SocketOption<Integer> name) throws SocketException {
        try {
            return ((Integer) this.sc.getOption(name)).lambda$-java_util_stream_IntPipeline_14709();
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

    public void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can't be negative");
        }
        this.timeout = timeout;
    }

    public int getSoTimeout() throws SocketException {
        return this.timeout;
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
        if (this.sc.isConnected()) {
            return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
        }
        return "Socket[unconnected]";
    }

    public boolean isConnected() {
        return this.sc.isConnected();
    }

    public boolean isBound() {
        return this.sc.localAddress() != null;
    }

    public boolean isClosed() {
        return this.sc.isOpen() ^ 1;
    }

    public boolean isInputShutdown() {
        return this.sc.isInputOpen() ^ 1;
    }

    public boolean isOutputShutdown() {
        return this.sc.isOutputOpen() ^ 1;
    }

    public FileDescriptor getFileDescriptor$() {
        return this.sc.getFD();
    }
}
