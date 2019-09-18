package java.net;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.ResourceManager;

abstract class AbstractPlainSocketImpl extends SocketImpl {
    public static final int SHUT_RD = 0;
    public static final int SHUT_WR = 1;
    private int CONNECTION_NOT_RESET = 0;
    private int CONNECTION_RESET = 2;
    private int CONNECTION_RESET_PENDING = 1;
    protected boolean closePending = false;
    protected final Object fdLock = new Object();
    protected int fdUseCount = 0;
    private final CloseGuard guard = CloseGuard.get();
    private final Object resetLock = new Object();
    private int resetState;
    private boolean shut_rd = false;
    private boolean shut_wr = false;
    private SocketInputStream socketInputStream = null;
    private SocketOutputStream socketOutputStream = null;
    protected boolean stream;
    int timeout;

    /* access modifiers changed from: package-private */
    public abstract void socketAccept(SocketImpl socketImpl) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int socketAvailable() throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void socketBind(InetAddress inetAddress, int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void socketClose0(boolean z) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void socketConnect(InetAddress inetAddress, int i, int i2) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void socketCreate(boolean z) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract Object socketGetOption(int i) throws SocketException;

    /* access modifiers changed from: package-private */
    public abstract void socketListen(int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void socketSendUrgentData(int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void socketSetOption(int i, Object obj) throws SocketException;

    /* access modifiers changed from: package-private */
    public abstract void socketShutdown(int i) throws IOException;

    AbstractPlainSocketImpl() {
    }

    /* access modifiers changed from: protected */
    public synchronized void create(boolean stream2) throws IOException {
        this.stream = stream2;
        if (!stream2) {
            ResourceManager.beforeUdpCreate();
            try {
                socketCreate(false);
            } catch (IOException ioe) {
                ResourceManager.afterUdpClose();
                throw ioe;
            }
        } else {
            socketCreate(true);
        }
        if (this.socket != null) {
            this.socket.setCreated();
        }
        if (this.serverSocket != null) {
            this.serverSocket.setCreated();
        }
        if (this.fd != null && this.fd.valid()) {
            this.guard.open("close");
            SocketCounter.incrementAndDumpStackIfOverload();
        }
    }

    /* access modifiers changed from: protected */
    public void connect(String host, int port) throws UnknownHostException, IOException {
        boolean connected = false;
        try {
            InetAddress address = InetAddress.getByName(host);
            this.port = port;
            this.address = address;
            connectToAddress(address, port, this.timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException e) {
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void connect(InetAddress address, int port) throws IOException {
        this.port = port;
        this.address = address;
        try {
            connectToAddress(address, port, this.timeout);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /* access modifiers changed from: protected */
    public void connect(SocketAddress address, int timeout2) throws IOException {
        if (address != null) {
            try {
                if (address instanceof InetSocketAddress) {
                    InetSocketAddress addr = (InetSocketAddress) address;
                    if (!addr.isUnresolved()) {
                        this.port = addr.getPort();
                        this.address = addr.getAddress();
                        connectToAddress(this.address, this.port, timeout2);
                        if (1 == 0) {
                            try {
                                close();
                                return;
                            } catch (IOException e) {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        throw new UnknownHostException(addr.getHostName());
                    }
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    try {
                        close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        }
        throw new IllegalArgumentException("unsupported address type");
    }

    private void connectToAddress(InetAddress address, int port, int timeout2) throws IOException {
        if (address.isAnyLocalAddress()) {
            doConnect(InetAddress.getLocalHost(), port, timeout2);
        } else {
            doConnect(address, port, timeout2);
        }
    }

    public void setOption(int opt, Object val) throws SocketException {
        if (!isClosedOrPending()) {
            if (opt == 4102) {
                this.timeout = ((Integer) val).intValue();
            }
            socketSetOption(opt, val);
            return;
        }
        throw new SocketException("Socket Closed");
    }

    public Object getOption(int opt) throws SocketException {
        if (!isClosedOrPending()) {
            return opt == 4102 ? new Integer(this.timeout) : socketGetOption(opt);
        }
        throw new SocketException("Socket Closed");
    }

    /* access modifiers changed from: package-private */
    public synchronized void doConnect(InetAddress address, int port, int timeout2) throws IOException {
        synchronized (this.fdLock) {
            if (!this.closePending && (this.socket == null || !this.socket.isBound())) {
                NetHooks.beforeTcpConnect(this.fd, address, port);
            }
        }
        try {
            acquireFD();
            try {
                BlockGuard.getThreadPolicy().onNetwork();
                socketConnect(address, port, timeout2);
                synchronized (this.fdLock) {
                    if (this.closePending) {
                        throw new SocketException("Socket closed");
                    }
                }
                if (this.socket != null) {
                    this.socket.setBound();
                    this.socket.setConnected();
                }
                releaseFD();
            } catch (Throwable th) {
                releaseFD();
                throw th;
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void bind(InetAddress address, int lport) throws IOException {
        synchronized (this.fdLock) {
            if (!this.closePending && (this.socket == null || !this.socket.isBound())) {
                NetHooks.beforeTcpBind(this.fd, address, lport);
            }
        }
        socketBind(address, lport);
        if (this.socket != null) {
            this.socket.setBound();
        }
        if (this.serverSocket != null) {
            this.serverSocket.setBound();
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void listen(int count) throws IOException {
        socketListen(count);
    }

    /* access modifiers changed from: protected */
    public void accept(SocketImpl s) throws IOException {
        acquireFD();
        try {
            BlockGuard.getThreadPolicy().onNetwork();
            socketAccept(s);
        } finally {
            releaseFD();
        }
    }

    /* access modifiers changed from: protected */
    public synchronized InputStream getInputStream() throws IOException {
        synchronized (this.fdLock) {
            if (isClosedOrPending()) {
                throw new IOException("Socket Closed");
            } else if (this.shut_rd) {
                throw new IOException("Socket input is shutdown");
            } else if (this.socketInputStream == null) {
                this.socketInputStream = new SocketInputStream(this);
            }
        }
        return this.socketInputStream;
    }

    /* access modifiers changed from: package-private */
    public void setInputStream(SocketInputStream in) {
        this.socketInputStream = in;
    }

    /* access modifiers changed from: protected */
    public synchronized OutputStream getOutputStream() throws IOException {
        synchronized (this.fdLock) {
            if (isClosedOrPending()) {
                throw new IOException("Socket Closed");
            } else if (this.shut_wr) {
                throw new IOException("Socket output is shutdown");
            } else if (this.socketOutputStream == null) {
                this.socketOutputStream = new SocketOutputStream(this);
            }
        }
        return this.socketOutputStream;
    }

    /* access modifiers changed from: package-private */
    public void setFileDescriptor(FileDescriptor fd) {
        this.fd = fd;
    }

    /* access modifiers changed from: package-private */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /* access modifiers changed from: package-private */
    public void setPort(int port) {
        this.port = port;
    }

    /* access modifiers changed from: package-private */
    public void setLocalPort(int localport) {
        this.localport = localport;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0038, code lost:
        return 0;
     */
    public synchronized int available() throws IOException {
        int n;
        if (isClosedOrPending()) {
            throw new IOException("Stream closed.");
        } else if (!isConnectionReset() && !this.shut_rd) {
            n = 0;
            try {
                n = socketAvailable();
                if (n == 0 && isConnectionResetPending()) {
                    setConnectionReset();
                }
            } catch (ConnectionResetException e) {
                setConnectionResetPending();
                try {
                    n = socketAvailable();
                    if (n == 0) {
                        setConnectionReset();
                    }
                } catch (ConnectionResetException e2) {
                }
            }
        }
        return n;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003d, code lost:
        return;
     */
    public void close() throws IOException {
        synchronized (this.fdLock) {
            if (this.fd != null && this.fd.valid()) {
                if (!this.stream) {
                    ResourceManager.afterUdpClose();
                }
                if (!this.closePending) {
                    this.closePending = true;
                    this.guard.close();
                    if (this.fdUseCount == 0) {
                        try {
                            socketPreClose();
                        } finally {
                            socketClose();
                        }
                    } else {
                        this.fdUseCount--;
                        socketPreClose();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reset() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketClose();
            this.guard.close();
        }
        super.reset();
    }

    /* access modifiers changed from: protected */
    public void shutdownInput() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketShutdown(0);
            if (this.socketInputStream != null) {
                this.socketInputStream.setEOF(true);
            }
            this.shut_rd = true;
        }
    }

    /* access modifiers changed from: protected */
    public void shutdownOutput() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketShutdown(1);
            this.shut_wr = true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean supportsUrgentData() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendUrgentData(int data) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new IOException("Socket Closed");
        }
        socketSendUrgentData(data);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor acquireFD() {
        FileDescriptor fileDescriptor;
        synchronized (this.fdLock) {
            this.fdUseCount++;
            fileDescriptor = this.fd;
        }
        return fileDescriptor;
    }

    /* access modifiers changed from: package-private */
    public void releaseFD() {
        synchronized (this.fdLock) {
            this.fdUseCount--;
            if (this.fdUseCount == -1 && this.fd != null) {
                try {
                    socketClose();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean isConnectionReset() {
        boolean z;
        synchronized (this.resetLock) {
            z = this.resetState == this.CONNECTION_RESET;
        }
        return z;
    }

    public boolean isConnectionResetPending() {
        boolean z;
        synchronized (this.resetLock) {
            z = this.resetState == this.CONNECTION_RESET_PENDING;
        }
        return z;
    }

    public void setConnectionReset() {
        synchronized (this.resetLock) {
            this.resetState = this.CONNECTION_RESET;
        }
    }

    public void setConnectionResetPending() {
        synchronized (this.resetLock) {
            if (this.resetState == this.CONNECTION_NOT_RESET) {
                this.resetState = this.CONNECTION_RESET_PENDING;
            }
        }
    }

    public boolean isClosedOrPending() {
        synchronized (this.fdLock) {
            if (!this.closePending && this.fd != null) {
                if (this.fd.valid()) {
                    return false;
                }
            }
            return true;
        }
    }

    public int getTimeout() {
        return this.timeout;
    }

    private void socketPreClose() throws IOException {
        socketClose0(true);
    }

    /* access modifiers changed from: protected */
    public void socketClose() throws IOException {
        socketClose0(false);
        SocketCounter.decrement();
    }
}
