package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/* compiled from: ServerSocketFactory */
class DefaultServerSocketFactory extends ServerSocketFactory {
    DefaultServerSocketFactory() {
    }

    public ServerSocket createServerSocket() throws IOException {
        return new ServerSocket();
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return new ServerSocket(port, backlog);
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        return new ServerSocket(port, backlog, ifAddress);
    }
}
