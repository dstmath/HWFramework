package java.net;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy.Type;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.net.ApplicationProxy;
import sun.security.util.SecurityConstants;

public class Socket implements Closeable {
    private static SocketImplFactory factory = null;
    private boolean bound;
    private Object closeLock;
    private boolean closed;
    private boolean connected;
    private boolean created;
    SocketImpl impl;
    private boolean oldImpl;
    private boolean shutIn;
    private boolean shutOut;

    public Socket() {
        this.created = false;
        this.bound = false;
        this.connected = false;
        this.closed = false;
        this.closeLock = new Object();
        this.shutIn = false;
        this.shutOut = false;
        this.oldImpl = false;
        setImpl();
    }

    public Socket(Proxy proxy) {
        this.created = false;
        this.bound = false;
        this.connected = false;
        this.closed = false;
        this.closeLock = new Object();
        this.shutIn = false;
        this.shutOut = false;
        this.oldImpl = false;
        if (proxy == null) {
            throw new IllegalArgumentException("Invalid Proxy");
        }
        Proxy p;
        if (proxy == Proxy.NO_PROXY) {
            p = Proxy.NO_PROXY;
        } else {
            p = ApplicationProxy.create(proxy);
        }
        if (p.type() == Type.SOCKS) {
            SecurityManager security = System.getSecurityManager();
            InetSocketAddress epoint = (InetSocketAddress) p.address();
            if (epoint.getAddress() != null) {
                checkAddress(epoint.getAddress(), "Socket");
            }
            if (security != null) {
                if (epoint.isUnresolved()) {
                    epoint = new InetSocketAddress(epoint.getHostName(), epoint.getPort());
                }
                if (epoint.isUnresolved()) {
                    security.checkConnect(epoint.getHostName(), epoint.getPort());
                } else {
                    security.checkConnect(epoint.getAddress().getHostAddress(), epoint.getPort());
                }
            }
            this.impl = new SocksSocketImpl(p);
            this.impl.setSocket(this);
        } else if (p != Proxy.NO_PROXY) {
            throw new IllegalArgumentException("Invalid Proxy");
        } else if (factory == null) {
            this.impl = new PlainSocketImpl();
            this.impl.setSocket(this);
        } else {
            setImpl();
        }
    }

    protected Socket(SocketImpl impl) throws SocketException {
        this.created = false;
        this.bound = false;
        this.connected = false;
        this.closed = false;
        this.closeLock = new Object();
        this.shutIn = false;
        this.shutOut = false;
        this.oldImpl = false;
        this.impl = impl;
        if (impl != null) {
            checkOldImpl();
            this.impl.setSocket(this);
        }
    }

    public Socket(String host, int port) throws UnknownHostException, IOException {
        this(InetAddress.getAllByName(host), port, (SocketAddress) null, true);
    }

    public Socket(InetAddress address, int port) throws IOException {
        this(nonNullAddress(address), port, (SocketAddress) null, true);
    }

