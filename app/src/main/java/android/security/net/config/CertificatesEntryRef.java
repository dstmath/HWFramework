package android.security.net.config;

import android.util.ArraySet;
import java.security.cert.X509Certificate;
import java.util.Set;

public final class CertificatesEntryRef {
    private final boolean mOverridesPins;
    private final CertificateSource mSource;

    public CertificatesEntryRef(CertificateSource source, boolean overridesPins) {
        this.mSource = source;
        this.mOverridesPins = overridesPins;
    }

    boolean overridesPins() {
        return this.mOverridesPins;
    }

    public Set<TrustAnchor> getTrustAnchors() {
        Set<TrustAnchor> anchors = new ArraySet();
        for (X509Certificate cert : this.mSource.getCertificates()) {
            anchors.add(new TrustAnchor(cert, this.mOverridesPins));
        }
        return anchors;
    }

    public TrustAnchor findBySubjectAndPublicKey(X509Certificate cert) {
        X509Certificate foundCert = this.mSource.findBySubjectAndPublicKey(cert);
        if (foundCert == null) {
            return null;
        }
        return new TrustAnchor(foundCert, this.mOverridesPins);
    }

    public TrustAnchor findByIssuerAndSignature(X509Certificate cert) {
        X509Certificate foundCert = this.mSource.findByIssuerAndSignature(cert);
        if (foundCert == null) {
            return null;
        }
        return new TrustAnchor(foundCert, this.mOverridesPins);
    }

    public Set<X509Certificate> findAllCertificatesByIssuerAndSignature(X509Certificate cert) {
        return this.mSource.findAllByIssuerAndSignature(cert);
    }

    public void handleTrustStorageUpdate() {
        this.mSource.handleTrustStorageUpdate();
    }
}
