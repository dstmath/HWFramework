package java.net;

import java.io.FileDescriptor;
import java.io.IOException;

public abstract class DatagramSocketImpl implements SocketOptions {
    protected FileDescriptor fd;
    protected int localPort;
    DatagramSocket socket;

    /* access modifiers changed from: protected */
    public abstract void bind(int i, InetAddress inetAddress) throws SocketException;

    /* access modifiers changed from: protected */
    public abstract void close();

    /* access modifiers changed from: protected */
    public abstract void create() throws SocketException;

    /* access modifiers changed from: protected */
    @Deprecated
    public abstract byte getTTL() throws IOException;

    /* access modifiers changed from: protected */
    public abstract int getTimeToLive() throws IOException;

    /* access modifiers changed from: protected */
    public abstract void join(InetAddress inetAddress) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void joinGroup(SocketAddress socketAddress, NetworkInterface networkInterface) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void leave(InetAddress inetAddress) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void leaveGroup(SocketAddress socketAddress, NetworkInterface networkInterface) throws IOException;

    /* access modifiers changed from: protected */
    public abstract int peek(InetAddress inetAddress) throws IOException;

    /* access modifiers changed from: protected */
    public abstract int peekData(DatagramPacket datagramPacket) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void receive(DatagramPacket datagramPacket) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void send(DatagramPacket datagramPacket) throws IOException;

    /* access modifiers changed from: protected */
    @Deprecated
    public abstract void setTTL(byte b) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void setTimeToLive(int i) throws IOException;

    /* access modifiers changed from: package-private */
    public int dataAvailable() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setDatagramSocket(DatagramSocket socket2) {
        this.socket = socket2;
    }

    /* access modifiers changed from: package-private */
    public DatagramSocket getDatagramSocket() {
        return this.socket;
    }

    /* access modifiers changed from: protected */
    public void connect(InetAddress address, int port) throws SocketException {
    }

    /* access modifiers changed from: protected */
    public void disconnect() {
    }

    /* access modifiers changed from: protected */
    public int getLocalPort() {
        return this.localPort;
    }

    /* access modifiers changed from: package-private */
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (name == StandardSocketOptions.SO_SNDBUF) {
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(4, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            setOption(3, value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_IF && (getDatagramSocket() instanceof MulticastSocket)) {
            setOption(31, value);
        } else if (name != StandardSocketOptions.IP_MULTICAST_TTL || !(getDatagramSocket() instanceof MulticastSocket)) {
            if (name != StandardSocketOptions.IP_MULTICAST_LOOP || !(getDatagramSocket() instanceof MulticastSocket)) {
                throw new UnsupportedOperationException("unsupported option");
            }
            setOption(18, value);
        } else if (value instanceof Integer) {
            setTimeToLive(((Integer) value).intValue());
        } else {
            throw new IllegalArgumentException("not an integer");
        }
    }

    /* access modifiers changed from: package-private */
    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == StandardSocketOptions.SO_SNDBUF) {
            return getOption(SocketOptions.SO_SNDBUF);
        }
        if (name == StandardSocketOptions.SO_RCVBUF) {
            return getOption(SocketOptions.SO_RCVBUF);
        }
        if (name == StandardSocketOptions.SO_REUSEADDR) {
            return getOption(4);
        }
        if (name == StandardSocketOptions.IP_TOS) {
            return getOption(3);
        }
        if (name == StandardSocketOptions.IP_MULTICAST_IF && (getDatagramSocket() instanceof MulticastSocket)) {
            return getOption(31);
        }
        if (name == StandardSocketOptions.IP_MULTICAST_TTL && (getDatagramSocket() instanceof MulticastSocket)) {
            return Integer.valueOf(getTimeToLive());
        }
        if (name == StandardSocketOptions.IP_MULTICAST_LOOP && (getDatagramSocket() instanceof MulticastSocket)) {
            return getOption(18);
        }
        throw new UnsupportedOperationException("unsupported option");
    }

    /* access modifiers changed from: protected */
    public FileDescriptor getFileDescriptor() {
        return this.fd;
    }
}