    public Socket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        this(InetAddress.getAllByName(host), port, new InetSocketAddress(localAddr, localPort), true);
    }

    public Socket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        this(nonNullAddress(address), port, new InetSocketAddress(localAddr, localPort), true);
    }

    @Deprecated
    public Socket(String host, int port, boolean stream) throws IOException {
        this(InetAddress.getAllByName(host), port, (SocketAddress) null, stream);
    }

    @Deprecated
    public Socket(InetAddress host, int port, boolean stream) throws IOException {
        this(nonNullAddress(host), port, new InetSocketAddress(0), stream);
    }

    private static InetAddress[] nonNullAddress(InetAddress address) {
        if (address == null) {
            throw new NullPointerException();
        }
        return new InetAddress[]{address};
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0042 A:{Splitter: B:10:0x002f, ExcHandler: java.io.IOException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0042 A:{Splitter: B:10:0x002f, ExcHandler: java.io.IOException (r2_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0056 A:{LOOP_END, LOOP:0: B:7:0x0029->B:23:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0050 A:{SYNTHETIC} */
    /* JADX WARNING: Missing block: B:15:0x0042, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            r7.impl.close();
            r7.closed = true;
     */
    /* JADX WARNING: Missing block: B:20:0x0050, code:
            throw r2;
     */
    /* JADX WARNING: Missing block: B:21:0x0051, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:0x0052, code:
            r2.addSuppressed(r1);
     */
    /* JADX WARNING: Missing block: B:23:0x0056, code:
            r7.impl = null;
            r7.created = false;
            r7.bound = false;
            r7.closed = false;
            r3 = r3 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Socket(InetAddress[] addresses, int port, SocketAddress localAddr, boolean stream) throws IOException {
        int i;
        this.created = false;
        this.bound = false;
        this.connected = false;
        this.closed = false;
        this.closeLock = new Object();
        this.shutIn = false;
        this.shutOut = false;
        this.oldImpl = false;
        if (addresses == null || addresses.length == 0) {
            throw new SocketException("Impossible: empty address list");
        }
        i = 0;
        while (i < addresses.length) {
            setImpl();
            try {
                InetSocketAddress address = new InetSocketAddress(addresses[i], port);
                createImpl(stream);
                if (localAddr != null) {
                    bind(localAddr);
                }
                connect(address);
                return;
            } catch (Exception e) {
            }
        }
        return;
        if (i != addresses.length - 1) {
        }
    }

    void createImpl(boolean stream) throws SocketException {
        if (this.impl == null) {
            setImpl();
        }
        try {
            this.impl.create(stream);
            this.created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    private void checkOldImpl() {
        if (this.impl != null) {
            this.oldImpl = ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    Class<?> clazz = Socket.this.impl.getClass();
                    do {
                        try {
                            clazz.getDeclaredMethod(SecurityConstants.SOCKET_CONNECT_ACTION, SocketAddress.class, Integer.TYPE);
                            return Boolean.FALSE;
                        } catch (NoSuchMethodException e) {
                            clazz = clazz.getSuperclass();
                            if (clazz.lambda$-java_util_function_Predicate_4628(SocketImpl.class)) {
                                return Boolean.TRUE;
                            }
                        }
                    } while (clazz.lambda$-java_util_function_Predicate_4628(SocketImpl.class));
                    return Boolean.TRUE;
                }
            })).booleanValue();
        }
    }

    void setImpl() {
        if (factory != null) {
            this.impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            this.impl = new SocksSocketImpl();
        }
        if (this.impl != null) {
            this.impl.setSocket(this);
        }
    }

    SocketImpl getImpl() throws SocketException {
        if (!this.created) {
            createImpl(true);
        }
        return this.impl;
    }

    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint == null) {
            throw new IllegalArgumentException("connect: The address can't be null");
        } else if (timeout < 0) {
            throw new IllegalArgumentException("connect: timeout can't be negative");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!this.oldImpl && isConnected()) {
            throw new SocketException("already connected");
        } else if (endpoint instanceof InetSocketAddress) {
            SocketAddress epoint = (InetSocketAddress) endpoint;
            InetAddress addr = epoint.getAddress();
            int port = epoint.getPort();
            checkAddress(addr, SecurityConstants.SOCKET_CONNECT_ACTION);
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (epoint.isUnresolved()) {
                    security.checkConnect(epoint.getHostName(), port);
                } else {
                    security.checkConnect(addr.getHostAddress(), port);
                }
            }
            if (!this.created) {
                createImpl(true);
            }
            if (!this.oldImpl) {
                this.impl.connect(epoint, timeout);
            } else if (timeout != 0) {
                throw new UnsupportedOperationException("SocketImpl.connect(addr, timeout)");
            } else if (epoint.isUnresolved()) {
                this.impl.connect(addr.getHostName(), port);
            } else {
                this.impl.connect(addr, port);
            }
            this.connected = true;
            this.bound = true;
        } else {
            throw new IllegalArgumentException("Unsupported address type");
        }
    }

    public void bind(SocketAddress bindpoint) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!this.oldImpl && isBound()) {
            throw new SocketException("Already bound");
        } else if (bindpoint == null || ((bindpoint instanceof InetSocketAddress) ^ 1) == 0) {
            InetSocketAddress epoint = (InetSocketAddress) bindpoint;
            if (epoint == null || !epoint.isUnresolved()) {
                if (epoint == null) {
                    epoint = new InetSocketAddress(0);
                }
                InetAddress addr = epoint.getAddress();
                int port = epoint.getPort();
                checkAddress(addr, "bind");
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkListen(port);
                }
                getImpl().bind(addr, port);
                this.bound = true;
                return;
            }
            throw new SocketException("Unresolved address");
        } else {
            throw new IllegalArgumentException("Unsupported address type");
        }
    }

    private void checkAddress(InetAddress addr, String op) {
        if (addr != null) {
            if (!(!(addr instanceof Inet4Address) ? addr instanceof Inet6Address : true)) {
                throw new IllegalArgumentException(op + ": invalid address type");
            }
        }
    }

    final void postAccept() {
        this.connected = true;
        this.created = true;
        this.bound = true;
    }

    void setCreated() {
        this.created = true;
    }

    void setBound() {
        this.bound = true;
    }

    void setConnected() {
        this.connected = true;
    }

    public InetAddress getInetAddress() {
        if (!isConnected()) {
            return null;
        }
        try {
            return getImpl().getInetAddress();
        } catch (SocketException e) {
            return null;
        }
    }

    public InetAddress getLocalAddress() {
        if (!isBound()) {
            return InetAddress.anyLocalAddress();
        }
        InetAddress in;
        try {
            in = (InetAddress) getImpl().getOption(15);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkConnect(in.getHostAddress(), -1);
            }
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
        } catch (SecurityException e) {
            in = InetAddress.getLoopbackAddress();
        } catch (Exception e2) {
            in = InetAddress.anyLocalAddress();
        }
        return in;
    }

    public int getPort() {
        if (!isConnected()) {
            return 0;
        }
        try {
            return getImpl().getPort();
        } catch (SocketException e) {
            return -1;
        }
    }

    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        try {
            return getImpl().getLocalPort();
        } catch (SocketException e) {
            return -1;
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        if (isConnected()) {
            return new InetSocketAddress(getInetAddress(), getPort());
        }
        return null;
    }

    public SocketAddress getLocalSocketAddress() {
        if (isBound()) {
            return new InetSocketAddress(getLocalAddress(), getLocalPort());
        }
        return null;
    }

    public SocketChannel getChannel() {
        return null;
    }

    public InputStream getInputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (isInputShutdown()) {
            throw new SocketException("Socket input is shutdown");
        } else {
            try {
                return (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                    public InputStream run() throws IOException {
                        return Socket.this.impl.getInputStream();
                    }
                });
            } catch (PrivilegedActionException e) {
                throw ((IOException) e.getException());
            }
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (isOutputShutdown()) {
            throw new SocketException("Socket output is shutdown");
        } else {
            try {
                return (OutputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<OutputStream>() {
                    public OutputStream run() throws IOException {
                        return Socket.this.impl.getOutputStream();
                    }
                });
            } catch (PrivilegedActionException e) {
                throw ((IOException) e.getException());
            }
        }
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(1, Boolean.valueOf(on));
    }

    public boolean getTcpNoDelay() throws SocketException {
        if (!isClosed()) {
            return ((Boolean) getImpl().getOption(1)).booleanValue();
        }
        throw new SocketException("Socket is closed");
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!on) {
            getImpl().setOption(128, new Boolean(on));
        } else if (linger < 0) {
            throw new IllegalArgumentException("invalid value for SO_LINGER");
        } else {
            if (linger > 65535) {
                linger = 65535;
            }
            getImpl().setOption(128, new Integer(linger));
        }
    }

    public int getSoLinger() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        Object o = getImpl().getOption(128);
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        }
        return -1;
    }

    public void sendUrgentData(int data) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (getImpl().supportsUrgentData()) {
            getImpl().sendUrgentData(data);
        } else {
            throw new SocketException("Urgent data not supported");
        }
    }

    public void setOOBInline(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(SocketOptions.SO_OOBINLINE, Boolean.valueOf(on));
    }

    public boolean getOOBInline() throws SocketException {
        if (!isClosed()) {
            return ((Boolean) getImpl().getOption(SocketOptions.SO_OOBINLINE)).booleanValue();
        }
        throw new SocketException("Socket is closed");
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (timeout < 0) {
            throw new IllegalArgumentException("timeout can't be negative");
        } else {
            getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
        }
    }

    public synchronized int getSoTimeout() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        if (!(o instanceof Integer)) {
            return 0;
        }
        return ((Integer) o).intValue();
    }

    public synchronized void setSendBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("negative send size");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else {
            getImpl().setOption(SocketOptions.SO_SNDBUF, new Integer(size));
        }
    }

    public synchronized int getSendBufferSize() throws SocketException {
        int result;
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_SNDBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("invalid receive size");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else {
            getImpl().setOption(SocketOptions.SO_RCVBUF, new Integer(size));
        }
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        int result;
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }

    public void setKeepAlive(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(8, Boolean.valueOf(on));
    }

    public boolean getKeepAlive() throws SocketException {
        if (!isClosed()) {
            return ((Boolean) getImpl().getOption(8)).booleanValue();
        }
        throw new SocketException("Socket is closed");
    }

    public void setTrafficClass(int tc) throws SocketException {
        if (tc < 0 || tc > 255) {
            throw new IllegalArgumentException("tc is not in range 0 -- 255");
        } else if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else {
            try {
                getImpl().setOption(3, Integer.valueOf(tc));
            } catch (SocketException se) {
                if (!isConnected()) {
                    throw se;
                }
            }
        }
    }

    public int getTrafficClass() throws SocketException {
        if (!isClosed()) {
            return ((Integer) getImpl().getOption(3)).intValue();
        }
        throw new SocketException("Socket is closed");
    }

    public void setReuseAddress(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(4, Boolean.valueOf(on));
    }

    public boolean getReuseAddress() throws SocketException {
        if (!isClosed()) {
            return ((Boolean) getImpl().getOption(4)).booleanValue();
        }
        throw new SocketException("Socket is closed");
    }

    public synchronized void close() throws IOException {
        synchronized (this.closeLock) {
            if (isClosed()) {
                return;
            }
            if (this.created) {
                this.impl.close();
            }
            this.closed = true;
        }
    }

    public void shutdownInput() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (isInputShutdown()) {
            throw new SocketException("Socket input is already shutdown");
        } else {
            getImpl().shutdownInput();
            this.shutIn = true;
        }
    }

    public void shutdownOutput() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (!isConnected()) {
            throw new SocketException("Socket is not connected");
        } else if (isOutputShutdown()) {
            throw new SocketException("Socket output is already shutdown");
        } else {
            getImpl().shutdownOutput();
            this.shutOut = true;
        }
    }

    public String toString() {
        try {
            if (isConnected()) {
                return "Socket[address=" + getImpl().getInetAddress() + ",port=" + getImpl().getPort() + ",localPort=" + getImpl().getLocalPort() + "]";
            }
        } catch (SocketException e) {
        }
        return "Socket[unconnected]";
    }

    public boolean isConnected() {
        return !this.connected ? this.oldImpl : true;
    }

    public boolean isBound() {
        return !this.bound ? this.oldImpl : true;
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this.closeLock) {
            z = this.closed;
        }
        return z;
    }

    public boolean isInputShutdown() {
        return this.shutIn;
    }

    public boolean isOutputShutdown() {
        return this.shutOut;
    }

    public static synchronized void setSocketImplFactory(SocketImplFactory fac) throws IOException {
        synchronized (Socket.class) {
            if (factory != null) {
                throw new SocketException("factory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            factory = fac;
        }
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    public FileDescriptor getFileDescriptor$() {
        return this.impl.getFileDescriptor();
    }
}
