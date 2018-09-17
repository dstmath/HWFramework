package java.net;

import android.system.ErrnoException;
import android.system.GaiException;
import android.system.OsConstants;
import android.system.StructAddrinfo;
import android.system.StructIcmpHdr;
import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Enumeration;
import libcore.io.IoBridge;
import libcore.io.Libcore;

class Inet6AddressImpl implements InetAddressImpl {
    private static final AddressCache addressCache = new AddressCache();
    private static InetAddress anyLocalAddress;
    private static InetAddress[] loopbackAddresses;

    Inet6AddressImpl() {
    }

    public InetAddress[] lookupAllHostAddr(String host, int netId) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            return loopbackAddresses();
        }
        InetAddress result = InetAddress.parseNumericAddressNoThrow(host);
        if (result == null) {
            return lookupHostByName(host, netId);
        }
        if (InetAddress.disallowDeprecatedFormats(host, result) == null) {
            throw new UnknownHostException("Deprecated IPv4 address format: " + host);
        }
        return new InetAddress[]{InetAddress.disallowDeprecatedFormats(host, result)};
    }

    private static InetAddress[] lookupHostByName(String host, int netId) throws UnknownHostException {
        BlockGuard.getThreadPolicy().onNetwork();
        Object cachedResult = addressCache.get(host, netId);
        if (cachedResult == null) {
            try {
                StructAddrinfo hints = new StructAddrinfo();
                hints.ai_flags = OsConstants.AI_ADDRCONFIG;
                hints.ai_family = OsConstants.AF_UNSPEC;
                hints.ai_socktype = OsConstants.SOCK_STREAM;
                InetAddress[] addresses = Libcore.os.android_getaddrinfo(host, hints, netId);
                for (InetAddress address : addresses) {
                    address.holder().hostName = host;
                }
                addressCache.put(host, netId, addresses);
                return addresses;
            } catch (GaiException gaiException) {
                if ((gaiException.getCause() instanceof ErrnoException) && ((ErrnoException) gaiException.getCause()).errno == OsConstants.EACCES) {
                    throw new SecurityException("Permission denied (missing INTERNET permission?)", gaiException);
                }
                String detailMessage = "Unable to resolve host \"" + host + "\": " + Libcore.os.gai_strerror(gaiException.error);
                addressCache.putUnknownHost(host, netId, detailMessage);
                throw gaiException.rethrowAsUnknownHostException(detailMessage);
            }
        } else if (cachedResult instanceof InetAddress[]) {
            return (InetAddress[]) cachedResult;
        } else {
            throw new UnknownHostException((String) cachedResult);
        }
    }

    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        BlockGuard.getThreadPolicy().onNetwork();
        return getHostByAddr0(addr);
    }

    public void clearAddressCache() {
        addressCache.clear();
    }

    public boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif, int ttl) throws IOException {
        InetAddress sourceAddr = null;
        if (netif != null) {
            Enumeration<InetAddress> it = netif.getInetAddresses();
            while (it.hasMoreElements()) {
                InetAddress inetaddr = (InetAddress) it.nextElement();
                if (inetaddr.getClass().isInstance(addr)) {
                    sourceAddr = inetaddr;
                    break;
                }
            }
            if (sourceAddr == null) {
                return false;
            }
        }
        if (icmpEcho(addr, timeout, sourceAddr, ttl)) {
            return true;
        }
        return tcpEcho(addr, timeout, sourceAddr, ttl);
    }

    private boolean tcpEcho(InetAddress addr, int timeout, InetAddress sourceAddr, int ttl) throws IOException {
        boolean z = true;
        try {
            FileDescriptor fd = IoBridge.socket(OsConstants.AF_INET6, OsConstants.SOCK_STREAM, 0);
            if (ttl > 0) {
                IoBridge.setSocketOption(fd, 25, Integer.valueOf(ttl));
            }
            if (sourceAddr != null) {
                IoBridge.bind(fd, sourceAddr, 0);
            }
            IoBridge.connect(fd, addr, 7, timeout);
            IoBridge.closeAndSignalBlockedThreads(fd);
            return true;
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (!(cause instanceof ErrnoException)) {
                z = false;
            } else if (((ErrnoException) cause).errno != OsConstants.ECONNREFUSED) {
                z = false;
            }
            IoBridge.closeAndSignalBlockedThreads(null);
            return z;
        } catch (Throwable th) {
            IoBridge.closeAndSignalBlockedThreads(null);
            throw th;
        }
    }

    protected boolean icmpEcho(InetAddress addr, int timeout, InetAddress sourceAddr, int ttl) throws IOException {
        try {
            boolean isIPv4 = addr instanceof Inet4Address;
            FileDescriptor fd = IoBridge.socket(isIPv4 ? OsConstants.AF_INET : OsConstants.AF_INET6, OsConstants.SOCK_DGRAM, isIPv4 ? OsConstants.IPPROTO_ICMP : OsConstants.IPPROTO_ICMPV6);
            if (ttl > 0) {
                IoBridge.setSocketOption(fd, 25, Integer.valueOf(ttl));
            }
            if (sourceAddr != null) {
                IoBridge.bind(fd, sourceAddr, 0);
            }
            int to = timeout;
            int seq = 0;
            while (to > 0) {
                int sockTo = to >= 1000 ? 1000 : to;
                IoBridge.setSocketOption(fd, SocketOptions.SO_TIMEOUT, Integer.valueOf(sockTo));
                byte[] packet = StructIcmpHdr.IcmpEchoHdr(isIPv4, seq).getBytes();
                IoBridge.sendto(fd, packet, 0, packet.length, 0, addr, 0);
                int icmpId = IoBridge.getLocalInetSocketAddress(fd).getPort();
                byte[] received = new byte[packet.length];
                DatagramPacket receivedPacket = new DatagramPacket(received, packet.length);
                if (IoBridge.recvfrom(true, fd, received, 0, received.length, 0, receivedPacket, false) == packet.length) {
                    byte expectedType;
                    if (isIPv4) {
                        expectedType = (byte) OsConstants.ICMP_ECHOREPLY;
                    } else {
                        expectedType = (byte) OsConstants.ICMP6_ECHO_REPLY;
                    }
                    if (receivedPacket.getAddress().equals(addr) && received[0] == expectedType && received[4] == ((byte) (icmpId >> 8)) && received[5] == ((byte) icmpId) && received[6] == ((byte) (seq >> 8)) && received[7] == ((byte) seq)) {
                        try {
                            Libcore.os.close(fd);
                        } catch (ErrnoException e) {
                        }
                        return true;
                    }
                }
                to -= sockTo;
                seq++;
            }
            try {
                Libcore.os.close(fd);
            } catch (ErrnoException e2) {
            }
        } catch (IOException e3) {
            try {
                Libcore.os.close(null);
            } catch (ErrnoException e4) {
            }
        } catch (Throwable th) {
            try {
                Libcore.os.close(null);
            } catch (ErrnoException e5) {
            }
            throw th;
        }
        return false;
    }

    public InetAddress anyLocalAddress() {
        InetAddress inetAddress;
        synchronized (Inet6AddressImpl.class) {
            if (anyLocalAddress == null) {
                Inet6Address anyAddress = new Inet6Address();
                anyAddress.holder().hostName = "::";
                anyLocalAddress = anyAddress;
            }
            inetAddress = anyLocalAddress;
        }
        return inetAddress;
    }

    public InetAddress[] loopbackAddresses() {
        InetAddress[] inetAddressArr;
        synchronized (Inet6AddressImpl.class) {
            if (loopbackAddresses == null) {
                loopbackAddresses = new InetAddress[]{Inet6Address.LOOPBACK, Inet4Address.LOOPBACK};
            }
            inetAddressArr = loopbackAddresses;
        }
        return inetAddressArr;
    }

    private String getHostByAddr0(byte[] addr) throws UnknownHostException {
        InetAddress hostaddr = InetAddress.getByAddress(addr);
        try {
            return Libcore.os.getnameinfo(hostaddr, OsConstants.NI_NAMEREQD);
        } catch (GaiException e) {
            UnknownHostException uhe = new UnknownHostException(hostaddr.toString());
            uhe.initCause(e);
            throw uhe;
        }
    }
}
