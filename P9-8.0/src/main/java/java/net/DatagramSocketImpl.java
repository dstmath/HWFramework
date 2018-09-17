package java.net;

import java.io.FileDescriptor;
import java.io.IOException;

public abstract class DatagramSocketImpl implements SocketOptions {
    protected FileDescriptor fd;
    protected int localPort;
    DatagramSocket socket;

    protected abstract void bind(int i, InetAddress inetAddress) throws SocketException;

    protected abstract void close();

    protected abstract void create() throws SocketException;

    @Deprecated
    protected abstract byte getTTL() throws IOException;

    protected abstract int getTimeToLive() throws IOException;

    protected abstract void join(InetAddress inetAddress) throws IOException;

    protected abstract void joinGroup(SocketAddress socketAddress, NetworkInterface networkInterface) throws IOException;

    protected abstract void leave(InetAddress inetAddress) throws IOException;

    protected abstract void leaveGroup(SocketAddress socketAddress, NetworkInterface networkInterface) throws IOException;

    protected abstract int peek(InetAddress inetAddress) throws IOException;

    protected abstract int peekData(DatagramPacket datagramPacket) throws IOException;

    protected abstract void receive(DatagramPacket datagramPacket) throws IOException;

    protected abstract void send(DatagramPacket datagramPacket) throws IOException;

    @Deprecated
    protected abstract void setTTL(byte b) throws IOException;

    protected abstract void setTimeToLive(int i) throws IOException;

    int dataAvailable() {
        return 0;
    }

    void setDatagramSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    DatagramSocket getDatagramSocket() {
        return this.socket;
    }

    protected void connect(InetAddress address, int port) throws SocketException {
    }

    protected void disconnect() {
    }

    protected int getLocalPort() {
        return this.localPort;
    }

    <T> void setOption(SocketOption<T> name, T value) throws IOException {
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
        } else if (name == StandardSocketOptions.IP_MULTICAST_TTL && (getDatagramSocket() instanceof MulticastSocket)) {
            if (value instanceof Integer) {
                setTimeToLive(((Integer) value).intValue());
                return;
            }
            throw new IllegalArgumentException("not an integer");
        } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP && (getDatagramSocket() instanceof MulticastSocket)) {
            setOption(18, value);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    <T> T getOption(SocketOption<T> name) throws IOException {
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

    protected FileDescriptor getFileDescriptor() {
        return this.fd;
    }
}
