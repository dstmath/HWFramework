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

    /* access modifiers changed from: protected */
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            super.setOption(name, value);
        } else if (!isClosed()) {
            ExtendedOptionsImpl.checkSetOptionPermission(name);
            ExtendedOptionsImpl.checkValueType(value, SocketFlow.class);
            ExtendedOptionsImpl.setFlowOption(getFileDescriptor(), (SocketFlow) value);
        } else {
            throw new SocketException("Socket closed");
        }
    }

    /* access modifiers changed from: protected */
    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            return super.getOption(name);
        }
        if (!isClosed()) {
            ExtendedOptionsImpl.checkGetOptionPermission(name);
            SocketFlow flow = SocketFlow.create();
            ExtendedOptionsImpl.getFlowOption(getFileDescriptor(), flow);
            return flow;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void socketSetOption(int opt, Object val) throws SocketException {
        try {
            socketSetOption0(opt, val);
        } catch (SocketException se) {
            if (!this.connected) {
                throw se;
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void bind0(int lport, InetAddress laddr) throws SocketException {
        if (!isClosed()) {
            IoBridge.bind(this.fd, laddr, lport);
            if (lport == 0) {
                this.localPort = IoBridge.getLocalInetSocketAddress(this.fd).getPort();
            } else {
                this.localPort = lport;
            }
        } else {
            throw new SocketException("Socket closed");
        }
    }

    /* access modifiers changed from: protected */
    public void send(DatagramPacket p) throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket closed");
        } else if (p.getData() == null || p.getAddress() == null) {
            throw new NullPointerException("null buffer || null address");
        } else {
            IoBridge.sendto(this.fd, p.getData(), p.getOffset(), p.getLength(), 0, this.connected ? null : p.getAddress(), this.connected ? 0 : p.getPort());
        }
    }

    /* access modifiers changed from: protected */
    public synchronized int peek(InetAddress i) throws IOException {
        DatagramPacket p;
        p = new DatagramPacket(EmptyArray.BYTE, 0);
        doRecv(p, OsConstants.MSG_PEEK);
        i.holder().address = p.getAddress().holder().address;
        return p.getPort();
    }

    /* access modifiers changed from: protected */
    public synchronized int peekData(DatagramPacket p) throws IOException {
        doRecv(p, OsConstants.MSG_PEEK);
        return p.getPort();
    }

    /* access modifiers changed from: protected */
    public synchronized void receive0(DatagramPacket p) throws IOException {
        doRecv(p, 0);
    }

    private void doRecv(DatagramPacket p, int flags) throws IOException {
        if (!isClosed()) {
            if (this.timeout != 0) {
                IoBridge.poll(this.fd, OsConstants.POLLIN | OsConstants.POLLERR, this.timeout);
            }
            IoBridge.recvfrom(false, this.fd, p.getData(), p.getOffset(), p.bufLength, flags, p, this.connected);
            return;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void setTimeToLive(int ttl) throws IOException {
        IoBridge.setSocketOption(this.fd, 17, Integer.valueOf(ttl));
    }

    /* access modifiers changed from: protected */
    public int getTimeToLive() throws IOException {
        return ((Integer) IoBridge.getSocketOption(this.fd, 17)).intValue();
    }

    /* access modifiers changed from: protected */
    public void setTTL(byte ttl) throws IOException {
        setTimeToLive(ttl & Character.DIRECTIONALITY_UNDEFINED);
    }

    /* access modifiers changed from: protected */
    public byte getTTL() throws IOException {
        return (byte) getTimeToLive();
    }

    private static StructGroupReq makeGroupReq(InetAddress gr_group, NetworkInterface networkInterface) {
        return new StructGroupReq(networkInterface != null ? networkInterface.getIndex() : 0, gr_group);
    }

    /* access modifiers changed from: protected */
    public void join(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        if (!isClosed()) {
            IoBridge.setSocketOption(this.fd, 19, makeGroupReq(inetaddr, netIf));
            return;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void leave(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        if (!isClosed()) {
            IoBridge.setSocketOption(this.fd, 20, makeGroupReq(inetaddr, netIf));
            return;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void datagramSocketCreate() throws SocketException {
        this.fd = IoBridge.socket(OsConstants.AF_INET6, OsConstants.SOCK_DGRAM, 0);
        IoBridge.setSocketOption(this.fd, 32, true);
        try {
            Libcore.os.setsockoptInt(this.fd, OsConstants.IPPROTO_IP, OsConstants.IP_MULTICAST_ALL, 0);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    /* access modifiers changed from: protected */
    public void datagramSocketClose() {
        try {
            IoBridge.closeAndSignalBlockedThreads(this.fd);
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void socketSetOption0(int opt, Object val) throws SocketException {
        if (!isClosed()) {
            IoBridge.setSocketOption(this.fd, opt, val);
            return;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public Object socketGetOption(int opt) throws SocketException {
        if (!isClosed()) {
            return IoBridge.getSocketOption(this.fd, opt);
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void connect0(InetAddress address, int port) throws SocketException {
        if (!isClosed()) {
            IoBridge.connect(this.fd, address, port);
            return;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void disconnect0(int family) {
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
