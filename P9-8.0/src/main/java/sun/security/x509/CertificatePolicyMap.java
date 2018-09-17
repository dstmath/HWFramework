package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificatePolicyMap {
    private CertificatePolicyId issuerDomain;
    private CertificatePolicyId subjectDomain;

    public CertificatePolicyMap(CertificatePolicyId issuer, CertificatePolicyId subject) {
        this.issuerDomain = issuer;
        this.subjectDomain = subject;
    }

    public CertificatePolicyMap(DerValue val) throws IOException {
        if (val.tag != (byte) 48) {
            throw new IOException("Invalid encoding for CertificatePolicyMap");
        }
        this.issuerDomain = new CertificatePolicyId(val.data.getDerValue());
        this.subjectDomain = new CertificatePolicyId(val.data.getDerValue());
    }

    public CertificatePolicyId getIssuerIdentifier() {
        return this.issuerDomain;
    }

    public CertificatePolicyId getSubjectIdentifier() {
        return this.subjectDomain;
    }

    public String toString() {
        return "CertificatePolicyMap: [\nIssuerDomain:" + this.issuerDomain.toString() + "SubjectDomain:" + this.subjectDomain.toString() + "]\n";
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.issuerDomain.encode(tmp);
        this.subjectDomain.encode(tmp);
        out.write((byte) 48, tmp);
    }
}
