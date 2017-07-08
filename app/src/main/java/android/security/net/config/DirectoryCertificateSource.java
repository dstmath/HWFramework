package android.security.net.config;

import android.util.ArraySet;
import com.android.org.conscrypt.Hex;
import com.android.org.conscrypt.NativeCrypto;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import libcore.io.IoUtils;

abstract class DirectoryCertificateSource implements CertificateSource {
    private final CertificateFactory mCertFactory;
    private Set<X509Certificate> mCertificates;
    private final File mDir;
    private final Object mLock;

    private interface CertSelector {
        boolean match(X509Certificate x509Certificate);
    }

    /* renamed from: android.security.net.config.DirectoryCertificateSource.1 */
    class AnonymousClass1 implements CertSelector {
        final /* synthetic */ X509Certificate val$cert;

        AnonymousClass1(X509Certificate val$cert) {
            this.val$cert = val$cert;
        }

        public boolean match(X509Certificate ca) {
            return ca.getPublicKey().equals(this.val$cert.getPublicKey());
        }
    }

    /* renamed from: android.security.net.config.DirectoryCertificateSource.2 */
    class AnonymousClass2 implements CertSelector {
        final /* synthetic */ X509Certificate val$cert;

        AnonymousClass2(X509Certificate val$cert) {
            this.val$cert = val$cert;
        }

        public boolean match(X509Certificate ca) {
            try {
                this.val$cert.verify(ca.getPublicKey());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /* renamed from: android.security.net.config.DirectoryCertificateSource.3 */
    class AnonymousClass3 implements CertSelector {
        final /* synthetic */ X509Certificate val$cert;

        AnonymousClass3(X509Certificate val$cert) {
            this.val$cert = val$cert;
        }

        public boolean match(X509Certificate ca) {
            try {
                this.val$cert.verify(ca.getPublicKey());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    protected abstract boolean isCertMarkedAsRemoved(String str);

    protected DirectoryCertificateSource(File caDir) {
        this.mLock = new Object();
        this.mDir = caDir;
        try {
            this.mCertFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e);
        }
    }

    public Set<X509Certificate> getCertificates() {
        synchronized (this.mLock) {
            if (this.mCertificates != null) {
                return this.mCertificates;
            }
            Set<X509Certificate> certs = new ArraySet();
            if (this.mDir.isDirectory()) {
                for (String caFile : this.mDir.list()) {
                    if (!isCertMarkedAsRemoved(caFile)) {
                        X509Certificate cert = readCertificate(caFile);
                        if (cert != null) {
                            certs.add(cert);
                        }
                    }
                }
            }
            this.mCertificates = certs;
            return this.mCertificates;
        }
    }

    public X509Certificate findBySubjectAndPublicKey(X509Certificate cert) {
        return findCert(cert.getSubjectX500Principal(), new AnonymousClass1(cert));
    }

    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        return findCert(cert.getIssuerX500Principal(), new AnonymousClass2(cert));
    }

    public Set<X509Certificate> findAllByIssuerAndSignature(X509Certificate cert) {
        return findCerts(cert.getIssuerX500Principal(), new AnonymousClass3(cert));
    }

    public void handleTrustStorageUpdate() {
        synchronized (this.mLock) {
            this.mCertificates = null;
        }
    }

    private Set<X509Certificate> findCerts(X500Principal subj, CertSelector selector) {
        String hash = getHash(subj);
        Set<X509Certificate> certs = null;
        for (int index = 0; index >= 0; index++) {
            String fileName = hash + "." + index;
            if (!new File(this.mDir, fileName).exists()) {
                break;
            }
            if (!isCertMarkedAsRemoved(fileName)) {
                X509Certificate cert = readCertificate(fileName);
                if (subj.equals(cert.getSubjectX500Principal()) && selector.match(cert)) {
                    if (certs == null) {
                        certs = new ArraySet();
                    }
                    certs.add(cert);
                }
            }
        }
        if (certs != null) {
            return certs;
        }
        return Collections.emptySet();
    }

    private X509Certificate findCert(X500Principal subj, CertSelector selector) {
        String hash = getHash(subj);
        for (int index = 0; index >= 0; index++) {
            String fileName = hash + "." + index;
            if (!new File(this.mDir, fileName).exists()) {
                break;
            }
            if (!isCertMarkedAsRemoved(fileName)) {
                X509Certificate cert = readCertificate(fileName);
                if (subj.equals(cert.getSubjectX500Principal()) && selector.match(cert)) {
                    return cert;
                }
            }
        }
        return null;
    }

    private String getHash(X500Principal name) {
        return Hex.intToHexString(NativeCrypto.X509_NAME_hash_old(name), 8);
    }

    private X509Certificate readCertificate(String file) {
        Throwable th;
        InputStream is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(new File(this.mDir, file)));
            try {
                X509Certificate x509Certificate = (X509Certificate) this.mCertFactory.generateCertificate(is2);
                IoUtils.closeQuietly(is2);
                return x509Certificate;
            } catch (CertificateException e) {
                Object obj = is2;
                IoUtils.closeQuietly(r1);
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                IoUtils.closeQuietly(is);
                throw th;
            }
        } catch (CertificateException e2) {
            IoUtils.closeQuietly(r1);
            return null;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(is);
            throw th;
        }
    }
}
