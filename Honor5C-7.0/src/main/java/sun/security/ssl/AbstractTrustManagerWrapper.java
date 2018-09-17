package sun.security.ssl;

import java.net.Socket;
import java.security.AlgorithmConstraints;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import sun.security.provider.certpath.AlgorithmChecker;

/* compiled from: SSLContextImpl */
final class AbstractTrustManagerWrapper extends X509ExtendedTrustManager implements X509TrustManager {
    private final X509TrustManager tm;

    AbstractTrustManagerWrapper(X509TrustManager tm) {
        this.tm = tm;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.tm.checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.tm.checkServerTrusted(chain, authType);
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.tm.getAcceptedIssuers();
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        this.tm.checkClientTrusted(chain, authType);
        checkAdditionalTrust(chain, authType, socket, true);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        this.tm.checkServerTrusted(chain, authType);
        checkAdditionalTrust(chain, authType, socket, false);
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        this.tm.checkClientTrusted(chain, authType);
        checkAdditionalTrust(chain, authType, engine, true);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        this.tm.checkServerTrusted(chain, authType);
        checkAdditionalTrust(chain, authType, engine, false);
    }

    private void checkAdditionalTrust(X509Certificate[] chain, String authType, Socket socket, boolean isClient) throws CertificateException {
        if (socket != null && socket.isConnected() && (socket instanceof SSLSocket)) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLSession session = sslSocket.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            AlgorithmConstraints constraints;
            String identityAlg = sslSocket.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (!(identityAlg == null || identityAlg.length() == 0)) {
                X509TrustManagerImpl.checkIdentity(session.getPeerHost(), chain[0], identityAlg);
            }
            if (ProtocolVersion.valueOf(session.getProtocol()).v < ProtocolVersion.TLS12.v) {
                constraints = new SSLAlgorithmConstraints(sslSocket, true);
            } else if (session instanceof ExtendedSSLSession) {
                constraints = new SSLAlgorithmConstraints(sslSocket, ((ExtendedSSLSession) session).getLocalSupportedSignatureAlgorithms(), true);
            } else {
                constraints = new SSLAlgorithmConstraints(sslSocket, true);
            }
            checkAlgorithmConstraints(chain, constraints);
        }
    }

    private void checkAdditionalTrust(X509Certificate[] chain, String authType, SSLEngine engine, boolean isClient) throws CertificateException {
        if (engine != null) {
            SSLSession session = engine.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            AlgorithmConstraints constraints;
            String identityAlg = engine.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (!(identityAlg == null || identityAlg.length() == 0)) {
                X509TrustManagerImpl.checkIdentity(session.getPeerHost(), chain[0], identityAlg);
            }
            if (ProtocolVersion.valueOf(session.getProtocol()).v < ProtocolVersion.TLS12.v) {
                constraints = new SSLAlgorithmConstraints(engine, true);
            } else if (session instanceof ExtendedSSLSession) {
                constraints = new SSLAlgorithmConstraints(engine, ((ExtendedSSLSession) session).getLocalSupportedSignatureAlgorithms(), true);
            } else {
                constraints = new SSLAlgorithmConstraints(engine, true);
            }
            checkAlgorithmConstraints(chain, constraints);
        }
    }

    private void checkAlgorithmConstraints(X509Certificate[] chain, AlgorithmConstraints constraints) throws CertificateException {
        try {
            int checkedLength = chain.length - 1;
            Collection<X509Certificate> trustedCerts = new HashSet();
            X509Certificate[] certs = this.tm.getAcceptedIssuers();
            if (certs != null && certs.length > 0) {
                Collections.addAll(trustedCerts, certs);
            }
            if (trustedCerts.contains(chain[checkedLength])) {
                checkedLength--;
            }
            if (checkedLength >= 0) {
                AlgorithmChecker checker = new AlgorithmChecker(constraints);
                checker.init(false);
                for (int i = checkedLength; i >= 0; i--) {
                    checker.check(chain[i], Collections.emptySet());
                }
            }
        } catch (CertPathValidatorException e) {
            throw new CertificateException("Certificates does not conform to algorithm constraints");
        }
    }
}
