package com.android.org.conscrypt;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;

final class ExternalSession implements SessionDecorator {
    private final Provider provider;
    private final HashMap<String, Object> values = new HashMap<>(2);

    interface Provider {
        ConscryptSession provideSession();
    }

    public ExternalSession(Provider provider2) {
        this.provider = provider2;
    }

    public ConscryptSession getDelegate() {
        return this.provider.provideSession();
    }

    public String getRequestedServerName() {
        return getDelegate().getRequestedServerName();
    }

    public List<byte[]> getStatusResponses() {
        return getDelegate().getStatusResponses();
    }

    public byte[] getPeerSignedCertificateTimestamp() {
        return getDelegate().getPeerSignedCertificateTimestamp();
    }

    public byte[] getId() {
        return getDelegate().getId();
    }

    public SSLSessionContext getSessionContext() {
        return getDelegate().getSessionContext();
    }

    public long getCreationTime() {
        return getDelegate().getCreationTime();
    }

    public long getLastAccessedTime() {
        return getDelegate().getLastAccessedTime();
    }

    public void invalidate() {
        getDelegate().invalidate();
    }

    public boolean isValid() {
        return getDelegate().isValid();
    }

    public X509Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        return getDelegate().getPeerCertificates();
    }

    public Certificate[] getLocalCertificates() {
        return getDelegate().getLocalCertificates();
    }

    public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        return getDelegate().getPeerCertificateChain();
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return getDelegate().getPeerPrincipal();
    }

    public Principal getLocalPrincipal() {
        return getDelegate().getLocalPrincipal();
    }

    public String getCipherSuite() {
        return getDelegate().getCipherSuite();
    }

    public String getProtocol() {
        return getDelegate().getProtocol();
    }

    public String getPeerHost() {
        return getDelegate().getPeerHost();
    }

    public int getPeerPort() {
        return getDelegate().getPeerPort();
    }

    public int getPacketBufferSize() {
        return getDelegate().getPacketBufferSize();
    }

    public int getApplicationBufferSize() {
        return getDelegate().getApplicationBufferSize();
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
        if (name != null) {
            Object old = this.values.remove(name);
            if (old instanceof SSLSessionBindingListener) {
                ((SSLSessionBindingListener) old).valueUnbound(new SSLSessionBindingEvent(this, name));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("name == null");
    }
}
