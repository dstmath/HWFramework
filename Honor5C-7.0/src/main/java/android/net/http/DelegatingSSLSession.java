package android.net.http;

import java.security.Principal;
import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;

public class DelegatingSSLSession implements SSLSession {

    public static class CertificateWrap extends DelegatingSSLSession {
        private final Certificate mCertificate;

        public CertificateWrap(Certificate certificate) {
            this.mCertificate = certificate;
        }

        public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
            return new Certificate[]{this.mCertificate};
        }
    }

    protected DelegatingSSLSession() {
    }

    public int getApplicationBufferSize() {
        throw new UnsupportedOperationException();
    }

    public String getCipherSuite() {
        throw new UnsupportedOperationException();
    }

    public long getCreationTime() {
        throw new UnsupportedOperationException();
    }

    public byte[] getId() {
        throw new UnsupportedOperationException();
    }

    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }

    public Certificate[] getLocalCertificates() {
        throw new UnsupportedOperationException();
    }

    public Principal getLocalPrincipal() {
        throw new UnsupportedOperationException();
    }

    public int getPacketBufferSize() {
        throw new UnsupportedOperationException();
    }

    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        throw new UnsupportedOperationException();
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        throw new UnsupportedOperationException();
    }

    public String getPeerHost() {
        throw new UnsupportedOperationException();
    }

    public int getPeerPort() {
        throw new UnsupportedOperationException();
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        throw new UnsupportedOperationException();
    }

    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    public SSLSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }

    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }

    public void invalidate() {
        throw new UnsupportedOperationException();
    }

    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }
}
