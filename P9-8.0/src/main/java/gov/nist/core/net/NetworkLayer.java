package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public interface NetworkLayer {
    DatagramSocket createDatagramSocket() throws SocketException;

    DatagramSocket createDatagramSocket(int i, InetAddress inetAddress) throws SocketException;

    SSLServerSocket createSSLServerSocket(int i, int i2, InetAddress inetAddress) throws IOException;

    SSLSocket createSSLSocket(InetAddress inetAddress, int i) throws IOException;

    SSLSocket createSSLSocket(InetAddress inetAddress, int i, InetAddress inetAddress2) throws IOException;

    ServerSocket createServerSocket(int i, int i2, InetAddress inetAddress) throws IOException;

    Socket createSocket(InetAddress inetAddress, int i) throws IOException;

    Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2) throws IOException;

    Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException;
}
