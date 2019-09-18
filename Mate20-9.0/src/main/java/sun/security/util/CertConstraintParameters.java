package sun.security.util;

import java.security.cert.X509Certificate;

public class CertConstraintParameters {
    private final X509Certificate cert;
    private final boolean trustedMatch;

    public CertConstraintParameters(X509Certificate c, boolean match) {
        this.cert = c;
        this.trustedMatch = match;
    }

    public CertConstraintParameters(X509Certificate c) {
        this(c, false);
    }

    public boolean isTrustedMatch() {
        return this.trustedMatch;
    }

    public X509Certificate getCertificate() {
        return this.cert;
    }
}
