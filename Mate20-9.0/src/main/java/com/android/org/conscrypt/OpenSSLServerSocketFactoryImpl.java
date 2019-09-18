package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import javax.net.ssl.SSLServerSocketFactory;

final class OpenSSLServerSocketFactoryImpl extends SSLServerSocketFactory {
    private static boolean useEngineSocketByDefault = SSLUtils.USE_ENGINE_SOCKET_BY_DEFAULT;
    private IOException instantiationException;
    private SSLParametersImpl sslParameters;
    private boolean useEngineSocket = useEngineSocketByDefault;

    OpenSSLServerSocketFactoryImpl() {
        try {
            this.sslParameters = SSLParametersImpl.getDefault();
            this.sslParameters.setUseClientMode(false);
        } catch (KeyManagementException e) {
            this.instantiationException = new IOException("Delayed instantiation exception:");
            this.instantiationException.initCause(e);
        }
    }

    OpenSSLServerSocketFactoryImpl(SSLParametersImpl sslParameters2) {
        this.sslParameters = (SSLParametersImpl) sslParameters2.clone();
        this.sslParameters.setUseClientMode(false);
    }

    static void setUseEngineSocketByDefault(boolean useEngineSocket2) {
        useEngineSocketByDefault = useEngineSocket2;
    }

    /* access modifiers changed from: package-private */
    public void setUseEngineSocket(boolean useEngineSocket2) {
        this.useEngineSocket = useEngineSocket2;
    }

    public String[] getDefaultCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public ServerSocket createServerSocket() throws IOException {
        return new ConscryptServerSocket((SSLParametersImpl) this.sslParameters.clone()).setUseEngineSocket(this.useEngineSocket);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ConscryptServerSocket(port, (SSLParametersImpl) this.sslParameters.clone()).setUseEngineSocket(this.useEngineSocket);
    }

    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return new ConscryptServerSocket(port, backlog, (SSLParametersImpl) this.sslParameters.clone()).setUseEngineSocket(this.useEngineSocket);
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress iAddress) throws IOException {
        return new ConscryptServerSocket(port, backlog, iAddress, (SSLParametersImpl) this.sslParameters.clone()).setUseEngineSocket(this.useEngineSocket);
    }
}
