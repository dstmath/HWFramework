package android.security.net.config;

import android.util.LogException;
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
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }
        this.mConfig = config;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.mConfig.getConfigForHostname(LogException.NO_VALUE).getTrustManager().checkClientTrusted(chain, authType);
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        this.mConfig.getConfigForHostname(LogException.NO_VALUE).getTrustManager().checkClientTrusted(certs, authType, socket);
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        this.mConfig.getConfigForHostname(LogException.NO_VALUE).getTrustManager().checkClientTrusted(certs, authType, engine);
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        if (socket instanceof SSLSocket) {
            SSLSession session = ((SSLSocket) socket).getHandshakeSession();
            if (session == null) {
                throw new CertificateException("Not in handshake; no session available");
            }
            this.mConfig.getConfigForHostname(session.getPeerHost()).getTrustManager().checkServerTrusted(certs, authType, socket);
            return;
        }
        checkServerTrusted(certs, authType);
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        SSLSession session = engine.getHandshakeSession();
        if (session == null) {
            throw new CertificateException("Not in handshake; no session available");
        }
        this.mConfig.getConfigForHostname(session.getPeerHost()).getTrustManager().checkServerTrusted(certs, authType, engine);
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        if (this.mConfig.hasPerDomainConfigs()) {
            throw new CertificateException("Domain specific configurations require that hostname aware checkServerTrusted(X509Certificate[], String, String) is used");
        }
        this.mConfig.getConfigForHostname(LogException.NO_VALUE).getTrustManager().checkServerTrusted(certs, authType);
    }

    public List<X509Certificate> checkServerTrusted(X509Certificate[] certs, String authType, String hostname) throws CertificateException {
        if (hostname != null || !this.mConfig.hasPerDomainConfigs()) {
            return this.mConfig.getConfigForHostname(hostname).getTrustManager().checkServerTrusted(certs, authType, hostname);
        }
        throw new CertificateException("Domain specific configurations require that the hostname be provided");
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.mConfig.getConfigForHostname(LogException.NO_VALUE).getTrustManager().getAcceptedIssuers();
    }

    public boolean isSameTrustConfiguration(String hostname1, String hostname2) {
        return this.mConfig.getConfigForHostname(hostname1).equals(this.mConfig.getConfigForHostname(hostname2));
    }
}
