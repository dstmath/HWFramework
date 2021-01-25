package android.security.net.config;

import android.os.Environment;
import android.os.UserHandle;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Set;

public final class UserCertificateSource extends DirectoryCertificateSource {

    private static class NoPreloadHolder {
        private static final UserCertificateSource INSTANCE = new UserCertificateSource();

        private NoPreloadHolder() {
        }
    }

    @Override // android.security.net.config.DirectoryCertificateSource, android.security.net.config.CertificateSource
    public /* bridge */ /* synthetic */ Set findAllByIssuerAndSignature(X509Certificate x509Certificate) {
        return super.findAllByIssuerAndSignature(x509Certificate);
    }

    @Override // android.security.net.config.DirectoryCertificateSource, android.security.net.config.CertificateSource
    public /* bridge */ /* synthetic */ X509Certificate findByIssuerAndSignature(X509Certificate x509Certificate) {
        return super.findByIssuerAndSignature(x509Certificate);
    }

    @Override // android.security.net.config.DirectoryCertificateSource, android.security.net.config.CertificateSource
    public /* bridge */ /* synthetic */ X509Certificate findBySubjectAndPublicKey(X509Certificate x509Certificate) {
        return super.findBySubjectAndPublicKey(x509Certificate);
    }

    @Override // android.security.net.config.DirectoryCertificateSource, android.security.net.config.CertificateSource
    public /* bridge */ /* synthetic */ Set getCertificates() {
        return super.getCertificates();
    }

    @Override // android.security.net.config.DirectoryCertificateSource, android.security.net.config.CertificateSource
    public /* bridge */ /* synthetic */ void handleTrustStorageUpdate() {
        super.handleTrustStorageUpdate();
    }

    private UserCertificateSource() {
        super(new File(Environment.getUserConfigDirectory(UserHandle.myUserId()), "cacerts-added"));
    }

    public static UserCertificateSource getInstance() {
        return NoPreloadHolder.INSTANCE;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.net.config.DirectoryCertificateSource
    public boolean isCertMarkedAsRemoved(String caFile) {
        return false;
    }
}
