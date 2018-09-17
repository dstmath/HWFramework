package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertSelector;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.util.Set;

public class SunCertPathBuilderParameters extends PKIXBuilderParameters {
    private boolean buildForward;

    public SunCertPathBuilderParameters(Set<TrustAnchor> trustAnchors, CertSelector targetConstraints) throws InvalidAlgorithmParameterException {
        super((Set) trustAnchors, targetConstraints);
        this.buildForward = true;
        setBuildForward(true);
    }

    public SunCertPathBuilderParameters(KeyStore keystore, CertSelector targetConstraints) throws KeyStoreException, InvalidAlgorithmParameterException {
        super(keystore, targetConstraints);
        this.buildForward = true;
        setBuildForward(true);
    }

    public boolean getBuildForward() {
        return this.buildForward;
    }

    public void setBuildForward(boolean buildForward) {
        this.buildForward = buildForward;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append(super.toString());
        sb.append("  Build Forward Flag: ").append(String.valueOf(this.buildForward)).append("\n");
        sb.append("]\n");
        return sb.toString();
    }
}
