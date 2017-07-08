package com.android.org.conscrypt;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.CertificateException;

public class OpenSSLSessionImpl implements SSLSession {
    private String cipherSuite;
    private long creationTime;
    private byte[] id;
    private boolean isValid;
    long lastAccessedTime;
    final X509Certificate[] localCertificates;
    private volatile javax.security.cert.X509Certificate[] peerCertificateChain;
    final X509Certificate[] peerCertificates;
    private String peerHost;
    private int peerPort;
    private String protocol;
    private AbstractSessionContext sessionContext;
    protected long sslSessionNativePointer;
    private final Map<String, Object> values;

    protected OpenSSLSessionImpl(long sslSessionNativePointer, X509Certificate[] localCertificates, X509Certificate[] peerCertificates, String peerHost, int peerPort, AbstractSessionContext sessionContext) {
        this.creationTime = 0;
        this.lastAccessedTime = 0;
        this.isValid = true;
        this.values = new HashMap();
        this.peerPort = -1;
        this.sslSessionNativePointer = sslSessionNativePointer;
        this.localCertificates = localCertificates;
        this.peerCertificates = peerCertificates;
        this.peerHost = peerHost;
        this.peerPort = peerPort;
        this.sessionContext = sessionContext;
    }

    OpenSSLSessionImpl(byte[] derData, String peerHost, int peerPort, X509Certificate[] peerCertificates, AbstractSessionContext sessionContext) throws IOException {
        this(NativeCrypto.d2i_SSL_SESSION(derData), null, peerCertificates, peerHost, peerPort, sessionContext);
    }

    public byte[] getId() {
        if (this.id == null) {
            resetId();
        }
        return this.id;
    }

    void resetId() {
        this.id = NativeCrypto.SSL_SESSION_session_id(this.sslSessionNativePointer);
    }

    byte[] getEncoded() {
        return NativeCrypto.i2d_SSL_SESSION(this.sslSessionNativePointer);
    }

    public long getCreationTime() {
        if (this.creationTime == 0) {
            this.creationTime = NativeCrypto.SSL_SESSION_get_time(this.sslSessionNativePointer);
        }
        return this.creationTime;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime == 0 ? getCreationTime() : this.lastAccessedTime;
    }

    public int getApplicationBufferSize() {
        return NativeConstants.SSL_OP_NO_TICKET;
    }

    public int getPacketBufferSize() {
        return 18437;
    }

    public Principal getLocalPrincipal() {
        if (this.localCertificates == null || this.localCertificates.length <= 0) {
            return null;
        }
        return this.localCertificates[0].getSubjectX500Principal();
    }

    public Certificate[] getLocalCertificates() {
        return this.localCertificates;
    }

    public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        checkPeerCertificatesPresent();
        javax.security.cert.X509Certificate[] result = this.peerCertificateChain;
        if (result != null) {
            return result;
        }
        result = createPeerCertificateChain();
        this.peerCertificateChain = result;
        return result;
    }

    private javax.security.cert.X509Certificate[] createPeerCertificateChain() throws SSLPeerUnverifiedException {
        SSLPeerUnverifiedException exception;
        try {
            javax.security.cert.X509Certificate[] chain = new javax.security.cert.X509Certificate[this.peerCertificates.length];
            for (int i = 0; i < this.peerCertificates.length; i++) {
                chain[i] = javax.security.cert.X509Certificate.getInstance(this.peerCertificates[i].getEncoded());
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

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        checkPeerCertificatesPresent();
        return this.peerCertificates;
    }

    private void checkPeerCertificatesPresent() throws SSLPeerUnverifiedException {
        if (this.peerCertificates == null || this.peerCertificates.length == 0) {
            throw new SSLPeerUnverifiedException("No peer certificates");
        }
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        checkPeerCertificatesPresent();
        return this.peerCertificates[0].getSubjectX500Principal();
    }

    public String getPeerHost() {
        return this.peerHost;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public String getCipherSuite() {
        if (this.cipherSuite == null) {
            String name = NativeCrypto.SSL_SESSION_cipher(this.sslSessionNativePointer);
            this.cipherSuite = (String) NativeCrypto.OPENSSL_TO_STANDARD_CIPHER_SUITES.get(name);
            if (this.cipherSuite == null) {
                this.cipherSuite = name;
            }
        }
        return this.cipherSuite;
    }

    public String getProtocol() {
        if (this.protocol == null) {
            this.protocol = NativeCrypto.SSL_SESSION_get_version(this.sslSessionNativePointer);
        }
        return this.protocol;
    }

    public SSLSessionContext getSessionContext() {
        return this.sessionContext;
    }

    public boolean isValid() {
        if (!this.isValid) {
            return false;
        }
        SSLSessionContext context = this.sessionContext;
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

    public String getRequestedServerName() {
        return NativeCrypto.get_SSL_SESSION_tlsext_hostname(this.sslSessionNativePointer);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.sslSessionNativePointer != 0) {
                NativeCrypto.SSL_SESSION_free(this.sslSessionNativePointer);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }
}
