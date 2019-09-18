package com.android.org.conscrypt;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionContext;

class Java7ExtendedSSLSession extends ExtendedSSLSession implements SessionDecorator {
    private static final String[] LOCAL_SUPPORTED_SIGNATURE_ALGORITHMS = {"SHA512withRSA", "SHA512withECDSA", "SHA384withRSA", "SHA384withECDSA", "SHA256withRSA", "SHA256withECDSA", "SHA224withRSA", "SHA224withECDSA", "SHA1withRSA", "SHA1withECDSA"};
    private static final String[] PEER_SUPPORTED_SIGNATURE_ALGORITHMS = {"SHA1withRSA", "SHA1withECDSA"};
    private final ConscryptSession delegate;

    Java7ExtendedSSLSession(ConscryptSession delegate2) {
        this.delegate = delegate2;
    }

    public final ConscryptSession getDelegate() {
        return this.delegate;
    }

    public final String[] getLocalSupportedSignatureAlgorithms() {
        return (String[]) LOCAL_SUPPORTED_SIGNATURE_ALGORITHMS.clone();
    }

    public final String[] getPeerSupportedSignatureAlgorithms() {
        return (String[]) PEER_SUPPORTED_SIGNATURE_ALGORITHMS.clone();
    }

    public final String getRequestedServerName() {
        return getDelegate().getRequestedServerName();
    }

    public final List<byte[]> getStatusResponses() {
        return getDelegate().getStatusResponses();
    }

    public final byte[] getPeerSignedCertificateTimestamp() {
        return getDelegate().getPeerSignedCertificateTimestamp();
    }

    public final byte[] getId() {
        return getDelegate().getId();
    }

    public final SSLSessionContext getSessionContext() {
        return getDelegate().getSessionContext();
    }

    public final long getCreationTime() {
        return getDelegate().getCreationTime();
    }

    public final long getLastAccessedTime() {
        return getDelegate().getLastAccessedTime();
    }

    public final void invalidate() {
        getDelegate().invalidate();
    }

    public final boolean isValid() {
        return getDelegate().isValid();
    }

    public final void putValue(String s, Object o) {
        getDelegate().putValue(s, o);
    }

    public final Object getValue(String s) {
        return getDelegate().getValue(s);
    }

    public final void removeValue(String s) {
        getDelegate().removeValue(s);
    }

    public final String[] getValueNames() {
        return getDelegate().getValueNames();
    }

    public X509Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        return getDelegate().getPeerCertificates();
    }

    public final Certificate[] getLocalCertificates() {
        return getDelegate().getLocalCertificates();
    }

    public final javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        return getDelegate().getPeerCertificateChain();
    }

    public final Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return getDelegate().getPeerPrincipal();
    }

    public final Principal getLocalPrincipal() {
        return getDelegate().getLocalPrincipal();
    }

    public final String getCipherSuite() {
        return getDelegate().getCipherSuite();
    }

    public final String getProtocol() {
        return getDelegate().getProtocol();
    }

    public final String getPeerHost() {
        return getDelegate().getPeerHost();
    }

    public final int getPeerPort() {
        return getDelegate().getPeerPort();
    }

    public final int getPacketBufferSize() {
        return getDelegate().getPacketBufferSize();
    }

    public final int getApplicationBufferSize() {
        return getDelegate().getApplicationBufferSize();
    }
}
