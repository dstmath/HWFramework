package com.android.org.conscrypt;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;

final class SSLNullSession implements ConscryptSession, Cloneable {
    static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";
    private long creationTime;
    private long lastAccessedTime;

    private static class DefaultHolder {
        static final SSLNullSession NULL_SESSION = new SSLNullSession();

        private DefaultHolder() {
        }
    }

    static ConscryptSession getNullSession() {
        return DefaultHolder.NULL_SESSION;
    }

    static boolean isNullSession(SSLSession session) {
        return SSLUtils.unwrapSession(session) == DefaultHolder.NULL_SESSION;
    }

    private SSLNullSession() {
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
    }

    public String getRequestedServerName() {
        return null;
    }

    public List<byte[]> getStatusResponses() {
        return Collections.emptyList();
    }

    public byte[] getPeerSignedCertificateTimestamp() {
        return EmptyArray.BYTE;
    }

    public int getApplicationBufferSize() {
        return 16384;
    }

    public String getCipherSuite() {
        return INVALID_CIPHER;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public byte[] getId() {
        return EmptyArray.BYTE;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public Certificate[] getLocalCertificates() {
        return null;
    }

    public Principal getLocalPrincipal() {
        return null;
    }

    public int getPacketBufferSize() {
        return 16709;
    }

    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("No peer certificate");
    }

    public java.security.cert.X509Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("No peer certificate");
    }

    public String getPeerHost() {
        return null;
    }

    public int getPeerPort() {
        return -1;
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("No peer certificate");
    }

    public String getProtocol() {
        return "NONE";
    }

    public SSLSessionContext getSessionContext() {
        return null;
    }

    public Object getValue(String name) {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public String[] getValueNames() {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public void invalidate() {
    }

    public boolean isValid() {
        return false;
    }

    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public void removeValue(String name) {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }
}
