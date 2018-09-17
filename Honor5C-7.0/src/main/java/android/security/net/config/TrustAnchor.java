package android.security.net.config;

import java.security.cert.X509Certificate;

public final class TrustAnchor {
    public final X509Certificate certificate;
    public final boolean overridesPins;

    public TrustAnchor(X509Certificate certificate, boolean overridesPins) {
        if (certificate == null) {
            throw new NullPointerException("certificate");
        }
        this.certificate = certificate;
        this.overridesPins = overridesPins;
    }
}
