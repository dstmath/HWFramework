package android.security.net.config;

import android.annotation.UnsupportedAppUsage;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;

public class RootTrustManager extends X509ExtendedTrustManager {
    private final ApplicationConfig mConfig;

    public RootTrustManager(ApplicationConfig config) {
        if (config != null) {
            this.mConfig = config;
            return;
        }
        throw new NullPointerException("config must not be null");
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.mConfig.getConfigForHostname("").getTrustManager().checkClientTrusted(chain, authType);
    }

    @Override // javax.net.ssl.X509ExtendedTrustManager
    public void checkClientTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        this.mConfig.getConfigForHostname("").getTrustManager().checkClientTrusted(certs, authType, socket);
    }

    @Override // javax.net.ssl.X509ExtendedTrustManager
    public void checkClientTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        this.mConfig.getConfigForHostname("").getTrustManager().checkClientTrusted(certs, authType, engine);
    }

    @Override // javax.net.ssl.X509ExtendedTrustManager
    public void checkServerTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        if (socket instanceof SSLSocket) {
            SSLSession session = ((SSLSocket) socket).getHandshakeSession();
            if (session != null) {
                this.mConfig.getConfigForHostname(session.getPeerHost()).getTrustManager().checkServerTrusted(certs, authType, socket);
                return;
            }
            throw new CertificateException("Not in handshake; no session available");
        }
        checkServerTrusted(certs, authType);
    }

    @Override // javax.net.ssl.X509ExtendedTrustManager
    public void checkServerTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        SSLSession session = engine.getHandshakeSession();
        if (session != null) {
            this.mConfig.getConfigForHostname(session.getPeerHost()).getTrustManager().checkServerTrusted(certs, authType, engine);
            return;
        }
        throw new CertificateException("Not in handshake; no session available");
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        if (!this.mConfig.hasPerDomainConfigs()) {
            this.mConfig.getConfigForHostname("").getTrustManager().checkServerTrusted(certs, authType);
            return;
        }
        throw new CertificateException("Domain specific configurations require that hostname aware checkServerTrusted(X509Certificate[], String, String) is used");
    }

    @UnsupportedAppUsage
    public List<X509Certificate> checkServerTrusted(X509Certificate[] certs, String authType, String hostname) throws CertificateException {
        if (hostname != null || !this.mConfig.hasPerDomainConfigs()) {
            return this.mConfig.getConfigForHostname(hostname).getTrustManager().checkServerTrusted(certs, authType, hostname);
        }
        throw new CertificateException("Domain specific configurations require that the hostname be provided");
    }

    @Override // javax.net.ssl.X509TrustManager
    public X509Certificate[] getAcceptedIssuers() {
        return this.mConfig.getConfigForHostname("").getTrustManager().getAcceptedIssuers();
    }

    public boolean isSameTrustConfiguration(String hostname1, String hostname2) {
        return this.mConfig.getConfigForHostname(hostname1).equals(this.mConfig.getConfigForHostname(hostname2));
    }
}
