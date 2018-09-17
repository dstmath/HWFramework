package javax.net.ssl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public abstract class HttpsURLConnection extends HttpURLConnection {
    private static SSLSocketFactory defaultSSLSocketFactory = null;
    protected HostnameVerifier hostnameVerifier;
    private SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();

    private static class NoPreloadHolder {
        public static HostnameVerifier defaultHostnameVerifier;
        public static final Class<? extends HostnameVerifier> originalDefaultHostnameVerifierClass;

        private NoPreloadHolder() {
        }

        static {
            try {
                defaultHostnameVerifier = (HostnameVerifier) Class.forName("com.android.okhttp.internal.tls.OkHostnameVerifier").getField("INSTANCE").get(null);
                originalDefaultHostnameVerifierClass = defaultHostnameVerifier.getClass();
            } catch (Exception e) {
                throw new AssertionError("Failed to obtain okhttp HostnameVerifier", e);
            }
        }
    }

    public abstract String getCipherSuite();

    public abstract Certificate[] getLocalCertificates();

    public abstract Certificate[] getServerCertificates() throws SSLPeerUnverifiedException;

    protected HttpsURLConnection(URL url) {
        super(url);
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return ((X509Certificate) getServerCertificates()[0]).getSubjectX500Principal();
    }

    public Principal getLocalPrincipal() {
        Certificate[] certs = getLocalCertificates();
        if (certs != null) {
            return ((X509Certificate) certs[0]).getSubjectX500Principal();
        }
        return null;
    }

    public static void setDefaultHostnameVerifier(HostnameVerifier v) {
        if (v == null) {
            throw new IllegalArgumentException("no default HostnameVerifier specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("setHostnameVerifier"));
        }
        NoPreloadHolder.defaultHostnameVerifier = v;
    }

    public static HostnameVerifier getDefaultHostnameVerifier() {
        return NoPreloadHolder.defaultHostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier v) {
        if (v == null) {
            throw new IllegalArgumentException("no HostnameVerifier specified");
        }
        this.hostnameVerifier = v;
    }

    public HostnameVerifier getHostnameVerifier() {
        if (this.hostnameVerifier == null) {
            this.hostnameVerifier = NoPreloadHolder.defaultHostnameVerifier;
        }
        return this.hostnameVerifier;
    }

    public static void setDefaultSSLSocketFactory(SSLSocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException("no default SSLSocketFactory specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSetFactory();
        }
        defaultSSLSocketFactory = sf;
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        if (defaultSSLSocketFactory == null) {
            defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
        return defaultSSLSocketFactory;
    }

    public void setSSLSocketFactory(SSLSocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException("no SSLSocketFactory specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSetFactory();
        }
        this.sslSocketFactory = sf;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return this.sslSocketFactory;
    }
}
