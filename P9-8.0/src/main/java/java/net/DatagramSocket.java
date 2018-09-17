package java.net;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import libcore.io.Libcore;
import sun.security.util.SecurityConstants;

public class DatagramSocket implements Closeable {
    static final int ST_CONNECTED = 1;
    static final int ST_CONNECTED_NO_IMPL = 2;
    static final int ST_NOT_CONNECTED = 0;
    static DatagramSocketImplFactory factory;
    static Class<?> implClass = null;
    private boolean bound;
    private int bytesLeftToFilter;
    private Object closeLock;
    private boolean closed;
    int connectState;
    InetAddress connectedAddress;
    int connectedPort;
    private boolean created;
    private boolean explicitFilter;
    DatagramSocketImpl impl;
    boolean oldImpl;
    private SocketException pendingConnectException;

    private synchronized void connectInternal(InetAddress address, int port) throws SocketException {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("connect: " + port);
        } else if (address == null) {
            throw new IllegalArgumentException("connect: null address");
        } else {
            checkAddress(address, SecurityConstants.SOCKET_CONNECT_ACTION);
            if (!isClosed()) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    if (address.isMulticastAddress()) {
                        security.checkMulticast(address);
                    } else {
                        security.checkConnect(address.getHostAddress(), port);
                        security.checkAccept(address.getHostAddress(), port);
                    }
                }
                if (!isBound()) {
                    bind(new InetSocketAddress(0));
                }
                try {
                    if (this.oldImpl || ((this.impl instanceof AbstractPlainDatagramSocketImpl) && ((AbstractPlainDatagramSocketImpl) this.impl).nativeConnectDisabled())) {
                        this.connectState = 2;
                    } else {
                        getImpl().connect(address, port);
                        this.connectState = 1;
                        int avail = getImpl().dataAvailable();
                        if (avail == -1) {
                            throw new SocketException();
                        }
                        boolean z;
                        if (avail > 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        this.explicitFilter = z;
                        if (this.explicitFilter) {
                            this.bytesLeftToFilter = getReceiveBufferSize();
                        }
                    }
                    this.connectedAddress = address;
                    this.connectedPort = port;
                } catch (SocketException se) {
                    this.connectState = 2;
                    throw se;
                } catch (Throwable th) {
                    this.connectedAddress = address;
                    this.connectedPort = port;
                }
            }
        }
    }

    public DatagramSocket() throws SocketException {
        this(new InetSocketAddress(0));
    }

    protected DatagramSocket(DatagramSocketImpl impl) {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        this.explicitFilter = false;
        this.connectState = 0;
        this.connectedAddress = null;
        this.connectedPort = -1;
        if (impl == null) {
            throw new NullPointerException();
        }
        this.impl = impl;
        checkOldImpl();
    }

    public DatagramSocket(SocketAddress bindaddr) throws SocketException {
        this.created = false;
        this.bound = false;
        this.closed = false;
        this.closeLock = new Object();
        this.oldImpl = false;
        this.explicitFilter = false;
        this.connectState = 0;
        this.connectedAddress = null;
        this.connectedPort = -1;
        createImpl();
        if (bindaddr != null) {
            try {
                bind(bindaddr);
            } finally {
                if (!isBound()) {
                    close();
                }
            }
        }
    }

    public DatagramSocket(int port) throws SocketException {
        this(port, null);
    }

    public DatagramSocket(int port, InetAddress laddr) throws SocketException {
        this(new InetSocketAddress(laddr, port));
    }

    private void checkOldImpl() {
        if (this.impl != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    public Void run() throws NoSuchMethodException {
                        DatagramSocket.this.impl.getClass().getDeclaredMethod("peekData", DatagramPacket.class);
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                this.oldImpl = true;
            }
        }
    }

    void createImpl() throws SocketException {
        if (this.impl == null) {
            if (factory != null) {
                this.impl = factory.createDatagramSocketImpl();
                checkOldImpl();
            } else {
                this.impl = DefaultDatagramSocketImplFactory.createDatagramSocketImpl(this instanceof MulticastSocket);
                checkOldImpl();
            }
        }
        this.impl.create();
        this.impl.setDatagramSocket(this);
        this.created = true;
    }

    DatagramSocketImpl getImpl() throws SocketException {
        if (!this.created) {
            createImpl();
        }
        return this.impl;
    }

    public synchronized void bind(SocketAddress addr) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (isBound()) {
            throw new SocketException("already bound");
        } else {
            if (addr == null) {
                addr = new InetSocketAddress(0);
            }
            if (addr instanceof InetSocketAddress) {
                InetSocketAddress epoint = (InetSocketAddress) addr;
                if (epoint.isUnresolved()) {
                    throw new SocketException("Unresolved address");
                }
                InetAddress iaddr = epoint.getAddress();
                int port = epoint.getPort();
                checkAddress(iaddr, "bind");
                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    sec.checkListen(port);
                }
                try {
                    getImpl().bind(port, iaddr);
                    this.bound = true;
                } catch (SocketException e) {
                    getImpl().close();
                    throw e;
                }
            }
            throw new IllegalArgumentException("Unsupported address type!");
        }
    }

    void checkAddress(InetAddress addr, String op) {
        if (addr != null) {
            if (!(!(addr instanceof Inet4Address) ? addr instanceof Inet6Address : true)) {
                throw new IllegalArgumentException(op + ": invalid address type");
            }
        }
    }

    public void connect(InetAddress address, int port) {
        try {
            connectInternal(address, port);
        } catch (SocketException se) {
            this.pendingConnectException = se;
        }
    }

    public void connect(SocketAddress addr) throws SocketException {
        if (addr == null) {
            throw new IllegalArgumentException("Address can't be null");
        } else if (addr instanceof InetSocketAddress) {
            InetSocketAddress epoint = (InetSocketAddress) addr;
            if (epoint.isUnresolved()) {
                throw new SocketException("Unresolved address");
            }
            connectInternal(epoint.getAddress(), epoint.getPort());
        } else {
            throw new IllegalArgumentException("Unsupported address type");
        }
    }

    public void disconnect() {
        synchronized (this) {
            if (isClosed()) {
                return;
            }
            if (this.connectState == 1) {
                this.impl.disconnect();
            }
            this.connectedAddress = null;
            this.connectedPort = -1;
            this.connectState = 0;
            this.explicitFilter = false;
        }
    }

    public boolean isBound() {
        return this.bound;
    }

    public boolean isConnected() {
        return this.connectState != 0;
    }

    public InetAddress getInetAddress() {
        return this.connectedAddress;
    }

    public int getPort() {
        return this.connectedPort;
    }

    public SocketAddress getRemoteSocketAddress() {
        if (isConnected()) {
            return new InetSocketAddress(getInetAddress(), getPort());
        }
        return null;
    }

    public SocketAddress getLocalSocketAddress() {
        if (!isClosed() && isBound()) {
            return new InetSocketAddress(getLocalAddress(), getLocalPort());
        }
        return null;
    }

    public void send(DatagramPacket p) throws IOException {
        synchronized (p) {
            if (this.pendingConnectException != null) {
                throw new SocketException("Pending connect failure", this.pendingConnectException);
            } else if (isClosed()) {
                throw new SocketException("Socket is closed");
            } else {
                checkAddress(p.getAddress(), "send");
                if (this.connectState == 0) {
                    SecurityManager security = System.getSecurityManager();
                    if (security != null) {
                        if (p.getAddress().isMulticastAddress()) {
                            security.checkMulticast(p.getAddress());
                        } else {
                            security.checkConnect(p.getAddress().getHostAddress(), p.getPort());
                        }
                    }
                } else {
                    InetAddress packetAddress = p.getAddress();
                    if (packetAddress == null) {
                        p.setAddress(this.connectedAddress);
                        p.setPort(this.connectedPort);
                    } else if (!(packetAddress.equals(this.connectedAddress) && p.getPort() == this.connectedPort)) {
                        throw new IllegalArgumentException("connected address and packet address differ");
                    }
                }
                if (!isBound()) {
                    bind(new InetSocketAddress(0));
                }
                getImpl().send(p);
            }
        }
    }

    public synchronized void receive(DatagramPacket p) throws IOException {
        synchronized (p) {
            if (!isBound()) {
                bind(new InetSocketAddress(0));
            }
            if (this.pendingConnectException != null) {
                throw new SocketException("Pending connect failure", this.pendingConnectException);
            }
            int peekPort;
            DatagramPacket peekPacket;
            if (this.connectState == 0) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    while (true) {
                        String peekAd;
                        if (this.oldImpl) {
                            InetAddress adr = new InetAddress();
                            peekPort = getImpl().peek(adr);
                            peekAd = adr.getHostAddress();
                        } else {
                            peekPacket = new DatagramPacket(new byte[1], 1);
                            peekPort = getImpl().peekData(peekPacket);
                            peekAd = peekPacket.getAddress().getHostAddress();
                        }
                        try {
                            security.checkAccept(peekAd, peekPort);
                            break;
                        } catch (SecurityException e) {
                            getImpl().receive(new DatagramPacket(new byte[1], 1));
                        }
                    }
                }
            }
            DatagramPacket tmp = null;
            if (this.connectState == 2 || this.explicitFilter) {
                boolean stop = false;
                while (!stop) {
                    InetAddress peekAddress;
                    if (this.oldImpl) {
                        peekAddress = new InetAddress();
                        peekPort = getImpl().peek(peekAddress);
                    } else {
                        peekPacket = new DatagramPacket(new byte[1], 1);
                        peekPort = getImpl().peekData(peekPacket);
                        peekAddress = peekPacket.getAddress();
                    }
                    if (this.connectedAddress.equals(peekAddress) && this.connectedPort == peekPort) {
                        stop = true;
                    } else {
                        tmp = new DatagramPacket(new byte[1024], 1024);
                        getImpl().receive(tmp);
                        if (this.explicitFilter && checkFiltering(tmp)) {
                            stop = true;
                        }
                    }
                }
            }
            getImpl().receive(p);
            if (this.explicitFilter && tmp == null) {
                checkFiltering(p);
            }
        }
    }

    private boolean checkFiltering(DatagramPacket p) throws SocketException {
        this.bytesLeftToFilter -= p.getLength();
        if (this.bytesLeftToFilter > 0 && getImpl().dataAvailable() > 0) {
            return false;
        }
        this.explicitFilter = false;
        return true;
    }

    public InetAddress getLocalAddress() {
        if (isClosed()) {
            return null;
        }
        InetAddress in;
        try {
            in = (InetAddress) getImpl().getOption(15);
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                s.checkConnect(in.getHostAddress(), -1);
            }
        } catch (Exception e) {
            in = InetAddress.anyLocalAddress();
        }
        return in;
    }

    public int getLocalPort() {
        if (isClosed()) {
            return -1;
        }
        try {
            return getImpl().getLocalPort();
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    public synchronized int getSoTimeout() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (getImpl() == null) {
            return 0;
        } else {
            Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
            if (!(o instanceof Integer)) {
                return 0;
            }
            return ((Integer) o).intValue();
        }
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

    public synchronized void setReuseAddress(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (this.oldImpl) {
            getImpl().setOption(4, new Integer(on ? -1 : 0));
        } else {
            getImpl().setOption(4, Boolean.valueOf(on));
        }
    }

    public synchronized boolean getReuseAddress() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return ((Boolean) getImpl().getOption(4)).booleanValue();
    }

    public synchronized void setBroadcast(boolean on) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        getImpl().setOption(32, Boolean.valueOf(on));
    }

    public synchronized boolean getBroadcast() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return ((Boolean) getImpl().getOption(32)).booleanValue();
    }

    public synchronized void setTrafficClass(int tc) throws SocketException {
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

    public synchronized int getTrafficClass() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return ((Integer) getImpl().getOption(3)).intValue();
    }

    public void close() {
        synchronized (this.closeLock) {
            if (isClosed()) {
                return;
            }
            this.impl.close();
            this.closed = true;
        }
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this.closeLock) {
            z = this.closed;
        }
        return z;
    }

    public DatagramChannel getChannel() {
        return null;
    }

    public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac) throws IOException {
        synchronized (DatagramSocket.class) {
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

    public FileDescriptor getFileDescriptor$() {
        return this.impl.fd;
    }

    public void setNetworkInterface(NetworkInterface netInterface) throws SocketException {
        if (netInterface == null) {
            throw new NullPointerException("netInterface == null");
        }
        try {
            Libcore.os.setsockoptIfreq(this.impl.fd, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, netInterface.getName());
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }
}
