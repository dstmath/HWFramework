package sun.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import sun.security.ssl.SSLContextImpl.DefaultSSLContext;

public final class SSLServerSocketFactoryImpl extends SSLServerSocketFactory {
    private static final int DEFAULT_BACKLOG = 50;
    private SSLContextImpl context;

    public SSLServerSocketFactoryImpl() throws Exception {
        this.context = DefaultSSLContext.getDefaultImpl();
    }

    SSLServerSocketFactoryImpl(SSLContextImpl context) {
        this.context = context;
    }

    public ServerSocket createServerSocket() throws IOException {
        return new SSLServerSocketImpl(this.context);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new SSLServerSocketImpl(port, DEFAULT_BACKLOG, this.context);
    }

    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return new SSLServerSocketImpl(port, backlog, this.context);
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        return new SSLServerSocketImpl(port, backlog, ifAddress, this.context);
    }

    public String[] getDefaultCipherSuites() {
        return this.context.getDefaultCipherSuiteList(true).toStringArray();
    }

    public String[] getSupportedCipherSuites() {
        return this.context.getSupportedCipherSuiteList().toStringArray();
    }
}
