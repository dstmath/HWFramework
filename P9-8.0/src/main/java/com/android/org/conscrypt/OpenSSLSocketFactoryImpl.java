package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import javax.net.ssl.SSLSocketFactory;

public class OpenSSLSocketFactoryImpl extends SSLSocketFactory {
    private static boolean useEngineSocketByDefault = SSLUtils.USE_ENGINE_SOCKET_BY_DEFAULT;
    private final IOException instantiationException;
    private final SSLParametersImpl sslParameters;
    private boolean useEngineSocket = useEngineSocketByDefault;

    public OpenSSLSocketFactoryImpl() {
        SSLParametersImpl sslParametersLocal = null;
        IOException instantiationExceptionLocal = null;
        try {
            sslParametersLocal = SSLParametersImpl.getDefault();
        } catch (KeyManagementException e) {
            instantiationExceptionLocal = new IOException("Delayed instantiation exception:");
            instantiationExceptionLocal.initCause(e);
        }
        this.sslParameters = sslParametersLocal;
        this.instantiationException = instantiationExceptionLocal;
    }

    public OpenSSLSocketFactoryImpl(SSLParametersImpl sslParameters) {
        this.sslParameters = sslParameters;
        this.instantiationException = null;
    }

    static void setUseEngineSocketByDefault(boolean useEngineSocket) {
        useEngineSocketByDefault = useEngineSocket;
    }

    public void setUseEngineSocket(boolean useEngineSocket) {
        this.useEngineSocket = useEngineSocket;
    }

    public String[] getDefaultCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public Socket createSocket() throws IOException {
        if (this.instantiationException == null) {
            return new OpenSSLSocketImpl((SSLParametersImpl) this.sslParameters.clone());
        }
        throw this.instantiationException;
    }

    public Socket createSocket(String hostname, int port) throws IOException, UnknownHostException {
        return new OpenSSLSocketImpl(hostname, port, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(String hostname, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return new OpenSSLSocketImpl(hostname, port, localHost, localPort, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new OpenSSLSocketImpl(address, port, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return new OpenSSLSocketImpl(address, port, localAddress, localPort, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(Socket s, String hostname, int port, boolean autoClose) throws IOException {
        boolean socketHasFd = false;
        try {
            socketHasFd = Platform.getFileDescriptor(s) != null;
        } catch (RuntimeException e) {
        }
        if (!socketHasFd || (this.useEngineSocket ^ 1) == 0) {
            return new OpenSSLEngineSocketImpl(s, hostname, port, autoClose, (SSLParametersImpl) this.sslParameters.clone());
        }
        return new OpenSSLSocketImplWrapper(s, hostname, port, autoClose, (SSLParametersImpl) this.sslParameters.clone());
    }
}
