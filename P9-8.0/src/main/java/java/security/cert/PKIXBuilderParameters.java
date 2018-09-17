package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Set;

public class PKIXBuilderParameters extends PKIXParameters {
    private int maxPathLength = 5;

    public PKIXBuilderParameters(Set<TrustAnchor> trustAnchors, CertSelector targetConstraints) throws InvalidAlgorithmParameterException {
        super((Set) trustAnchors);
        setTargetCertConstraints(targetConstraints);
    }

    public PKIXBuilderParameters(KeyStore keystore, CertSelector targetConstraints) throws KeyStoreException, InvalidAlgorithmParameterException {
        super(keystore);
        setTargetCertConstraints(targetConstraints);
    }

    public void setMaxPathLength(int maxPathLength) {
        if (maxPathLength < -1) {
            throw new InvalidParameterException("the maximum path length parameter can not be less than -1");
        }
        this.maxPathLength = maxPathLength;
    }

    public int getMaxPathLength() {
        return this.maxPathLength;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        sb.append(super.toString());
        sb.append("  Maximum Path Length: " + this.maxPathLength + "\n");
        sb.append("]\n");
        return sb.toString();
    }
}
