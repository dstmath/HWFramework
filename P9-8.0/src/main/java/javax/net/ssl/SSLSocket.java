package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class SSLSocket extends Socket {
    public abstract void addHandshakeCompletedListener(HandshakeCompletedListener handshakeCompletedListener);

    public abstract boolean getEnableSessionCreation();

    public abstract String[] getEnabledCipherSuites();

    public abstract String[] getEnabledProtocols();

    public abstract boolean getNeedClientAuth();

    public abstract SSLSession getSession();

    public abstract String[] getSupportedCipherSuites();

    public abstract String[] getSupportedProtocols();

    public abstract boolean getUseClientMode();

    public abstract boolean getWantClientAuth();

    public abstract void removeHandshakeCompletedListener(HandshakeCompletedListener handshakeCompletedListener);

    public abstract void setEnableSessionCreation(boolean z);

    public abstract void setEnabledCipherSuites(String[] strArr);

    public abstract void setEnabledProtocols(String[] strArr);

    public abstract void setNeedClientAuth(boolean z);

    public abstract void setUseClientMode(boolean z);

    public abstract void setWantClientAuth(boolean z);

    public abstract void startHandshake() throws IOException;

    protected SSLSocket() {
    }

    protected SSLSocket(String host, int port) throws IOException, UnknownHostException {
        super(host, port);
    }

    protected SSLSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    protected SSLSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException, UnknownHostException {
        super(host, port, clientAddress, clientPort);
    }

    protected SSLSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(address, port, clientAddress, clientPort);
    }

    public SSLSession getHandshakeSession() {
        throw new UnsupportedOperationException();
    }

    public SSLParameters getSSLParameters() {
        SSLParameters params = new SSLParameters();
        params.setCipherSuites(getEnabledCipherSuites());
        params.setProtocols(getEnabledProtocols());
        if (getNeedClientAuth()) {
            params.setNeedClientAuth(true);
        } else if (getWantClientAuth()) {
            params.setWantClientAuth(true);
        }
        return params;
    }

    public void setSSLParameters(SSLParameters params) {
        String[] s = params.getCipherSuites();
        if (s != null) {
            setEnabledCipherSuites(s);
        }
        s = params.getProtocols();
        if (s != null) {
            setEnabledProtocols(s);
        }
        if (params.getNeedClientAuth()) {
            setNeedClientAuth(true);
        } else if (params.getWantClientAuth()) {
            setWantClientAuth(true);
        } else {
            setWantClientAuth(false);
        }
    }

    public String toString() {
        return "SSL" + super.toString();
    }
}
