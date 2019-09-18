package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificatePoliciesExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.CertificatePolicies";
    public static final String NAME = "CertificatePolicies";
    public static final String POLICIES = "policies";
    private List<PolicyInformation> certPolicies;

    private void encodeThis() throws IOException {
        if (this.certPolicies == null || this.certPolicies.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        for (PolicyInformation info : this.certPolicies) {
            info.encode(tmp);
        }
        os.write((byte) 48, tmp);
        this.extensionValue = os.toByteArray();
    }

    public CertificatePoliciesExtension(List<PolicyInformation> certPolicies2) throws IOException {
        this(Boolean.FALSE, certPolicies2);
    }

    public CertificatePoliciesExtension(Boolean critical, List<PolicyInformation> certPolicies2) throws IOException {
        this.certPolicies = certPolicies2;
        this.extensionId = PKIXExtensions.CertificatePolicies_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }

    public CertificatePoliciesExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.CertificatePolicies_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag == 48) {
            this.certPolicies = new ArrayList();
            while (val.data.available() != 0) {
                this.certPolicies.add(new PolicyInformation(val.data.getDerValue()));
            }
            return;
        }
        throw new IOException("Invalid encoding for CertificatePoliciesExtension.");
    }

    public String toString() {
        if (this.certPolicies == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("CertificatePolicies [\n");
        for (PolicyInformation info : this.certPolicies) {
            sb.append(info.toString());
        }
        sb.append("]\n");
        return sb.toString();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.CertificatePolicies_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase(POLICIES)) {
            throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:CertificatePoliciesExtension.");
        } else if (obj instanceof List) {
            this.certPolicies = (List) obj;
            encodeThis();
        } else {
            throw new IOException("Attribute value should be of type List.");
        }
    }

    public List<PolicyInformation> get(String name) throws IOException {
        if (name.equalsIgnoreCase(POLICIES)) {
            return this.certPolicies;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:CertificatePoliciesExtension.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(POLICIES)) {
            this.certPolicies = null;
            encodeThis();
            return;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:CertificatePoliciesExtension.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(POLICIES);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
