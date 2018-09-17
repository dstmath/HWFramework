package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public abstract class SSLServerSocket extends ServerSocket {
    public abstract boolean getEnableSessionCreation();

    public abstract String[] getEnabledCipherSuites();

    public abstract String[] getEnabledProtocols();

    public abstract boolean getNeedClientAuth();

    public abstract String[] getSupportedCipherSuites();

    public abstract String[] getSupportedProtocols();

    public abstract boolean getUseClientMode();

    public abstract boolean getWantClientAuth();

    public abstract void setEnableSessionCreation(boolean z);

    public abstract void setEnabledCipherSuites(String[] strArr);

    public abstract void setEnabledProtocols(String[] strArr);

    public abstract void setNeedClientAuth(boolean z);

    public abstract void setUseClientMode(boolean z);

    public abstract void setWantClientAuth(boolean z);

    protected SSLServerSocket() throws IOException {
    }

    protected SSLServerSocket(int port) throws IOException {
        super(port);
    }

    protected SSLServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    protected SSLServerSocket(int port, int backlog, InetAddress address) throws IOException {
        super(port, backlog, address);
    }

    public SSLParameters getSSLParameters() {
        SSLParameters parameters = new SSLParameters();
        parameters.setCipherSuites(getEnabledCipherSuites());
        parameters.setProtocols(getEnabledProtocols());
        if (getNeedClientAuth()) {
            parameters.setNeedClientAuth(true);
        } else if (getWantClientAuth()) {
            parameters.setWantClientAuth(true);
        }
        return parameters;
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
