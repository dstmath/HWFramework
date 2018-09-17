package android.net.http;

import android.net.http.DelegatingSSLSession.CertificateWrap;
import android.util.Log;
import com.android.org.conscrypt.SSLParametersImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CertificateChainValidator {
    private static final String TAG = "CertificateChainValidator";
    private X509TrustManager mTrustManager;

    private static class NoPreloadHolder {
        private static final CertificateChainValidator sInstance = new CertificateChainValidator();
        private static final HostnameVerifier sVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

        private NoPreloadHolder() {
        }
    }

    /* synthetic */ CertificateChainValidator(CertificateChainValidator -this0) {
        this();
    }

    public static CertificateChainValidator getInstance() {
        return NoPreloadHolder.sInstance;
    }

    private CertificateChainValidator() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X.509");
            tmf.init((KeyStore) null);
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    this.mTrustManager = (X509TrustManager) tm;
                }
            }
            if (this.mTrustManager == null) {
                throw new RuntimeException("None of the X.509 TrustManagers are X509TrustManager");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("X.509 TrustManagerFactory must be available", e);
        } catch (KeyStoreException e2) {
            throw new RuntimeException("X.509 TrustManagerFactory cannot be initialized", e2);
        }
    }

    public SslError doHandshakeAndValidateServerCertificates(HttpsConnection connection, SSLSocket sslSocket, String domain) throws IOException {
        if (!sslSocket.getSession().isValid()) {
            closeSocketThrowException(sslSocket, "failed to perform SSL handshake");
        }
        Certificate[] peerCertificates = sslSocket.getSession().getPeerCertificates();
        if (peerCertificates == null || peerCertificates.length == 0) {
            closeSocketThrowException(sslSocket, "failed to retrieve peer certificates");
        } else if (!(connection == null || peerCertificates[0] == null)) {
            connection.setCertificate(new SslCertificate((X509Certificate) peerCertificates[0]));
        }
        return verifyServerDomainAndCertificates((X509Certificate[]) peerCertificates, domain, "RSA");
    }

    public static SslError verifyServerCertificates(byte[][] certChain, String domain, String authType) throws IOException {
        if (certChain == null || certChain.length == 0) {
            throw new IllegalArgumentException("bad certificate chain");
        }
        X509Certificate[] serverCertificates = new X509Certificate[certChain.length];
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int i = 0; i < certChain.length; i++) {
                serverCertificates[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certChain[i]));
            }
            return verifyServerDomainAndCertificates(serverCertificates, domain, authType);
        } catch (CertificateException e) {
            throw new IOException("can't read certificate", e);
        }
    }

    public static void handleTrustStorageUpdate() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X.509");
            tmf.init((KeyStore) null);
            boolean sentUpdate = false;
            for (TrustManager tm : tmf.getTrustManagers()) {
                try {
                    Method updateMethod = tm.getClass().getDeclaredMethod("handleTrustStorageUpdate", new Class[0]);
                    updateMethod.setAccessible(true);
                    updateMethod.invoke(tm, new Object[0]);
                    sentUpdate = true;
                } catch (Exception e) {
                }
            }
            if (!sentUpdate) {
                Log.w(TAG, "Didn't find a TrustManager to handle CA list update");
            }
        } catch (NoSuchAlgorithmException e2) {
            Log.w(TAG, "Couldn't find default X.509 TrustManagerFactory");
        } catch (KeyStoreException e3) {
            Log.w(TAG, "Couldn't initialize default X.509 TrustManagerFactory", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0084 A:{Splitter: B:14:0x0034, ExcHandler: java.lang.NoSuchMethodException (e java.lang.NoSuchMethodException), Catch:{ NoSuchMethodException -> 0x0084, NoSuchMethodException -> 0x0084, InvocationTargetException -> 0x0062, GeneralSecurityException -> 0x0072 }} */
    /* JADX WARNING: Missing block: B:31:0x0085, code:
            r6.checkServerTrusted(r12, r14);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static SslError verifyServerDomainAndCertificates(X509Certificate[] chain, String domain, String authType) throws IOException {
        X509Certificate currCertificate = chain[0];
        if (currCertificate == null) {
            throw new IllegalArgumentException("certificate for this site is null");
        }
        boolean valid;
        if (domain == null || (domain.isEmpty() ^ 1) == 0) {
            valid = false;
        } else {
            valid = NoPreloadHolder.sVerifier.verify(domain, new CertificateWrap(currCertificate));
        }
        if (!valid) {
            return new SslError(2, currCertificate);
        }
        try {
            X509TrustManager x509TrustManager = SSLParametersImpl.getDefaultX509TrustManager();
            x509TrustManager.getClass().getMethod("checkServerTrusted", new Class[]{X509Certificate[].class, String.class, String.class}).invoke(x509TrustManager, new Object[]{chain, authType, domain});
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e2) {
            if (e2.getCause() instanceof CertificateException) {
                throw ((CertificateException) e2.getCause());
            }
            throw new RuntimeException(e2.getCause());
        } catch (GeneralSecurityException e3) {
            return new SslError(3, currCertificate);
        }
        return null;
    }

    private X509TrustManager getTrustManager() {
        return this.mTrustManager;
    }

    private void closeSocketThrowException(SSLSocket socket, String errorMessage, String defaultErrorMessage) throws IOException {
        if (errorMessage == null) {
            errorMessage = defaultErrorMessage;
        }
        closeSocketThrowException(socket, errorMessage);
    }

    private void closeSocketThrowException(SSLSocket socket, String errorMessage) throws IOException {
        if (socket != null) {
            SSLSession session = socket.getSession();
            if (session != null) {
                session.invalidate();
            }
            socket.close();
        }
        throw new SSLHandshakeException(errorMessage);
    }
}
