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
    ServerSocket serverSocket;
    Socket socket;

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

    public SocketImpl() {
        this.socket = null;
        this.serverSocket = null;
    }

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
}
