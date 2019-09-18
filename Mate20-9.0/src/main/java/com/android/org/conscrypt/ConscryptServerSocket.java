package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLServerSocket;

final class ConscryptServerSocket extends SSLServerSocket {
    private boolean channelIdEnabled;
    private final SSLParametersImpl sslParameters;
    private boolean useEngineSocket;

    ConscryptServerSocket(SSLParametersImpl sslParameters2) throws IOException {
        this.sslParameters = sslParameters2;
    }

    ConscryptServerSocket(int port, SSLParametersImpl sslParameters2) throws IOException {
        super(port);
        this.sslParameters = sslParameters2;
    }

    ConscryptServerSocket(int port, int backlog, SSLParametersImpl sslParameters2) throws IOException {
        super(port, backlog);
        this.sslParameters = sslParameters2;
    }

    ConscryptServerSocket(int port, int backlog, InetAddress iAddress, SSLParametersImpl sslParameters2) throws IOException {
        super(port, backlog, iAddress);
        this.sslParameters = sslParameters2;
    }

    /* access modifiers changed from: package-private */
    public ConscryptServerSocket setUseEngineSocket(boolean useEngineSocket2) {
        this.useEngineSocket = useEngineSocket2;
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

    /* access modifiers changed from: package-private */
    public void setChannelIdEnabled(boolean enabled) {
        this.channelIdEnabled = enabled;
    }

    /* access modifiers changed from: package-private */
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
        ConscryptSocketBase socket;
        if (this.useEngineSocket) {
            socket = Platform.createEngineSocket(this.sslParameters);
        } else {
            socket = Platform.createFileDescriptorSocket(this.sslParameters);
        }
        socket.setChannelIdEnabled(this.channelIdEnabled);
        implAccept(socket);
        return socket;
    }
}
