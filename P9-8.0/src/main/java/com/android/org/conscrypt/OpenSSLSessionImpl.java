package com.android.org.conscrypt;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;

public class OpenSSLSessionImpl extends AbstractOpenSSLSession {
    private String cipherSuite;
    private long creationTime;
    private byte[] id;
    long lastAccessedTime;
    final X509Certificate[] localCertificates;
    private byte[] peerCertificateOcspData;
    final X509Certificate[] peerCertificates;
    private String peerHost;
    private int peerPort;
    private byte[] peerTlsSctData;
    private String protocol;
    protected long sslSessionNativePointer;
    private final Map<String, Object> values;

    protected OpenSSLSessionImpl(long sslSessionNativePointer, X509Certificate[] localCertificates, X509Certificate[] peerCertificates, byte[] peerCertificateOcspData, byte[] peerTlsSctData, String peerHost, int peerPort, AbstractSessionContext sessionContext) {
        super(sessionContext);
        this.creationTime = 0;
        this.lastAccessedTime = 0;
        this.values = new HashMap();
        this.peerPort = -1;
        this.sslSessionNativePointer = sslSessionNativePointer;
        this.localCertificates = localCertificates;
        this.peerCertificates = peerCertificates;
        this.peerCertificateOcspData = peerCertificateOcspData;
        this.peerTlsSctData = peerTlsSctData;
        this.peerHost = peerHost;
        this.peerPort = peerPort;
    }

    OpenSSLSessionImpl(byte[] derData, String peerHost, int peerPort, X509Certificate[] peerCertificates, byte[] peerCertificateOcspData, byte[] peerTlsSctData, AbstractSessionContext sessionContext) throws IOException {
        this(NativeCrypto.d2i_SSL_SESSION(derData), null, peerCertificates, peerCertificateOcspData, peerTlsSctData, peerHost, peerPort, sessionContext);
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

    public byte[] getEncoded() {
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

    public void setLastAccessedTime(long accessTimeMillis) {
        this.lastAccessedTime = accessTimeMillis;
    }

    protected X509Certificate[] getX509LocalCertificates() {
        return this.localCertificates;
    }

    protected X509Certificate[] getX509PeerCertificates() throws SSLPeerUnverifiedException {
        if (this.peerCertificates != null && this.peerCertificates.length != 0) {
            return this.peerCertificates;
        }
        throw new SSLPeerUnverifiedException("No peer certificates");
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

    public List<byte[]> getStatusResponses() {
        if (this.peerCertificateOcspData == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList((byte[]) this.peerCertificateOcspData.clone());
    }

    public byte[] getTlsSctData() {
        if (this.peerTlsSctData == null) {
            return null;
        }
        return (byte[]) this.peerTlsSctData.clone();
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
