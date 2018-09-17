package java.net;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.security.util.SecurityConstants;

public class ServerSocket implements Closeable {
    private static SocketImplFactory factory = null;
    private boolean bound;
    private Object closeLock;
    private boolean closed;
    private boolean created;
    private SocketImpl impl;
    private boolean oldImpl;

    ServerSocket(SocketImpl impl) {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        this.impl = impl;
        impl.setServerSocket(this);
    }

    public ServerSocket() throws IOException {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        setImpl();
    }

    public ServerSocket(int port) throws IOException {
        this(port, 50, null);
    }

    public ServerSocket(int port, int backlog) throws IOException {
        this(port, backlog, null);
    }

    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        setImpl();
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port value out of range: " + port);
        }
        if (backlog < 1) {
            backlog = 50;
        }
        try {
            bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch (SecurityException e) {
            close();
            throw e;
        } catch (IOException e2) {
            close();
            throw e2;
        }
    }

    public SocketImpl getImpl() throws SocketException {
        if (!this.created) {
            createImpl();
        }
        return this.impl;
    }

    private void checkOldImpl() {
        if (this.impl != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    public Void run() throws NoSuchMethodException {
                        ServerSocket.this.impl.getClass().getDeclaredMethod(SecurityConstants.SOCKET_CONNECT_ACTION, SocketAddress.class, Integer.TYPE);
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                this.oldImpl = true;
            }
        }
    }

    private void setImpl() {
        if (factory != null) {
            this.impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            this.impl = new SocksSocketImpl();
        }
        if (this.impl != null) {
            this.impl.setServerSocket(this);
        }
    }

    void createImpl() throws SocketException {
        if (this.impl == null) {
            setImpl();
        }
        try {
            this.impl.create(true);
            this.created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    public void bind(SocketAddress endpoint) throws IOException {
        bind(endpoint, 50);
    }

    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (this.oldImpl || !isBound()) {
            if (endpoint == null) {
                endpoint = new InetSocketAddress(0);
            }
            if (endpoint instanceof InetSocketAddress) {
                InetSocketAddress epoint = (InetSocketAddress) endpoint;
                if (epoint.isUnresolved()) {
                    throw new SocketException("Unresolved address");
                }
                if (backlog < 1) {
                    backlog = 50;
                }
                try {
                    SecurityManager security = System.getSecurityManager();
                    if (security != null) {
                        security.checkListen(epoint.getPort());
                    }
                    getImpl().bind(epoint.getAddress(), epoint.getPort());
                    getImpl().listen(backlog);
                    this.bound = true;
                    return;
                } catch (SecurityException e) {
                    this.bound = false;
                    throw e;
                } catch (IOException e2) {
                    this.bound = false;
                    throw e2;
                }
            }
            throw new IllegalArgumentException("Unsupported address type");
        } else {
            throw new SocketException("Already bound");
        }
    }

    public InetAddress getInetAddress() {
        if (!isBound()) {
            return null;
        }
        try {
            InetAddress in = getImpl().getInetAddress();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkConnect(in.getHostAddress(), -1);
            }
            return in;
        } catch (SecurityException e) {
            return InetAddress.getLoopbackAddress();
        } catch (SocketException e2) {
            return null;
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

    public SocketAddress getLocalSocketAddress() {
        if (isBound()) {
            return new InetSocketAddress(getInetAddress(), getLocalPort());
        }
        return null;
    }

    public Socket accept() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (isBound()) {
            Socket s = new Socket((SocketImpl) null);
            implAccept(s);
            return s;
        } else {
            throw new SocketException("Socket is not bound yet");
        }
    }

    protected final void implAccept(Socket s) throws IOException {
        SocketImpl socketImpl = null;
        try {
            if (s.impl == null) {
                s.setImpl();
            } else {
                s.impl.reset();
            }
            socketImpl = s.impl;
            s.impl = null;
            socketImpl.address = new InetAddress();
            socketImpl.fd = new FileDescriptor();
            getImpl().accept(socketImpl);
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkAccept(socketImpl.getInetAddress().getHostAddress(), socketImpl.getPort());
            }
            s.impl = socketImpl;
            s.postAccept();
        } catch (IOException e) {
            if (socketImpl != null) {
                socketImpl.reset();
            }
            s.impl = socketImpl;
            throw e;
        } catch (SecurityException e2) {
            if (socketImpl != null) {
                socketImpl.reset();
            }
            s.impl = socketImpl;
            throw e2;
        }
    }

    public void close() throws IOException {
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

    public ServerSocketChannel getChannel() {
        return null;
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

    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    public synchronized int getSoTimeout() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        if (!(o instanceof Integer)) {
            return 0;
        }
        return ((Integer) o).intValue();
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

    public String toString() {
        if (!isBound()) {
            return "ServerSocket[unbound]";
        }
        Object in;
        if (System.getSecurityManager() != null) {
            in = InetAddress.getLoopbackAddress();
        } else {
            in = this.impl.getInetAddress();
        }
        return "ServerSocket[addr=" + in + ",localport=" + this.impl.getLocalPort() + "]";
    }

    void setBound() {
        this.bound = true;
    }

    void setCreated() {
        this.created = true;
    }

    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
        synchronized (ServerSocket.class) {
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

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("negative receive size");
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

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    public FileDescriptor getFileDescriptor$() {
        return this.impl.getFileDescriptor();
    }
}
