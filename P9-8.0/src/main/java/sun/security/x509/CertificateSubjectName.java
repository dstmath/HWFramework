package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateSubjectName implements CertAttrSet<String> {
    public static final String DN_NAME = "dname";
    public static final String DN_PRINCIPAL = "x500principal";
    public static final String IDENT = "x509.info.subject";
    public static final String NAME = "subject";
    private X500Name dnName;
    private X500Principal dnPrincipal;

    public CertificateSubjectName(X500Name name) {
        this.dnName = name;
    }

    public CertificateSubjectName(DerInputStream in) throws IOException {
        this.dnName = new X500Name(in);
    }

    public CertificateSubjectName(InputStream in) throws IOException {
        this.dnName = new X500Name(new DerValue(in));
    }

    public String toString() {
        if (this.dnName == null) {
            return "";
        }
        return this.dnName.toString();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.dnName.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof X500Name)) {
            throw new IOException("Attribute must be of type X500Name.");
        } else if (name.equalsIgnoreCase("dname")) {
            this.dnName = (X500Name) obj;
            this.dnPrincipal = null;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:CertificateSubjectName.");
        }
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase("dname")) {
            return this.dnName;
        }
        if (name.equalsIgnoreCase("x500principal")) {
            if (this.dnPrincipal == null && this.dnName != null) {
                this.dnPrincipal = this.dnName.asX500Principal();
            }
            return this.dnPrincipal;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:CertificateSubjectName.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase("dname")) {
            this.dnName = null;
            this.dnPrincipal = null;
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:CertificateSubjectName.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement("dname");
        return elements.elements();
    }

    public String getName() {
        return "subject";
    }
}
