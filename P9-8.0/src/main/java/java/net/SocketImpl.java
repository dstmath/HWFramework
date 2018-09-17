package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class SocketImpl implements SocketOptions {
    protected InetAddress address;
    protected FileDescriptor fd;
    protected int localport;
    protected int port;
    ServerSocket serverSocket = null;
    Socket socket = null;

    protected abstract void accept(SocketImpl socketImpl) throws IOException;

    protected abstract int available() throws IOException;

    protected abstract void bind(InetAddress inetAddress, int i) throws IOException;

    protected abstract void close() throws IOException;

    protected abstract void connect(String str, int i) throws IOException;

    protected abstract void connect(InetAddress inetAddress, int i) throws IOException;

    protected abstract void connect(SocketAddress socketAddress, int i) throws IOException;

    protected abstract void create(boolean z) throws IOException;

    protected abstract InputStream getInputStream() throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    protected abstract void listen(int i) throws IOException;

    protected abstract void sendUrgentData(int i) throws IOException;

    protected void shutdownInput() throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected void shutdownOutput() throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected FileDescriptor getFileDescriptor() {
        return this.fd;
    }

    public FileDescriptor getFD$() {
        return this.fd;
    }

    protected InetAddress getInetAddress() {
        return this.address;
    }

    protected int getPort() {
        return this.port;
    }

    protected boolean supportsUrgentData() {
        return false;
    }

    protected int getLocalPort() {
        return this.localport;
    }

    void setSocket(Socket soc) {
        this.socket = soc;
    }

    Socket getSocket() {
        return this.socket;
    }

    void setServerSocket(ServerSocket soc) {
        this.serverSocket = soc;
    }

    ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public String toString() {
        return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
    }

    void reset() throws IOException {
        this.address = null;
        this.port = 0;
        this.localport = 0;
    }

    protected void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            setOption(8, value);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(4, value);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            setOption(128, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            setOption(3, value);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            setOption(1, value);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            return getOption(8);
        }
        if (name == StandardSocketOptions.SO_SNDBUF) {
            return getOption(SocketOptions.SO_SNDBUF);
        }
        if (name == StandardSocketOptions.SO_RCVBUF) {
            return getOption(SocketOptions.SO_RCVBUF);
        }
        if (name == StandardSocketOptions.SO_REUSEADDR) {
            return getOption(4);
        }
        if (name == StandardSocketOptions.SO_LINGER) {
            return getOption(128);
        }
        if (name == StandardSocketOptions.IP_TOS) {
            return getOption(3);
        }
        if (name == StandardSocketOptions.TCP_NODELAY) {
            return getOption(1);
        }
        throw new UnsupportedOperationException("unsupported option");
    }
}
