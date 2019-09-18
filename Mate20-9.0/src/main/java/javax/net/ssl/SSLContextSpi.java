package javax.net.ssl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.SecureRandom;

public abstract class SSLContextSpi {
    /* access modifiers changed from: protected */
    public abstract SSLEngine engineCreateSSLEngine();

    /* access modifiers changed from: protected */
    public abstract SSLEngine engineCreateSSLEngine(String str, int i);

    /* access modifiers changed from: protected */
    public abstract SSLSessionContext engineGetClientSessionContext();

    /* access modifiers changed from: protected */
    public abstract SSLSessionContext engineGetServerSessionContext();

    /* access modifiers changed from: protected */
    public abstract SSLServerSocketFactory engineGetServerSocketFactory();

    /* access modifiers changed from: protected */
    public abstract SSLSocketFactory engineGetSocketFactory();

    /* access modifiers changed from: protected */
    public abstract void engineInit(KeyManager[] keyManagerArr, TrustManager[] trustManagerArr, SecureRandom secureRandom) throws KeyManagementException;

    private SSLSocket getDefaultSocket() {
        try {
            return (SSLSocket) engineGetSocketFactory().createSocket();
        } catch (IOException e) {
            throw new UnsupportedOperationException("Could not obtain parameters", e);
        }
    }

    /* access modifiers changed from: protected */
    public SSLParameters engineGetDefaultSSLParameters() {
        return getDefaultSocket().getSSLParameters();
    }

    /* access modifiers changed from: protected */
    public SSLParameters engineGetSupportedSSLParameters() {
        SSLSocket socket = getDefaultSocket();
        SSLParameters params = new SSLParameters();
        params.setCipherSuites(socket.getSupportedCipherSuites());
        params.setProtocols(socket.getSupportedProtocols());
        return params;
    }
}
