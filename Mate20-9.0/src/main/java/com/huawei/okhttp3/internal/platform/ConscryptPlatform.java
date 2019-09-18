package com.huawei.okhttp3.internal.platform;

import com.android.org.conscrypt.Conscrypt;
import com.android.org.conscrypt.OpenSSLProvider;
import com.huawei.okhttp3.Protocol;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class ConscryptPlatform extends Platform {
    private ConscryptPlatform() {
    }

    private Provider getProvider() {
        return new OpenSSLProvider();
    }

    @Nullable
    public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        if (!Conscrypt.isConscrypt(sslSocketFactory)) {
            return super.trustManager(sslSocketFactory);
        }
        try {
            Object sp = readFieldOrNull(sslSocketFactory, Object.class, "sslParameters");
            if (sp != null) {
                return (X509TrustManager) readFieldOrNull(sp, X509TrustManager.class, "x509TrustManager");
            }
            return null;
        } catch (Exception e) {
            throw new UnsupportedOperationException("clientBuilder.sslSocketFactory(SSLSocketFactory) not supported on Conscrypt", e);
        }
    }

    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        if (Conscrypt.isConscrypt(sslSocket)) {
            if (hostname != null) {
                Conscrypt.setUseSessionTickets(sslSocket, true);
                Conscrypt.setHostname(sslSocket, hostname);
            }
            Conscrypt.setApplicationProtocols(sslSocket, (String[]) Platform.alpnProtocolNames(protocols).toArray(new String[0]));
            return;
        }
        super.configureTlsExtensions(sslSocket, hostname, protocols);
    }

    @Nullable
    public String getSelectedProtocol(SSLSocket sslSocket) {
        if (Conscrypt.isConscrypt(sslSocket)) {
            return Conscrypt.getApplicationProtocol(sslSocket);
        }
        return super.getSelectedProtocol(sslSocket);
    }

    public SSLContext getSSLContext() {
        try {
            return SSLContext.getInstance("TLS", getProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No TLS provider", e);
        }
    }

    public static Platform buildIfSupported() {
        try {
            Class.forName("org.conscrypt.ConscryptEngineSocket");
            if (!Conscrypt.isAvailable()) {
                return null;
            }
            return new ConscryptPlatform();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void configureSslSocketFactory(SSLSocketFactory socketFactory) {
        if (Conscrypt.isConscrypt(socketFactory)) {
            Conscrypt.setUseEngineSocket(socketFactory, true);
        }
    }
}
