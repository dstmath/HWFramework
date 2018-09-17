package android.security.net.config;

import java.security.cert.X509Certificate;
import java.util.Set;

public interface CertificateSource {
    Set<X509Certificate> findAllByIssuerAndSignature(X509Certificate x509Certificate);

    X509Certificate findByIssuerAndSignature(X509Certificate x509Certificate);

    X509Certificate findBySubjectAndPublicKey(X509Certificate x509Certificate);

    Set<X509Certificate> getCertificates();

    void handleTrustStorageUpdate();
}
