package java.net;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.security.AccessController;
import java.util.Enumeration;
import libcore.io.IoBridge;
import sun.net.ResourceManager;
import sun.security.action.GetPropertyAction;

abstract class AbstractPlainDatagramSocketImpl extends DatagramSocketImpl {
    private static final boolean connectDisabled = os.contains("OS X");
    private static final String os = ((String) AccessController.doPrivileged(new GetPropertyAction("os.name")));
    boolean connected = false;
    protected InetAddress connectedAddress = null;
    private int connectedPort = -1;
    private final CloseGuard guard = CloseGuard.get();
    int timeout = 0;
    private int trafficClass = 0;

    protected abstract void bind0(int i, InetAddress inetAddress) throws SocketException;

    protected abstract void connect0(InetAddress inetAddress, int i) throws SocketException;

    protected abstract void datagramSocketClose();

    protected abstract void datagramSocketCreate() throws SocketException;

    protected abstract void disconnect0(int i);

    @Deprecated
    protected abstract byte getTTL() throws IOException;

    protected abstract int getTimeToLive() throws IOException;

    protected abstract void join(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected abstract void leave(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected abstract int peek(InetAddress inetAddress) throws IOException;

    protected abstract int peekData(DatagramPacket datagramPacket) throws IOException;

    protected abstract void receive0(DatagramPacket datagramPacket) throws IOException;

    protected abstract void send(DatagramPacket datagramPacket) throws IOException;

    @Deprecated
    protected abstract void setTTL(byte b) throws IOException;

    protected abstract void setTimeToLive(int i) throws IOException;

    protected abstract Object socketGetOption(int i) throws SocketException;

    protected abstract void socketSetOption(int i, Object obj) throws SocketException;

    AbstractPlainDatagramSocketImpl() {
    }

    protected synchronized void create() throws SocketException {
        ResourceManager.beforeUdpCreate();
        this.fd = new FileDescriptor();
        try {
            datagramSocketCreate();
            if (this.fd != null && this.fd.valid()) {
                this.guard.open("close");
            }
        } catch (SocketException ioe) {
            ResourceManager.afterUdpClose();
            this.fd = null;
            throw ioe;
        }
    }

    protected synchronized void bind(int lport, InetAddress laddr) throws SocketException {
        bind0(lport, laddr);
    }

    protected void connect(InetAddress address, int port) throws SocketException {
        BlockGuard.getThreadPolicy().onNetwork();
        connect0(address, port);
        this.connectedAddress = address;
        this.connectedPort = port;
        this.connected = true;
    }

    protected void disconnect() {
        disconnect0(this.connectedAddress.holder().getFamily());
        this.connected = false;
        this.connectedAddress = null;
        this.connectedPort = -1;
    }

    protected synchronized void receive(DatagramPacket p) throws IOException {
        receive0(p);
    }

    protected void join(InetAddress inetaddr) throws IOException {
        join(inetaddr, null);
    }

    protected void leave(InetAddress inetaddr) throws IOException {
        leave(inetaddr, null);
    }

    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        if (mcastaddr == null || ((mcastaddr instanceof InetSocketAddress) ^ 1) != 0) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        join(((InetSocketAddress) mcastaddr).getAddress(), netIf);
    }

    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        if (mcastaddr == null || ((mcastaddr instanceof InetSocketAddress) ^ 1) != 0) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        leave(((InetSocketAddress) mcastaddr).getAddress(), netIf);
    }

    protected void close() {
        this.guard.close();
        if (this.fd != null) {
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            this.fd = null;
        }
    }

    protected boolean isClosed() {
        return this.fd == null;
    }

    protected void finalize() {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }

    public void setOption(int optID, Object o) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        switch (optID) {
            case 3:
                if (o != null && ((o instanceof Integer) ^ 1) == 0) {
                    this.trafficClass = ((Integer) o).intValue();
                    break;
                }
                throw new SocketException("bad argument for IP_TOS");
                break;
            case 4:
                if (o == null || ((o instanceof Boolean) ^ 1) != 0) {
                    throw new SocketException("bad argument for SO_REUSEADDR");
                }
            case 15:
                throw new SocketException("Cannot re-bind Socket");
            case 16:
                if (o == null || ((o instanceof InetAddress) ^ 1) != 0) {
                    throw new SocketException("bad argument for IP_MULTICAST_IF");
                }
            case 18:
                if (o == null || ((o instanceof Boolean) ^ 1) != 0) {
                    throw new SocketException("bad argument for IP_MULTICAST_LOOP");
                }
            case SocketOptions.IP_MULTICAST_IF2 /*31*/:
                if (o != null) {
                    if (((!(o instanceof Integer) ? o instanceof NetworkInterface : 1) ^ 1) == 0) {
                        if (o instanceof NetworkInterface) {
                            o = new Integer(((NetworkInterface) o).getIndex());
                            break;
                        }
                    }
                }
                throw new SocketException("bad argument for IP_MULTICAST_IF2");
            case 32:
                if (o == null || ((o instanceof Boolean) ^ 1) != 0) {
                    throw new SocketException("bad argument for SO_BROADCAST");
                }
            case SocketOptions.SO_SNDBUF /*4097*/:
            case SocketOptions.SO_RCVBUF /*4098*/:
                if (o == null || ((o instanceof Integer) ^ 1) != 0 || ((Integer) o).intValue() < 0) {
                    throw new SocketException("bad argument for SO_SNDBUF or SO_RCVBUF");
                }
            case SocketOptions.SO_TIMEOUT /*4102*/:
                if (o == null || ((o instanceof Integer) ^ 1) != 0) {
                    throw new SocketException("bad argument for SO_TIMEOUT");
                }
                int tmp = ((Integer) o).intValue();
                if (tmp < 0) {
                    throw new IllegalArgumentException("timeout < 0");
                }
                this.timeout = tmp;
                return;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        socketSetOption(optID, o);
    }

    public Object getOption(int optID) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        Object result;
        switch (optID) {
            case 3:
                result = socketGetOption(optID);
                if (((Integer) result).intValue() == -1) {
                    result = new Integer(this.trafficClass);
                    break;
                }
                break;
            case 4:
            case 15:
            case 16:
            case 18:
            case SocketOptions.IP_MULTICAST_IF2 /*31*/:
            case 32:
            case SocketOptions.SO_SNDBUF /*4097*/:
            case SocketOptions.SO_RCVBUF /*4098*/:
                result = socketGetOption(optID);
                if (optID == 16) {
                    return getNIFirstAddress(((Integer) result).intValue());
                }
                break;
            case SocketOptions.SO_TIMEOUT /*4102*/:
                result = new Integer(this.timeout);
                break;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        return result;
    }

    static InetAddress getNIFirstAddress(int niIndex) throws SocketException {
        if (niIndex > 0) {
            Enumeration<InetAddress> addressesEnum = NetworkInterface.getByIndex(niIndex).getInetAddresses();
            if (addressesEnum.hasMoreElements()) {
                return (InetAddress) addressesEnum.nextElement();
            }
        }
        return InetAddress.anyLocalAddress();
    }

    protected boolean nativeConnectDisabled() {
        return connectDisabled;
    }

    int dataAvailable() {
        try {
            return IoBridge.available(this.fd);
        } catch (IOException e) {
            return -1;
        }
    }
}
