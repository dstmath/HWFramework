package com.android.org.conscrypt;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

public abstract class AbstractOpenSSLSession implements SSLSession {
    private boolean isValid = true;
    private volatile X509Certificate[] peerCertificateChain;
    private AbstractSessionContext sessionContext;
    private final Map<String, Object> values = new HashMap();

    public abstract String getRequestedServerName();

    public abstract List<byte[]> getStatusResponses();

    public abstract byte[] getTlsSctData();

    protected abstract java.security.cert.X509Certificate[] getX509LocalCertificates();

    protected abstract java.security.cert.X509Certificate[] getX509PeerCertificates() throws SSLPeerUnverifiedException;

    abstract void resetId();

    public abstract void setLastAccessedTime(long j);

    protected AbstractOpenSSLSession(AbstractSessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    private void checkPeerCertificatesPresent() throws SSLPeerUnverifiedException {
        java.security.cert.X509Certificate[] peerCertificates = getX509PeerCertificates();
        if (peerCertificates == null || peerCertificates.length == 0) {
            throw new SSLPeerUnverifiedException("No peer certificates");
        }
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        return getX509PeerCertificates();
    }

    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        checkPeerCertificatesPresent();
        X509Certificate[] result = this.peerCertificateChain;
        if (result != null) {
            return result;
        }
        result = createPeerCertificateChain();
        this.peerCertificateChain = result;
        return result;
    }

    private X509Certificate[] createPeerCertificateChain() throws SSLPeerUnverifiedException {
        SSLPeerUnverifiedException exception;
        java.security.cert.X509Certificate[] peerCertificates = getX509PeerCertificates();
        try {
            X509Certificate[] chain = new X509Certificate[peerCertificates.length];
            for (int i = 0; i < peerCertificates.length; i++) {
                chain[i] = X509Certificate.getInstance(peerCertificates[i].getEncoded());
            }
            return chain;
        } catch (CertificateEncodingException e) {
            exception = new SSLPeerUnverifiedException(e.getMessage());
            exception.initCause(exception);
            throw exception;
        } catch (CertificateException e2) {
            exception = new SSLPeerUnverifiedException(e2.getMessage());
            exception.initCause(exception);
            throw exception;
        }
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        checkPeerCertificatesPresent();
        return getX509PeerCertificates()[0].getSubjectX500Principal();
    }

    public Principal getLocalPrincipal() {
        java.security.cert.X509Certificate[] localCertificates = getX509LocalCertificates();
        if (localCertificates == null || localCertificates.length <= 0) {
            return null;
        }
        return localCertificates[0].getSubjectX500Principal();
    }

    public Certificate[] getLocalCertificates() {
        return getX509LocalCertificates();
    }

    public int getApplicationBufferSize() {
        return 16384;
    }

    public int getPacketBufferSize() {
        return NativeConstants.SSL3_RT_MAX_PACKET_SIZE;
    }

    public Object getValue(String name) {
        if (name != null) {
            return this.values.get(name);
        }
        throw new IllegalArgumentException("name == null");
    }

    public String[] getValueNames() {
        return (String[]) this.values.keySet().toArray(new String[this.values.size()]);
    }

    public void putValue(String name, Object value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("name == null || value == null");
        }
        Object old = this.values.put(name, value);
        if (value instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) value).valueBound(new SSLSessionBindingEvent(this, name));
        }
        if (old instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) old).valueUnbound(new SSLSessionBindingEvent(this, name));
        }
    }

    public void removeValue(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        SSLSessionBindingListener old = this.values.remove(name);
        if (old instanceof SSLSessionBindingListener) {
            old.valueUnbound(new SSLSessionBindingEvent(this, name));
        }
    }

    public SSLSessionContext getSessionContext() {
        return this.sessionContext;
    }

    public boolean isValid() {
        if (!this.isValid) {
            return false;
        }
        SSLSessionContext context = getSessionContext();
        if (context == null) {
            return true;
        }
        int timeoutSeconds = context.getSessionTimeout();
        if (timeoutSeconds == 0) {
            return true;
        }
        long ageSeconds = (System.currentTimeMillis() - getCreationTime()) / 1000;
        if (ageSeconds < ((long) timeoutSeconds) && ageSeconds >= 0) {
            return true;
        }
        this.isValid = false;
        return false;
    }

    public void invalidate() {
        this.isValid = false;
        this.sessionContext = null;
    }
}
