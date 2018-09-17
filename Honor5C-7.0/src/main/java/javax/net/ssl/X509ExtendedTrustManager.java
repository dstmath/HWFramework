package javax.net.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public abstract class X509ExtendedTrustManager implements X509TrustManager {
    public abstract void checkClientTrusted(X509Certificate[] x509CertificateArr, String str, Socket socket) throws CertificateException;

    public abstract void checkClientTrusted(X509Certificate[] x509CertificateArr, String str, SSLEngine sSLEngine) throws CertificateException;

    public abstract void checkServerTrusted(X509Certificate[] x509CertificateArr, String str, Socket socket) throws CertificateException;

    public abstract void checkServerTrusted(X509Certificate[] x509CertificateArr, String str, SSLEngine sSLEngine) throws CertificateException;
}
