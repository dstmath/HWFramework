package android.security.net.config;

import android.util.ArrayMap;
import android.util.ArraySet;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NetworkSecurityConfig {
    public static final boolean DEFAULT_CLEARTEXT_TRAFFIC_PERMITTED = true;
    public static final boolean DEFAULT_HSTS_ENFORCED = false;
    private Set<TrustAnchor> mAnchors;
    private final Object mAnchorsLock;
    private final List<CertificatesEntryRef> mCertificatesEntryRefs;
    private final boolean mCleartextTrafficPermitted;
    private final boolean mHstsEnforced;
    private final PinSet mPins;
    private NetworkSecurityTrustManager mTrustManager;
    private final Object mTrustManagerLock;

    public static final class Builder {
        private List<CertificatesEntryRef> mCertificatesEntryRefs;
        private boolean mCleartextTrafficPermitted = true;
        private boolean mCleartextTrafficPermittedSet = false;
        private boolean mHstsEnforced = false;
        private boolean mHstsEnforcedSet = false;
        private Builder mParentBuilder;
        private PinSet mPinSet;

        public Builder setParent(Builder parent) {
            for (Builder current = parent; current != null; current = current.getParent()) {
                if (current == this) {
                    throw new IllegalArgumentException("Loops are not allowed in Builder parents");
                }
            }
            this.mParentBuilder = parent;
            return this;
        }

        public Builder getParent() {
            return this.mParentBuilder;
        }

        public Builder setPinSet(PinSet pinSet) {
            this.mPinSet = pinSet;
            return this;
        }

        private PinSet getEffectivePinSet() {
            if (this.mPinSet != null) {
                return this.mPinSet;
            }
            if (this.mParentBuilder != null) {
                return this.mParentBuilder.getEffectivePinSet();
            }
            return PinSet.EMPTY_PINSET;
        }

        public Builder setCleartextTrafficPermitted(boolean cleartextTrafficPermitted) {
            this.mCleartextTrafficPermitted = cleartextTrafficPermitted;
            this.mCleartextTrafficPermittedSet = true;
            return this;
        }

        private boolean getEffectiveCleartextTrafficPermitted() {
            if (this.mCleartextTrafficPermittedSet) {
                return this.mCleartextTrafficPermitted;
            }
            if (this.mParentBuilder != null) {
                return this.mParentBuilder.getEffectiveCleartextTrafficPermitted();
            }
            return true;
        }

        public Builder setHstsEnforced(boolean hstsEnforced) {
            this.mHstsEnforced = hstsEnforced;
            this.mHstsEnforcedSet = true;
            return this;
        }

        private boolean getEffectiveHstsEnforced() {
            if (this.mHstsEnforcedSet) {
                return this.mHstsEnforced;
            }
            if (this.mParentBuilder != null) {
                return this.mParentBuilder.getEffectiveHstsEnforced();
            }
            return false;
        }

        public Builder addCertificatesEntryRef(CertificatesEntryRef ref) {
            if (this.mCertificatesEntryRefs == null) {
                this.mCertificatesEntryRefs = new ArrayList();
            }
            this.mCertificatesEntryRefs.add(ref);
            return this;
        }

        public Builder addCertificatesEntryRefs(Collection<? extends CertificatesEntryRef> refs) {
            if (this.mCertificatesEntryRefs == null) {
                this.mCertificatesEntryRefs = new ArrayList();
            }
            this.mCertificatesEntryRefs.addAll(refs);
            return this;
        }

        private List<CertificatesEntryRef> getEffectiveCertificatesEntryRefs() {
            if (this.mCertificatesEntryRefs != null) {
                return this.mCertificatesEntryRefs;
            }
            if (this.mParentBuilder != null) {
                return this.mParentBuilder.getEffectiveCertificatesEntryRefs();
            }
            return Collections.emptyList();
        }

        public boolean hasCertificatesEntryRefs() {
            return this.mCertificatesEntryRefs != null;
        }

        List<CertificatesEntryRef> getCertificatesEntryRefs() {
            return this.mCertificatesEntryRefs;
        }

        public NetworkSecurityConfig build() {
            return new NetworkSecurityConfig(getEffectiveCleartextTrafficPermitted(), getEffectiveHstsEnforced(), getEffectivePinSet(), getEffectiveCertificatesEntryRefs(), null);
        }
    }

    /* synthetic */ NetworkSecurityConfig(boolean cleartextTrafficPermitted, boolean hstsEnforced, PinSet pins, List certificatesEntryRefs, NetworkSecurityConfig -this4) {
        this(cleartextTrafficPermitted, hstsEnforced, pins, certificatesEntryRefs);
    }

    private NetworkSecurityConfig(boolean cleartextTrafficPermitted, boolean hstsEnforced, PinSet pins, List<CertificatesEntryRef> certificatesEntryRefs) {
        this.mAnchorsLock = new Object();
        this.mTrustManagerLock = new Object();
        this.mCleartextTrafficPermitted = cleartextTrafficPermitted;
        this.mHstsEnforced = hstsEnforced;
        this.mPins = pins;
        this.mCertificatesEntryRefs = certificatesEntryRefs;
        Collections.sort(this.mCertificatesEntryRefs, new Comparator<CertificatesEntryRef>() {
            public int compare(CertificatesEntryRef lhs, CertificatesEntryRef rhs) {
                int i = 0;
                if (lhs.overridesPins()) {
                    if (!rhs.overridesPins()) {
                        i = -1;
                    }
                    return i;
                }
                if (rhs.overridesPins()) {
                    i = 1;
                }
                return i;
            }
        });
    }

    public Set<TrustAnchor> getTrustAnchors() {
        synchronized (this.mAnchorsLock) {
            Set<TrustAnchor> set;
            if (this.mAnchors != null) {
                set = this.mAnchors;
                return set;
            }
            Map<X509Certificate, TrustAnchor> anchorMap = new ArrayMap();
            for (CertificatesEntryRef ref : this.mCertificatesEntryRefs) {
                for (TrustAnchor anchor : ref.getTrustAnchors()) {
                    X509Certificate cert = anchor.certificate;
                    if (!anchorMap.containsKey(cert)) {
                        anchorMap.put(cert, anchor);
                    }
                }
            }
            ArraySet<TrustAnchor> anchors = new ArraySet(anchorMap.size());
            anchors.addAll(anchorMap.values());
            this.mAnchors = anchors;
            set = this.mAnchors;
            return set;
        }
    }

    public boolean isCleartextTrafficPermitted() {
        return this.mCleartextTrafficPermitted;
    }

    public boolean isHstsEnforced() {
        return this.mHstsEnforced;
    }

    public PinSet getPins() {
        return this.mPins;
    }

    public NetworkSecurityTrustManager getTrustManager() {
        NetworkSecurityTrustManager networkSecurityTrustManager;
        synchronized (this.mTrustManagerLock) {
            if (this.mTrustManager == null) {
                this.mTrustManager = new NetworkSecurityTrustManager(this);
            }
            networkSecurityTrustManager = this.mTrustManager;
        }
        return networkSecurityTrustManager;
    }

    public TrustAnchor findTrustAnchorBySubjectAndPublicKey(X509Certificate cert) {
        for (CertificatesEntryRef ref : this.mCertificatesEntryRefs) {
            TrustAnchor anchor = ref.findBySubjectAndPublicKey(cert);
            if (anchor != null) {
                return anchor;
            }
        }
        return null;
    }

    public TrustAnchor findTrustAnchorByIssuerAndSignature(X509Certificate cert) {
        for (CertificatesEntryRef ref : this.mCertificatesEntryRefs) {
            TrustAnchor anchor = ref.findByIssuerAndSignature(cert);
            if (anchor != null) {
                return anchor;
            }
        }
        return null;
    }

    public Set<X509Certificate> findAllCertificatesByIssuerAndSignature(X509Certificate cert) {
        Set<X509Certificate> certs = new ArraySet();
        for (CertificatesEntryRef ref : this.mCertificatesEntryRefs) {
            certs.addAll(ref.findAllCertificatesByIssuerAndSignature(cert));
        }
        return certs;
    }

    public void handleTrustStorageUpdate() {
        synchronized (this.mAnchorsLock) {
            this.mAnchors = null;
            for (CertificatesEntryRef ref : this.mCertificatesEntryRefs) {
                ref.handleTrustStorageUpdate();
            }
        }
        getTrustManager().handleTrustStorageUpdate();
    }

    public static final Builder getDefaultBuilder(int targetSdkVersion, int targetSandboxVesrsion) {
        Builder builder = new Builder().setHstsEnforced(false).addCertificatesEntryRef(new CertificatesEntryRef(SystemCertificateSource.getInstance(), false));
        builder.setCleartextTrafficPermitted(targetSandboxVesrsion < 2);
        if (targetSdkVersion <= 23) {
            builder.addCertificatesEntryRef(new CertificatesEntryRef(UserCertificateSource.getInstance(), false));
        }
        return builder;
    }
}
