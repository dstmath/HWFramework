package java.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.cert.CertPath;

public final class CodeSigner implements Serializable {
    private static final long serialVersionUID = 6819288105193937581L;
    private transient int myhash = -1;
    private CertPath signerCertPath;
    private Timestamp timestamp;

    public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
        if (signerCertPath == null) {
            throw new NullPointerException();
        }
        this.signerCertPath = signerCertPath;
        this.timestamp = timestamp;
    }

    public CertPath getSignerCertPath() {
        return this.signerCertPath;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public int hashCode() {
        if (this.myhash == -1) {
            if (this.timestamp == null) {
                this.myhash = this.signerCertPath.hashCode();
            } else {
                this.myhash = this.signerCertPath.hashCode() + this.timestamp.hashCode();
            }
        }
        return this.myhash;
    }

    public boolean equals(Object obj) {
        if (obj == null || ((obj instanceof CodeSigner) ^ 1) != 0) {
            return false;
        }
        CodeSigner that = (CodeSigner) obj;
        if (this == that) {
            return true;
        }
        Timestamp thatTimestamp = that.getTimestamp();
        if (this.timestamp == null) {
            if (thatTimestamp != null) {
                return false;
            }
        } else if (thatTimestamp == null || (this.timestamp.equals(thatTimestamp) ^ 1) != 0) {
            return false;
        }
        return this.signerCertPath.equals(that.getSignerCertPath());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("Signer: " + this.signerCertPath.getCertificates().get(0));
        if (this.timestamp != null) {
            sb.append("timestamp: " + this.timestamp);
        }
        sb.append(")");
        return sb.toString();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.myhash = -1;
    }
}
