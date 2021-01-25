package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public final class Handshake {
    private final CipherSuite cipherSuite;
    private final List<Certificate> localCertificates;
    private final List<Certificate> peerCertificates;
    private final TlsVersion tlsVersion;

    private Handshake(TlsVersion tlsVersion2, CipherSuite cipherSuite2, List<Certificate> peerCertificates2, List<Certificate> localCertificates2) {
        this.tlsVersion = tlsVersion2;
        this.cipherSuite = cipherSuite2;
        this.peerCertificates = peerCertificates2;
        this.localCertificates = localCertificates2;
    }

    public static Handshake get(SSLSession session) throws IOException {
        Certificate[] peerCertificates2;
        List<Certificate> peerCertificatesList;
        List<Certificate> localCertificatesList;
        String cipherSuiteString = session.getCipherSuite();
        if (cipherSuiteString == null) {
            throw new IllegalStateException("cipherSuite == null");
        } else if (!"SSL_NULL_WITH_NULL_NULL".equals(cipherSuiteString)) {
            CipherSuite cipherSuite2 = CipherSuite.forJavaName(cipherSuiteString);
            String tlsVersionString = session.getProtocol();
            if (tlsVersionString == null) {
                throw new IllegalStateException("tlsVersion == null");
            } else if (!"NONE".equals(tlsVersionString)) {
                TlsVersion tlsVersion2 = TlsVersion.forJavaName(tlsVersionString);
                try {
                    peerCertificates2 = session.getPeerCertificates();
                } catch (SSLPeerUnverifiedException e) {
                    peerCertificates2 = null;
                }
                if (peerCertificates2 != null) {
                    peerCertificatesList = Util.immutableList(peerCertificates2);
                } else {
                    peerCertificatesList = Collections.emptyList();
                }
                Certificate[] localCertificates2 = session.getLocalCertificates();
                if (localCertificates2 != null) {
                    localCertificatesList = Util.immutableList(localCertificates2);
                } else {
                    localCertificatesList = Collections.emptyList();
                }
                return new Handshake(tlsVersion2, cipherSuite2, peerCertificatesList, localCertificatesList);
            } else {
                throw new IOException("tlsVersion == NONE");
            }
        } else {
            throw new IOException("cipherSuite == SSL_NULL_WITH_NULL_NULL");
        }
    }

    public static Handshake get(TlsVersion tlsVersion2, CipherSuite cipherSuite2, List<Certificate> peerCertificates2, List<Certificate> localCertificates2) {
        if (tlsVersion2 == null) {
            throw new NullPointerException("tlsVersion == null");
        } else if (cipherSuite2 != null) {
            return new Handshake(tlsVersion2, cipherSuite2, Util.immutableList(peerCertificates2), Util.immutableList(localCertificates2));
        } else {
            throw new NullPointerException("cipherSuite == null");
        }
    }

    public TlsVersion tlsVersion() {
        return this.tlsVersion;
    }

    public CipherSuite cipherSuite() {
        return this.cipherSuite;
    }

    public List<Certificate> peerCertificates() {
        return this.peerCertificates;
    }

    @Nullable
    public Principal peerPrincipal() {
        if (!this.peerCertificates.isEmpty()) {
            return ((X509Certificate) this.peerCertificates.get(0)).getSubjectX500Principal();
        }
        return null;
    }

    public List<Certificate> localCertificates() {
        return this.localCertificates;
    }

    @Nullable
    public Principal localPrincipal() {
        if (!this.localCertificates.isEmpty()) {
            return ((X509Certificate) this.localCertificates.get(0)).getSubjectX500Principal();
        }
        return null;
    }

    public boolean equals(@Nullable Object other) {
        if (!(other instanceof Handshake)) {
            return false;
        }
        Handshake that = (Handshake) other;
        if (!this.tlsVersion.equals(that.tlsVersion) || !this.cipherSuite.equals(that.cipherSuite) || !this.peerCertificates.equals(that.peerCertificates) || !this.localCertificates.equals(that.localCertificates)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((((((17 * 31) + this.tlsVersion.hashCode()) * 31) + this.cipherSuite.hashCode()) * 31) + this.peerCertificates.hashCode()) * 31) + this.localCertificates.hashCode();
    }

    public String toString() {
        return "Handshake{tlsVersion=" + this.tlsVersion + " cipherSuite=" + this.cipherSuite + " peerCertificates=" + names(this.peerCertificates) + " localCertificates=" + names(this.localCertificates) + '}';
    }

    private List<String> names(List<Certificate> certificates) {
        ArrayList<String> strings = new ArrayList<>();
        for (Certificate cert : certificates) {
            if (cert instanceof X509Certificate) {
                strings.add(String.valueOf(((X509Certificate) cert).getSubjectDN()));
            } else {
                strings.add(cert.getType());
            }
        }
        return strings;
    }
}
