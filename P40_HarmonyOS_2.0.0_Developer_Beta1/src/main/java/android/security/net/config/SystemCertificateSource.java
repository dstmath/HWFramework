package android.security.net.config;

import android.os.Environment;
import android.os.UserHandle;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Set;

public final class SystemCertificateSource extends DirectoryCertificateSource {
    private final File mUserRemovedCaDir;

    /* access modifiers changed from: private */
    public static class NoPreloadHolder {
        private static final SystemCertificateSource INSTANCE = new SystemCertificateSource();

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

    private SystemCertificateSource() {
        super(new File(System.getenv("ANDROID_ROOT") + "/etc/security/cacerts"));
        this.mUserRemovedCaDir = new File(Environment.getUserConfigDirectory(UserHandle.myUserId()), "cacerts-removed");
    }

    public static SystemCertificateSource getInstance() {
        return NoPreloadHolder.INSTANCE;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.net.config.DirectoryCertificateSource
    public boolean isCertMarkedAsRemoved(String caFile) {
        return new File(this.mUserRemovedCaDir, caFile).exists();
    }
}
