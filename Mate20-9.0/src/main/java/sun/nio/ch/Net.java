package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import jdk.net.NetworkPermission;
import jdk.net.SocketFlow;
import sun.net.ExtendedOptionsImpl;

public class Net {
    public static final short POLLCONN = pollconnValue();
    public static final short POLLERR = pollerrValue();
    public static final short POLLHUP = pollhupValue();
    public static final short POLLIN = pollinValue();
    public static final short POLLNVAL = pollnvalValue();
    public static final short POLLOUT = polloutValue();
    public static final int SHUT_RD = 0;
    public static final int SHUT_RDWR = 2;
    public static final int SHUT_WR = 1;
    static final ProtocolFamily UNSPEC = new ProtocolFamily() {
        public String name() {
            return "UNSPEC";
        }
    };
    private static volatile boolean checkedIPv6 = false;
    private static final boolean exclusiveBind;
    private static final boolean fastLoopback = isFastTcpLoopbackRequested();
    private static volatile boolean isIPv6Available;

    private static native void bind0(FileDescriptor fileDescriptor, boolean z, boolean z2, InetAddress inetAddress, int i) throws IOException;

    private static native int blockOrUnblock4(boolean z, FileDescriptor fileDescriptor, int i, int i2, int i3) throws IOException;

    static native int blockOrUnblock6(boolean z, FileDescriptor fileDescriptor, byte[] bArr, int i, byte[] bArr2) throws IOException;

    private static native boolean canIPv6SocketJoinIPv4Group0();

    private static native boolean canJoin6WithIPv4Group0();

    private static native int connect0(boolean z, FileDescriptor fileDescriptor, InetAddress inetAddress, int i) throws IOException;

    private static native int getIntOption0(FileDescriptor fileDescriptor, boolean z, int i, int i2) throws IOException;

    static native int getInterface4(FileDescriptor fileDescriptor) throws IOException;

    static native int getInterface6(FileDescriptor fileDescriptor) throws IOException;

    private static native int isExclusiveBindAvailable();

    private static native boolean isIPv6Available0();

    private static native int joinOrDrop4(boolean z, FileDescriptor fileDescriptor, int i, int i2, int i3) throws IOException;

    private static native int joinOrDrop6(boolean z, FileDescriptor fileDescriptor, byte[] bArr, int i, byte[] bArr2) throws IOException;

    static native void listen(FileDescriptor fileDescriptor, int i) throws IOException;

    private static native InetAddress localInetAddress(FileDescriptor fileDescriptor) throws IOException;

    private static native int localPort(FileDescriptor fileDescriptor) throws IOException;

    static native int poll(FileDescriptor fileDescriptor, int i, long j) throws IOException;

    static native short pollconnValue();

    static native short pollerrValue();

    static native short pollhupValue();

    static native short pollinValue();

    static native short pollnvalValue();

    static native short polloutValue();

    private static native InetAddress remoteInetAddress(FileDescriptor fileDescriptor) throws IOException;

    private static native int remotePort(FileDescriptor fileDescriptor) throws IOException;

    private static native void setIntOption0(FileDescriptor fileDescriptor, boolean z, int i, int i2, int i3, boolean z2) throws IOException;

    static native void setInterface4(FileDescriptor fileDescriptor, int i) throws IOException;

    static native void setInterface6(FileDescriptor fileDescriptor, int i) throws IOException;

    static native void shutdown(FileDescriptor fileDescriptor, int i) throws IOException;

    private static native int socket0(boolean z, boolean z2, boolean z3, boolean z4);

    private Net() {
    }

