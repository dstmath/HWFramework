package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.tls.CertificateChainCleaner;
import com.huawei.okio.ByteString;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;

public final class CertificatePinner {
    public static final CertificatePinner DEFAULT = new Builder().build();
    @Nullable
    private final CertificateChainCleaner certificateChainCleaner;
    private final Set<Pin> pins;

    CertificatePinner(Set<Pin> pins2, @Nullable CertificateChainCleaner certificateChainCleaner2) {
        this.pins = pins2;
        this.certificateChainCleaner = certificateChainCleaner2;
    }

    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CertificatePinner) || !Objects.equals(this.certificateChainCleaner, ((CertificatePinner) other).certificateChainCleaner) || !this.pins.equals(((CertificatePinner) other).pins)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (Objects.hashCode(this.certificateChainCleaner) * 31) + this.pins.hashCode();
    }

    public void check(String hostname, List<Certificate> peerCertificates) throws SSLPeerUnverifiedException {
        List<Pin> pins2 = findMatchingPins(hostname);
        if (!pins2.isEmpty()) {
            CertificateChainCleaner certificateChainCleaner2 = this.certificateChainCleaner;
            if (certificateChainCleaner2 != null) {
                peerCertificates = certificateChainCleaner2.clean(peerCertificates, hostname);
            }
            int certsSize = peerCertificates.size();
            for (int c = 0; c < certsSize; c++) {
                X509Certificate x509Certificate = (X509Certificate) peerCertificates.get(c);
                ByteString sha1 = null;
                ByteString sha256 = null;
                int pinsSize = pins2.size();
                for (int p = 0; p < pinsSize; p++) {
                    Pin pin = pins2.get(p);
                    if (pin.hashAlgorithm.equals("sha256/")) {
                        if (sha256 == null) {
                            sha256 = sha256(x509Certificate);
                        }
                        if (pin.hash.equals(sha256)) {
                            return;
                        }
                    } else if (pin.hashAlgorithm.equals("sha1/")) {
                        if (sha1 == null) {
                            sha1 = sha1(x509Certificate);
                        }
                        if (pin.hash.equals(sha1)) {
                            return;
                        }
                    } else {
                        throw new AssertionError("unsupported hashAlgorithm: " + pin.hashAlgorithm);
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Certificate pinning failure!");
            StringBuilder message = sb.append("\n  Peer certificate chain:");
            int certsSize2 = peerCertificates.size();
            for (int c2 = 0; c2 < certsSize2; c2++) {
                X509Certificate x509Certificate2 = (X509Certificate) peerCertificates.get(c2);
                message.append("\n    ");
                message.append(pin(x509Certificate2));
                message.append(": ");
                message.append(x509Certificate2.getSubjectDN().getName());
            }
            message.append("\n  Pinned certificates for ");
            message.append(hostname);
            message.append(":");
            int pinsSize2 = pins2.size();
            for (int p2 = 0; p2 < pinsSize2; p2++) {
                message.append("\n    ");
                message.append(pins2.get(p2));
            }
            throw new SSLPeerUnverifiedException(message.toString());
        }
    }

    public void check(String hostname, Certificate... peerCertificates) throws SSLPeerUnverifiedException {
        check(hostname, Arrays.asList(peerCertificates));
    }

    /* access modifiers changed from: package-private */
    public List<Pin> findMatchingPins(String hostname) {
        List<Pin> result = Collections.emptyList();
        for (Pin pin : this.pins) {
            if (pin.matches(hostname)) {
                if (result.isEmpty()) {
                    result = new ArrayList();
                }
                result.add(pin);
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public CertificatePinner withCertificateChainCleaner(@Nullable CertificateChainCleaner certificateChainCleaner2) {
        if (Objects.equals(this.certificateChainCleaner, certificateChainCleaner2)) {
            return this;
        }
        return new CertificatePinner(this.pins, certificateChainCleaner2);
    }

    public static String pin(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            return "sha256/" + sha256((X509Certificate) certificate).base64();
        }
        throw new IllegalArgumentException("Certificate pinning requires X509 certificates");
    }

    static ByteString sha1(X509Certificate x509Certificate) {
        return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha1();
    }

    static ByteString sha256(X509Certificate x509Certificate) {
        return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha256();
    }

    /* access modifiers changed from: package-private */
    public static final class Pin {
        private static final String WILDCARD = "*.";
        final String canonicalHostname;
        final ByteString hash;
        final String hashAlgorithm;
        final String pattern;

        Pin(String pattern2, String pin) {
            String str;
            this.pattern = pattern2;
            if (pattern2.startsWith(WILDCARD)) {
                str = HttpUrl.get("http://" + pattern2.substring(WILDCARD.length())).host();
            } else {
                str = HttpUrl.get("http://" + pattern2).host();
            }
            this.canonicalHostname = str;
            if (pin.startsWith("sha1/")) {
                this.hashAlgorithm = "sha1/";
                this.hash = ByteString.decodeBase64(pin.substring("sha1/".length()));
            } else if (pin.startsWith("sha256/")) {
                this.hashAlgorithm = "sha256/";
                this.hash = ByteString.decodeBase64(pin.substring("sha256/".length()));
            } else {
                throw new IllegalArgumentException("pins must start with 'sha256/' or 'sha1/': " + pin);
            }
            if (this.hash == null) {
                throw new IllegalArgumentException("pins must be base64: " + pin);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean matches(String hostname) {
            if (!this.pattern.startsWith(WILDCARD)) {
                return hostname.equals(this.canonicalHostname);
            }
            int firstDot = hostname.indexOf(46);
            if ((hostname.length() - firstDot) - 1 == this.canonicalHostname.length()) {
                String str = this.canonicalHostname;
                if (hostname.regionMatches(false, firstDot + 1, str, 0, str.length())) {
                    return true;
                }
            }
            return false;
        }

        public boolean equals(Object other) {
            return (other instanceof Pin) && this.pattern.equals(((Pin) other).pattern) && this.hashAlgorithm.equals(((Pin) other).hashAlgorithm) && this.hash.equals(((Pin) other).hash);
        }

        public int hashCode() {
            return (((((17 * 31) + this.pattern.hashCode()) * 31) + this.hashAlgorithm.hashCode()) * 31) + this.hash.hashCode();
        }

        public String toString() {
            return this.hashAlgorithm + this.hash.base64();
        }
    }

    public static final class Builder {
        private final List<Pin> pins = new ArrayList();

        public Builder add(String pattern, String... pins2) {
            if (pattern != null) {
                for (String pin : pins2) {
                    this.pins.add(new Pin(pattern, pin));
                }
                return this;
            }
            throw new NullPointerException("pattern == null");
        }

        public CertificatePinner build() {
            return new CertificatePinner(new LinkedHashSet(this.pins), null);
        }
    }
}
