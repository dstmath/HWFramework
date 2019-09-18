package org.apache.http.conn.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

@Deprecated
public class SSLSocketFactory implements LayeredSocketFactory {
    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER = new BrowserCompatHostnameVerifier();
    public static final String SSL = "SSL";
    public static final String SSLV2 = "SSLv2";
    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER = new StrictHostnameVerifier();
    public static final String TLS = "TLS";
    private X509HostnameVerifier hostnameVerifier;
    private final HostNameResolver nameResolver;
    private final javax.net.ssl.SSLSocketFactory socketfactory;
    private final SSLContext sslcontext;

    private static class NoPreloadHolder {
        /* access modifiers changed from: private */
        public static final SSLSocketFactory DEFAULT_FACTORY = new SSLSocketFactory();

        private NoPreloadHolder() {
        }
    }

    public static SSLSocketFactory getSocketFactory() {
        return NoPreloadHolder.DEFAULT_FACTORY;
    }

    public SSLSocketFactory(String algorithm, KeyStore keystore, String keystorePassword, KeyStore truststore, SecureRandom random, HostNameResolver nameResolver2) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this.hostnameVerifier = BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        algorithm = algorithm == null ? TLS : algorithm;
        KeyManager[] keymanagers = keystore != null ? createKeyManagers(keystore, keystorePassword) : null;
        TrustManager[] trustmanagers = truststore != null ? createTrustManagers(truststore) : null;
        this.sslcontext = SSLContext.getInstance(algorithm);
        this.sslcontext.init(keymanagers, trustmanagers, random);
        this.socketfactory = this.sslcontext.getSocketFactory();
        this.nameResolver = nameResolver2;
    }

    public SSLSocketFactory(KeyStore keystore, String keystorePassword, KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(TLS, keystore, keystorePassword, truststore, null, null);
    }

    public SSLSocketFactory(KeyStore keystore, String keystorePassword) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(TLS, keystore, keystorePassword, null, null, null);
    }

    public SSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(TLS, null, null, truststore, null, null);
    }

    public SSLSocketFactory(javax.net.ssl.SSLSocketFactory socketfactory2) {
        this.hostnameVerifier = BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        this.sslcontext = null;
        this.socketfactory = socketfactory2;
        this.nameResolver = null;
    }

    private SSLSocketFactory() {
        this.hostnameVerifier = BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        this.sslcontext = null;
        this.socketfactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        this.nameResolver = null;
    }

    private static KeyManager[] createKeyManagers(KeyStore keystore, String password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (keystore != null) {
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, password != null ? password.toCharArray() : null);
            return kmfactory.getKeyManagers();
        }
        throw new IllegalArgumentException("Keystore may not be null");
    }

    private static TrustManager[] createTrustManagers(KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException {
        if (keystore != null) {
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(keystore);
            return tmfactory.getTrustManagers();
        }
        throw new IllegalArgumentException("Keystore may not be null");
    }

    public Socket createSocket() throws IOException {
        return (SSLSocket) this.socketfactory.createSocket();
    }

    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
        InetSocketAddress remoteAddress;
        if (host == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        } else if (params != null) {
            SSLSocket sslsock = (SSLSocket) (sock != null ? sock : createSocket());
            if (localAddress != null || localPort > 0) {
                if (localPort < 0) {
                    localPort = 0;
                }
                sslsock.bind(new InetSocketAddress(localAddress, localPort));
            }
            int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
            int soTimeout = HttpConnectionParams.getSoTimeout(params);
            if (this.nameResolver != null) {
                remoteAddress = new InetSocketAddress(this.nameResolver.resolve(host), port);
            } else {
                remoteAddress = new InetSocketAddress(host, port);
            }
            sslsock.connect(remoteAddress, connTimeout);
            sslsock.setSoTimeout(soTimeout);
            try {
                sslsock.startHandshake();
                this.hostnameVerifier.verify(host, sslsock);
                return sslsock;
            } catch (IOException iox) {
                try {
                    sslsock.close();
                } catch (Exception e) {
                }
                throw iox;
            }
        } else {
            throw new IllegalArgumentException("Parameters may not be null.");
        }
    }

    public boolean isSecure(Socket sock) throws IllegalArgumentException {
        if (sock == null) {
            throw new IllegalArgumentException("Socket may not be null.");
        } else if (!(sock instanceof SSLSocket)) {
            throw new IllegalArgumentException("Socket not created by this factory.");
        } else if (!sock.isClosed()) {
            return true;
        } else {
            throw new IllegalArgumentException("Socket is closed.");
        }
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) this.socketfactory.createSocket(socket, host, port, autoClose);
        sslSocket.startHandshake();
        this.hostnameVerifier.verify(host, sslSocket);
        return sslSocket;
    }

    public void setHostnameVerifier(X509HostnameVerifier hostnameVerifier2) {
        if (hostnameVerifier2 != null) {
            this.hostnameVerifier = hostnameVerifier2;
            return;
        }
        throw new IllegalArgumentException("Hostname verifier may not be null");
    }

    public X509HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }
}
