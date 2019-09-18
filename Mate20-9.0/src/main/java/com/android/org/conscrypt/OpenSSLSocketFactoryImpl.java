package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
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
            instantiationExceptionLocal = new IOException("Delayed instantiation exception:", e);
        }
        this.sslParameters = sslParametersLocal;
        this.instantiationException = instantiationExceptionLocal;
    }

    public OpenSSLSocketFactoryImpl(SSLParametersImpl sslParameters2) {
        this.sslParameters = sslParameters2;
        this.instantiationException = null;
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

    public Socket createSocket() throws IOException {
        if (this.instantiationException != null) {
            throw this.instantiationException;
        } else if (this.useEngineSocket) {
            return Platform.createEngineSocket((SSLParametersImpl) this.sslParameters.clone());
        } else {
            return Platform.createFileDescriptorSocket((SSLParametersImpl) this.sslParameters.clone());
        }
    }

    public Socket createSocket(String hostname, int port) throws IOException, UnknownHostException {
        if (this.useEngineSocket) {
            return Platform.createEngineSocket(hostname, port, (SSLParametersImpl) this.sslParameters.clone());
        }
        return Platform.createFileDescriptorSocket(hostname, port, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(String hostname, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        if (this.useEngineSocket) {
            return Platform.createEngineSocket(hostname, port, localHost, localPort, (SSLParametersImpl) this.sslParameters.clone());
        }
        return Platform.createFileDescriptorSocket(hostname, port, localHost, localPort, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        if (this.useEngineSocket) {
            return Platform.createEngineSocket(address, port, (SSLParametersImpl) this.sslParameters.clone());
        }
        return Platform.createFileDescriptorSocket(address, port, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        if (this.useEngineSocket) {
            return Platform.createEngineSocket(address, port, localAddress, localPort, (SSLParametersImpl) this.sslParameters.clone());
        }
        return Platform.createFileDescriptorSocket(address, port, localAddress, localPort, (SSLParametersImpl) this.sslParameters.clone());
    }

    public Socket createSocket(Socket socket, String hostname, int port, boolean autoClose) throws IOException {
        Preconditions.checkNotNull(socket, "socket");
        if (!socket.isConnected()) {
            throw new SocketException("Socket is not connected.");
        } else if (!hasFileDescriptor(socket) || this.useEngineSocket) {
            return Platform.createEngineSocket(socket, hostname, port, autoClose, (SSLParametersImpl) this.sslParameters.clone());
        } else {
            return Platform.createFileDescriptorSocket(socket, hostname, port, autoClose, (SSLParametersImpl) this.sslParameters.clone());
        }
    }

    private boolean hasFileDescriptor(Socket s) {
        try {
            Platform.getFileDescriptor(s);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
