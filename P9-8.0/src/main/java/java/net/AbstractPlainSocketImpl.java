package java.net;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import dalvik.system.SocketTagger;
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

    abstract void socketAccept(SocketImpl socketImpl) throws IOException;

    abstract int socketAvailable() throws IOException;

    abstract void socketBind(InetAddress inetAddress, int i) throws IOException;

    abstract void socketClose0(boolean z) throws IOException;

    abstract void socketConnect(InetAddress inetAddress, int i, int i2) throws IOException;

    abstract void socketCreate(boolean z) throws IOException;

    abstract Object socketGetOption(int i) throws SocketException;

    abstract void socketListen(int i) throws IOException;

    abstract void socketSendUrgentData(int i) throws IOException;

    abstract void socketSetOption(int i, Object obj) throws SocketException;

    abstract void socketShutdown(int i) throws IOException;

    AbstractPlainSocketImpl() {
    }

    protected synchronized void create(boolean stream) throws IOException {
        this.stream = stream;
        if (stream) {
            socketCreate(true);
        } else {
            ResourceManager.beforeUdpCreate();
            try {
                socketCreate(false);
            } catch (IOException ioe) {
                ResourceManager.afterUdpClose();
                throw ioe;
            }
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

    protected void connect(String host, int port) throws UnknownHostException, IOException {
        try {
            InetAddress address = InetAddress.getByName(host);
            this.port = port;
            this.address = address;
            connectToAddress(address, port, this.timeout);
            if (!true) {
                try {
                    close();
                } catch (IOException e) {
                }
            }
        } catch (Throwable th) {
            if (!false) {
                try {
                    close();
                } catch (IOException e2) {
                }
            }
        }
    }

    protected void connect(InetAddress address, int port) throws IOException {
        this.port = port;
        this.address = address;
        try {
            connectToAddress(address, port, this.timeout);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    protected void connect(SocketAddress address, int timeout) throws IOException {
        if (address != null) {
            try {
                if (((address instanceof InetSocketAddress) ^ 1) == 0) {
                    InetSocketAddress addr = (InetSocketAddress) address;
                    if (addr.isUnresolved()) {
                        throw new UnknownHostException(addr.getHostName());
                    }
                    this.port = addr.getPort();
                    this.address = addr.getAddress();
                    connectToAddress(this.address, this.port, timeout);
                    if (!true) {
                        try {
                            close();
                            return;
                        } catch (IOException e) {
                            return;
                        }
                    }
                    return;
                }
            } catch (Throwable th) {
                if (!false) {
                    try {
                        close();
                    } catch (IOException e2) {
                    }
                }
            }
        }
        throw new IllegalArgumentException("unsupported address type");
    }

    private void connectToAddress(InetAddress address, int port, int timeout) throws IOException {
        if (address.isAnyLocalAddress()) {
            doConnect(InetAddress.getLocalHost(), port, timeout);
        } else {
            doConnect(address, port, timeout);
        }
    }

    public void setOption(int opt, Object val) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        if (opt == SocketOptions.SO_TIMEOUT) {
            this.timeout = ((Integer) val).intValue();
        }
        socketSetOption(opt, val);
    }

    public Object getOption(int opt) throws SocketException {
        if (!isClosedOrPending()) {
            return opt == SocketOptions.SO_TIMEOUT ? new Integer(this.timeout) : socketGetOption(opt);
        } else {
            throw new SocketException("Socket Closed");
        }
    }

    synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
        synchronized (this.fdLock) {
            if (!this.closePending && (this.socket == null || (this.socket.isBound() ^ 1) != 0)) {
                NetHooks.beforeTcpConnect(this.fd, address, port);
            }
        }
        try {
            acquireFD();
            try {
                BlockGuard.getThreadPolicy().onNetwork();
                socketConnect(address, port, timeout);
                synchronized (this.fdLock) {
                    if (this.closePending) {
                        throw new SocketException("Socket closed");
                    }
                }
                if (this.socket != null) {
                    this.socket.setBound();
                    this.socket.setConnected();
                }
            } finally {
                releaseFD();
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    protected synchronized void bind(InetAddress address, int lport) throws IOException {
        synchronized (this.fdLock) {
            if (!this.closePending && (this.socket == null || (this.socket.isBound() ^ 1) != 0)) {
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

    protected synchronized void listen(int count) throws IOException {
        socketListen(count);
    }

    protected void accept(SocketImpl s) throws IOException {
        acquireFD();
        try {
            BlockGuard.getThreadPolicy().onNetwork();
            socketAccept(s);
        } finally {
            releaseFD();
        }
    }

    protected synchronized InputStream getInputStream() throws IOException {
        synchronized (this.fdLock) {
            if (isClosedOrPending()) {
                throw new IOException("Socket Closed");
            } else if (this.shut_rd) {
                throw new IOException("Socket input is shutdown");
            } else {
                if (this.socketInputStream == null) {
                    this.socketInputStream = new SocketInputStream(this);
                }
            }
        }
        return this.socketInputStream;
    }

    void setInputStream(SocketInputStream in) {
        this.socketInputStream = in;
    }

    protected synchronized OutputStream getOutputStream() throws IOException {
        synchronized (this.fdLock) {
            if (isClosedOrPending()) {
                throw new IOException("Socket Closed");
            } else if (this.shut_wr) {
                throw new IOException("Socket output is shutdown");
            } else {
                if (this.socketOutputStream == null) {
                    this.socketOutputStream = new SocketOutputStream(this);
                }
            }
        }
        return this.socketOutputStream;
    }

    void setFileDescriptor(FileDescriptor fd) {
        this.fd = fd;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setLocalPort(int localport) {
        this.localport = localport;
    }

    protected synchronized int available() throws IOException {
        int n;
        if (isClosedOrPending()) {
            throw new IOException("Stream closed.");
        } else if (isConnectionReset()) {
            return 0;
        } else {
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

    /* JADX WARNING: Missing block: B:30:0x0049, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void close() throws IOException {
        synchronized (this.fdLock) {
            if (this.fd != null && this.fd.valid()) {
                if (!this.stream) {
                    ResourceManager.afterUdpClose();
                }
                if (!this.closePending) {
                    this.closePending = true;
                    SocketTagger.get().untag(this.fd);
                    this.guard.close();
                    if (this.fdUseCount == 0) {
                        try {
                            socketPreClose();
                            socketClose();
                        } catch (Throwable th) {
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

    void reset() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketClose();
            this.guard.close();
        }
        super.reset();
    }

    protected void shutdownInput() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketShutdown(0);
            if (this.socketInputStream != null) {
                this.socketInputStream.setEOF(true);
            }
            this.shut_rd = true;
        }
    }

    protected void shutdownOutput() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketShutdown(1);
            this.shut_wr = true;
        }
    }

    protected boolean supportsUrgentData() {
        return true;
    }

    protected void sendUrgentData(int data) throws IOException {
        if (this.fd == null || (this.fd.valid() ^ 1) != 0) {
            throw new IOException("Socket Closed");
        }
        socketSendUrgentData(data);
    }

    protected void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }

    FileDescriptor acquireFD() {
        FileDescriptor fileDescriptor;
        synchronized (this.fdLock) {
            this.fdUseCount++;
            fileDescriptor = this.fd;
        }
        return fileDescriptor;
    }

    void releaseFD() {
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
            if (!(this.closePending || this.fd == null)) {
                if ((this.fd.valid() ^ 1) == 0) {
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

    protected void socketClose() throws IOException {
        socketClose0(false);
        SocketCounter.decrement();
    }
}
