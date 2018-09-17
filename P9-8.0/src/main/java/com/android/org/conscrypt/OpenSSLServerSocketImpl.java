package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLServerSocket;

public class OpenSSLServerSocketImpl extends SSLServerSocket {
    private boolean channelIdEnabled;
    private final SSLParametersImpl sslParameters;
    private boolean useEngineSocket;

    protected OpenSSLServerSocketImpl(SSLParametersImpl sslParameters) throws IOException {
        this.sslParameters = sslParameters;
    }

    protected OpenSSLServerSocketImpl(int port, SSLParametersImpl sslParameters) throws IOException {
        super(port);
        this.sslParameters = sslParameters;
    }

    protected OpenSSLServerSocketImpl(int port, int backlog, SSLParametersImpl sslParameters) throws IOException {
        super(port, backlog);
        this.sslParameters = sslParameters;
    }

    protected OpenSSLServerSocketImpl(int port, int backlog, InetAddress iAddress, SSLParametersImpl sslParameters) throws IOException {
        super(port, backlog, iAddress);
        this.sslParameters = sslParameters;
    }

    public OpenSSLServerSocketImpl setUseEngineSocket(boolean useEngineSocket) {
        this.useEngineSocket = useEngineSocket;
        return this;
    }

    public boolean getEnableSessionCreation() {
        return this.sslParameters.getEnableSessionCreation();
    }

    public void setEnableSessionCreation(boolean flag) {
        this.sslParameters.setEnableSessionCreation(flag);
    }

    public String[] getSupportedProtocols() {
        return NativeCrypto.getSupportedProtocols();
    }

    public String[] getEnabledProtocols() {
        return this.sslParameters.getEnabledProtocols();
    }

    public void setEnabledProtocols(String[] protocols) {
        this.sslParameters.setEnabledProtocols(protocols);
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public String[] getEnabledCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public void setChannelIdEnabled(boolean enabled) {
        this.channelIdEnabled = enabled;
    }

    public boolean isChannelIdEnabled() {
        return this.channelIdEnabled;
    }

    public void setEnabledCipherSuites(String[] suites) {
        this.sslParameters.setEnabledCipherSuites(suites);
    }

    public boolean getWantClientAuth() {
        return this.sslParameters.getWantClientAuth();
    }

    public void setWantClientAuth(boolean want) {
        this.sslParameters.setWantClientAuth(want);
    }

    public boolean getNeedClientAuth() {
        return this.sslParameters.getNeedClientAuth();
    }

    public void setNeedClientAuth(boolean need) {
        this.sslParameters.setNeedClientAuth(need);
    }

    public void setUseClientMode(boolean mode) {
        this.sslParameters.setUseClientMode(mode);
    }

    public boolean getUseClientMode() {
        return this.sslParameters.getUseClientMode();
    }

    public Socket accept() throws IOException {
        if (this.useEngineSocket) {
            Socket rawSocket = new Socket();
            implAccept(rawSocket);
            OpenSSLEngineSocketImpl socket = new OpenSSLEngineSocketImpl(rawSocket, null, -1, true, this.sslParameters);
            socket.setChannelIdEnabled(this.channelIdEnabled);
            socket.startHandshake();
            return socket;
        }
        OpenSSLSocketImpl socket2 = new OpenSSLSocketImpl(this.sslParameters);
        socket2.setChannelIdEnabled(this.channelIdEnabled);
        implAccept(socket2);
        return socket2;
    }
}
