package com.android.org.conscrypt;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public abstract class OpenSSLContextImpl extends SSLContextSpi {
    private static DefaultSSLContextImpl defaultSslContextImpl;
    private final String[] algorithms;
    private final ClientSessionContext clientSessionContext;
    private final ServerSessionContext serverSessionContext;
    SSLParametersImpl sslParameters;

    public static final class TLSv1 extends OpenSSLContextImpl {
        public TLSv1() {
            super(NativeCrypto.TLSV1_PROTOCOLS);
        }
    }

    public static final class TLSv11 extends OpenSSLContextImpl {
        public TLSv11() {
            super(NativeCrypto.TLSV11_PROTOCOLS);
        }
    }

    public static final class TLSv12 extends OpenSSLContextImpl {
        public TLSv12() {
            super(NativeCrypto.TLSV12_PROTOCOLS);
        }
    }

    static OpenSSLContextImpl getPreferred() {
        return new TLSv12();
    }

    OpenSSLContextImpl(String[] algorithms2) {
        this.algorithms = algorithms2;
        this.clientSessionContext = new ClientSessionContext();
        this.serverSessionContext = new ServerSessionContext();
    }

    OpenSSLContextImpl() throws GeneralSecurityException, IOException {
        synchronized (DefaultSSLContextImpl.class) {
            this.algorithms = null;
            if (defaultSslContextImpl == null) {
                this.clientSessionContext = new ClientSessionContext();
                this.serverSessionContext = new ServerSessionContext();
                defaultSslContextImpl = (DefaultSSLContextImpl) this;
            } else {
                this.clientSessionContext = defaultSslContextImpl.engineGetClientSessionContext();
                this.serverSessionContext = defaultSslContextImpl.engineGetServerSessionContext();
            }
            SSLParametersImpl sSLParametersImpl = new SSLParametersImpl(defaultSslContextImpl.getKeyManagers(), defaultSslContextImpl.getTrustManagers(), null, this.clientSessionContext, this.serverSessionContext, this.algorithms);
            this.sslParameters = sSLParametersImpl;
        }
    }

    public void engineInit(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {
        SSLParametersImpl sSLParametersImpl = new SSLParametersImpl(kms, tms, sr, this.clientSessionContext, this.serverSessionContext, this.algorithms);
        this.sslParameters = sSLParametersImpl;
    }

    public SSLSocketFactory engineGetSocketFactory() {
        if (this.sslParameters != null) {
            return Platform.wrapSocketFactoryIfNeeded(new OpenSSLSocketFactoryImpl(this.sslParameters));
        }
        throw new IllegalStateException("SSLContext is not initialized.");
    }

    public SSLServerSocketFactory engineGetServerSocketFactory() {
        if (this.sslParameters != null) {
            return new OpenSSLServerSocketFactoryImpl(this.sslParameters);
        }
        throw new IllegalStateException("SSLContext is not initialized.");
    }

    public SSLEngine engineCreateSSLEngine(String host, int port) {
        if (this.sslParameters != null) {
            SSLParametersImpl p = (SSLParametersImpl) this.sslParameters.clone();
            p.setUseClientMode(false);
            return Platform.wrapEngine(new ConscryptEngine(host, port, p));
        }
        throw new IllegalStateException("SSLContext is not initialized.");
    }

    public SSLEngine engineCreateSSLEngine() {
        if (this.sslParameters != null) {
            SSLParametersImpl p = (SSLParametersImpl) this.sslParameters.clone();
            p.setUseClientMode(false);
            return Platform.wrapEngine(new ConscryptEngine(p));
        }
        throw new IllegalStateException("SSLContext is not initialized.");
    }

    public ServerSessionContext engineGetServerSessionContext() {
        return this.serverSessionContext;
    }

    public ClientSessionContext engineGetClientSessionContext() {
        return this.clientSessionContext;
    }
}
