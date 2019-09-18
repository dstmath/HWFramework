package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class DefaultNetworkLayer implements NetworkLayer {
    public static final DefaultNetworkLayer SINGLETON = new DefaultNetworkLayer();
    private SSLServerSocketFactory sslServerSocketFactory = ((SSLServerSocketFactory) SSLServerSocketFactory.getDefault());
    private SSLSocketFactory sslSocketFactory = ((SSLSocketFactory) SSLSocketFactory.getDefault());

    private DefaultNetworkLayer() {
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        return new ServerSocket(port, backlog, bindAddress);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new Socket(address, port);
    }

    public DatagramSocket createDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr) throws SocketException {
        if (!laddr.isMulticastAddress()) {
            return new DatagramSocket(port, laddr);
        }
        try {
            MulticastSocket ds = new MulticastSocket(port);
            ds.joinGroup(laddr);
            return ds;
        } catch (IOException e) {
            throw new SocketException(e.getLocalizedMessage());
        }
    }

    public SSLServerSocket createSSLServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        return (SSLServerSocket) this.sslServerSocketFactory.createServerSocket(port, backlog, bindAddress);
    }

    public SSLSocket createSSLSocket(InetAddress address, int port) throws IOException {
        return (SSLSocket) this.sslSocketFactory.createSocket(address, port);
    }

    public SSLSocket createSSLSocket(InetAddress address, int port, InetAddress myAddress) throws IOException {
        return (SSLSocket) this.sslSocketFactory.createSocket(address, port, myAddress, 0);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress myAddress) throws IOException {
        if (myAddress != null) {
            return new Socket(address, port, myAddress, 0);
        }
        return new Socket(address, port);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress myAddress, int myPort) throws IOException {
        if (myAddress != null) {
            return new Socket(address, port, myAddress, myPort);
        }
        if (port == 0) {
            return new Socket(address, port);
        }
        Socket sock = new Socket();
        sock.bind(new InetSocketAddress(port));
        sock.connect(new InetSocketAddress(address, port));
        return sock;
    }
}
