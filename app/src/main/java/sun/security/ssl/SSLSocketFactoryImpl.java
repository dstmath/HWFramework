package sun.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import sun.security.ssl.SSLContextImpl.DefaultSSLContext;

public final class SSLSocketFactoryImpl extends SSLSocketFactory {
    private static SSLContextImpl defaultContext;
    private SSLContextImpl context;

    public SSLSocketFactoryImpl() throws Exception {
        this.context = DefaultSSLContext.getDefaultImpl();
    }

    SSLSocketFactoryImpl(SSLContextImpl context) {
        this.context = context;
    }

    public Socket createSocket() {
        return new SSLSocketImpl(this.context);
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return new SSLSocketImpl(this.context, host, port);
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return new SSLSocketImpl(this.context, s, host, port, autoClose);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new SSLSocketImpl(this.context, address, port);
    }

    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return new SSLSocketImpl(this.context, host, port, clientAddress, clientPort);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return new SSLSocketImpl(this.context, address, port, clientAddress, clientPort);
    }

    public String[] getDefaultCipherSuites() {
        return this.context.getDefaultCipherSuiteList(false).toStringArray();
    }

    public String[] getSupportedCipherSuites() {
        return this.context.getSupportedCipherSuiteList().toStringArray();
    }
}
