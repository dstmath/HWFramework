package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import javax.net.ssl.SSLServerSocketFactory;

public class OpenSSLServerSocketFactoryImpl extends SSLServerSocketFactory {
    private IOException instantiationException;
    private SSLParametersImpl sslParameters;

    public OpenSSLServerSocketFactoryImpl() {
        try {
            this.sslParameters = SSLParametersImpl.getDefault();
            this.sslParameters.setUseClientMode(false);
        } catch (KeyManagementException e) {
            this.instantiationException = new IOException("Delayed instantiation exception:");
            this.instantiationException.initCause(e);
        }
    }

    public OpenSSLServerSocketFactoryImpl(SSLParametersImpl sslParameters) {
        this.sslParameters = (SSLParametersImpl) sslParameters.clone();
        this.sslParameters.setUseClientMode(false);
    }

    public String[] getDefaultCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public ServerSocket createServerSocket() throws IOException {
        return new OpenSSLServerSocketImpl((SSLParametersImpl) this.sslParameters.clone());
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new OpenSSLServerSocketImpl(port, (SSLParametersImpl) this.sslParameters.clone());
    }

    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return new OpenSSLServerSocketImpl(port, backlog, (SSLParametersImpl) this.sslParameters.clone());
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress iAddress) throws IOException {
        return new OpenSSLServerSocketImpl(port, backlog, iAddress, (SSLParametersImpl) this.sslParameters.clone());
    }
}
