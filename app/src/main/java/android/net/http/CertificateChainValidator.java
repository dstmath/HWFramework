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
        private static final CertificateChainValidator sInstance = null;
        private static final HostnameVerifier sVerifier = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.http.CertificateChainValidator.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.http.CertificateChainValidator.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.http.CertificateChainValidator.NoPreloadHolder.<clinit>():void");
        }

        private NoPreloadHolder() {
        }
    }

    /* synthetic */ CertificateChainValidator(CertificateChainValidator certificateChainValidator) {
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

    private static SslError verifyServerDomainAndCertificates(X509Certificate[] chain, String domain, String authType) throws IOException {
        X509Certificate currCertificate = chain[0];
        if (currCertificate == null) {
            throw new IllegalArgumentException("certificate for this site is null");
        }
        boolean valid;
        if (domain == null || domain.isEmpty()) {
            valid = false;
        } else {
            valid = NoPreloadHolder.sVerifier.verify(domain, new CertificateWrap(currCertificate));
        }
        if (!valid) {
            return new SslError(2, currCertificate);
        }
        X509TrustManager x509TrustManager;
        try {
            x509TrustManager = SSLParametersImpl.getDefaultX509TrustManager();
            x509TrustManager.getClass().getMethod("checkServerTrusted", new Class[]{X509Certificate[].class, String.class, String.class}).invoke(x509TrustManager, new Object[]{chain, authType, domain});
        } catch (NoSuchMethodException e) {
            x509TrustManager.checkServerTrusted(chain, authType);
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
