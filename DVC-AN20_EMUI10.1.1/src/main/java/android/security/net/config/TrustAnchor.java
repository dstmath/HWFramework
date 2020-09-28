package android.security.net.config;

import java.security.cert.X509Certificate;

public final class TrustAnchor {
    public final X509Certificate certificate;
    public final boolean overridesPins;

    public TrustAnchor(X509Certificate certificate2, boolean overridesPins2) {
        if (certificate2 != null) {
            this.certificate = certificate2;
            this.overridesPins = overridesPins2;
            return;
        }
        throw new NullPointerException("certificate");
    }
}
