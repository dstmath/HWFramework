package android.security.net.config;

import android.content.Context;
import android.util.ArraySet;
import com.android.org.conscrypt.TrustedCertificateIndex;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import libcore.io.IoUtils;

public class ResourceCertificateSource implements CertificateSource {
    private Set<X509Certificate> mCertificates;
    private Context mContext;
    private TrustedCertificateIndex mIndex;
    private final Object mLock = new Object();
    private final int mResourceId;

    public ResourceCertificateSource(int resourceId, Context context) {
        this.mResourceId = resourceId;
        this.mContext = context;
    }

    private void ensureInitialized() {
        synchronized (this.mLock) {
            if (this.mCertificates != null) {
                return;
            }
            Set<X509Certificate> certificates = new ArraySet();
            AutoCloseable autoCloseable = null;
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                autoCloseable = this.mContext.getResources().openRawResource(this.mResourceId);
                Collection<? extends Certificate> certs = factory.generateCertificates(autoCloseable);
                IoUtils.closeQuietly(autoCloseable);
                TrustedCertificateIndex indexLocal = new TrustedCertificateIndex();
                for (Certificate cert : certs) {
                    certificates.add((X509Certificate) cert);
                    indexLocal.index((X509Certificate) cert);
                }
                this.mCertificates = certificates;
                this.mIndex = indexLocal;
                this.mContext = null;
            } catch (CertificateException e) {
                throw new RuntimeException("Failed to load trust anchors from id " + this.mResourceId, e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    public Set<X509Certificate> getCertificates() {
        ensureInitialized();
        return this.mCertificates;
    }

    public X509Certificate findBySubjectAndPublicKey(X509Certificate cert) {
        ensureInitialized();
        TrustAnchor anchor = this.mIndex.findBySubjectAndPublicKey(cert);
        if (anchor == null) {
            return null;
        }
        return anchor.getTrustedCert();
    }

    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        ensureInitialized();
        TrustAnchor anchor = this.mIndex.findByIssuerAndSignature(cert);
        if (anchor == null) {
            return null;
        }
        return anchor.getTrustedCert();
    }

    public Set<X509Certificate> findAllByIssuerAndSignature(X509Certificate cert) {
        ensureInitialized();
        Set<TrustAnchor> anchors = this.mIndex.findAllByIssuerAndSignature(cert);
        if (anchors.isEmpty()) {
            return Collections.emptySet();
        }
        Set<X509Certificate> certs = new ArraySet(anchors.size());
        for (TrustAnchor anchor : anchors) {
            certs.add(anchor.getTrustedCert());
        }
        return certs;
    }

    public void handleTrustStorageUpdate() {
    }
}
