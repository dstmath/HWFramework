package java.net;

import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructGroupReq;
import java.io.IOException;
import jdk.net.ExtendedSocketOptions;
import jdk.net.SocketFlow;
import libcore.io.IoBridge;
import libcore.io.Libcore;
import libcore.util.EmptyArray;
import sun.net.ExtendedOptionsImpl;

class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl {
    PlainDatagramSocketImpl() {
    }

    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (!name.lambda$-java_util_function_Predicate_4628(ExtendedSocketOptions.SO_FLOW_SLA)) {
            super.setOption(name, value);
        } else if (isClosed()) {
            throw new SocketException("Socket closed");
        } else {
            ExtendedOptionsImpl.checkSetOptionPermission(name);
            ExtendedOptionsImpl.checkValueType(value, SocketFlow.class);
            ExtendedOptionsImpl.setFlowOption(getFileDescriptor(), (SocketFlow) value);
        }
    }

    protected <T> T getOption(SocketOption<T> name) throws IOException {
        if (!name.lambda$-java_util_function_Predicate_4628(ExtendedSocketOptions.SO_FLOW_SLA)) {
            return super.getOption(name);
        }
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        ExtendedOptionsImpl.checkGetOptionPermission(name);
        SocketFlow flow = SocketFlow.create();
        ExtendedOptionsImpl.getFlowOption(getFileDescriptor(), flow);
        return flow;
    }

    protected void socketSetOption(int opt, Object val) throws SocketException {
        try {
            socketSetOption0(opt, val);
        } catch (SocketException se) {
            if (!this.connected) {
                throw se;
            }
        }
    }

    protected synchronized void bind0(int lport, InetAddress laddr) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.bind(this.fd, laddr, lport);
        if (lport == 0) {
            this.localPort = IoBridge.getLocalInetSocketAddress(this.fd).getPort();
        } else {
            this.localPort = lport;
        }
    }

    protected void send(DatagramPacket p) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        } else if (p.getData() == null || p.getAddress() == null) {
            throw new NullPointerException("null buffer || null address");
        } else {
            IoBridge.sendto(this.fd, p.getData(), p.getOffset(), p.getLength(), 0, this.connected ? null : p.getAddress(), this.connected ? 0 : p.getPort());
        }
    }

    protected synchronized int peek(InetAddress i) throws IOException {
        DatagramPacket p;
        p = new DatagramPacket(EmptyArray.BYTE, 0);
        doRecv(p, OsConstants.MSG_PEEK);
        i.holder().address = p.getAddress().holder().address;
        return p.getPort();
    }

    protected synchronized int peekData(DatagramPacket p) throws IOException {
        doRecv(p, OsConstants.MSG_PEEK);
        return p.getPort();
    }

    protected synchronized void receive0(DatagramPacket p) throws IOException {
        doRecv(p, 0);
    }

    private void doRecv(DatagramPacket p, int flags) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        if (this.timeout != 0) {
            IoBridge.poll(this.fd, OsConstants.POLLIN | OsConstants.POLLERR, this.timeout);
        }
        IoBridge.recvfrom(false, this.fd, p.getData(), p.getOffset(), p.bufLength, flags, p, this.connected);
    }

    protected void setTimeToLive(int ttl) throws IOException {
        IoBridge.setSocketOption(this.fd, 17, Integer.valueOf(ttl));
    }

    protected int getTimeToLive() throws IOException {
        return ((Integer) IoBridge.getSocketOption(this.fd, 17)).intValue();
    }

    protected void setTTL(byte ttl) throws IOException {
        setTimeToLive(ttl & 255);
    }

    protected byte getTTL() throws IOException {
        return (byte) getTimeToLive();
    }

    private static StructGroupReq makeGroupReq(InetAddress gr_group, NetworkInterface networkInterface) {
        return new StructGroupReq(networkInterface != null ? networkInterface.getIndex() : 0, gr_group);
    }

    protected void join(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.setSocketOption(this.fd, 19, makeGroupReq(inetaddr, netIf));
    }

    protected void leave(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.setSocketOption(this.fd, 20, makeGroupReq(inetaddr, netIf));
    }

    protected void datagramSocketCreate() throws SocketException {
        this.fd = IoBridge.socket(OsConstants.AF_INET6, OsConstants.SOCK_DGRAM, 0);
        IoBridge.setSocketOption(this.fd, 32, Boolean.valueOf(true));
        try {
            Libcore.os.setsockoptInt(this.fd, OsConstants.IPPROTO_IP, OsConstants.IP_MULTICAST_ALL, 0);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    protected void datagramSocketClose() {
        try {
            IoBridge.closeAndSignalBlockedThreads(this.fd);
        } catch (IOException e) {
        }
    }

    protected void socketSetOption0(int opt, Object val) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.setSocketOption(this.fd, opt, val);
    }

    protected Object socketGetOption(int opt) throws SocketException {
        if (!isClosed()) {
            return IoBridge.getSocketOption(this.fd, opt);
        }
        throw new SocketException("Socket closed");
    }

    protected void connect0(InetAddress address, int port) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.connect(this.fd, address, port);
    }

    protected void disconnect0(int family) {
        if (!isClosed()) {
            InetAddress inetAddressUnspec = new InetAddress();
            inetAddressUnspec.holder().family = OsConstants.AF_UNSPEC;
            try {
                IoBridge.connect(this.fd, inetAddressUnspec, 0);
            } catch (SocketException e) {
            }
        }
    }
}