    static {
        int availLevel = isExclusiveBindAvailable();
        if (availLevel >= 0) {
            String exclBindProp = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("sun.net.useExclusiveBind");
                }
            });
            boolean z = true;
            if (exclBindProp != null) {
                if (exclBindProp.length() != 0) {
                    z = Boolean.parseBoolean(exclBindProp);
                }
                exclusiveBind = z;
            } else if (availLevel == 1) {
                exclusiveBind = true;
            } else {
                exclusiveBind = false;
            }
        } else {
            exclusiveBind = false;
        }
    }

    static boolean isIPv6Available() {
        if (!checkedIPv6) {
            isIPv6Available = isIPv6Available0();
            checkedIPv6 = true;
        }
        return isIPv6Available;
    }

    static boolean useExclusiveBind() {
        return exclusiveBind;
    }

    static boolean canIPv6SocketJoinIPv4Group() {
        return canIPv6SocketJoinIPv4Group0();
    }

    static boolean canJoin6WithIPv4Group() {
        return canJoin6WithIPv4Group0();
    }

    public static InetSocketAddress checkAddress(SocketAddress sa) {
        if (sa == null) {
            throw new IllegalArgumentException("sa == null");
        } else if (sa instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress) sa;
            if (!isa.isUnresolved()) {
                InetAddress addr = isa.getAddress();
                if ((addr instanceof Inet4Address) || (addr instanceof Inet6Address)) {
                    return isa;
                }
                throw new IllegalArgumentException("Invalid address type");
            }
            throw new UnresolvedAddressException();
        } else {
            throw new UnsupportedAddressTypeException();
        }
    }

    static InetSocketAddress asInetSocketAddress(SocketAddress sa) {
        if (sa instanceof InetSocketAddress) {
            return (InetSocketAddress) sa;
        }
        throw new UnsupportedAddressTypeException();
    }

    static void translateToSocketException(Exception x) throws SocketException {
        if (!(x instanceof SocketException)) {
            Exception nx = x;
            if (x instanceof ClosedChannelException) {
                nx = new SocketException("Socket is closed");
            } else if (x instanceof NotYetConnectedException) {
                nx = new SocketException("Socket is not connected");
            } else if (x instanceof AlreadyBoundException) {
                nx = new SocketException("Already bound");
            } else if (x instanceof NotYetBoundException) {
                nx = new SocketException("Socket is not bound yet");
            } else if (x instanceof UnsupportedAddressTypeException) {
                nx = new SocketException("Unsupported address type");
            } else if (x instanceof UnresolvedAddressException) {
                nx = new SocketException("Unresolved address");
            } else if (x instanceof AlreadyConnectedException) {
                nx = new SocketException("Already connected");
            }
            if (nx != x) {
                nx.initCause(x);
            }
            if (nx instanceof SocketException) {
                throw ((SocketException) nx);
            } else if (nx instanceof RuntimeException) {
                throw ((RuntimeException) nx);
            } else {
                throw new Error("Untranslated exception", nx);
            }
        } else {
            throw ((SocketException) x);
        }
    }

    static void translateException(Exception x, boolean unknownHostForUnresolved) throws IOException {
        if (x instanceof IOException) {
            throw ((IOException) x);
        } else if (!unknownHostForUnresolved || !(x instanceof UnresolvedAddressException)) {
            translateToSocketException(x);
        } else {
            throw new UnknownHostException();
        }
    }

    static void translateException(Exception x) throws IOException {
        translateException(x, false);
    }

    static InetSocketAddress getRevealedLocalAddress(InetSocketAddress addr) {
        SecurityManager sm = System.getSecurityManager();
        if (addr == null || sm == null) {
            return addr;
        }
        try {
            sm.checkConnect(addr.getAddress().getHostAddress(), -1);
        } catch (SecurityException e) {
            addr = getLoopbackAddress(addr.getPort());
        }
        return addr;
    }

    static String getRevealedLocalAddressAsString(InetSocketAddress addr) {
        if (System.getSecurityManager() == null) {
            return addr.toString();
        }
        return getLoopbackAddress(addr.getPort()).toString();
    }

    private static InetSocketAddress getLoopbackAddress(int port) {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    }

    static Inet4Address anyInet4Address(final NetworkInterface interf) {
        return (Inet4Address) AccessController.doPrivileged(new PrivilegedAction<Inet4Address>() {
            public Inet4Address run() {
                Enumeration<InetAddress> addrs = NetworkInterface.this.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address) {
                        return (Inet4Address) addr;
                    }
                }
                return null;
            }
        });
    }

    static int inet4AsInt(InetAddress ia) {
        if (ia instanceof Inet4Address) {
            byte[] addr = ia.getAddress();
            return (addr[3] & 255) | ((addr[2] << 8) & 65280) | ((addr[1] << 16) & 16711680) | ((addr[0] << 24) & -16777216);
        }
        throw new AssertionError((Object) "Should not reach here");
    }

    static InetAddress inet4FromInt(int address) {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) ((address >>> 24) & 255), (byte) ((address >>> 16) & 255), (byte) ((address >>> 8) & 255), (byte) (address & 255)});
        } catch (UnknownHostException e) {
            throw new AssertionError((Object) "Should not reach here");
        }
    }

    static byte[] inet6AsByteArray(InetAddress ia) {
        if (ia instanceof Inet6Address) {
            return ia.getAddress();
        }
        if (ia instanceof Inet4Address) {
            byte[] ip4address = ia.getAddress();
            byte[] address = new byte[16];
            address[10] = -1;
            address[11] = -1;
            address[12] = ip4address[0];
            address[13] = ip4address[1];
            address[14] = ip4address[2];
            address[15] = ip4address[3];
            return address;
        }
        throw new AssertionError((Object) "Should not reach here");
    }

    static void setSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name, Object value) throws IOException {
        int arg;
        if (value != null) {
            Class<?> type = name.type();
            if (type == SocketFlow.class) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(new NetworkPermission("setOption.SO_FLOW_SLA"));
                }
                ExtendedOptionsImpl.setFlowOption(fd, (SocketFlow) value);
            } else if (type != Integer.class && type != Boolean.class) {
                throw new AssertionError((Object) "Should not reach here");
            } else if ((name == StandardSocketOptions.SO_RCVBUF || name == StandardSocketOptions.SO_SNDBUF) && ((Integer) value).intValue() < 0) {
                throw new IllegalArgumentException("Invalid send/receive buffer size");
            } else {
                if (name == StandardSocketOptions.SO_LINGER) {
                    int i = ((Integer) value).intValue();
                    if (i < 0) {
                        value = -1;
                    }
                    if (i > 65535) {
                        value = 65535;
                    }
                }
                if (name == StandardSocketOptions.IP_TOS) {
                    int i2 = ((Integer) value).intValue();
                    if (i2 < 0 || i2 > 255) {
                        throw new IllegalArgumentException("Invalid IP_TOS value");
                    }
                }
                if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
                    int i3 = ((Integer) value).intValue();
                    if (i3 < 0 || i3 > 255) {
                        throw new IllegalArgumentException("Invalid TTL/hop value");
                    }
                }
                OptionKey key = SocketOptionRegistry.findOption(name, family);
                if (key != null) {
                    if (type == Integer.class) {
                        arg = ((Integer) value).intValue();
                    } else {
                        arg = ((Boolean) value).booleanValue();
                    }
                    setIntOption0(fd, family == UNSPEC, key.level(), key.name(), arg, family == StandardProtocolFamily.INET6);
                    return;
                }
                throw new AssertionError((Object) "Option not found");
            }
        } else {
            throw new IllegalArgumentException("Invalid option value");
        }
    }

    static Object getSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name) throws IOException {
        Class<?> type = name.type();
        if (type == SocketFlow.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new NetworkPermission("getOption.SO_FLOW_SLA"));
            }
            SocketFlow flow = SocketFlow.create();
            ExtendedOptionsImpl.getFlowOption(fd, flow);
            return flow;
        } else if (type == Integer.class || type == Boolean.class) {
            OptionKey key = SocketOptionRegistry.findOption(name, family);
            if (key != null) {
                int value = getIntOption0(fd, family == UNSPEC, key.level(), key.name());
                if (type == Integer.class) {
                    return Integer.valueOf(value);
                }
                return value == 0 ? Boolean.FALSE : Boolean.TRUE;
            }
            throw new AssertionError((Object) "Option not found");
        } else {
            throw new AssertionError((Object) "Should not reach here");
        }
    }

    public static boolean isFastTcpLoopbackRequested() {
        String loopbackProp = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty("jdk.net.useFastTcpLoopback");
            }
        });
        if ("".equals(loopbackProp)) {
            return true;
        }
        return Boolean.parseBoolean(loopbackProp);
    }

    static FileDescriptor socket(boolean stream) throws IOException {
        return socket(UNSPEC, stream);
    }

    static FileDescriptor socket(ProtocolFamily family, boolean stream) throws IOException {
        return IOUtil.newFD(socket0(isIPv6Available() && family != StandardProtocolFamily.INET, stream, false, fastLoopback));
    }

    static FileDescriptor serverSocket(boolean stream) {
        return IOUtil.newFD(socket0(isIPv6Available(), stream, true, fastLoopback));
    }

    public static void bind(FileDescriptor fd, InetAddress addr, int port) throws IOException {
        bind(UNSPEC, fd, addr, port);
    }

    static void bind(ProtocolFamily family, FileDescriptor fd, InetAddress addr, int port) throws IOException {
        bind0(fd, isIPv6Available() && family != StandardProtocolFamily.INET, exclusiveBind, addr, port);
    }

    static int connect(FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        return connect(UNSPEC, fd, remote, remotePort);
    }

    static int connect(ProtocolFamily family, FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return connect0(isIPv6Available() && family != StandardProtocolFamily.INET, fd, remote, remotePort);
    }

    public static InetSocketAddress localAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(localInetAddress(fd), localPort(fd));
    }

    static InetSocketAddress remoteAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(remoteInetAddress(fd), remotePort(fd));
    }

    static int join4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return joinOrDrop4(true, fd, group, interf, source);
    }

    static void drop4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        joinOrDrop4(false, fd, group, interf, source);
    }

    static int block4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return blockOrUnblock4(true, fd, group, interf, source);
    }

    static void unblock4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        blockOrUnblock4(false, fd, group, interf, source);
    }

    static int join6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return joinOrDrop6(true, fd, group, index, source);
    }

    static void drop6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        joinOrDrop6(false, fd, group, index, source);
    }

    static int block6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return blockOrUnblock6(true, fd, group, index, source);
    }

    static void unblock6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        blockOrUnblock6(false, fd, group, index, source);
    }
}
