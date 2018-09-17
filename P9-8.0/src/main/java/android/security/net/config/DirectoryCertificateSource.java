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
    private static final String LOG_TAG = "DirectoryCertificateSrc";
    private final CertificateFactory mCertFactory;
    private Set<X509Certificate> mCertificates;
    private final File mDir;
    private final Object mLock = new Object();

    private interface CertSelector {
        boolean match(X509Certificate x509Certificate);
    }

    protected abstract boolean isCertMarkedAsRemoved(String str);

    protected DirectoryCertificateSource(File caDir) {
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

    public X509Certificate findBySubjectAndPublicKey(final X509Certificate cert) {
        return findCert(cert.getSubjectX500Principal(), new CertSelector() {
            public boolean match(X509Certificate ca) {
                return ca.getPublicKey().equals(cert.getPublicKey());
            }
        });
    }

    public X509Certificate findByIssuerAndSignature(final X509Certificate cert) {
        return findCert(cert.getIssuerX500Principal(), new CertSelector() {
            public boolean match(X509Certificate ca) {
                try {
                    cert.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    public Set<X509Certificate> findAllByIssuerAndSignature(final X509Certificate cert) {
        return findCerts(cert.getIssuerX500Principal(), new CertSelector() {
            public boolean match(X509Certificate ca) {
                try {
                    cert.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
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
                if (cert != null && subj.equals(cert.getSubjectX500Principal()) && selector.match(cert)) {
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
                if (cert != null && subj.equals(cert.getSubjectX500Principal()) && selector.match(cert)) {
                    return cert;
                }
            }
        }
        return null;
    }

    private String getHash(X500Principal name) {
        return Hex.intToHexString(NativeCrypto.X509_NAME_hash_old(name), 8);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0046 A:{Splitter: B:3:0x0012, ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001e A:{Splitter: B:1:0x0001, ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException)} */
    /* JADX WARNING: Missing block: B:7:0x001e, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            android.util.Log.e(LOG_TAG, "Failed to read certificate from " + r7, r0);
     */
    /* JADX WARNING: Missing block: B:10:0x0039, code:
            libcore.io.IoUtils.closeQuietly(r1);
     */
    /* JADX WARNING: Missing block: B:11:0x003d, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:12:0x003e, code:
            r3 = th;
     */
    /* JADX WARNING: Missing block: B:17:0x0046, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:18:0x0047, code:
            r1 = r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private X509Certificate readCertificate(String file) {
        AutoCloseable is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(new File(this.mDir, file)));
            try {
                X509Certificate x509Certificate = (X509Certificate) this.mCertFactory.generateCertificate(is2);
                IoUtils.closeQuietly(is2);
                return x509Certificate;
            } catch (CertificateException e) {
            } catch (Throwable th) {
                Throwable th2 = th;
                Object is3 = is2;
                IoUtils.closeQuietly(is);
                throw th2;
            }
        } catch (CertificateException e2) {
        }
    }
}
