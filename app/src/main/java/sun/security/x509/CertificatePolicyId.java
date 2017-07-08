package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificatePolicyId {
    private ObjectIdentifier id;

    public CertificatePolicyId(ObjectIdentifier id) {
        this.id = id;
    }

    public CertificatePolicyId(DerValue val) throws IOException {
        this.id = val.getOID();
    }

    public ObjectIdentifier getIdentifier() {
        return this.id;
    }

    public String toString() {
        return "CertificatePolicyId: [" + this.id.toString() + "]\n";
    }

    public void encode(DerOutputStream out) throws IOException {
        out.putOID(this.id);
    }

    public boolean equals(Object other) {
        if (other instanceof CertificatePolicyId) {
            return this.id.equals(((CertificatePolicyId) other).getIdentifier());
        }
        return false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }
}
