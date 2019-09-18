package gov.nist.core.net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.sip.ListeningPoint;

public class SslNetworkLayer implements NetworkLayer {
    private SSLServerSocketFactory sslServerSocketFactory;
    private SSLSocketFactory sslSocketFactory;

    public SslNetworkLayer(String trustStoreFile, String keyStoreFile, char[] keyStorePassword, String keyStoreType) throws GeneralSecurityException, FileNotFoundException, IOException {
        SSLContext sslContext = SSLContext.getInstance(ListeningPoint.TLS);
        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(algorithm);
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(algorithm);
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextInt();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        KeyStore trustStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
        trustStore.load(new FileInputStream(trustStoreFile), keyStorePassword);
        tmFactory.init(trustStore);
        kmFactory.init(keyStore, keyStorePassword);
        sslContext.init(kmFactory.getKeyManagers(), tmFactory.getTrustManagers(), secureRandom);
        this.sslServerSocketFactory = sslContext.getServerSocketFactory();
        this.sslSocketFactory = sslContext.getSocketFactory();
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
        return new DatagramSocket(port, laddr);
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
