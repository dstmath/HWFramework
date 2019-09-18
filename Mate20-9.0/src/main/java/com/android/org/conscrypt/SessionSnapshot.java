package com.android.org.conscrypt;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionContext;

final class SessionSnapshot implements ConscryptSession {
    private final String cipherSuite;
    private final long creationTime;
    private final byte[] id;
    private final long lastAccessedTime;
    private final String peerHost;
    private final int peerPort;
    private final byte[] peerTlsSctData;
    private final String protocol;
    private final String requestedServerName;
    private final SSLSessionContext sessionContext;
    private final List<byte[]> statusResponses;

    SessionSnapshot(ConscryptSession session) {
        this.sessionContext = session.getSessionContext();
        this.id = session.getId();
        this.requestedServerName = session.getRequestedServerName();
        this.statusResponses = session.getStatusResponses();
        this.peerTlsSctData = session.getPeerSignedCertificateTimestamp();
        this.creationTime = session.getCreationTime();
        this.lastAccessedTime = session.getLastAccessedTime();
        this.cipherSuite = session.getCipherSuite();
        this.protocol = session.getProtocol();
        this.peerHost = session.getPeerHost();
        this.peerPort = session.getPeerPort();
    }

    public String getRequestedServerName() {
        return this.requestedServerName;
    }

    public List<byte[]> getStatusResponses() {
        List<byte[]> ret = new ArrayList<>(this.statusResponses.size());
        for (byte[] resp : this.statusResponses) {
            ret.add(resp.clone());
        }
        return ret;
    }

    public byte[] getPeerSignedCertificateTimestamp() {
        if (this.peerTlsSctData != null) {
            return (byte[]) this.peerTlsSctData.clone();
        }
        return null;
    }

    public byte[] getId() {
        return this.id;
    }

    public SSLSessionContext getSessionContext() {
        return this.sessionContext;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public void invalidate() {
    }

    public boolean isValid() {
        return false;
    }

    public void putValue(String s, Object o) {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public Object getValue(String s) {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public void removeValue(String s) {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public String[] getValueNames() {
        throw new UnsupportedOperationException("All calls to this method should be intercepted by ProvidedSessionDecorator.");
    }

    public X509Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("No peer certificates");
    }

    public Certificate[] getLocalCertificates() {
        return null;
    }

    public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("No peer certificates");
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("No peer certificates");
    }

    public Principal getLocalPrincipal() {
        return null;
    }

    public String getCipherSuite() {
        return this.cipherSuite;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getPeerHost() {
        return this.peerHost;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public int getPacketBufferSize() {
        return 16709;
    }

    public int getApplicationBufferSize() {
        return 16384;
    }
}
