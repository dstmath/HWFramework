package java.security.cert;

import java.security.PublicKey;

public class PKIXCertPathBuilderResult extends PKIXCertPathValidatorResult implements CertPathBuilderResult {
    private CertPath certPath;

    public PKIXCertPathBuilderResult(CertPath certPath, TrustAnchor trustAnchor, PolicyNode policyTree, PublicKey subjectPublicKey) {
        super(trustAnchor, policyTree, subjectPublicKey);
        if (certPath == null) {
            throw new NullPointerException("certPath must be non-null");
        }
        this.certPath = certPath;
    }

    public CertPath getCertPath() {
        return this.certPath;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PKIXCertPathBuilderResult: [\n");
        sb.append("  Certification Path: " + this.certPath + "\n");
        sb.append("  Trust Anchor: " + getTrustAnchor().toString() + "\n");
        sb.append("  Policy Tree: " + String.valueOf(getPolicyTree()) + "\n");
        sb.append("  Subject Public Key: " + getPublicKey() + "\n");
        sb.append("]");
        return sb.toString();
    }
}
