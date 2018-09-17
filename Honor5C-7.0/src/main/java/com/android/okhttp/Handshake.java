package com.android.okhttp;

import com.android.okhttp.internal.Util;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public final class Handshake {
    private final String cipherSuite;
    private final List<Certificate> localCertificates;
    private final List<Certificate> peerCertificates;

    private Handshake(String cipherSuite, List<Certificate> peerCertificates, List<Certificate> localCertificates) {
        this.cipherSuite = cipherSuite;
        this.peerCertificates = peerCertificates;
        this.localCertificates = localCertificates;
    }

    public static Handshake get(SSLSession session) {
        String cipherSuite = session.getCipherSuite();
        if (cipherSuite == null) {
            throw new IllegalStateException("cipherSuite == null");
        }
        Object[] peerCertificates;
        List<Certificate> peerCertificatesList;
        List<Certificate> localCertificatesList;
        try {
            peerCertificates = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            peerCertificates = null;
        }
        if (peerCertificates != null) {
            peerCertificatesList = Util.immutableList(peerCertificates);
        } else {
            peerCertificatesList = Collections.emptyList();
        }
        Object[] localCertificates = session.getLocalCertificates();
        if (localCertificates != null) {
            localCertificatesList = Util.immutableList(localCertificates);
        } else {
            localCertificatesList = Collections.emptyList();
        }
        return new Handshake(cipherSuite, peerCertificatesList, localCertificatesList);
    }

    public static Handshake get(String cipherSuite, List<Certificate> peerCertificates, List<Certificate> localCertificates) {
        if (cipherSuite != null) {
            return new Handshake(cipherSuite, Util.immutableList((List) peerCertificates), Util.immutableList((List) localCertificates));
        }
        throw new IllegalArgumentException("cipherSuite == null");
    }

    public String cipherSuite() {
        return this.cipherSuite;
    }

    public List<Certificate> peerCertificates() {
        return this.peerCertificates;
    }

    public Principal peerPrincipal() {
        if (this.peerCertificates.isEmpty()) {
            return null;
        }
        return ((X509Certificate) this.peerCertificates.get(0)).getSubjectX500Principal();
    }

    public List<Certificate> localCertificates() {
        return this.localCertificates;
    }

    public Principal localPrincipal() {
        if (this.localCertificates.isEmpty()) {
            return null;
        }
        return ((X509Certificate) this.localCertificates.get(0)).getSubjectX500Principal();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof Handshake)) {
            return false;
        }
        Handshake that = (Handshake) other;
        if (this.cipherSuite.equals(that.cipherSuite) && this.peerCertificates.equals(that.peerCertificates)) {
            z = this.localCertificates.equals(that.localCertificates);
        }
        return z;
    }

    public int hashCode() {
        return ((((this.cipherSuite.hashCode() + 527) * 31) + this.peerCertificates.hashCode()) * 31) + this.localCertificates.hashCode();
    }
}
