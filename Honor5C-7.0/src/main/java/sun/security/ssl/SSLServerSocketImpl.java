package sun.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.AlgorithmConstraints;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;

final class SSLServerSocketImpl extends SSLServerSocket {
    private AlgorithmConstraints algorithmConstraints;
    private boolean checkedEnabled;
    private byte doClientAuth;
    private boolean enableSessionCreation;
    private CipherSuiteList enabledCipherSuites;
    private ProtocolList enabledProtocols;
    private String identificationProtocol;
    private SSLContextImpl sslContext;
    private boolean useServerMode;

    SSLServerSocketImpl(int port, int backlog, SSLContextImpl context) throws IOException, SSLException {
        super(port, backlog);
        this.doClientAuth = (byte) 0;
        this.useServerMode = true;
        this.enableSessionCreation = true;
        this.enabledCipherSuites = null;
        this.enabledProtocols = null;
        this.checkedEnabled = false;
        this.identificationProtocol = null;
        this.algorithmConstraints = null;
        initServer(context);
    }

    SSLServerSocketImpl(int port, int backlog, InetAddress address, SSLContextImpl context) throws IOException {
        super(port, backlog, address);
        this.doClientAuth = (byte) 0;
        this.useServerMode = true;
        this.enableSessionCreation = true;
        this.enabledCipherSuites = null;
        this.enabledProtocols = null;
        this.checkedEnabled = false;
        this.identificationProtocol = null;
        this.algorithmConstraints = null;
        initServer(context);
    }

    SSLServerSocketImpl(SSLContextImpl context) throws IOException {
        this.doClientAuth = (byte) 0;
        this.useServerMode = true;
        this.enableSessionCreation = true;
        this.enabledCipherSuites = null;
        this.enabledProtocols = null;
        this.checkedEnabled = false;
        this.identificationProtocol = null;
        this.algorithmConstraints = null;
        initServer(context);
    }

    private void initServer(SSLContextImpl context) throws SSLException {
        if (context == null) {
            throw new SSLException("No Authentication context given");
        }
        this.sslContext = context;
        this.enabledCipherSuites = this.sslContext.getDefaultCipherSuiteList(true);
        this.enabledProtocols = this.sslContext.getDefaultProtocolList(true);
    }

    public String[] getSupportedCipherSuites() {
        return this.sslContext.getSupportedCipherSuiteList().toStringArray();
    }

    public synchronized String[] getEnabledCipherSuites() {
        return this.enabledCipherSuites.toStringArray();
    }

    public synchronized void setEnabledCipherSuites(String[] suites) {
        this.enabledCipherSuites = new CipherSuiteList(suites);
        this.checkedEnabled = false;
    }

    public String[] getSupportedProtocols() {
        return this.sslContext.getSuportedProtocolList().toStringArray();
    }

    public synchronized void setEnabledProtocols(String[] protocols) {
        this.enabledProtocols = new ProtocolList(protocols);
    }

    public synchronized String[] getEnabledProtocols() {
        return this.enabledProtocols.toStringArray();
    }

    public void setNeedClientAuth(boolean flag) {
        this.doClientAuth = flag ? (byte) 2 : (byte) 0;
    }

    public boolean getNeedClientAuth() {
        return this.doClientAuth == 2;
    }

    public void setWantClientAuth(boolean flag) {
        this.doClientAuth = flag ? (byte) 1 : (byte) 0;
    }

    public boolean getWantClientAuth() {
        return this.doClientAuth == (byte) 1;
    }

    public void setUseClientMode(boolean flag) {
        boolean z;
        boolean z2 = false;
        boolean z3 = this.useServerMode;
        if (flag) {
            z = false;
        } else {
            z = true;
        }
        if (z3 != z && this.sslContext.isDefaultProtocolList(this.enabledProtocols)) {
            SSLContextImpl sSLContextImpl = this.sslContext;
            if (flag) {
                z = false;
            } else {
                z = true;
            }
            this.enabledProtocols = sSLContextImpl.getDefaultProtocolList(z);
        }
        if (!flag) {
            z2 = true;
        }
        this.useServerMode = z2;
    }

    public boolean getUseClientMode() {
        return !this.useServerMode;
    }

    public void setEnableSessionCreation(boolean flag) {
        this.enableSessionCreation = flag;
    }

    public boolean getEnableSessionCreation() {
        return this.enableSessionCreation;
    }

    public synchronized SSLParameters getSSLParameters() {
        SSLParameters params;
        params = super.getSSLParameters();
        params.setEndpointIdentificationAlgorithm(this.identificationProtocol);
        params.setAlgorithmConstraints(this.algorithmConstraints);
        return params;
    }

    public synchronized void setSSLParameters(SSLParameters params) {
        super.setSSLParameters(params);
        this.identificationProtocol = params.getEndpointIdentificationAlgorithm();
        this.algorithmConstraints = params.getAlgorithmConstraints();
    }

    public Socket accept() throws IOException {
        SSLSocketImpl s = new SSLSocketImpl(this.sslContext, this.useServerMode, this.enabledCipherSuites, this.doClientAuth, this.enableSessionCreation, this.enabledProtocols, this.identificationProtocol, this.algorithmConstraints);
        implAccept(s);
        s.doneConnect();
        return s;
    }

    public String toString() {
        return "[SSL: " + super.toString() + "]";
    }
}
