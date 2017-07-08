package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/* compiled from: SSLSocketFactory */
class DefaultSSLSocketFactory extends SSLSocketFactory {
    private Exception reason;

    DefaultSSLSocketFactory(Exception reason) {
        this.reason = reason;
    }

    private Socket throwException() throws SocketException {
        throw ((SocketException) new SocketException(this.reason.toString()).initCause(this.reason));
    }

    public Socket createSocket() throws IOException {
        return throwException();
    }

    public Socket createSocket(String host, int port) throws IOException {
        return throwException();
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return throwException();
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return throwException();
    }

    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return throwException();
    }

    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return throwException();
    }

    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    public String[] getSupportedCipherSuites() {
        return new String[0];
    }
}
