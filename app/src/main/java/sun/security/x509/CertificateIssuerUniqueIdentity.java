package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateIssuerUniqueIdentity implements CertAttrSet<String> {
    public static final String ID = "id";
    public static final String IDENT = "x509.info.issuerID";
    public static final String NAME = "issuerID";
    private UniqueIdentity id;

    public CertificateIssuerUniqueIdentity(UniqueIdentity id) {
        this.id = id;
    }

    public CertificateIssuerUniqueIdentity(DerInputStream in) throws IOException {
        this.id = new UniqueIdentity(in);
    }

    public CertificateIssuerUniqueIdentity(InputStream in) throws IOException {
        this.id = new UniqueIdentity(new DerValue(in));
    }

    public CertificateIssuerUniqueIdentity(DerValue val) throws IOException {
        this.id = new UniqueIdentity(val);
    }

    public String toString() {
        if (this.id == null) {
            return "";
        }
        return this.id.toString();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.id.encode(tmp, DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) 1));
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof UniqueIdentity)) {
            throw new IOException("Attribute must be of type UniqueIdentity.");
        } else if (name.equalsIgnoreCase(ID)) {
            this.id = (UniqueIdentity) obj;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateIssuerUniqueIdentity.");
        }
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            return this.id;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateIssuerUniqueIdentity.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            this.id = null;
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateIssuerUniqueIdentity.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(ID);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
