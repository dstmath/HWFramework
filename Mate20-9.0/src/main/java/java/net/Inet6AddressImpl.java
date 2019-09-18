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
        InetAddress result2 = InetAddress.disallowDeprecatedFormats(host, result);
        if (result2 != null) {
            return new InetAddress[]{result2};
        }
        throw new UnknownHostException("Deprecated IPv4 address format: " + host);
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
                    address.holder().originalHostName = host;
                }
                addressCache.put(host, netId, addresses);
                return addresses;
            } catch (GaiException gaiException) {
                if (!(gaiException.getCause() instanceof ErrnoException) || ((ErrnoException) gaiException.getCause()).errno != OsConstants.EACCES) {
                    String detailMessage = "Unable to resolve host \"" + host + "\": " + Libcore.os.gai_strerror(gaiException.error);
                    addressCache.putUnknownHost(host, netId, detailMessage);
                    throw gaiException.rethrowAsUnknownHostException(detailMessage);
                }
                throw new SecurityException("Permission denied (missing INTERNET permission?)", gaiException);
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
            while (true) {
                if (!it.hasMoreElements()) {
                    break;
                }
                InetAddress inetaddr = it.nextElement();
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
        FileDescriptor fd = null;
        boolean z = true;
        try {
            fd = IoBridge.socket(OsConstants.AF_INET6, OsConstants.SOCK_STREAM, 0);
            if (ttl > 0) {
                IoBridge.setSocketOption(fd, 25, Integer.valueOf(ttl));
            }
            if (sourceAddr != null) {
                IoBridge.bind(fd, sourceAddr, 0);
            }
            IoBridge.connect(fd, addr, 7, timeout);
            return true;
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (!(cause instanceof ErrnoException) || ((ErrnoException) cause).errno != OsConstants.ECONNREFUSED) {
                z = false;
            }
            return z;
        } finally {
            IoBridge.closeAndSignalBlockedThreads(fd);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0106 A[SYNTHETIC, Splitter:B:76:0x0106] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0112 A[SYNTHETIC, Splitter:B:83:0x0112] */
    public boolean icmpEcho(InetAddress addr, int timeout, InetAddress sourceAddr, int ttl) throws IOException {
        FileDescriptor fd;
        FileDescriptor fd2;
        Throwable th;
        byte[] packet;
        int sockTo;
        int seq;
        int icmpId;
        byte[] received;
        DatagramPacket receivedPacket;
        int to;
        int seq2;
        byte b;
        InetAddress inetAddress = addr;
        InetAddress inetAddress2 = sourceAddr;
        try {
            boolean isIPv4 = inetAddress instanceof Inet4Address;
            fd = IoBridge.socket(isIPv4 ? OsConstants.AF_INET : OsConstants.AF_INET6, OsConstants.SOCK_DGRAM, isIPv4 ? OsConstants.IPPROTO_ICMP : OsConstants.IPPROTO_ICMPV6);
            if (ttl > 0) {
                try {
                    IoBridge.setSocketOption(fd, 25, Integer.valueOf(ttl));
                } catch (IOException e) {
                    if (fd != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    fd2 = fd;
                    if (fd2 != null) {
                    }
                    throw th;
                }
            }
            if (inetAddress2 != null) {
                IoBridge.bind(fd, inetAddress2, 0);
            }
            int seq3 = timeout;
            int to2 = 0;
            while (true) {
                int seq4 = to2;
                if (seq3 > 0) {
                    int i = 1000;
                    if (seq3 < 1000) {
                        i = seq3;
                    }
                    int sockTo2 = i;
                    try {
                        IoBridge.setSocketOption(fd, SocketOptions.SO_TIMEOUT, Integer.valueOf(sockTo2));
                        byte[] packet2 = StructIcmpHdr.IcmpEchoHdr(isIPv4, seq4).getBytes();
                        packet = packet2;
                        sockTo = sockTo2;
                        seq = seq4;
                        IoBridge.sendto(fd, packet2, 0, packet2.length, 0, inetAddress, 0);
                        icmpId = IoBridge.getLocalInetSocketAddress(fd).getPort();
                        received = new byte[packet.length];
                        receivedPacket = new DatagramPacket(received, packet.length);
                        to = seq3;
                        fd2 = fd;
                    } catch (IOException e2) {
                        FileDescriptor fileDescriptor = fd;
                        if (fd != null) {
                        }
                        return false;
                    } catch (Throwable th3) {
                        fd2 = fd;
                        th = th3;
                        if (fd2 != null) {
                        }
                        throw th;
                    }
                    try {
                        if (IoBridge.recvfrom(true, fd, received, 0, received.length, 0, receivedPacket, false) == packet.length) {
                            if (isIPv4) {
                                b = (byte) OsConstants.ICMP_ECHOREPLY;
                            } else {
                                b = (byte) OsConstants.ICMP6_ECHO_REPLY;
                            }
                            byte expectedType = b;
                            if (receivedPacket.getAddress().equals(inetAddress) && received[0] == expectedType && received[4] == ((byte) (icmpId >> 8)) && received[5] == ((byte) icmpId)) {
                                seq2 = seq;
                                if (received[6] == ((byte) (seq2 >> 8)) && received[7] == ((byte) seq2)) {
                                    if (fd2 != null) {
                                        try {
                                            Libcore.os.close(fd2);
                                        } catch (ErrnoException e3) {
                                        }
                                    }
                                    return true;
                                }
                                int seq5 = seq2 + 1;
                                seq3 = to - sockTo;
                                to2 = seq5;
                                fd = fd2;
                            }
                        }
                        seq2 = seq;
                        int seq52 = seq2 + 1;
                        seq3 = to - sockTo;
                        to2 = seq52;
                        fd = fd2;
                    } catch (IOException e4) {
                        fd = fd2;
                        if (fd != null) {
                        }
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        th = th;
                        if (fd2 != null) {
                        }
                        throw th;
                    }
                } else {
                    FileDescriptor fd3 = fd;
                    if (fd3 != null) {
                        try {
                            Libcore.os.close(fd3);
                        } catch (ErrnoException e5) {
                        }
                    }
                    FileDescriptor fileDescriptor2 = fd3;
                }
            }
        } catch (IOException e6) {
            fd = null;
            if (fd != null) {
                try {
                    Libcore.os.close(fd);
                } catch (ErrnoException e7) {
                }
            }
            return false;
        } catch (Throwable th5) {
            th = th5;
            fd2 = null;
            th = th;
            if (fd2 != null) {
                try {
                    Libcore.os.close(fd2);
                } catch (ErrnoException e8) {
                }
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
