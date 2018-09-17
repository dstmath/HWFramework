package java.net;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.regex.Pattern;
import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.ResourceManager;
import sun.util.calendar.BaseCalendar;

abstract class AbstractPlainSocketImpl extends SocketImpl {
    public static final int SHUT_RD = 0;
    public static final int SHUT_WR = 1;
    private int CONNECTION_NOT_RESET;
    private int CONNECTION_RESET;
    private int CONNECTION_RESET_PENDING;
    protected boolean closePending;
    protected final Object fdLock;
    private final CloseGuard guard;
    private final Object resetLock;
    private int resetState;
    private boolean shut_rd;
    private boolean shut_wr;
    private SocketInputStream socketInputStream;
    protected boolean stream;
    int timeout;
    private int trafficClass;

    abstract void socketAccept(SocketImpl socketImpl) throws IOException;

    abstract int socketAvailable() throws IOException;

    abstract void socketBind(InetAddress inetAddress, int i) throws IOException;

    abstract void socketClose0() throws IOException;

    abstract void socketConnect(InetAddress inetAddress, int i, int i2) throws IOException;

    abstract void socketCreate(boolean z) throws IOException;

    abstract int socketGetOption(int i, Object obj) throws SocketException;

    abstract void socketListen(int i) throws IOException;

    abstract void socketSendUrgentData(int i) throws IOException;

    abstract void socketSetOption(int i, boolean z, Object obj) throws SocketException;

    abstract void socketShutdown(int i) throws IOException;

    AbstractPlainSocketImpl() {
        this.shut_rd = false;
        this.shut_wr = false;
        this.socketInputStream = null;
        this.fdLock = new Object();
        this.closePending = false;
        this.CONNECTION_NOT_RESET = SHUT_RD;
        this.CONNECTION_RESET_PENDING = SHUT_WR;
        this.CONNECTION_RESET = 2;
        this.resetLock = new Object();
        this.guard = CloseGuard.get();
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
                if (address instanceof InetSocketAddress) {
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
        boolean on = true;
        switch (opt) {
            case SHUT_WR /*1*/:
                if (val != null && (val instanceof Boolean)) {
                    on = ((Boolean) val).booleanValue();
                    break;
                }
                throw new SocketException("bad parameter for TCP_NODELAY");
            case BaseCalendar.TUESDAY /*3*/:
                if (val != null && (val instanceof Integer)) {
                    this.trafficClass = ((Integer) val).intValue();
                    break;
                }
                throw new SocketException("bad argument for IP_TOS");
            case BaseCalendar.WEDNESDAY /*4*/:
                if (val != null && (val instanceof Boolean)) {
                    on = ((Boolean) val).booleanValue();
                    break;
                }
                throw new SocketException("bad parameter for SO_REUSEADDR");
            case BaseCalendar.AUGUST /*8*/:
                if (val != null && (val instanceof Boolean)) {
                    on = ((Boolean) val).booleanValue();
                    break;
                }
                throw new SocketException("bad parameter for SO_KEEPALIVE");
            case Calendar.ZONE_OFFSET /*15*/:
                throw new SocketException("Cannot re-bind socket");
            case Pattern.CANON_EQ /*128*/:
                if (val != null && ((val instanceof Integer) || (val instanceof Boolean))) {
                    if (val instanceof Boolean) {
                        on = false;
                        break;
                    }
                }
                throw new SocketException("Bad parameter for option");
                break;
            case SocketOptions.SO_SNDBUF /*4097*/:
            case SocketOptions.SO_RCVBUF /*4098*/:
                if (val == null || !(val instanceof Integer) || ((Integer) val).intValue() <= 0) {
                    throw new SocketException("bad parameter for SO_SNDBUF or SO_RCVBUF");
                }
            case SocketOptions.SO_OOBINLINE /*4099*/:
                if (val != null && (val instanceof Boolean)) {
                    on = ((Boolean) val).booleanValue();
                    break;
                }
                throw new SocketException("bad parameter for SO_OOBINLINE");
                break;
            case SocketOptions.SO_TIMEOUT /*4102*/:
                if (val != null && (val instanceof Integer)) {
                    int tmp = ((Integer) val).intValue();
                    if (tmp >= 0) {
                        this.timeout = tmp;
                        break;
                    }
                    throw new IllegalArgumentException("timeout < 0");
                }
                throw new SocketException("Bad parameter for SO_TIMEOUT");
                break;
            default:
                throw new SocketException("unrecognized TCP option: " + opt);
        }
        socketSetOption(opt, on, val);
    }

    public Object getOption(int opt) throws SocketException {
        boolean z = true;
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        } else if (opt == SocketOptions.SO_TIMEOUT) {
            return new Integer(this.timeout);
        } else {
            int ret;
            switch (opt) {
                case SHUT_WR /*1*/:
                    if (socketGetOption(opt, null) == -1) {
                        z = false;
                    }
                    return Boolean.valueOf(z);
                case BaseCalendar.TUESDAY /*3*/:
                    ret = socketGetOption(opt, null);
                    if (ret == -1) {
                        return new Integer(this.trafficClass);
                    }
                    return new Integer(ret);
                case BaseCalendar.WEDNESDAY /*4*/:
                    if (socketGetOption(opt, null) == -1) {
                        z = false;
                    }
                    return Boolean.valueOf(z);
                case BaseCalendar.AUGUST /*8*/:
                    if (socketGetOption(opt, null) == -1) {
                        z = false;
                    }
                    return Boolean.valueOf(z);
                case Calendar.ZONE_OFFSET /*15*/:
                    InetAddressContainer in = new InetAddressContainer();
                    ret = socketGetOption(opt, in);
                    return in.addr;
                case Pattern.CANON_EQ /*128*/:
                    ret = socketGetOption(opt, null);
                    return ret == -1 ? Boolean.FALSE : new Integer(ret);
                case SocketOptions.SO_SNDBUF /*4097*/:
                case SocketOptions.SO_RCVBUF /*4098*/:
                    return new Integer(socketGetOption(opt, null));
                case SocketOptions.SO_OOBINLINE /*4099*/:
                    if (socketGetOption(opt, null) == -1) {
                        z = false;
                    }
                    return Boolean.valueOf(z);
                default:
                    return null;
            }
        }
    }

    synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
        synchronized (this.fdLock) {
            if (!this.closePending && (this.socket == null || !this.socket.isBound())) {
                NetHooks.beforeTcpConnect(this.fd, address, port);
            }
        }
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
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    protected synchronized void bind(InetAddress address, int lport) throws IOException {
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

    protected synchronized void listen(int count) throws IOException {
        socketListen(count);
    }

    protected void accept(SocketImpl s) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        socketAccept(s);
    }

    protected synchronized InputStream getInputStream() throws IOException {
        if (isClosedOrPending()) {
            throw new IOException("Socket Closed");
        } else if (this.shut_rd) {
            throw new IOException("Socket input is shutdown");
        } else {
            if (this.socketInputStream == null) {
                this.socketInputStream = new SocketInputStream(this);
            }
        }
        return this.socketInputStream;
    }

    void setInputStream(SocketInputStream in) {
        this.socketInputStream = in;
    }

    protected synchronized OutputStream getOutputStream() throws IOException {
        if (isClosedOrPending()) {
            throw new IOException("Socket Closed");
        } else if (this.shut_wr) {
            throw new IOException("Socket output is shutdown");
        }
        return new SocketOutputStream(this);
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
        if (isClosedOrPending()) {
            throw new IOException("Stream closed.");
        } else if (isConnectionReset()) {
            return SHUT_RD;
        } else {
            int n = SHUT_RD;
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
            return n;
        }
    }

    protected void close() throws IOException {
        synchronized (this.fdLock) {
            if (this.fd == null || !this.fd.valid()) {
                return;
            }
            if (!this.stream) {
                ResourceManager.afterUdpClose();
            }
            if (this.closePending) {
                return;
            }
            this.closePending = true;
            socketClose();
        }
    }

    void reset() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketClose();
        }
        super.reset();
    }

    protected void shutdownInput() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketShutdown(SHUT_RD);
            if (this.socketInputStream != null) {
                this.socketInputStream.setEOF(true);
            }
            this.shut_rd = true;
        }
    }

    protected void shutdownOutput() throws IOException {
        if (this.fd != null && this.fd.valid()) {
            socketShutdown(SHUT_WR);
            this.shut_wr = true;
        }
    }

    protected boolean supportsUrgentData() {
        return true;
    }

    protected void sendUrgentData(int data) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
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
            fileDescriptor = this.fd;
        }
        return fileDescriptor;
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

    protected void socketClose() throws IOException {
        this.guard.close();
        socketClose0();
    }
}
