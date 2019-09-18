package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/* compiled from: SocketFactory */
class DefaultSocketFactory extends SocketFactory {
    DefaultSocketFactory() {
    }

    public Socket createSocket() {
        return new Socket();
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return new Socket(host, port);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new Socket(address, port);
    }

    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException, UnknownHostException {
        return new Socket(host, port, clientAddress, clientPort);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return new Socket(address, port, clientAddress, clientPort);
    }
}
