package java.security.cert;

import java.security.PublicKey;

public class PKIXCertPathValidatorResult implements CertPathValidatorResult {
    private PolicyNode policyTree;
    private PublicKey subjectPublicKey;
    private TrustAnchor trustAnchor;

    public PKIXCertPathValidatorResult(TrustAnchor trustAnchor2, PolicyNode policyTree2, PublicKey subjectPublicKey2) {
        if (subjectPublicKey2 == null) {
            throw new NullPointerException("subjectPublicKey must be non-null");
        } else if (trustAnchor2 != null) {
            this.trustAnchor = trustAnchor2;
            this.policyTree = policyTree2;
            this.subjectPublicKey = subjectPublicKey2;
        } else {
            throw new NullPointerException("trustAnchor must be non-null");
        }
    }

    public TrustAnchor getTrustAnchor() {
        return this.trustAnchor;
    }

    public PolicyNode getPolicyTree() {
        return this.policyTree;
    }

    public PublicKey getPublicKey() {
        return this.subjectPublicKey;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PKIXCertPathValidatorResult: [\n");
        sb.append("  Trust Anchor: " + this.trustAnchor.toString() + "\n");
        sb.append("  Policy Tree: " + String.valueOf((Object) this.policyTree) + "\n");
        sb.append("  Subject Public Key: " + this.subjectPublicKey + "\n");
        sb.append("]");
        return sb.toString();
    }
}
